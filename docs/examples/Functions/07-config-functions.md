---
title: Config Functions Test
---

# Config Functions Test

This template demonstrates configuration and metadata functions.

## Config Module

- **Active File:** `<% tp.config.active_file %>`
- **Run Mode:** `<% tp.config.run_mode %>`
- **Target File:** `<% tp.config.target_file %>`
- **Template File:** `<% tp.config.template_file %>`

## Understanding Run Modes

The `run_mode` indicates how Templater was executed:
- **DYNAMIC_PROCESSOR**: Template executed on current file (default in this plugin)
- **CREATE_NEW**: Template used to create a new file
- **USER_SCRIPT**: Template executed as a user script

## File Paths Explained

- **active_file**: The file that was active when template execution started
- **target_file**: The file where the template output will be inserted
- **template_file**: The file containing the template code

For inline templates (like this one), all three are typically the same file.

---
*Press Alt+R to execute this template*

