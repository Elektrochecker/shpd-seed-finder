#!/bin/bash

java -version || exit

if find shattered-pixel-dungeon -maxdepth 1 | read; then
    echo "ERROR: please remove shattered-pixel-dungeon/"
    exit
fi

if find seed-finder -maxdepth 1 | read; then
    echo "ERROR: please remove seed-finder/"
    exit
fi

git clone https://github.com/00-Evan/shattered-pixel-dungeon
curl -o shattered-pixel-dungeon/changes.patch https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/changes.patch

git -C shattered-pixel-dungeon apply changes.patch --reject

cd shattered-pixel-dungeon
./gradlew desktop:release
cd ..

mkdir seed-finder
mv shattered-pixel-dungeon/desktop/build/libs/desktop*.jar seed-finder/seed-finder.jar
curl -o seed-finder/index.js https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/tools/turbo.js
curl -o seed-finder/readme.md https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/README.md
curl -o seed-finder/seedfinder.cfg https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/seedfinder.cfg

rm -rf shattered-pixel-dungeon
