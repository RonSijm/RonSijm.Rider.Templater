# Insertion Sort

## Description
Insertion Sort builds the final sorted array one item at a time. It takes each element and inserts it into its correct position in the already-sorted portion of the array. Similar to how you might sort playing cards in your hand.

## When to Use
- **Nearly sorted data** - Excellent performance on almost-sorted arrays
- **Small datasets** - Very efficient for small arrays (< 50 elements)
- **Online sorting** - Can sort data as it arrives
- **Adaptive algorithm** - Performance improves with partially sorted data

## Time Complexity
- **Best Case**: O(n) - when array is already sorted
- **Average Case**: O(n²)
- **Worst Case**: O(n²) - when array is reverse sorted
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
let shifts = 0;
let comparisons = 0;

// Insertion Sort Algorithm
for (let i = 1; i < n; i++) {
    let key = arr[i];
    let j = i - 1;
    
    // Move elements greater than key one position ahead
    while (j >= 0 && arr[j] > key) {
        comparisons++;
        arr[j + 1] = arr[j];
        j--;
        shifts++;
    }
    if (j >= 0) {
        comparisons++;
    }
    
    arr[j + 1] = key;
}
%>
## Insertion Sort Results

**Array Size**: <%= size %>

**Original Array**: 
<%= originalArray.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Total Comparisons: <%= comparisons %>
- Total Shifts: <%= shifts %>
- Average shifts per element: <%= (shifts / n).toFixed(2) %>

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>
```

