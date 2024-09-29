# Process Scheduler

This is Team 13's repository.

## Running the project
> [!NOTE]
> When running the `scheduler.jar` file, ensure that you specify both the input file location and the number of processors.

### Steps to Run the Project
1. Clone the repository:
    ```sh
    git clone https://github.com/SoftEng306-2024/project-2-13-gallon-and-friends
    ```
2. Use the following command to run the project:
    ```sh
    java -jar scheduler.jar INPUT.dot P [OPTION]
   ```
- `INPUT.dot`: A task graph in DOT format.
- `P`: The number of processors to schedule the input graph on.

Optional Parameters:
- `-p N`: Use N cores for execution in parallel (default is sequential).
- `-v`: Visualise the search.
- `-o` OUTPUT: The output file is named OUTPUT (default is INPUT-output.dot).

## Algorithm progress
- The A* algorithm is currently being developed in the `SequentialScheduler.java` file.
- Meanwhile, the `DFSScheduler.java` file contains a working algorithm that produces valid and optimal task schedules.

## Team

* Gallon Zhou - gzho038@aucklanduni.ac.nz - DuckyShine004
* Benjamin Qian - bqia247@aucklanduni.ac.nz - bqia247
* Nicolas Lianto - nlia656@aucklanduni.ac.nz - nlia656
* Stephen Fang - cfan816@aucklanduni.ac.nz - shinramenisbae
* Zion Suh - zsuh022@aucklanduni.ac.nz - zsuh022
