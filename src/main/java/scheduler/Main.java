package scheduler;

import java.io.IOException;

import scheduler.models.GraphModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.parsers.InputOutputParser;
import scheduler.schedulers.DFSScheduler;

/**
 * The Main Class contains the necessary driver code for ensuring our program runs smoothly, and that a valid and
 * optimal schedule is generated. JavaFX code will also run if the user specifies that they want the schedule to be
 * visualised.
 */
public class Main {
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
            GraphModel graph = new GraphModel(arguments.getInputDOTFilePath());

            DFSScheduler scheduler = new DFSScheduler(graph, arguments.getProcessors());
            StateModel bestState = scheduler.getSchedule();

            graph.setNodesAndEdgesForState(bestState);

            InputOutputParser.outputDOTFile(graph, arguments.getOutputDOTFilePath());

            System.out.println("Scheduled successfully! Output written to " + arguments.getOutputDOTFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Visualiser.run(arguments);
    }
}
