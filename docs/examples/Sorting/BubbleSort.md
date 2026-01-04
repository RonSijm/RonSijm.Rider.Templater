# Bubble Sort

## Description
Bubble Sort is one of the simplest sorting algorithms. It repeatedly steps through the list, compares adjacent elements, and swaps them if they're in the wrong order. The algorithm gets its name because smaller elements "bubble" to the top of the list.

## When to Use
- **Educational purposes** - Great for learning sorting concepts
- **Small datasets** - Works fine for arrays with < 50 elements
- **Nearly sorted data** - Can be optimized to detect if array is already sorted
- **Simplicity is key** - When code readability matters more than performance

## Time Complexity
- **Best Case**: O(n) - when array is already sorted (with optimization)
- **Average Case**: O(n²)
- **Worst Case**: O(n²)
- **Space Complexity**: O(1) - sorts in place

## Templater Script

```
<%*
// Configuration
let size = 50;

// Generate random array
let arr = [];
for (let i = 0; i < size; i++) {
    arr[i] = Math.floor(Math.random() * 100);
}

let originalArray = [...arr];
let n = arr.length;
let swaps = 0;
let comparisons = 0;

// Bubble Sort Algorithm
for (let i = 0; i < n - 1; i++) {
    let swapped = false;
    for (let j = 0; j < n - i - 1; j++) {
        comparisons++;
        if (arr[j] > arr[j + 1]) {
            // Swap elements
            let temp = arr[j];
            arr[j] = arr[j + 1];
            arr[j + 1] = temp;
            swaps++;
            swapped = true;
        }
    }
    // Optimization: if no swaps occurred, array is sorted
    if (!swapped) {
        break;
    }
}
%>
## Bubble Sort Results

**Array Size**: <%= size %>

**Original Array**: 
<%= originalArray.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Total Comparisons: <%= comparisons %>
- Total Swaps: <%= swaps %>
- Theoretical Max Comparisons: <%= (n * (n - 1)) / 2 %>

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>
```

