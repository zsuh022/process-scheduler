# Scheduler

## Algorithm
We used the A* algorithm to try and create the most optimal schedule for any given graph. Different heuristics were experimented with to improve the algorithm's speed and ultimately chose the f value using a combination of including maximum data ready time, maximum bottom level length, and idle time to guide our state exploration, as well as load-balancing to optimise our search. This gave us a significant speed-up while maintaining solution accuracy. We are also optimising the search by using two pruning strategies, processor normalisation and node equivalence.

Additionally, we parallelised the algorithm to further improve the exceution speed. By utilizing multithreading, we can distribute the workload across multiple cores, allowing for parallel exploration of the search space. To evaluate the effectiveness of our parallelized search, we developed a random Directed Acyclic Graph (DAG) generator that produces a range of graphs, from sparse to dense.
