# Scheduler

## Algorithm
We implemented the A* algorithm to generate the most optimal schedule for any given graph. Various heuristics were tested to enhance the algorithm's efficiency, ultimately selecting an f-value based on a combination of maximum data ready time, maximum bottom-level length, idle time, and load-balanced time as a lower bound to guide state exploration. These choices significantly improved the algorithm's speed while preserving the accuracy of the solution. 

To further optimise performance, we employed several pruning strategies, including node equivalence, processor normalisation, fixed task order pruning, and schedule equivalence, resulting in reduced memory usage.

We also parallelised the algorithm to boost execution speed. By leveraging multithreading, we distributed the workload across multiple cores, enabling parallel exploration of the search space. To assess the effectiveness of this parallelised approach, we developed a random Directed Acyclic Graph (DAG) generator capable of producing a wide range of graph types, from sparse to dense.

## Additional Things We Would Implement
- Optimisation of data structures to reduce overhead
- Refinements to existing pruning strategies for better efficiency
