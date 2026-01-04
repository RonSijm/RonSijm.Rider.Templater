<%*
const now = () => (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now();
const RUNS = 20000;
const STR_LEN = 256;

function htmlEscape(s) {
let r = '';
for (let i = 0; i < s.length; i++) {
const c = s.charCodeAt(i);
if (c === 38) r += '&amp;';
else if (c === 60) r += '&lt;';
else if (c === 62) r += '&gt;';
else if (c === 34) r += '&quot;';
else if (c === 39) r += '&#39;';
else r += s[i];
}
return r;
}

const base = ('<&>"\'' + ' loremIPSUM0123 ').repeat(64).slice(0, STR_LEN);

const t0 = now();
let bytesOut = 0;
for (let i = 0; i < RUNS; i++) bytesOut += htmlEscape(base + i).length;
const t1 = now();

const ms = t1 - t0;
const mb = bytesOut / (1024 * 1024);
const mbps = mb / (ms / 1000);

tR += '# Escape-heavy benchmark\n\n';
tR += `Runs: ${RUNS}, StrLen: ${STR_LEN}\n\n`;
tR += `Time: ${ms.toFixed(3)} ms\n`;
tR += `Output: ${mb.toFixed(3)} MiB\n`;
tR += `Throughput: ${mbps.toFixed(3)} MiB/s\n`;
%>
