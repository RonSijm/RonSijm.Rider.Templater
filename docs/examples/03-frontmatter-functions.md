---
title: Frontmatter Test Document
author: Jane Smith
version: 1.0.0
status: draft
priority: high
tags:
  - documentation
  - frontmatter
  - yaml
metadata:
  project: Templater Plugin
  department: Engineering
  reviewers:
    - Alice
    - Bob
    - Charlie
---

# Frontmatter Functions Test

This template demonstrates accessing YAML frontmatter values.

## Simple Frontmatter Values

- **Title:** `<% tp.frontmatter.title %>`
- **Author:** `<% tp.frontmatter.author %>`
- **Version:** `<% tp.frontmatter.version %>`
- **Status:** `<% tp.frontmatter.status %>`
- **Priority:** `<% tp.frontmatter.priority %>`

## Array Values

- **Tags:** `<% tp.frontmatter.tags %>`

## Nested Object Values

- **Project:** `<% tp.frontmatter.metadata.project %>`
- **Department:** `<% tp.frontmatter.metadata.department %>`
- **Reviewers:** `<% tp.frontmatter.metadata.reviewers %>`

## Using Frontmatter in Text

Welcome to **<% tp.frontmatter.title %>**!

This document was created by **<% tp.frontmatter.author %>** and is currently in **<% tp.frontmatter.status %>** status with **<% tp.frontmatter.priority %>** priority.

Project: <% tp.frontmatter.metadata.project %>

---
*Press Alt+R to execute this template*

