git clone https://github.com/00-Evan/shattered-pixel-dungeon
cd shattered-pixel-dungeon
curl -o changes.patch https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/changes.patch
git apply changes.patch --reject
call ./gradlew desktop:release
cd ..
xcopy shattered-pixel-dungeon\desktop\build\libs\
ren desktop*.jar seed-finder.jar
pause