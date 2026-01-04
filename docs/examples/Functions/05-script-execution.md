---
title: Script Execution Test
count: 5
---

# Script Execution Test

This template demonstrates JavaScript execution with `<%* %>` syntax.

## Basic Script Execution

<%*
// This is a script block - it executes but doesn't output
const message = "Hello from script!";
console.log(message);
%>

## Using tR Variable

<%*
// tR accumulates output that can be inserted later
tR += "This text was added via tR variable.\n";
tR += "You can build up content programmatically.\n";
%>

**Script Output:**
<% tR %>

## Loops and Iteration

<%*
// Generate a numbered list
for (let i = 1; i <= 5; i++) {
    tR += `${i}. Item number ${i}\n`;
}
%>

**Generated List:**
<% tR %>

## Conditional Logic

<%*
const hour = new Date().getHours();
if (hour < 12) {
    tR += "Good morning! ☀️";
} else if (hour < 18) {
    tR += "Good afternoon! 🌤️";
} else {
    tR += "Good evening! 🌙";
}
%>

**Greeting:** <% tR %>

## Using Frontmatter in Scripts

<%*
const count = tp.frontmatter.count || 3;
tR += "\n**Countdown:**\n";
for (let i = count; i > 0; i--) {
    tR += `- ${i}...\n`;
}
tR += "- Blast off! 🚀\n";
%>

<% tR %>

## Async/Await Support

<%*
// Async operations are supported
async function fetchData() {
    return "Async data loaded!";
}

const result = await fetchData();
tR += `\n**Async Result:** ${result}\n`;
%>

<% tR %>

---
*Press Alt+R to execute this template*

