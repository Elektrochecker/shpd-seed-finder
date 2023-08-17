let totalSeeds = 5429503678976;
let inputSeed = process.argv[2].replace("\"", "");

let letters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"];
let integers = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];

let convertFromCode = code => {
    if (code.length == 11 && code[3] == "-" && code[7] == "-") {
        code = code.toUpperCase();
    }

    code = code.replaceAll(/[-\\s]/g, "");

    if (code.length != 9) {
        return false;
    }

    let result = 0;

    for (let i = 8; i >= 0; i--) {
        let c = code[i];
        if (c > "Z" || c < "A") {
            return false;
        }

        result += (c.charCodeAt(0) - 65) * 26 ** (8 - i);
    }

    return result;
}

let convertToCode = seed => {
    if (seed < 0 || seed >= totalSeeds) {
        return false
    }

    let interim = seed.toString(26);
    let result = "";

    for (let i = 0; i < 9; i++) {
        if (i < interim.length) {
            let c = interim[i];

            if (!integers.includes(c)) {
                c = c.charCodeAt(0) - 87;
            }

            c = letters[c];

            result += c;
        } else {
            result = insertString(result, "A", 0);
        }
    }

    result = insertString(result, "-", 3);
    result = insertString(result, "-", 7);

    return result;
}

let convertFromText = txt => {
    if (convertFromCode(txt)) {
        return convertFromCode(txt);
    }

    let n = txt.replaceAll(" ", "");
    if (!isNaN(n)) {
        return n % totalSeeds;
    }

    let total = 0;
    txt.split("").forEach(c => {
        total = 31 * total + c.charCodeAt(0);
    });
    total %= totalSeeds;
    return total;
}

let insertString = (string, insert, offset) => {
    string = string.toString();
    return `${string.slice(0,offset)}${insert}${string.slice(offset,string.length)}`;
}

let seed = convertFromText(inputSeed);

if (!seed) {
    console.error("ERROR");
    console.log(seed);
    process.exit();
}

console.log("\nequivalent seeds:\n");
console.log(inputSeed);
console.log(seed);
console.log(convertToCode(seed));
console.log("\n");