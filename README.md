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

## Visualiser
When the visualise option is chosen, a JavaFX based application launches displaying metrics on the schedule, and provides a Gantt chart of said schedule that updates live.

Below is the Schedule Visualiser.
<img width="959" alt="image" src="https://github.com/user-attachments/assets/9cdfcbb9-a8cb-4219-a756-e67903c6d410">

Below is the Input Graph Visualiser. This contains information on the input graph as well as an interactable graph model.
<img width="959" alt="image" src="https://github.com/user-attachments/assets/59f36662-8142-41dc-b2b2-d99d3ae522ac">


## Algorithm progress
- The A* algorithm has been developed in the `AStarScheduler.java` file, and is our current algorithm of choice.
- Meanwhile, the `DFSScheduler.java` file contains a working algorithm that produces valid and optimal task schedules. This is not in use as it is considerably slower.

## Team

* Gallon Zhou - gzho038@aucklanduni.ac.nz - DuckyShine004
* Benjamin Qian - bqia247@aucklanduni.ac.nz - bqia247
* Nicolas Lianto - nlia656@aucklanduni.ac.nz - nlia656
* Stephen Fang - cfan816@aucklanduni.ac.nz - shinramenisbae
* Zion Suh - zsuh022@aucklanduni.ac.nz - zsuh022
