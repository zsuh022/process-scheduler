package scheduler;

import java.io.IOException;

import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
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

        Visualiser.run(arguments);
    }
}
