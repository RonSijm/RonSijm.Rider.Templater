---
title: Whitespace Control Test
author: Test User
---

# Whitespace Control Test

This template demonstrates whitespace trimming with `<%-` and `-%>`.

## Normal Template (No Trimming)

Before
<% tp.frontmatter.author %>
After

## Left Trim (`<%-`)

Before
<%- tp.frontmatter.author %>
After

## Right Trim (`-%>`)

Before
<% tp.frontmatter.author -%>
After

## Both Sides Trim

Before
<%- tp.frontmatter.author -%>
After

## Underscore Trim (Removes All Whitespace)

Before
<%_ tp.frontmatter.author _%>
After

## Script Blocks with Whitespace Control

Normal script block:
<%* const x = 1; %>
Text after script

Trimmed script block:
<%*- const y = 2; -%>
Text after script

## Practical Example: Clean Lists

<%*
const items = ["Apple", "Banana", "Cherry"];
for (const item of items) {
    tR += `- ${item}\n`;
}
-%>
<% tR %>

---
*Press Alt+R to execute this template*
*Notice the difference in spacing between sections*

