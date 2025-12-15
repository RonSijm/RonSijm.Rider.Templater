---
title: Parallel Execution Test
---

# Dependency Test

This template tests scenarios that would break with naive parallel execution.
All blocks are currently executed sequentially, which is correct.

## Test 1: Variable Dependencies
<%* let counter = 0 %>
<%* counter = counter + 1 %>
<%* counter = counter + 1 %>
<%* counter = counter + 1 %>

Counter should be 3: <% counter %>

## Test 2: Read-After-Write
<%* let greeting = "Hello" %>
<%* greeting = greeting + " World" %>
<%* greeting = greeting + "!" %>

Greeting should be "Hello World!": <% greeting %>

## Test 3: tR Accumulation Order
<%*
tR += "A"
tR += "B"
tR += "C"
%>

Above should show "ABC" (not "CAB" or "BCA")

## Test 4: Prompt Order (interactive test)
Uncomment to test:
<!-- 
<%* let first = await tp.system.prompt("Enter FIRST value") %>
<%* let second = await tp.system.prompt("Enter SECOND value") %>

You entered: <% first %> then <% second %>
-->

## Test 5: Computed Dependencies
<%* let x = 5 %>
<%* let y = x * 2 %>
<%* let z = y + x %>

z should be 15 (not undefined or NaN): <% z %>

---

## Expected Results (Sequential Execution)

| Test | Expected | Would Break With Parallel? |
|------|----------|---------------------------|
| Counter | 3 | Yes - race condition |
| Greeting | Hello World! | Yes - race condition |
| tR | ABC | Yes - ordering |
| z | 15 | Yes - undefined variables |

If any of these values are wrong, sequential execution is broken.

