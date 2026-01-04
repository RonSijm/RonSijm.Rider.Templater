# Counting Sort

## Description
Counting Sort is a non-comparison based sorting algorithm that counts the occurrences of each distinct element. It works by determining the position of each element based on these counts. Very efficient for sorting integers within a known range.

## When to Use
- **Small range of values** - When the range of values (k) is not much larger than n
- **Integer sorting** - Works with integers or values that can be mapped to integers
- **Linear time needed** - When O(n) performance is required
- **Stable sorting** - Preserves relative order of equal elements

## Time Complexity
- **Best Case**: O(n + k) where k is the range of input
- **Average Case**: O(n + k)
- **Worst Case**: O(n + k)
- **Space Complexity**: O(k) - requires extra space for counting array

## Templater Script

```
<%*
// Configuration
let size = 50;
let maxValue = 100; // Range of values: 0 to maxValue

// Generate random array
let arr = [];
for (let i = 0; i < size; i++) {
    arr[i] = Math.floor(Math.random() * maxValue);
}

let originalArray = [...arr];
let n = arr.length;

// Counting Sort Algorithm
function countingSort(arr) {
    let max = 0;
    for (let i = 0; i < arr.length; i++) {
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    
    // Create counting array
    let count = [];
    for (let i = 0; i <= max; i++) {
        count[i] = 0;
    }
    
    // Store count of each element
    for (let i = 0; i < arr.length; i++) {
        count[arr[i]]++;
    }
    
    // Change count[i] to contain actual position
    for (let i = 1; i <= max; i++) {
        count[i] += count[i - 1];
    }
    
    // Build output array
    let output = [];
    for (let i = arr.length - 1; i >= 0; i--) {
        output[count[arr[i]] - 1] = arr[i];
        count[arr[i]]--;
    }
    
    // Copy output array to arr
    for (let i = 0; i < arr.length; i++) {
        arr[i] = output[i];
    }
    
    return max;
}

let maxFound = countingSort(arr);
%>
## Counting Sort Results

**Array Size**: <%= size %>
**Value Range**: 0 to <%= maxValue - 1 %>
**Maximum Value Found**: <%= maxFound %>

**Original Array**: 
<%= originalArray.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Array Size (n): <%= size %>
- Range (k): <%= maxFound + 1 %>
- Time Complexity: O(<%= size %> + <%= maxFound + 1 %>) = O(<%= size + maxFound + 1 %>)
- Space Used: <%= maxFound + 1 %> counting array + <%= size %> output array

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>

**Note**: Counting Sort is most efficient when the range (k) is close to the array size (n).
Current ratio k/n = <%= ((maxFound + 1) / size).toFixed(2) %>
```

