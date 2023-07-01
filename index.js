const os = require("os");
const fs = require('fs');
const {exec} = require('child_process');

const seedFinderPath = "seed-finder-random.jar"
const seedFileName = "seedsFound.txt"
const seedListFileName = "seedList.txt"

const args = process.argv;
let depth = args[2];
let mode = args[3];
let itemFile = args[4];
const threadsArg = Math.abs(args[5]) || 4;

//check arguments
if (!(depth && mode && itemFile)) {
    console.error("missing arguments. syntax:");
    console.log("node this.js <floors> <any/all> <itemFileName> <threadsToUse>");
    process.exit();
}

//print CPU information
let cpus = os.cpus();
let threads = cpus.length;
console.log("\nCPU information");
console.log("model:     " + cpus[0].model); // only works for 1st CPU
console.log("threads:   " + threads);
console.log("\n");

let startFinders = (d, m, f) => {
    let n = Math.min(threadsArg, threads);

    let cmd = `java -jar ${seedFinderPath} ${d} ${m} ${f} ${seedFileName}`;
    console.log(`starting ${n} seedfinders sparing ${threads - n} threads...`);
    console.log(`java command: ` + cmd + "\n");

    for (let i = 0; i < n; i++) {
        exec(cmd, (err, stdout, stderr) => {
            //ignore java errors because there is always at least one
        });

        console.log(`process ${i+1} started`);
    }
}




let exit = () => {
    fs.readFile(seedFileName, 'utf8', (err, txt) => {
        if (err) {
            if (err.length > 25) {
                err = err.substring(0, 24) + "...";
            }
            console.error(err);
            return;
        }

        let seeds = txt.match(/[A-Z][A-Z][A-Z][-][A-Z][A-Z][A-Z][-][A-Z][A-Z][A-Z]/gm);

        if (seeds) {
            let s = seeds.length;
            
            if (s >= 10) {
                let seedsToSave = ``;

                seeds.forEach(seed => seedsToSave += seed + "\n");

                fs.writeFile(seedListFileName, seedsToSave, (err) => {
                    if (err)
                        console.log(err);
                });

                console.log(`\nfound ${s} seeds, saving to ${seedListFileName} ...`);

            } else {
                console.log(`\nfound ${s} seeds:`);
                seeds.forEach(seed => console.log(seed));
            }

        } else {
            console.log("found 0 seeds.");
        }

    });
}

startFinders(depth, mode, itemFile);
console.log("\npress ctrl + C to quit");

process.on("SIGINT", exit);