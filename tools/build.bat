git clone https://github.com/00-Evan/shattered-pixel-dungeon
cd shattered-pixel-dungeon
curl -o changes.patch https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/changes.patch
git apply changes.patch --reject
call ./gradlew desktop:release
cd ..
mkdir seed-finder
xcopy shattered-pixel-dungeon\desktop\build\libs\ seed-finder\
cd seed-finder
ren desktop*.jar seed-finder.jar
curl -o seed-converter.js https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/tools/seed-converter.js
curl -o index.js https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/tools/turbo.js
pause