<%*
const digitsText = 500;
if (digitsText == null) { tR += "Cancelled"; return; }

const digitsAfterDecimal = Math.max(1, Math.min(200000, parseInt(digitsText, 10) || 0));

const now = () =>
(typeof performance !== "undefined" && performance.now) ? performance.now() : Date.now();

function spy(totalDigits) {
const len = Math.floor(totalDigits * 10 / 3) + 1;
const a = new Array(len).fill(2);

let nines = 0;
let predigit = 0;
let out = "";

for (let j = 0; j < totalDigits; j++) {
let q = 0;

    for (let i = len - 1; i >= 0; i--) {
      const x = 10 * a[i] + q * (i + 1);
      const denom = 2 * (i + 1) - 1;
      a[i] = x % denom;
      q = Math.floor(x / denom);
    }

    a[0] = q % 10;
    q = Math.floor(q / 10);

    if (q === 9) {
      nines++;
    } else if (q === 10) {
      out += String(predigit + 1);
      out += "0".repeat(nines);
      predigit = 0;
      nines = 0;
    } else {
      out += String(predigit);
      predigit = q;
      if (nines) {
        out += "9".repeat(nines);
        nines = 0;
      }
    }
}

out += String(predigit);
return out;
}

function cs32(s) {
let h = 0;
for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0;
return h >>> 0;
}

const totalDigits = digitsAfterDecimal + 1;

spy(30);

const runs = 3;
const times = [];
let lastPi = "";

for (let r = 0; r < runs; r++) {
const t0 = now();
const raw = spy(totalDigits);
const t1 = now();

lastPi = raw.startsWith("0") ? raw.slice(1) : raw;
times.push(t1 - t0);
}

times.sort((a, b) => a - b);
const medianMs = times[Math.floor(times.length / 2)];

const pi = lastPi;
const formatted = pi[0] + "." + pi.slice(1);

const previewLen = Math.min(80, formatted.length);
const preview = formatted.slice(0, previewLen);
const tail = formatted.slice(Math.max(0, formatted.length - 20));

tR += `Pi benchmark\n`;
tR += `digits_after_decimal: ${digitsAfterDecimal}\n`;
tR += `runs_ms: ${times.map(x => x.toFixed(2)).join(", ")}\n`;
tR += `median_ms: ${medianMs.toFixed(2)}\n`;
tR += `cs32: ${cs32(formatted)}\n`;
tR += `preview: ${preview}\n`;
tR += `tail: ${tail}\n`;
%>