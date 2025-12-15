---
title: Meeting Notes Template
date: 
attendees: []
project: 
status: draft
---

# Meeting Notes: <% tp.frontmatter.title %>

**Date:** <% tp.date.now("MMMM dd, yyyy") %>
**Time:** <% tp.date.now("HH:mm") %>
**Project:** <% tp.frontmatter.project %>

## Attendees

<% tp.frontmatter.attendees %>

## Agenda

1. 
2. 
3. 

## Discussion

### Topic 1



### Topic 2



### Topic 3



## Action Items

<%*
// Generate action item template
for (let i = 1; i <= 5; i++) {
    tR += `- [ ] Action ${i}: \n`;
}
%>

<% tR %>

## Decisions Made

- 

## Next Steps

- **Next Meeting:** <% tp.date.weekday("MMMM dd, yyyy", 7) %>
- **Follow-up By:** <% tp.date.tomorrow("yyyy-MM-dd") %>

---

**Notes taken by:** <% tp.system.prompt("Your name?", "Anonymous") %>
**Status:** <% tp.frontmatter.status %>

---
*Press Alt+R to execute this template*

