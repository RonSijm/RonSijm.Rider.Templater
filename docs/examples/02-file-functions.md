---
title: File Functions Test
author: John Doe
tags:
  - example
  - test
  - file-operations
---

# File Functions Test

This template demonstrates all file-related functions.

## Basic File Properties

- **File Name:** `<% tp.file.name %>`
- **File Title:** `<% tp.file.title %>`
- **File Path:** `<% tp.file.path %>`
- **File Path (relative):** `<% tp.file.path(true) %>`

## Folder Information

- **Folder Path:** `<% tp.file.folder() %>`
- **Folder Path (relative):** `<% tp.file.folder(true) %>`

## File Content

- **Content Length:** `<% tp.file.content %>` characters

## File Metadata

- **Creation Date:** `<% tp.file.creation_date() %>`
- **Creation Date (formatted):** `<% tp.file.creation_date("yyyy-MM-dd HH:mm") %>`
- **Last Modified:** `<% tp.file.last_modified_date() %>`
- **Last Modified (formatted):** `<% tp.file.last_modified_date("MMMM dd, yyyy") %>`

## File Tags

- **Tags:** `<% tp.file.tags %>`

## File Selection

- **Current Selection:** `<% tp.file.selection() %>`

---
*Press Alt+R to execute this template*
*Try selecting some text before executing to test tp.file.selection()*

