# Process Scheduler

This is Team 13's repository.

## Running the project

To run the project, simply follow the instructions as listed below:

1. Clone the repository
2. Run the following command:  `java -jar scheduler.jar INPUT.dot P [OPTION]`

- `INPUT.dot`: A task graph with integer weights in DOT format.
- `P`: Number of processors to schedule the INPUT graph on.

Optional Parameters:
- `-p N`: Use N cores for execution in parallel (default is sequential).
- `-v`: Visualize the search.
- `-o` OUTPUT: The output file is named OUTPUT (default is INPUT-output.dot).

## Algorithm progress
The `SequentialScheduler.java` file contains our A* algorithm which is in progress. Instead, `DFSScheduler.java` contains an algorithm that produces a valid and optimal graph.

## Team

* Gallon Zhou - gzho038@aucklanduni.ac.nz - DuckyShine004
* Benjamin Qian - bqia247@aucklanduni.ac.nz - bqia247
* Nicolas Lianto - nlia656@aucklanduni.ac.nz - nlia656
* Stephen Fang - cfan816@aucklanduni.ac.nz - shinramenisbae
* Zion Suh - zsuh022@aucklanduni.ac.nz - zsuh022
