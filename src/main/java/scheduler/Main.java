package scheduler;

import java.io.IOException;

import scheduler.models.GraphModel;
import scheduler.models.MetricsModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.schedulers.Scheduler;
import scheduler.schedulers.parallel.ParallelSchedulerForkJoin;
import scheduler.schedulers.sequential.AStarScheduler;
import visualiser.Visualiser;

/**
 * The Main Class contains the necessary driver code for ensuring our program runs smoothly, and that a valid and
 * optimal schedule is generated. JavaFX code will also run if the user specifies that they want the schedule to be
 * visualised.
 */
public class Main {
    private static Scheduler scheduler;

    /**
     * The runScheduler method is responsible for running the scheduler and outputting the results to the user.
     * Different metrics are displayed such as the make-span, the number of processors used, the number of cores used,
     * the elapsed time, and the memory used.
     *
     * @param arguments the input arguments
     * @throws IOException if I/O file does not exist
     */
    private static void runScheduler(Arguments arguments) throws IOException {
        long startTimeTest = System.currentTimeMillis();

        scheduler.schedule();

        long endTimeTest = System.currentTimeMillis();

        float elapsedTimeTest = (endTimeTest - startTimeTest) / 1000.0f;

        MetricsModel metricsTest = scheduler.getMetrics();

        metricsTest.setElapsedTime(elapsedTimeTest);
        metricsTest.display();

        scheduler.saveBestState(arguments);
    }

    /**
     * The initialiseScheduler method is responsible for initialising the scheduler based on the user's input. The
     * scheduler is either a sequential scheduler or a parallel scheduler. The number of cores is also set based on the
     * user's input which is then parsed through by the CLI Parser.
     *
     * @param arguments the input arguments
     * @throws IOException when the I/O file does not exist
     */
    private static void initialiseScheduler(Arguments arguments) throws IOException {
        GraphModel graph = new GraphModel(arguments.getInputDOTFilePath());

        if (arguments.getCores() == 1) {
            scheduler = new AStarScheduler(graph, arguments.getProcessors());
        } else {
            scheduler = new ParallelSchedulerForkJoin(graph, arguments.getProcessors(), arguments.getCores());
        }
    }

    /**
     * The main method for executing the main driver code.
     *
     * @param CLIArguments CLIArguments the arguments passed by the user
     */
    public static void main(String[] CLIArguments) {
        Arguments arguments;

        try {
            arguments = CLIParser.parseCLIArguments(CLIArguments);
        } catch (Exception exception) {
            CLIParser.displayUsage(exception.getMessage());
            return;
        }

        try {
            initialiseScheduler(arguments);
        } catch (IOException exception) {
            exception.printStackTrace();
            return;
        }

        if (arguments.isVisualiseSearch()) {
            Visualiser.run(arguments, scheduler);
            return;
        }

        try {
            runScheduler(arguments);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
