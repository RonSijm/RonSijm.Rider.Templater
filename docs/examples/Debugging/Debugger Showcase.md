---
title: Debugger Showcase
description: A template designed to demonstrate the Templater debugger capabilities
---

# 🔍 Templater Debugger Showcase

This template is designed to showcase the **debugging capabilities** of the Rider Templater plugin.
Try setting breakpoints and stepping through the code!

---

## 1️⃣ Variable Inspection

<%*
// Set a breakpoint here to see variables being created
const userName = "Developer";
const projectName = "Templater";
let counter = 0;

// Step through to watch 'counter' change
counter = counter + 1;
counter = counter + 5;
counter = counter * 2;

tR += `Hello, ${userName}! Welcome to ${projectName}.\n`;
tR += `Counter value: ${counter}\n`;
%>

<% tR %>

---

## 2️⃣ Loop Debugging

<%*
// Set a breakpoint inside the loop to step through each iteration
const fruits = ["🍎 Apple", "🍊 Orange", "🍋 Lemon", "🍇 Grape", "🍓 Strawberry"];

tR += "**Shopping List:**\n";
for (let i = 0; i < fruits.length; i++) {
    // Breakpoint here - watch 'i' and 'fruit' change each iteration
    const fruit = fruits[i];
    tR += `${i + 1}. ${fruit}\n`;
}
%>

<% tR %>

---

## 3️⃣ Conditional Branching

<%*
// Step through to see which branch is taken
const hour = new Date().getHours();
let greeting = "";
let emoji = "";

if (hour < 6) {
    // Night owl branch
    greeting = "Working late?";
    emoji = "🦉";
} else if (hour < 12) {
    // Morning branch
    greeting = "Good morning!";
    emoji = "☀️";
} else if (hour < 18) {
    // Afternoon branch
    greeting = "Good afternoon!";
    emoji = "🌤️";
} else {
    // Evening branch
    greeting = "Good evening!";
    emoji = "🌙";
}

tR += `${emoji} ${greeting} (Current hour: ${hour})\n`;
%>

<% tR %>

---

## 4️⃣ Function Calls & Inner Functions

<%*
// This demonstrates stepping INTO functions
function calculateSum(numbers) {
    // Set breakpoint here to debug inside the function
    let total = 0;
    for (const num of numbers) {
        total = total + num;
    }
    return total;
}

function formatResult(label, value) {
    // Another function to step into
    return `**${label}:** ${value}`;
}

const numbers = [10, 20, 30, 40, 50];
const sum = calculateSum(numbers);
const average = sum / numbers.length;

tR += formatResult("Numbers", numbers.join(", ")) + "\n";
tR += formatResult("Sum", sum) + "\n";
tR += formatResult("Average", average) + "\n";
%>

<% tR %>

---

## 5️⃣ Nested Loops & Complex Flow

<%*
// Great for testing step-over vs step-into
function createGrid(rows, cols) {
    let grid = "";
    for (let r = 0; r < rows; r++) {
        for (let c = 0; c < cols; c++) {
            // Innermost point - lots of iterations!
            grid += (r + c) % 2 === 0 ? "⬜" : "⬛";
        }
        grid += "\n";
    }
    return grid;
}

tR += "**Checkerboard Pattern:**\n";
tR += "```\n";
tR += createGrid(5, 10);
tR += "```\n";
%>

<% tR %>

---

## 6️⃣ Error Handling Demo

<%*
// Uncomment the line below to test debugger behavior on errors
// const willFail = undefinedVariable.property;

function safeDivide(a, b) {
    if (b === 0) {
        return "Cannot divide by zero!";
    }
    return a / b;
}

tR += `10 / 2 = ${safeDivide(10, 2)}\n`;
tR += `10 / 0 = ${safeDivide(10, 0)}\n`;
%>

<% tR %>

---

## 🎯 Debugging Tips

1. **Set Breakpoints**: Click in the gutter next to line numbers
2. **Step Over (F10)**: Execute current line, skip function internals
3. **Step Into (F11)**: Enter function calls to debug inside them
4. **Step Out (Shift+F11)**: Finish current function and return
5. **Continue (F5)**: Run until next breakpoint
6. **Watch Variables**: Inspect variable values in the debug panel

---

*Press Alt+Shift+R to debug this template, or use the Debug action from the context menu*

