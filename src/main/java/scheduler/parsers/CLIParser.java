package scheduler.parsers;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static scheduler.constants.Constants.NUMBER_OF_REQUIRED_ARGUMENTS;

/**
 * The CLI parser provides an interface for parsing user CLI inputs. It ensures the correctness of their input, as any
 * incorrect arguments passed to our program will be detected, and a help message will be displayed to the user.
 */
public class CLIParser {
    private static final Options options = new Options();

    /**
     * Parses the user arguments.
     *
     * @param CLIArguments the cli arguments
     * @return the parsed arguments
     * @throws ParseException the exception encountered during parsing
     */
    public static Arguments parseCLIArguments(String[] CLIArguments) throws ParseException {
        options.addOption("p", true, "Use N cores for execution in parallel (default is sequential)");
        options.addOption("v", false, "Visualise the search");
        options.addOption("o", true, "Output file (default is INPUT-output.dot)");

        return parseOptions(CLIArguments);
    }

    /**
     * Parses the user's inputs. There are two steps, parsing the required inputs and parsing the optional inputs.
     *
     * @param CLIArguments CLIArguments the user's arguments
     * @return {@link Arguments} the arguments instance
     * @throws ParseException org.apache.commons.cli. parse exception
     * @see Arguments
     */
    private static Arguments parseOptions(String[] CLIArguments) throws ParseException {
        Arguments arguments = new Arguments();
        CommandLineParser parser = new DefaultParser();

        CommandLine commandPrompt = parser.parse(CLIParser.options, CLIArguments);
        String[] remainingArguments = commandPrompt.getArgs();

        if (remainingArguments.length < NUMBER_OF_REQUIRED_ARGUMENTS) {
            throw new ParseException("Missing input file and number of cores");
        }

        parseRequiredArguments(arguments, remainingArguments);
        parseOptionalArguments(arguments, commandPrompt);

        return arguments;
    }

    /**
     * Parses the required arguments. Required arguments include the input file location and the number of processors
     * to be used in the scheduler.
     *
     * @param arguments arguments the arguments passed by the user
     * @param remainingArguments remainingArguments remaining arguments
     */
    private static void parseRequiredArguments(Arguments arguments, String[] remainingArguments) {
        arguments.setInputDOTFilePath(remainingArguments[0]);
        arguments.setProcessors(Byte.parseByte(remainingArguments[1]));
    }

    /**
     * Parses optional arguments. Optional arguments include the number of cores, if the schedule should be visualised
     * or not, etc.,
     *
     * @param arguments arguments the arguments passed from the user
     * @param commandPrompt commandPrompt the command prompt instance
     */
    private static void parseOptionalArguments(Arguments arguments, CommandLine commandPrompt) {
        if (commandPrompt.hasOption("p")) {
            arguments.setCores(Byte.parseByte(commandPrompt.getOptionValue("p")));
        }

        if (commandPrompt.hasOption("v")) {
            arguments.setIsVisualiseSearch(true);
        }

        if (commandPrompt.hasOption("o")) {
            arguments.setOutputDOTFilePath(commandPrompt.getOptionValue("o"));
        } else {
            setDefaultOutputDOTFilePath(arguments);
        }
    }

    /**
     * Set the default output DOT file path.
     *
     * @param arguments arguments the arguments from the user
     */
    private static void setDefaultOutputDOTFilePath(Arguments arguments) {
        String inputDOTFilePath = arguments.getInputDOTFilePath();

        File inputFile = new File(inputDOTFilePath);


        String outputFileName = inputFile.getName().replace(".dot", "-output.dot");
        String outputDOTFilePath = inputFile.getParent() + File.separator + outputFileName;

        if (inputFile.getParent() == null) {
            outputDOTFilePath = outputFileName;
        }

        arguments.setOutputDOTFilePath(outputDOTFilePath);
    }

    /**
     * Displays the correct usage for our program.
     *
     * @param errorMessage the error message from parsing the user's input
     */
    public static void displayUsage(String errorMessage) {
        HelpFormatter formatter = new HelpFormatter();

        displayRequiredArguments(errorMessage);

        formatter.printHelp("java -jar scheduler.jar INPUT.dot P [OPTION]", options);
    }

    /**
     * If the user does not enter the required arguments, usage information will be displayed.
     *
     * @param errorMessage the error message from parsing the user's input
     */
    private static void displayRequiredArguments(String errorMessage) {
        System.out.println(errorMessage);
        System.out.println("\nRequired arguments:");
        System.out.printf(" %-10s %s %s", "INPUT.dot", "<String>", "The path to the input dot file");
        System.out.printf(" %-10s %s %s%n", "P", "<int>", "The number of processors to use");
    }
}
