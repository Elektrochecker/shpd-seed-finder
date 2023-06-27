const os = require("os");
const {exec} = require('child_process');

const seedFinderPath = "seed-finder-random.jar"

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

let cpus = os.cpus();
let threads = cpus.length;
console.log("\nCPU information");
console.log("model:     " + cpus[0].model); // only works for 1st CPU
console.log("threads:   " + threads);
console.log("\n");

let startFinders = (d, m, f) => {
    let n = Math.min(threadsArg, threads);

    let cmd = `java -jar ${seedFinderPath} ${d} ${m} ${f} multithread.txt`;
    console.log(`starting ${n} seedfinders sparing ${threads - n} threads...`);
    console.log(`java command: ` + cmd + "\n");

    for (let i = 0; i < n; i++) {
        exec(cmd, (err, stdout, stderr) => {
            if (err) {
                console.error(`error while executing: ${err}`);
                return;
            }
        });

        console.log(`process ${i+1} started`);
    }

    console.log("\npress ctrl + C to quit");
}

startFinders(depth, mode, itemFile);