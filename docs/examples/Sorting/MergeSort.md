# Merge Sort

## Description
Merge Sort is a divide-and-conquer algorithm that divides the array into two halves, recursively sorts them, and then merges the sorted halves back together. It's stable and has predictable performance.

## When to Use
- **Stable sorting required** - Preserves the relative order of equal elements
- **Predictable performance** - Always O(n log n), no worst-case degradation
- **Large datasets** - Excellent for large arrays
- **Linked lists** - Particularly efficient for sorting linked lists
- **External sorting** - Good for sorting data that doesn't fit in memory

## Time Complexity
- **Best Case**: O(n log n)
- **Average Case**: O(n log n)
- **Worst Case**: O(n log n)
- **Space Complexity**: O(n) - requires extra space for merging

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
let merges = 0;
let recursionDepth = 0;
let maxDepth = 0;

// Merge function
function merge(arr, left, mid, right) {
    let n1 = mid - left + 1;
    let n2 = right - mid;
    
    // Create temp arrays
    let L = [];
    let R = [];
    
    for (let i = 0; i < n1; i++) {
        L[i] = arr[left + i];
    }
    for (let j = 0; j < n2; j++) {
        R[j] = arr[mid + 1 + j];
    }
    
    // Merge the temp arrays back
    let i = 0, j = 0, k = left;
    
    while (i < n1 && j < n2) {
        comparisons++;
        if (L[i] <= R[j]) {
            arr[k] = L[i];
            i++;
        } else {
            arr[k] = R[j];
            j++;
        }
        k++;
        merges++;
    }
    
    // Copy remaining elements
    while (i < n1) {
        arr[k] = L[i];
        i++;
        k++;
        merges++;
    }
    
    while (j < n2) {
        arr[k] = R[j];
        j++;
        k++;
        merges++;
    }
}

// Merge Sort function
function mergeSort(arr, left, right) {
    recursionDepth++;
    if (recursionDepth > maxDepth) {
        maxDepth = recursionDepth;
    }
    
    if (left < right) {
        let mid = Math.floor((left + right) / 2);
        mergeSort(arr, left, mid);
        mergeSort(arr, mid + 1, right);
        merge(arr, left, mid, right);
    }
    
    recursionDepth--;
}

// Sort the array
mergeSort(arr, 0, arr.length - 1);
%>
## Merge Sort Results

**Array Size**: <%= size %>

**Original Array**: 
<%= originalArray.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Total Comparisons: <%= comparisons %>
- Total Merge Operations: <%= merges %>
- Maximum Recursion Depth: <%= maxDepth %>
- Theoretical Depth: <%= Math.ceil(Math.log2(size)) %>

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>
```

