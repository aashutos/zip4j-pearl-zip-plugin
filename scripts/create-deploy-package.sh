#!/bin/sh

RELEASE=$1
PZ_RELEASE=$2
JAVA_HOME=${3:-$JAVA_HOME}
P_SETTINGS=${4:-./scripts/settings.properties}

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

# 1. Copy unsigned jar from local repo
echo "Making build directory..."
[[ -d build ]] && rm -rf build
mkdir build
echo "Copying raw plugin compiled source..."
cp ~/.m2/repository/com/ntak/pearl-zip-archive-zip4j/${RELEASE}/pearl-zip-archive-zip4j-${RELEASE}.jar build/pearl-zip-archive-zip4j-${RELEASE}.jar

# 2. Sign jar using keystore
echo "Signing plugin archive..."
echo "JDK location: $JAVA_HOME"
echo $(cat /opt/.store/.pw-signer) | $JAVA_HOME/bin/jarsigner -tsa https://freetsa.org/tsr -keystore /opt/.store/.ks-signer -storepass $(cat /opt/.store/.pw) "build/pearl-zip-archive-zip4j-${RELEASE}.jar" 92ak

echo "Generating changelog..."
# Retrieve Change Log for release
echo "${P_YOUTRACK_HOST}/api/issues?fields=summary&query=project:%20PearlZip%20%23Bug%20%23Feature%20%23Task%20Fix%20versions:%20${PZ_RELEASE}%20tag:%20zip4j"
curl -X GET "${P_YOUTRACK_HOST}/api/issues?fields=summary&query=project:%20PearlZip%20%23Bug%20%23Feature%20%23Task%20Fix%20versions:%20${PZ_RELEASE}%20tag:%20zip4j" -H 'Accept: application/json' -H "Authorization: Bearer ${P_YOUTRACK_AUTH}" -H 'Cache-Control: no-cache' -H 'Content-Type: application/json' | tr , "\n" | grep summary | cut -d'"' -f4 | sort | xargs -I{} echo "+ {}" > build/CHANGELOG
printf "Zip4J for PearlZip Plugin Version $VERSION Change Log\nCopyright © $(date +%Y) 92AK\n====================================\n\nThe following features are in scope for this release of Zip4J Plugin for PearlZip:\n\n$(cat build/CHANGELOG)" > build/CHANGELOG

# Copy license file and deployment instructions
echo "Preparing deployment archive..."
echo "Preparing static resources..."
cp BSD-3-CLAUSE-LICENSE build/BSD-3-CLAUSE-LICENSE
cp scripts/ZIP4J-LICENSE build/ZIP4J-LICENSE
cp scripts/INSTRUCTIONS build/INSTRUCTIONS
cp scripts/MF build/MF
cp -r scripts/INSTALL-ZIP4J-PLUGIN.app build/INSTALL-ZIP4J-PLUGIN.app

# Copy Dependencies
echo "Preparing dependencies..."
mvn dependency:build-classpath -Dmdep.outputFile=build/deps.lst -f pom.xml
cp $(cat build/deps.lst | tr : '\n' | grep zip4j) build/
cd build
echo "Creating zip archive..."
shasum -a 256 pearl-zip-archive-zip4j-${RELEASE}.jar | cut -d" " -f1 > pearl-zip-archive-zip4j-${RELEASE}.sha256
zip -r pearl-zip-archive-zip4j-${RELEASE}.zip *.jar BSD-3-CLAUSE-LICENSE ZIP4J-LICENSE INSTRUCTIONS INSTALL-ZIP4J-PLUGIN.app/
zip -r pearl-zip-archive-zip4j-${RELEASE}.pzax *.jar *.sha256 BSD-3-CLAUSE-LICENSE ZIP4J-LICENSE INSTRUCTIONS MF
cd ..
