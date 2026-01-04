# Radix Sort

## Description
Radix Sort processes integers digit by digit, starting from the least significant digit to the most significant digit. It uses a stable sorting algorithm (like counting sort) as a subroutine to sort digits at each position.

## When to Use
- **Fixed-length integers** - Excellent for sorting integers with a fixed number of digits
- **String sorting** - Can be adapted to sort strings of equal length
- **Linear time** - Achieves O(d * n) where d is the number of digits
- **Large datasets** - Efficient for large arrays of integers

## Time Complexity
- **Best Case**: O(d * n) where d is the number of digits
- **Average Case**: O(d * n)
- **Worst Case**: O(d * n)
- **Space Complexity**: O(n + k) where k is the range of digits (0-9)

## Templater Script

```
<%*
// Configuration
let size = 50;
let maxValue = 10000; // Range of values

// Generate random array
let arr = [];
for (let i = 0; i < size; i++) {
    arr[i] = Math.floor(Math.random() * maxValue);
}

let originalArray = [...arr];
let passes = 0;

// Counting sort for a specific digit position
function countingSortByDigit(arr, exp) {
    let n = arr.length;
    let output = [];
    let count = [];
    
    for (let i = 0; i < 10; i++) {
        count[i] = 0;
    }
    
    // Store count of occurrences
    for (let i = 0; i < n; i++) {
        let digit = Math.floor(arr[i] / exp) % 10;
        count[digit]++;
    }
    
    // Change count[i] to contain actual position
    for (let i = 1; i < 10; i++) {
        count[i] += count[i - 1];
    }
    
    // Build output array
    for (let i = n - 1; i >= 0; i--) {
        let digit = Math.floor(arr[i] / exp) % 10;
        output[count[digit] - 1] = arr[i];
        count[digit]--;
    }
    
    // Copy output to arr
    for (let i = 0; i < n; i++) {
        arr[i] = output[i];
    }
    
    passes++;
}

// Radix Sort function
function radixSort(arr) {
    // Find maximum number to know number of digits
    let max = arr[0];
    for (let i = 1; i < arr.length; i++) {
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    
    // Do counting sort for every digit
    // exp is 10^i where i is current digit number
    for (let exp = 1; Math.floor(max / exp) > 0; exp *= 10) {
        countingSortByDigit(arr, exp);
    }
}

// Sort the array
radixSort(arr);
%>
## Radix Sort Results

**Array Size**: <%= size %>
**Value Range**: 0 to <%= maxValue - 1 %>

**Original Array**: 
<%= originalArray.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Number of Passes (d): <%= passes %>
- Array Size (n): <%= size %>
- Time Complexity: O(<%= passes %> × <%= size %>) = O(<%= passes * size %>)
- Maximum Digits: <%= Math.floor(Math.log10(maxValue)) + 1 %>

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>

**Note**: Radix Sort processes <%= passes %> digit position(s) for this dataset.
```

