package scheduler;

import java.io.IOException;

import scheduler.models.GraphModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.parsers.InputOutputParser;
import scheduler.schedulers.DFSScheduler;

public class Main {
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
