package scheduler;

import java.io.IOException;
import java.util.Arrays;

import scheduler.models.GraphModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.parsers.InputOutputParser;
import scheduler.schedulers.RoundRobinScheduler;
import scheduler.schedulers.Scheduler;
import scheduler.schedulers.SequentialScheduler;
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
            GraphModel graph = new GraphModel(arguments.getInputDOTFilePath());

            // RoundRobinScheduler scheduler = new RoundRobinScheduler(grpahModel,
            // arguments.getProcessors());
            // scheduler.schedule();

            // InputOutputParser.outputDOTFile(grpahModel,
            // arguments.getOutputDOTFilePath());

            Scheduler scheduler = new SequentialScheduler(graph, arguments.getProcessors());
            StateModel state = scheduler.getAStarSchedule();
            System.out.println(Arrays.stream(state.getFinishTimes()).max().getAsInt());

            System.out.println("Scheduled successfully! Output written to " + arguments.getOutputDOTFilePath());
        } catch (IOException e) {
            System.err.println("ERROR");
            e.printStackTrace();
        }
        Visualiser.run(arguments);
    }
}
