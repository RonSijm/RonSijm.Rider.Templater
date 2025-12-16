# 2 - Emoji-Labeled Callout with Content

Interactive callout creator using an object map with color-coded emoji labels for each type. Groups similar callout types together (e.g., "Tip / Hint / Important"). Prompts for title and multiline content, with content automatically formatted with blockquote prefixes.

**Source:** https://github.com/SilentVoid13/Templater/discussions/922#discussioncomment-5515759

```

<%*
const callouts = {
   note:     '🔵 ✏ Note',
   info:     '🔵 ℹ Info',
   todo:     '🔵 🔳 Todo',
   tip:      '🌐 🔥 Tip / Hint / Important',
   abstract: '🌐 📋 Abstract / Summary / TLDR',
   question: '🟡 ❓ Question / Help / FAQ',
   quote:    '🔘 💬 Quote / Cite',
   example:  '🟣 📑 Example',
   success:  '🟢 ✔ Success / Check / Done',
   warning:  '🟠 ⚠ Warning / Caution / Attention',
   failure:  '🔴 ❌ Failure / Fail / Missing',
   danger:   '🔴 ⚡ Danger / Error',
   bug:      '🔴 🐞 Bug',
};

const type = await tp.system.suggester(Object.values(callouts), Object.keys(callouts), true, 'Select callout type.');
const fold = await tp.system.suggester(['None', 'Expanded', 'Collapsed'], ['', '+', '-'], true, 'Select callout fold option.');

const title = await tp.system.prompt('Title:', '', true);
let content = await tp.system.prompt('Content (New line -> Shift + Enter):', '', true, true);
content = content.split('\n').map(line => `> ${line}`).join('\n')

const calloutHead = `> [!${type}]${fold} ${title}\n`;

tR += calloutHead + content
-%>

```