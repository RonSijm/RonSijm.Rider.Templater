<%*
const now = () => (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now(); // hi-res if available :contentReference[oaicite:1]{index=1}

const RUNS = 20000;     // increase until timings stabilize, keep under Templater timeout
const ITEMS = 2000;     // list size for loop benchmark
const STR_LEN = 64;     // string size for escape benchmark

const pad = (s, n) => (s + ' '.repeat(n)).slice(0, n);
const fmt = (n) => n.toFixed(3);

function bench(name, iters, fn) {
const t0 = now();
let sink = 0;
for (let i = 0; i < iters; i++) sink ^= fn(i) | 0;
const t1 = now();
const ms = t1 - t0;
return { name, iters, ms, nsPer: (ms * 1e6) / iters, sink };
}

function makeWord(i) {
return ('w' + i.toString(36)).padEnd(8, 'x');
}

// 1) Interpolation-like string building (small appends)
const interp = bench('string_build_small_appends', RUNS, (i) => {
const a = 'v' + i;
const b = 'x' + (i + 1);
const s = a + ':' + b + ';';     // small concat
return s.length;
});

// 2) Deep property access (template-style paths)
const data = { user: { profile: { address: { city: 'Amsterdam', zip: '1011' } } } };
const deep = bench('deep_property_access', RUNS, (_) => {
const v = data.user.profile.address.city;
return v.length;
});

// 3) Missing-path handling (common in templates)
const missing = bench('missing_property_fallback', RUNS, (i) => {
const v = (data.user && data.user.profile && data.user.profile.missingKey) || '';
return v.length + (i & 1);
});

// 4) Filter chain style transforms
function filterChain(s) {
return s.trim().toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9\-]/g, '');
}
const filters = bench('filter_chain', RUNS, (i) => {
const s = '  Hello Template ' + makeWord(i) + ' !!  ';
return filterChain(s).length;
});

// 5) Loop + conditional in body (typical list rendering)
const list = Array.from({ length: ITEMS }, (_, i) => ({ i, name: makeWord(i), active: (i % 3) !== 0 }));
const loop = bench('loop_with_conditional', 200, (_) => {
// 200 iterations * ITEMS work, keeps total bounded
let outLen = 0;
for (let i = 0; i < list.length; i++) {
if (!list[i].active) continue;
outLen += (list[i].name.length + 3);
}
return outLen;
});

// 6) HTML escaping workload
function htmlEscape(s) {
// intentionally simple, stresses scanning and branchy replace logic
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
const nasty = ('<&>"\'' + ' abcDEF0123 ').repeat(Math.ceil(STR_LEN / 12)).slice(0, STR_LEN);
const escape = bench('html_escape', RUNS, (i) => htmlEscape(nasty + i).length);

// Output
const results = [interp, deep, missing, filters, loop, escape];

tR += '# Templater Bench Suite\n\n';
tR += `Runs: ${RUNS}, Items: ${ITEMS}, StrLen: ${STR_LEN}\n\n`;
tR += '| Benchmark | iters | ms | ns/op |\n';
tR += '|---|---:|---:|---:|\n';
for (const r of results) {
tR += `| ${r.name} | ${r.iters} | ${fmt(r.ms)} | ${fmt(r.nsPer)} |\n`;
}
tR += '\n';
%>
