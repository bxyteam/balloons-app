#!/bin/bash -ex
cd $(dirname $0)

# check that compilerId environment variable is set
if [ -z "${balloonConfigId}" ]; then
    echo "balloonConfigId is empty, you need to add -e balloonConfigId=${balloonConfigId} to docker run "
    exit 1;
fi

# create needed paths
storagePath="/var/compiler/balloon"
mkdir -p $storagePath

BASE_DIR="/var/balloon/data"

# Clean up previous web folder
rm -rf "${BASE_DIR}/web"

# Recreate the structure cleanly
mkdir -p "${BASE_DIR}"

# Move web directory properly
mv /home/balloon/application/web "${BASE_DIR}/web"

mv /home/balloon/application/browxy_mvn/settings.xml /home/balloon/.m2/settings.xml

# mv /home/balloon/application/browxy_mvn/browxyLibs/gfx /home/balloon/.m2/com/browxy 

chmod -R ugo+rwx "${BASE_DIR}"
chmod -R ugo+rwx /home/balloon/.m2

# start application
cd $(dirname $0)
(nohup ./start.sh ${balloonConfigId} &) && tail -F /etc/hostname
