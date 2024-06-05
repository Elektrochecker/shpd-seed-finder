# Shattered Pixel Dungeon seed finder

https://github.com/Elektrochecker/shpd-seed-finder

Application to find seeds for Shattered Pixel Dungeon given constraints (e.g. wand of disintegration +2 and ring of evasion in the first 4 floors).
It can also display items found on a specific seed.

As different versions of Shattered Pixel Dungeon generate different dungeons, a release of shpd-seedfinder will only work with certain versions of Shattered Pixel Dungeon. Versions that only differ by the last digit in the version code (for example v2.3.0 and v2.3.1) are likeley, but not guaranteed to share the same level generation.

[SHPD Toolkit](https://github.com/Elektrochecker/shpd-toolkit) is a GUI version of this project which is easier to use and can be installed on android.

# How to use

## setup
- obtain a copy of the newest version of the seedfinder. It can be downloaded as a zip from the [releases tab](https://github.com/Elektrochecker/shpd-seed-finder/releases). Older versions might only offer the .jar file as a download.
- make sure Java is installed on your device
- if you want to use the multithreading script, [nodejs](https://nodejs.org/en) must also be installed

Extract the zip archive (or place the jar file in an empty folder). This Folder will be the working directory for the seedfinder.

In order to use the seedfinder, commands must be executed in the directory of the seedfinder. Open a command prompt as follows:
- windows: hold `shift` and right-click your folder. choose `open a powershell window here`.
- linux: linux users know how to open a termial
- other: inform yourself how to open a terminal on your OS

Commands can be executed by typing them in this window and pressing `Enter`. You can test your Java installation by running the following command:
```
java -version
```

## scouting seeds
Search items for a known dungeon seed by running the following command(s):

#### seeded runs
```
java -jar seed-finder.jar <floors> <seed>
```
where `<floors>` is the number of floors to scan and `<seed>` is the seed to scan.

example: `java -jar seed-finder.jar 4 SEE-EEE-EED`

#### daily runs
```
java -jar seed-finder.jar <floors> daily<offset>
```
where `<floors>` is the number of floors to scan and `<offset>` is an integer preceded by `+` or `-`.

examples:

- todays daily:         `java -jar seed-finder.jar 24 daily`
- yesterdays daily:     `java -jar seed-finder.jar 24 daily-1`
- tomorrows daily:      `java -jar seed-finder.jar 24 daily+1`
- last weeks daily:     `java -jar seed-finder.jar 24 daily-7`

## finding seeds
Using the seedfinder to generate specific seeds:
- create a new text file in your directoy (in this example called `seeditems.txt`). It will contain a list of items the seedfinder should search for.
- edit your item textfile and put in the items you are looking for. Make a new line for every item. Make sure that every item is spelled correctly (for example assassin's blade instead of assassins blade).

Open your terminal and start the seedfinder using the following command:
```
java -jar seed-finder.jar <floors> <mode> <item file name> [output file name]
```
where `<floors>` is the number of floors to scan, and  `<mode>` is either:
- `any` (find seeds that contain any one of the specified items)
- `all` (find seeds that contain all of the specified items).

When seeds are found they will be saved to `out.txt` or, if specified, your custom output file. Depending on the complexity of your item list, seeds will be found quickly, slowly or be near impossible to generate.

## configuration
Many features such as challenges and seed generation setting can be changed by editing the `seedfinder.cfg` file.
Starting the seedfinder without a config file will automatically generate a `seedfinder.cfg` file and result in a crash.

Some challenges might change level generation (most notably `forbidden runes`), therefore i provide options to turn them on or off.

## the multithreading script a.k.a. turbo mode
Using the script to start and control multiple seedfinders simultaneously will greatly increase generation speed, especially for beefy computers. Usage is similar to the seed finding mode. Syntax:
```
node . <floor> <mode> <seed item file> [number of processes]
```
This will start a number of seedfinders equal the provided argument (4 if left blank).  Make sure that sequential mode is disabled in the config file, there would be no point using it with multiple seedfinders. Don't overdo it with the number of seedfinders, since the program will take all the resources it can.

## json - converter
The JSON-converter script converts the human-readable output of the seedfinder or the turbo script into a JSON-format, convenient for further processing.

Usage:
```
node json-converter.js <input file> [output file]
```

# How to build
Git and Java must be installed in order to build the seedfinder.

### build script
The buid script `tools/build.bat` (windows) or `tools/build.sh` (linux) can be executed to automatically build a seedfinder for the newest version of [SHPD](https://github.com/00-Evan/shattered-pixel-dungeon) by running it in an empty (!) folder. No files other than the build script are required.

```
chmod +x build.sh && ./build.sh
```

### manual build
1. Clone the [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon) repository.

```
git clone https://github.com/00-Evan/shattered-pixel-dungeon
```

2. Download the patch (changes.patch) into the local repository.

```
cd shattered-pixel-dungeon
curl -o changes.patch https://raw.githubusercontent.com/Elektrochecker/shpd-seed-finder/master/changes.patch
```

3. Apply the patch to the repository.

```
git apply changes.patch --reject
```

Potentially fix any rejects manually. The rejected parts of the patch are marked with `.rej` files in the project.

4. Compile the application with the following command:

```
./gradlew desktop:release
```
Upon completion the build output wil be located at
```
desktop/build/libs
```

5. Assemble the full project:
- rename build to `seed-finder.jar` and put it in its own folder
- download required scripts from the tools folder
- rename `turbo.js` to `index.js` in order to keep the syntax simple
- run the seedfinder once to generate a config file
