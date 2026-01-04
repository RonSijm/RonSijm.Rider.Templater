# Selection Sort

## Description
Selection Sort divides the array into a sorted and unsorted region. It repeatedly finds the minimum element from the unsorted region and moves it to the end of the sorted region. Think of it as selecting the smallest card from your hand and placing it in order.

## When to Use
- **Small datasets** - Simple and effective for small arrays
- **Memory constraints** - Only requires O(1) extra space
- **Minimize writes** - Makes fewer swaps than bubble sort (at most n swaps)
- **Simple implementation** - Easy to understand and implement

## Time Complexity
- **Best Case**: O(n²)
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

// Selection Sort Algorithm
for (let i = 0; i < n - 1; i++) {
    let minIndex = i;
    
    // Find the minimum element in unsorted portion
    for (let j = i + 1; j < n; j++) {
        comparisons++;
        if (arr[j] < arr[minIndex]) {
            minIndex = j;
        }
    }
    
    // Swap the found minimum element with the first element
    if (minIndex !== i) {
        let temp = arr[i];
        arr[i] = arr[minIndex];
        arr[minIndex] = temp;
        swaps++;
    }
}
%>
## Selection Sort Results

**Array Size**: <%= size %>

**Original Array**: 
<%= originalArray.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Total Comparisons: <%= comparisons %>
- Total Swaps: <%= swaps %>
- Theoretical Comparisons: <%= (n * (n - 1)) / 2 %>
- Maximum Possible Swaps: <%= n - 1 %>

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>
```

