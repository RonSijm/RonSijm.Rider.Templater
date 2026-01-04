# Bucket Sort

## Description
Bucket Sort distributes elements into several buckets, sorts each bucket individually (using another sorting algorithm), and then concatenates the sorted buckets. It works well when input is uniformly distributed over a range.

## When to Use
- **Uniformly distributed data** - Works best when data is evenly spread
- **Floating point numbers** - Excellent for sorting decimals in a known range
- **Parallel processing** - Buckets can be sorted independently
- **External sorting** - Good for data that doesn't fit in memory

## Time Complexity
- **Best Case**: O(n + k) where k is the number of buckets
- **Average Case**: O(n + k)
- **Worst Case**: O(n²) - when all elements go to one bucket
- **Space Complexity**: O(n + k)

## Templater Script

```
<%*
// Configuration
let size = 50;
let numBuckets = 10;

// Generate random array (values between 0 and 1)
let arr = [];
for (let i = 0; i < size; i++) {
    arr[i] = Math.random();
}

let originalArray = [...arr];
let bucketsUsed = 0;
let maxBucketSize = 0;

// Insertion sort for individual buckets
function insertionSort(bucket) {
    for (let i = 1; i < bucket.length; i++) {
        let key = bucket[i];
        let j = i - 1;
        while (j >= 0 && bucket[j] > key) {
            bucket[j + 1] = bucket[j];
            j--;
        }
        bucket[j + 1] = key;
    }
}

// Bucket Sort function
function bucketSort(arr, numBuckets) {
    if (arr.length === 0) {
        return;
    }
    
    // Create empty buckets
    let buckets = [];
    for (let i = 0; i < numBuckets; i++) {
        buckets[i] = [];
    }
    
    // Distribute elements into buckets
    for (let i = 0; i < arr.length; i++) {
        let bucketIndex = Math.floor(arr[i] * numBuckets);
        if (bucketIndex === numBuckets) {
            bucketIndex = numBuckets - 1;
        }
        buckets[bucketIndex].push(arr[i]);
    }
    
    // Sort individual buckets and track statistics
    for (let i = 0; i < numBuckets; i++) {
        if (buckets[i].length > 0) {
            bucketsUsed++;
            if (buckets[i].length > maxBucketSize) {
                maxBucketSize = buckets[i].length;
            }
            insertionSort(buckets[i]);
        }
    }
    
    // Concatenate all buckets
    let index = 0;
    for (let i = 0; i < numBuckets; i++) {
        for (let j = 0; j < buckets[i].length; j++) {
            arr[index++] = buckets[i][j];
        }
    }
}

// Sort the array
bucketSort(arr, numBuckets);
%>
## Bucket Sort Results

**Array Size**: <%= size %>
**Number of Buckets**: <%= numBuckets %>
**Buckets Used**: <%= bucketsUsed %>
**Largest Bucket Size**: <%= maxBucketSize %>

**Original Array**: 
<%= originalArray.slice(0, 20).map(v => v.toFixed(3)).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Sorted Array**: 
<%= arr.slice(0, 20).map(v => v.toFixed(3)).join(', ') %><% if (size > 20) { %> ... (showing first 20)<% } %>

**Statistics**:
- Average elements per bucket: <%= (size / numBuckets).toFixed(2) %>
- Actual average (used buckets): <%= (size / bucketsUsed).toFixed(2) %>
- Load balance: <%= ((maxBucketSize / (size / bucketsUsed)) * 100).toFixed(1) %>% of average

**Verification**: <%= arr.every((val, i, a) => i === 0 || a[i - 1] <= val) ? '✓ Correctly sorted' : '✗ Sort failed' %>

**Note**: Bucket Sort efficiency depends on uniform distribution. 
Better distribution = more balanced buckets = faster sorting.
```

