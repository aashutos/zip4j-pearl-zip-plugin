#!/bin/bash
#
# Copyright © 2021 92AK
#

# Parameters
# $1 - Release e.g. PA-0.0.0.1
# $2 - Settings file location (Configures deployment process)
#

# Populate environment variables
P_RELEASE=$1
RELEASE=$1
P_SETTINGS=${2:-./scripts/settings.properties}

echo "Settings file: $P_SETTINGS"
while read line; do
  if [ $(echo "$line" | grep "=" | wc -l) == 1 ]
  then
    echo "Setting environment variable: $(echo $line | cut -d= -f1)..."
    key=$(echo $line | cut -d= -f1)
    value=$(echo $line | cut -d= -f2-)
    declare P_$key="$value"
  fi
done < $P_SETTINGS

# Get set environment variables
# ( set -o posix ; set )
JAVA_HOME=${P_JAVA_HOME:-$JAVA_HOME}

echo 'Configuring release name...'
P_RELEASE="${P_PREFIX_RELEASE:+$P_PREFIX_RELEASE-}$P_RELEASE"
echo "Release name has been set to: ${P_RELEASE}"

# Dependencies check on environment
if [ "$(which 7z | echo $?)" -ne 0 ]
then
  echo "7z has not been installed. Exiting..."
  exit 1
fi

if [ "$(which git | echo $?)" -ne 0 ]
then
  echo "git has not been installed. Exiting..."
  exit 1
fi

if [ "$(which shasum | echo $?)" -ne 0 ]
then
  echo "shasum has not been installed. Exiting..."
  exit 1
fi

# Create git tag and hotfix branch
if [ $(git branch | grep '*' | grep master | wc -l) -eq 1 ]
then
  echo "On master branch. Creating release tag ${P_RELEASE}..."
  git tag "${P_RELEASE}"
  git push --tags

  echo "Creating hotfix branch releases/${P_RELEASE}..."
  git checkout -b "releases/${P_RELEASE}"
  git push origin "releases/${P_RELEASE}"
else
  echo "Not on master branch. Exiting..."
  exit 2
fi
git checkout "releases/${P_RELEASE}"

# Creating packaged archive...
# 1. Copy unsigned jar from local repo
echo "Making build directory..."
mkdir build
echo "Copying raw plugin compiled source..."
cp ~/.m2/repository/com/ntak/pearl-zip-archive-zip4j/${RELEASE}/pearl-zip-archive-zip4j-${RELEASE}.jar build/pearl-zip-archive-zip4j-${RELEASE}.jar
ARCHIVE="build/pearl-zip-archive-zip4j-${RELEASE}.zip"

# 2. Sign jar using keystore
echo "Signing plugin archive..."
echo "JDK location: $JAVA_HOME"
echo $(cat /opt/.store/.pw-signer) | $JAVA_HOME/bin/jarsigner -tsa https://freetsa.org/tsr -keystore /opt/.store/.ks-signer -storepass $(cat /opt/.store/.pw-signer) "build/pearl-zip-archive-zip4j-${RELEASE}.jar" 92ak

# Copy license file and deployment instructions
echo "Preparing deployment archive..."
echo "Preparing static resources..."
cp BSD-3-CLAUSE-LICENSE build/BSD-3-CLAUSE-LICENSE
cp scripts/ZIP4J-LICENSE build/ZIP4J-LICENSE
cp scripts/INSTRUCTIONS build/INSTRUCTIONS

# Copy Dependencies
echo "Preparing dependencies..."
mvn dependency:build-classpath -Dmdep.outputFile=build/deps.lst -f pom.xml
cp $(cat build/deps.lst | tr : '\n' | grep zip4j) build/
cd build
echo "Creating zip archive..."
zip pearl-zip-archive-zip4j-${RELEASE}.zip *.jar BSD-3-CLAUSE-LICENSE ZIP4J-LICENSE INSTRUCTIONS INSTALL-ZIP4J-PLUGIN.app/
cd ..

######### Upload to GitHub ##########

# Obtaining asset Id to link deployment package with a tag
P_TAGS_API="${P_GITHUB_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/tags/${P_RELEASE}"
P_TOKEN_HEADER="Authorization: token ${P_GITHUB_API_TOKEN}"

echo "Uploading asset ${ARCHIVE} to ${P_REPOSITORY} for tag ${P_RELEASE}... "

# Get ID and remove whitespaces...
ID=$(curl -X GET -sH "${P_TOKEN_HEADER}" ${P_GITHUB_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases | grep -A1 "\"html_url\": \".*${P_RELEASE}\"" | grep id | tr ',' ' ' | cut -d: -f2)
ID=${ID//[$'\t\r\n ']}

echo "Existing Asset Id: $ID;"
if [ ${ID:-0} -gt 0 ]
then
  echo "Existing release detected. Deleting..."
  echo ${P_GITHUB_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)
  curl -X DELETE -sH "${P_TOKEN_HEADER}" ${P_GITHUB_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)
fi

echo "Creating new release..."
NEW_RELEASE_JSON=$(curl -X POST -sH "${P_TOKEN_HEADER}" -d "{\"name\":\"PearlZip Zip4j Plugin Release ${P_RELEASE}${P_CODENAME:+" ($P_CODENAME)"}\",\"body\":\"Zip4j PearlZip Plugin Release version ${P_RELEASE} as a zip archive.\",\"tag_name\":\"${P_RELEASE}\",\"draft\":${P_DRAFT_RELEASE}" ${P_GITHUB_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases)
ID=$(echo "${NEW_RELEASE_JSON}" | grep -m 1 '\"id\"' | tr ',' ' ' | cut -d':' -f2 | xargs -I{} echo {})

echo "Asset Id: $ID;"
echo "Creating SHA-512 hash of ${ARCHIVE}..."
ARCHIVE_HASH=${ARCHIVE}.sha512
shasum -a 512 "${ARCHIVE}" | cut -d" " -f1 > "${ARCHIVE_HASH}"

echo "Uploading asset ${ARCHIVE}"
echo "${P_GITHUB_UPLOAD_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)/assets?name=$(basename \"${ARCHIVE}\")"
curl --progress-bar -sH "${P_TOKEN_HEADER}" --data-binary @"${ARCHIVE}" -H "Content-Type: application/octet-stream" "${P_GITHUB_UPLOAD_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)/assets?name=$(basename "${ARCHIVE}")"
sleep 10

echo "Uploading asset ${ARCHIVE_HASH}"
echo "${P_GITHUB_UPLOAD_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)/assets?name=$(basename "${ARCHIVE_HASH}")"
curl --progress-bar -sH "${P_TOKEN_HEADER}" --data-binary @"${ARCHIVE_HASH}" -H "Content-Type: application/octet-stream" "${P_GITHUB_UPLOAD_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)/assets?name=$(basename "${ARCHIVE_HASH}")"

echo 'resetting to master branch...'
git checkout master

######### Clear working directory #########
echo 'clearing working directory...'
rm -rf build
