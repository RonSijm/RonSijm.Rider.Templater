<%*
const now = () => (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now();
const ITEMS = 5000;

const rows = Array.from({ length: ITEMS }, (_, i) => ({
  i,
  name: ('item-' + i).padEnd(12, 'x'),
  active: (i % 4) !== 0
}));

const t0 = now();
let out = '';
for (let i = 0; i < rows.length; i++) {
  if (!rows[i].active) continue;
  out += `- ${rows[i].name} (${rows[i].i})\n`;
}
const t1 = now();

tR += '# Loop render-like benchmark\n\n';
tR += `Items: ${ITEMS}, active: ${rows.filter(x => x.active).length}\n`;
tR += `Time: ${(t1 - t0).toFixed(3)} ms\n`;
tR += `Output chars: ${out.length}\n\n`;
tR += '---\n';
tR += out.slice(0, 2000); // keep the note size reasonable
%>
