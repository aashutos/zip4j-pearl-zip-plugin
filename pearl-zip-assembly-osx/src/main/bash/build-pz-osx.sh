#!/bin/bash

#
# Copyright (c) ${YEAR} 92AK
#

echo "Checking Maven and Java are installed"
JAVA_ROOT="${JAVA_HOME:+$JAVA_HOME/bin/}"

echo "Input parameters: $*"

VERSION=$1

if [ "$(which mvn | echo $?)" -ne 0 ]
then
  echo "Maven has not been installed. Exiting..."
  exit 1
fi

if [ $(${JAVA_ROOT}java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1) -lt 15 ]
then
  echo "Java >= 15 has not been installed. Exiting..."
  exit 1
fi

rootDir=$(pwd)
echo "Root directory: $(pwd)"

echo "Executing clean up script..."
rm -f $rootDir/deps.lst $rootDir/pz-class.lst $rootDir/pz-shared.jsa
rm -rf $rootDir/mods
rm -rf $rootDir/out
rm -rf $rootDir/work
rm -rf $rootDir/pz-runtime
rm -rf $rootDir/target/PearlZip.app

echo "Generating Dependencies..."
mvn dependency:build-classpath -Dmdep.outputFile=$rootDir/deps.lst -f ../pearl-zip-ui/pom.xml

echo "Retrieving Dependencies..."
mkdir -p $rootDir/mods
cp $(cat $rootDir/deps.lst | sed -e 's/\:/ /g') $rootDir/mods/
cp ~/.m2/repository/com/ntak/pearl-zip-ui/$VERSION/pearl-zip-ui-$VERSION.jar $rootDir/mods/

mkdir $rootDir/work

echo "Identifying legacy modules..."
ls -1 $rootDir/mods/ | while read line
do
   echo "Archive: $rootDir/mods/$line"
   ${JAVA_ROOT}jdeps --module-path $rootDir/mods/ --ignore-missing-deps --multi-release 15 --generate-module-info out "mods/$line" > /dev/null
   if [ "$?" -eq 0 ]
   then
     echo "Get module name"
     moduleName=$(${JAVA_ROOT}jdeps --module-path $rootDir/mods/ --ignore-missing-deps --multi-release 15 --generate-module-info work "$rootDir/mods/$line" | grep "writing to" | cut -d" " -f3 | cut -d/ -f2)
     echo "Module name: $moduleName"

     echo "Generate module-info file..."
     ${JAVA_ROOT}jdeps --ignore-missing-deps --multi-release 15 --generate-module-info $rootDir/work "$rootDir/mods/$line"
     cp "work/$(ls -1 $rootDir/work)/versions/15/module-info.java" $rootDir/work/$(ls -1 $rootDir/work)/module-info.java

     echo "Building new jar..."
     mkdir $rootDir/classes
     cd $rootDir/classes
     jar xf $rootDir/mods/$line

     echo "Modularising legacy jars..."
     cd $rootDir
     ${JAVA_ROOT}javac -cp $rootDir/mods/ --module-path $rootDir/mods/ -d $rootDir/classes/ $rootDir/work/$moduleName/module-info.java
     jar uf $rootDir/mods/$line -C classes module-info.class

     echo "Clearing work directories..."
     rm -rf $rootDir/work
     rm -rf $rootDir/classes
   fi
done

echo "Manual override with pre-compiled modules..."
cp $rootDir/src/main/resources/lib/org.apache.logging.log4j.core/log4j-core-2.14.0.jar mods/
cp $rootDir/src/main/resources/lib/org.apache.commons.compress/commons-compress-1.20.jar mods/
cp $rootDir/src/main/resources/lib/com.sun.jna/jna-5.6.0.jar mods/

echo "Create shared archive..."
${JAVA_ROOT}java -Xshare:off -Djava.awt.headless=true --enable-preview -XX:+UseZGC -XX:DumpLoadedClassList=pz-class.lst --module-path="$(ls mods | xargs -I{} echo mods/{} | tr '\n' ':'):mods/pearl-zip-ui-$VERSION.jar" -m com.ntak.pearlzip.ui/com.ntak.pearlzip.ui.pub.ZipLauncher &
pid=$!
echo "Dump list process PID: $pid"
sleep 10
kill -9 $pid
# KILL executed, Check for end of process
while [ $(ps -p $pid > /dev/null) ]; do
  sleep 10
done

${JAVA_ROOT}java -Xshare:dump -Djava.awt.headless=true --enable-preview -XX:+UseZGC -XX:SharedClassListFile=pz-class.lst -XX:SharedArchiveFile=pz-shared.jsa --module-path="$(ls mods | xargs -I{} echo mods/{} | tr '\n' ':'):mods/pearl-zip-ui-$VERSION.jar" com.ntak.pearlzip.ui.pub.ZipLauncher -m com.ntak.pearlzip.ui/com.ntak.pearlzip.ui.pub.ZipLauncher > /dev/null 2>&1 &
pid=$!
echo "Create JSA process PID: $pid"
sleep 10
kill -2 $pid
#   SIGINT executed, Check for end of process
while [ $(ps -p $pid > /dev/null) ]; do
  sleep 10
done

echo "Create runtime..."
#cp $rootDir/src/main/resources/lib/javafx-jmods-15.0.1/* mods/
#find $rootDir/mods -name  "javafx*[^m][^a][^c].jar" -delete
#find $rootDir/mods/ -name  "javafx*mac.jar" -delete
${JAVA_ROOT}jlink --module-path=$rootDir/mods/ --add-modules=ALL-MODULE-PATH --output $rootDir/pz-runtime

echo "Generate App Image"
mkdir -p target
${JAVA_ROOT}jpackage --type app-image --app-version 1.0.0 --copyright "© copyright 2021 92AK" --description "A JavaFX front-end wrapper for some common archive formats" --name PearlZip --vendor 92AK --verbose --java-options "--enable-preview -XX:SharedArchiveFile=pz-shared.jsa -XX:+UseZGC" --icon "$rootDir/src/main/resources/pz-icon.icns" --mac-package-identifier com.ntak.pearl-zip --mac-package-identifier PearlZip  --module-path $rootDir/mods/ -m com.ntak.pearlzip.ui/com.ntak.pearlzip.ui.pub.ZipLauncher --file-associations $rootDir/src/main/resources/file-associations/fa-xz.properties --file-associations $rootDir/src/main/resources/file-associations/fa-bz2.properties --file-associations $rootDir/src/main/resources/file-associations/fa-zip.properties --file-associations $rootDir/src/main/resources/file-associations/fa-gzip.properties --file-associations $rootDir/src/main/resources/file-associations/fa-tar.properties --file-associations $rootDir/src/main/resources/file-associations/fa-jar.properties --runtime-image $rootDir/pz-runtime -d target --verbose

echo "Clearing up working directories..."
rm -f pz-shared.jsa
rm -f pz-class.lst
rm -rf pz-runtime
rm -rf mods
rm -rf deps.lst
rm -rf out
rm -r  nohup.out

exit 0