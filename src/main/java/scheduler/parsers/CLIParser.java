package scheduler.parsers;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CLIParser {
    private static Options options = new Options();

    public static Arguments parseCLIArguments(String[] CLIArguments) throws ParseException {
        options.addOption("p", true, "Use N cores for execution in parallel (default is sequential)");
        options.addOption("v", false, "Visualise the search");
        options.addOption("o", true, "Output file (default is INPUT-output.dot)");

        return parseOptions(options, CLIArguments);
    }

    private static Arguments parseOptions(Options options, String[] CLIArguments) throws ParseException {
        Arguments arguments = new Arguments();
        CommandLineParser parser = new DefaultParser();

        CommandLine commandPrompt = parser.parse(options, CLIArguments);
        String[] remainingArguments = commandPrompt.getArgs();

        if (remainingArguments.length < 2) {
            throw new ParseException("Missing input file and number of cores");
        }

        parseRequiredArguments(arguments, remainingArguments);
        parseOptionalArguments(arguments, commandPrompt, remainingArguments);

        return arguments;
    }

    private static void parseRequiredArguments(Arguments arguments, String[] remainingArguments) {
        arguments.setInputDOTFilePath(remainingArguments[0]);
        arguments.setProcessors(Integer.parseInt(remainingArguments[1]));
    }

    private static void parseOptionalArguments(Arguments arguments, CommandLine commandPrompt,
            String[] remainingArguments) {
        if (commandPrompt.hasOption("p")) {
            arguments.setCores(Integer.parseInt(commandPrompt.getOptionValue("p")));
        }

        if (commandPrompt.hasOption("v")) {
            arguments.setIsVisualiseSearch(true);
        }

        if (commandPrompt.hasOption("o")) {
            arguments.setOutputDOTFilePath(commandPrompt.getOptionValue("o"));
        } else {
            String inputDOTFilePath = arguments.getInputDOTFilePath();
            arguments.setOutputDOTFilePath(inputDOTFilePath.replace(".dot", "-output.dot"));
        }
    }

    public static void displayUsage(String errorMessage) {
        HelpFormatter formatter = new HelpFormatter();

        System.out.println(errorMessage.concat("\n"));
        displayRequiredArguments();

        formatter.printHelp("java -jar scheduler.jar INPUT.dot P [OPTION]", options);
    }

    private static void displayRequiredArguments() {
        System.out.println("Required arguments:");
        System.out.println(String.format(" %-10s %s", "INPUT.dot", "The path to the input dot file"));
        System.out.println(String.format(" %-10s %s", "P", "The number of processors to use\n"));
    }
}
