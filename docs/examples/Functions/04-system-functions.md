---
title: System Functions Test
---

# System Functions Test

This template demonstrates system interaction functions.

## Clipboard

- **Clipboard Content:** `<% tp.system.clipboard() %>`

## User Prompts

**Note:** These will show dialog boxes when executed!

- **Simple Prompt:** `<% tp.system.prompt("What is your name?") %>`
- **Prompt with Default:** `<% tp.system.prompt("Enter your email", "user@example.com") %>`

## Suggester (Single Choice)

**Note:** This will show a selection dialog!

```
<% tp.system.suggester(["Option 1", "Option 2", "Option 3"], ["opt1", "opt2", "opt3"]) %>
```

## Multi-Suggester (Multiple Choices)

**Note:** This will show a multi-selection dialog!

```
<% tp.system.multi_suggester(["Task A", "Task B", "Task C"], ["a", "b", "c"]) %>
```

## Example: Interactive Document Creation

<%*
// Get user input
const projectName = tp.system.prompt("Enter project name", "My Project");
const author = tp.system.prompt("Enter author name", "Anonymous");
%>

**Project:** <% projectName %>
**Author:** <% author %>
**Created:** <% tp.date.now("yyyy-MM-dd") %>

---
*Press Alt+R to execute this template*
*Warning: This will show interactive dialogs!*

