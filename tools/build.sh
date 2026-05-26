#!/bin/bash

set -eu
set -o pipefail

java -version

if [ -d shattered-pixel-dungeon ] ; then
    echo "ERROR: please remove shattered-pixel-dungeon/"
    exit 1
fi

if [ -d seed-finder ] ; then
    echo "ERROR: please remove seed-finder/"
    exit 1
fi

if [ -d seed-finder-git ] ; then
    echo "ERROR: please remove seed-finder-git/"
    exit 1
fi

git clone https://github.com/Elektrochecker/shpd-seed-finder seed-finder-git

git clone https://github.com/00-Evan/shattered-pixel-dungeon
cp -v seed-finder-git/changes.patch shattered-pixel-dungeon/changes.patch

set +e
git -C shattered-pixel-dungeon apply changes.patch --reject
set -e

sed -i "s/com.shatteredpixel.shatteredpixeldungeon/com.elektrochecker.seedfinder/" shattered-pixel-dungeon/build.gradle

asset_files=$(find shattered-pixel-dungeon/core/src/main/assets/ -type f)
asset_foreign_messages=$(find shattered-pixel-dungeon/core/src/main/assets/messages -type f | grep -E '_[a-z]{2}')

for asset in $asset_files; do
    extension="$(echo $asset | grep -oE '\..+$')"

    case $extension in
        #'.png') cp -v "seed-finder-git/assets/empty.png" "$asset";;
        '.jpg') cp -v "seed-finder-git/assets/empty.jpg" "$asset";;
        '.mp3') cp -v "seed-finder-git/assets/empty.mp3" "$asset";;
        '.ogg') cp -v "seed-finder-git/assets/empty.ogg" "$asset";;
        *) ;;
    esac
done

for asset in $asset_foreign_messages; do
    echo "" > $asset
done

cd shattered-pixel-dungeon
./gradlew desktop:release
cd ..

mkdir seed-finder
mv shattered-pixel-dungeon/desktop/build/libs/desktop*.jar seed-finder/seed-finder.jar
cp -v seed-finder-git/tools/turbo.js seed-finder/index.js
cp -v seed-finder-git/README.md seed-finder/readme.md
cp -v seed-finder-git/seedfinder.cfg seed-finder/seedfinder.cfg

rm -rf shattered-pixel-dungeon
rm -rf seed-finder-git
