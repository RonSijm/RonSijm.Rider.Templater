---
title: Date Functions Test
category: examples
---

# Date Functions Test

This template demonstrates all date-related functions.

## Basic Date Functions

<% tp.date.now() %>

- **Current Date/Time:** `<% tp.date.now() %>`
- **Today:** `<% tp.date.today() %>`
- **Tomorrow:** `<% tp.date.tomorrow() %>`
- **Yesterday:** `<% tp.date.yesterday() %>`

## Custom Date Formats

- **ISO Format:** `<% tp.date.now("yyyy-MM-dd") %>`
- **US Format:** `<% tp.date.now("MM/dd/yyyy") %>`
- **Long Format:** `<% tp.date.now("MMMM dd, yyyy") %>`
- **Time Only:** `<% tp.date.now("HH:mm:ss") %>`
- **Full DateTime:** `<% tp.date.now("yyyy-MM-dd HH:mm:ss") %>`

## Weekday Functions

- **This Monday:** `<% tp.date.weekday("yyyy-MM-dd", 0) %>`
- **This Tuesday:** `<% tp.date.weekday("yyyy-MM-dd", 1) %>`
- **This Friday:** `<% tp.date.weekday("yyyy-MM-dd", 4) %>`
- **Next Monday:** `<% tp.date.weekday("yyyy-MM-dd", 7) %>`
- **Last Monday:** `<% tp.date.weekday("yyyy-MM-dd", -7) %>`

## Moment.js Formats

- **Moment Format 1:** `<% tp.date.now("YYYY-MM-DD") %>`
- **Moment Format 2:** `<% tp.date.now("dddd, MMMM Do YYYY") %>`
- **Moment Format 3:** `<% tp.date.now("h:mm:ss a") %>`

---
*Press Alt+R to execute this template*