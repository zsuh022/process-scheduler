package scheduler;

import java.io.IOException;

import org.apache.commons.cli.ParseException;

import scheduler.models.GraphModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.parsers.InputOutputParser;
import scheduler.visualiser.Visualiser;

public class Main {
    public static void main(String[] CLIArguments) throws IOException {
        Arguments arguments;

        try {
            arguments = CLIParser.parseCLIArguments(CLIArguments);
        } catch (ParseException parseException) {
            CLIParser.displayUsage(parseException.getMessage());
            return;
        }

        GraphModel graph = new GraphModel("src/main/resources/dotfiles/input/Nodes_10_Random.dot");

        Visualiser.run();
    }
}
