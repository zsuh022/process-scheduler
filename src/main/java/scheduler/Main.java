package scheduler;

import java.io.IOException;

import scheduler.models.GraphModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.parsers.InputOutputParser;
import scheduler.schedulers.RoundRobinScheduler;
import scheduler.visualiser.Visualiser;

public class Main {
    public static void main(String[] CLIArguments) throws IOException {
        Arguments arguments;

        try {
            arguments = CLIParser.parseCLIArguments(CLIArguments);
        } catch (Exception exception) {
            CLIParser.displayUsage(exception.getMessage());
            return;
        }

        try {
            GraphModel grpahModel = new GraphModel(arguments.getInputDOTFilePath());

            RoundRobinScheduler scheduler = new RoundRobinScheduler(grpahModel, arguments.getProcessors());
            scheduler.schedule();

            InputOutputParser.outputDOTFile(grpahModel, arguments.getOutputDOTFilePath());

            System.out.println("Scheduled successfully! Output written to " + arguments.getOutputDOTFilePath());
        } catch (IOException e) {
            System.err.println("ERROR");
            e.printStackTrace();
        }
        Visualiser.run(arguments);
    }
}
