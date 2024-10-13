# Visualiser

## Plan

Our team decided to use JavaFX to create the visualisation application, due to our previous experience with the framework from other courses.

We had always planned to have our user interface contain 2 screens, one for informative metrics on the process, and another for a Gantt chart visualisation of the process.

Key metrics we knew we wanted to show were:
 - Schedule time
 - Elapsed time
 - RAM and CPU graphs
 - Processors used

Eventually, we decided to add content for the following, to enhance the user understanding and experience:
 - Input graph nodes and edges
 - Input graph visualiser
 - Cores

## Implementation
Implementation for the Gantt chart was our main priority.

Originally, we created the chart using a JavaFX canvas. This method retrieved information from the output dot file, which was then used to manually draw the Gantt chart onto the canvas. This came with several problems:
 - Firstly, the canvas was limited in size, meaning that for larger graphs with larger process times, the canvas needed a large amount of horizontal room which could not be accomodated for, even with scrolling. This caused errors.
 - Secondly, the visualiser would not be updated live as it took information once the schedule was completed.

To fix this, implementation with JavaFX's XY chart proceeded. This fixed several of our problems as:
 - We used to live graph information, rather than information from the output file to create the Gantt chart so that the visualisation could be performed live.
 - The chart could dynamically shrink to fit larger graphs.