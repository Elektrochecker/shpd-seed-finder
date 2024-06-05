/*
    The JSON-converter script converts the human-readable output of the
    seedfinder or the turbo script into a JSON-format,
    convenient for further processing.

    Usage:
    node json-converter.js <input file> [output file]
*/

const { readFileSync, writeFileSync } = require("fs")

inputFile = process.argv[2]
outputFile = process.argv[3] || inputFile.replace(/\..*/g, ".json")

let out = []
let itemlist_plaintext = readFileSync(inputFile, "utf-8")
let seeds_plaintext = itemlist_plaintext.split("Items for seed ")

const descSeparator = " - "

seeds_plaintext.forEach((seed_plaintext, index) => {
    if (seed_plaintext) {
        let seed = {}
        seed.floors = []

        let regSeed = /[A-Z]{3}-[A-Z]{3}-[A-Z]{3} \([0-9]*\)\:/gm
        let regFloors = /\-\-\- floor [0-9]*\: /gmi
        let regItems = /\- .*\n/gmi

        let seedcode = seed_plaintext.match(regSeed)[0]

        seed.code = seedcode.slice(0, 11)
        seed.codeInt = seedcode.match(/\([0-9]*\)/)[0]
        seed.codeInt = seed.codeInt.slice(1, seed.codeInt.length - 1)
        seed.codeInt = Number(seed.codeInt)

        let floors = seed_plaintext.split(regFloors)

        floors.forEach((f, d) => {
            floor = {
                depth: d,
                items: [],
            }

            if (d > 0) floor.feeling = f.split("\n")[0]

            items = f.match(regItems)

            if (items) {
                items.forEach(item => {
                    item = item.replace("- ", "")
                    item = item.replace("\n", "")
                    item = item.replace(/( ){3,}/, descSeparator)

                    floor.items.push(item)
                })
            }

            seed.floors.push(floor)
        })

        out.push(seed)

        console.log(`converted seed ${seed.code}`)
    }
});

console.log()
console.log(`converted ${out.length} seeds, saving to ${outputFile}...`)
writeFileSync(outputFile, JSON.stringify(out))