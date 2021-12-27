#!/bin/sh

RELEASE=$1
JAVA_HOME=${$2:-$JAVA_HOME}

# 1. Copy unsigned jar from local repo
echo "Making build directory..."
[[ -d build ]] && rm -rf build
mkdir build
echo "Copying raw plugin compiled source..."
cp ~/.m2/repository/com/ntak/pearl-zip-archive-zip4j/${RELEASE}/pearl-zip-archive-zip4j-${RELEASE}.jar build/pearl-zip-archive-zip4j-${RELEASE}.jar

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
