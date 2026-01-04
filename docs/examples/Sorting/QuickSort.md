# Quick Sort

## Description
Quick Sort is a divide-and-conquer algorithm that picks a pivot element and partitions the array around it. Elements smaller than the pivot go to the left, larger elements go to the right. It then recursively sorts the sub-arrays.

## When to Use
- **Large datasets** - One of the fastest general-purpose sorting algorithms
- **Average case performance** - Excellent O(n log n) average performance
- **In-place sorting** - Uses minimal extra memory
- **General purpose** - Default choice for most sorting needs

## Time Complexity
- **Best Case**: O(n log n)
- **Average Case**: O(n log n)
- **Worst Case**: O(n²) - rare with good pivot selection
- **Space Complexity**: O(log n) - for recursion stack

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
let comparisons = 0;
let swaps = 0;
let recursionDepth = 0;
let maxDepth = 0;

// Partition function
function partition(arr, low, high) {
    let pivot = arr[high];
    let i = low - 1;
    
    for (let j = low; j < high; j++) {
        comparisons++;
        if (arr[j] < pivot) {
            i++;
            // Swap arr[i] and arr[j]
            let temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
            swaps++;
        }
    }
    
    // Swap arr[i+1] and arr[high] (pivot)
    let temp = arr[i + 1];
    arr[i + 1] = arr[high];
    arr[high] = temp;
    swaps++;
    
    return i + 1;
}

// Quick Sort function
function quickSort(arr, low, high) {
    recursionDepth++;
    if (recursionDepth > maxDepth) {
        maxDepth = recursionDepth;
    }
    
    if (low < high) {
        let pi = partition(arr, low, high);
        quickSort(arr, low, pi - 1);
        quickSort(arr, pi + 1, high);
    }
    
    recursionDepth--;
}

// Sort the array
quickSort(arr, 0, arr.length - 1);
%>
## Quick Sort Results

**Array Size**: <%= size %>

**Original Array**: 
<%= originalArray.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Total Comparisons: <%= comparisons %>
- Total Swaps: <%= swaps %>
- Maximum Recursion Depth: <%= maxDepth %>
- Theoretical Best Comparisons: ~<%= Math.floor(size * Math.log2(size)) %>

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>
```

