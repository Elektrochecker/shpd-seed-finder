const os = require("os");
const fs = require('fs');
const { spawn } = require('child_process');

let seedFinderPath = "seed-finder.jar"
const seedFinderPathAlternate = "seed-finder-random.jar"
const seedFileName = "seedsFound.txt"
const seedListFileName = "seedList.txt"
let SHPDversion

//does not seem to be supported on all versions of windows, change this to false if the output looks wrong
let useCliColors = true

const args = process.argv;
let depth = args[2];
let mode = args[3];
let itemFile = args[4];
const threadsArg = Math.abs(args[5]) || 4;

let finders = []
const seedRegex = /[A-Z]{3}[-][A-Z]{3}[-][A-Z]{3}/gm

let clicolors = ["", ""]
if (useCliColors) clicolors = [`\x1b[34m`, `\x1b[0m`]

//check arguments
if (!(depth && mode && itemFile)) {
    console.error("missing arguments. syntax:");
    console.log("node this.js <floors> <any/all> <itemFileName> <threadsToUse>");
    process.exit();
}

//the seedfinder defaults to "all", so this does as well
if (mode && mode != "any") mode = "all";

//check if seedfinder exists
if (!fs.existsSync(seedFinderPath)) {
    if (fs.existsSync(seedFinderPathAlternate)) {
        console.log(`could not find ${seedFinderPath}, using ${seedFinderPathAlternate} instead...`);
        seedFinderPath = seedFinderPathAlternate;
    } else {
        console.error(`could not find ${seedFinderPath}`);
        process.exit();
    }

}

//print system information
let cpus = os.cpus();
let threads = cpus.length;
console.log("\nCPU information");
console.log("model:     " + cpus[0].model); // only works for 1st CPU
console.log("threads:   " + threads);
console.log("platform:  " + os.platform());
console.log()

let startFinders = (d, m, f) => {
    let n = Math.min(threadsArg, threads);

    let cmd = `java -jar ${seedFinderPath} ${d} ${m} ${f} ${seedFileName}`;
    let spawn_args = ["-jar", seedFinderPath, d, m, f, seedFileName];
    console.log(`starting ${n} seedfinders sparing ${threads - n} threads...`);
    console.log(`java command: ` + cmd + "\n");

    for (let i = 0; i < n; i++) {
        finders.push(spawn("java", spawn_args))
        finders[i].stdout.on("data", d => {
            d = d.toString()

            if (!SHPDversion) {
                let v = d.match(/v[0-9]\.[0-9]\.[0-9]/gm)
                if (v) {
                    v = v[0]
                    SHPDversion = v
                    console.log(`detected version: ${v}\n`)
                }
            }

            let seed = d.match(seedRegex)
            if (seed) {
                console.log(clicolors[0] + `[${i+1}] ` + clicolors[1] + seed[0])
            }
        })

        console.log(`process ${i+1} started`);
    }
}

let exit = () => {
    finders.forEach(finder => {
        finder.kill("SIGINT")
    })

    fs.readFile(seedFileName, 'utf8', (err, txt) => {
        if (err) {
            console.error(err);
            return;
        }

        let seeds = txt.match(seedRegex);

        if (seeds) {
            let s = seeds.length;

            fs.readFile(itemFile, "utf8", (err, items) => {
                if (err) { console.error(err) }

                let seedsToSave = ""
                seedsToSave += "automatically generated by Elektrocheckers seedfinder\n\n"
                seedsToSave += SHPDversion ? `SHPD ${SHPDversion}\n` : `SHPD version not detected\n`
                seedsToSave += `Seeds containing ${mode} of the following items until floor ${depth}:\n\n`;

                items.split("\n").forEach(i => {
                    if (i.length > 0) {
                        seedsToSave += "- " + i + "\n"
                    }
                })

                seeds.forEach(seed => seedsToSave += "\n" + seed);

                fs.writeFile(seedListFileName, seedsToSave, (err) => {
                    if (err) { console.log(err) }
                });
            });

            console.log(`\nfound ${s} seeds, saving to ${seedListFileName} ...`);

        } else {
            console.log("found 0 seeds.");
        }

    });
}

startFinders(depth, mode, itemFile);
console.log("\npress ctrl + C to quit");

process.on("SIGINT", exit);
