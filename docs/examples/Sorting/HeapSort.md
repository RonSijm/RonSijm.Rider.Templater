# Heap Sort

## Description
Heap Sort uses a binary heap data structure to sort elements. It first builds a max heap from the array, then repeatedly extracts the maximum element and rebuilds the heap. It's an in-place algorithm with guaranteed O(n log n) performance.

## When to Use
- **Guaranteed performance** - Always O(n log n), no worst case like Quick Sort
- **Memory constraints** - Sorts in-place with O(1) extra space
- **Priority queue** - Natural fit when you need heap operations
- **Avoiding recursion** - No recursion overhead unlike Quick/Merge Sort

## Time Complexity
- **Best Case**: O(n log n)
- **Average Case**: O(n log n)
- **Worst Case**: O(n log n)
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
let comparisons = 0;
let swaps = 0;
let heapifyCount = 0;

// Heapify a subtree rooted at index i
function heapify(arr, n, i) {
    heapifyCount++;
    let largest = i;
    let left = 2 * i + 1;
    let right = 2 * i + 2;
    
    if (left < n) {
        comparisons++;
        if (arr[left] > arr[largest]) {
            largest = left;
        }
    }
    
    if (right < n) {
        comparisons++;
        if (arr[right] > arr[largest]) {
            largest = right;
        }
    }
    
    if (largest !== i) {
        let temp = arr[i];
        arr[i] = arr[largest];
        arr[largest] = temp;
        swaps++;
        
        heapify(arr, n, largest);
    }
}

// Heap Sort function
function heapSort(arr) {
    let n = arr.length;
    
    // Build max heap
    for (let i = Math.floor(n / 2) - 1; i >= 0; i--) {
        heapify(arr, n, i);
    }
    
    // Extract elements from heap one by one
    for (let i = n - 1; i > 0; i--) {
        // Move current root to end
        let temp = arr[0];
        arr[0] = arr[i];
        arr[i] = temp;
        swaps++;
        
        // Heapify the reduced heap
        heapify(arr, i, 0);
    }
}

// Sort the array
heapSort(arr);
%>
## Heap Sort Results

**Array Size**: <%= size %>

**Original Array**: 
<%= originalArray.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Total Comparisons: <%= comparisons %>
- Total Swaps: <%= swaps %>
- Heapify Calls: <%= heapifyCount %>
- Theoretical Comparisons: ~<%= Math.floor(2 * size * Math.log2(size)) %>

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>
```

