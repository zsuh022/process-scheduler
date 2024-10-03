package scheduler;

import java.io.IOException;

import scheduler.models.GraphModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.schedulers.Scheduler;
import scheduler.schedulers.sequential.AStarScheduler;
import scheduler.visualiser.Visualiser;

/**
 * The Main Class contains the necessary driver code for ensuring our program runs smoothly, and that a valid and
 * optimal schedule is generated. JavaFX code will also run if the user specifies that they want the schedule to be
 * visualised.
 */
public class Main {
    private static void runScheduler(Arguments arguments) throws IOException {
        GraphModel graph = new GraphModel(arguments.getInputDOTFilePath());

        Scheduler scheduler = new AStarScheduler(graph, arguments.getProcessors());

        long startTime = System.currentTimeMillis();
        scheduler.schedule();
        long endTime = System.currentTimeMillis();

        double durationInSeconds = (endTime - startTime) / 1000.0;

        System.out.println("Elapsed time: " + durationInSeconds + " seconds");

        StateModel bestState = scheduler.getBestState();
//            graph.setNodesAndEdgesForState(bestState);
//
//            InputOutputParser.outputDOTFile(graph, arguments.getOutputDOTFilePath());

        System.out.println("Scheduled successfully! Output written to " + arguments.getOutputDOTFilePath());
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
            runScheduler(arguments);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (arguments.isVisualiseSearch()) {
            Visualiser.run(arguments);
        }
    }
}
