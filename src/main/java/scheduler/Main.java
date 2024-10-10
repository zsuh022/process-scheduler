package scheduler;

import java.io.IOException;

import scheduler.generator.GraphGenerator;
import scheduler.models.GraphModel;
import scheduler.models.MetricsModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.parsers.InputOutputParser;
import scheduler.schedulers.Scheduler;
import scheduler.schedulers.parallel.ParallelSchedulerDynamic;
import scheduler.schedulers.parallel.ParallelSchedulerStatic;
import scheduler.schedulers.sequential.AStarScheduler;
import visualiser.Visualiser;

import static scheduler.constants.Constants.RANDOM_OUTPUT_DOT_FILE_PATH;

/**
 * The Main Class contains the necessary driver code for ensuring our program runs smoothly, and that a valid and
 * optimal schedule is generated. JavaFX code will also run if the user specifies that they want the schedule to be
 * visualised.
 */
public class Main {
    private static Scheduler scheduler;
    private static void runScheduler(Arguments arguments) throws IOException {
        GraphModel graph = new GraphModel(arguments.getInputDOTFilePath());
//        GraphModel graph = GraphGenerator.getRandomGraph();
        String filename = "Random_Graph.dot";
        InputOutputParser.outputDOTFile(graph, RANDOM_OUTPUT_DOT_FILE_PATH.concat(filename));

        scheduler = new AStarScheduler(graph, arguments.getProcessors());
        long startTimeTest = System.currentTimeMillis();
        scheduler.schedule();
        long endTimeTest = System.currentTimeMillis();
        float elapsedTimeTest = (endTimeTest - startTimeTest) / 1000.0f;

        MetricsModel metricsTest = scheduler.getMetrics();
        metricsTest.setElapsedTime(elapsedTimeTest);
        metricsTest.display();

        GraphGenerator.setNumberOfProcessors(arguments.getProcessors());
        GraphGenerator.displayGraphInformation();

        for (byte i = 1; i <= 8; i++) {
            arguments.setCores(i);
            Scheduler scheduler = new ParallelSchedulerStatic(graph, arguments.getProcessors(), arguments.getCores());

            MetricsModel metrics = scheduler.getMetrics();
            // track memory and cpu usage every x ms
//            metrics.startPeriodicTracking(500);

            long startTime = System.currentTimeMillis();
//            Runtime runtime = Runtime.getRuntime();
//            runtime.gc();
//            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

            scheduler.schedule();

            long endTime = System.currentTimeMillis();
            float elapsedTime = (endTime - startTime) / 1000.0f;
//            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
//            double memoryUsed = memoryAfter - memoryBefore;
//
//            metrics.stopPeriodicTracking();

            metrics.setElapsedTime(elapsedTime);
//            metrics.setMemoryUsed(memoryUsed);

            metrics.display();
        }

        StateModel bestState = scheduler.getMetrics().getBestState();
        graph.setNodesAndEdgesForState(bestState);
        InputOutputParser.outputDOTFile(graph, arguments.getOutputDOTFilePath());
        arguments.displayOutputDOTFilePath();
    }

    /**
     * The main method for executing the main driver code.
     *
     * @param CLIArguments CLIArguments the arguments passed by the user
     */
    public static void main(String[] CLIArguments){
        Arguments arguments;

        try {
            arguments = CLIParser.parseCLIArguments(CLIArguments);
        } catch (Exception exception) {
            CLIParser.displayUsage(exception.getMessage());
            return;
        }

        try {
            runScheduler(arguments);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (arguments.isVisualiseSearch()) {
            Visualiser.run(arguments, scheduler);
        }
    }
}
