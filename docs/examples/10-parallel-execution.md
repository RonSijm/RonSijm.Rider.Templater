---
title: Parallel Execution Demo
---

# Parallel Execution Demo

This template demonstrates how the parallel execution engine analyzes dependencies.

## Independent Blocks (Can Run in Parallel)

These blocks have no dependencies on each other:

<%* let a = tp.date.now("YYYY") %>
<%* let b = tp.date.now("MM") %>
<%* let c = tp.date.now("DD") %>

Year: <% a %>
Month: <% b %>
Day: <% c %>

## Dependent Blocks (Must Run Sequentially)

These blocks depend on each other:

<%* let counter = 0 %>
<%* counter = counter + 1 %>
<%* counter = counter + 1 %>
<%* counter = counter + 1 %>

Final counter: <% counter %>

## Mixed Dependencies

Phase 1 (parallel): x and y are independent
Phase 2 (sequential): z depends on both

<%* let x = 10 %>
<%* let y = 20 %>
<%* let z = x + y %>

x = <% x %>, y = <% y %>, z = <% z %>

## tR Accumulation (Always Sequential)

tR writes must be sequential to preserve order:

<%* tR += "First " %><%* tR += "Second " %><%* tR += "Third" %><% tR %>

---

## How It Works

The parallel execution engine:

1. **Analyzes each block** for:
   - Variables written (`let x = ...`)
   - Variables read (references to `x`)
   - tR writes (`tR += ...`)
   - Barrier functions (`tp.system.prompt()`)

2. **Builds a dependency graph**:
   - Block B depends on Block A if B reads a variable A writes
   - tR writes create sequential dependencies
   - Barrier functions force all subsequent blocks to wait

3. **Groups into execution phases**:
   - Blocks with no dependencies run in Phase 1
   - Blocks depending on Phase 1 run in Phase 2
   - And so on...

4. **Executes phases**:
   - Blocks within a phase run in parallel (using coroutines)
   - Phases execute sequentially

## Benefits

- **Faster execution** for templates with many independent blocks
- **Automatic** - no syntax changes needed
- **Safe** - dependencies are respected
- **Compatible** - same results as sequential execution

