<%*
const now = () => (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now();
const RUNS = 30000;

function bench(name, fn) {
const t0 = now();
let sink = 0;
for (let i = 0; i < RUNS; i++) sink ^= fn(i) | 0;
const t1 = now();
const ms = t1 - t0;
tR += `- ${name}: ${ms.toFixed(3)} ms, ${(ms * 1e6 / RUNS).toFixed(3)} ns/op\n`;
}

// Ensure your note has frontmatter like:
// ---
// title: Example Title
// tags: [a, b, c]
// ---
tR += '# Frontmatter lookup benchmark\n\n';

bench('tp.frontmatter.title (present)', () => {
const v = tp.frontmatter.title ?? '';
return ('' + v).length;
});

bench('tp.frontmatter.missing (missing)', () => {
const v = tp.frontmatter.missingKey ?? '';
return ('' + v).length;
});

bench('tp.frontmatter["key with spaces"] (if exists)', () => {
const v = tp.frontmatter['key with spaces'] ?? '';
return ('' + v).length;
});
%>
