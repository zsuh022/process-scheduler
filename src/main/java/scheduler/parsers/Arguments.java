package scheduler.parsers;

/**
 * Arguments class for storing user arguments, such as the input file location, the output file location, the number of
 * sdf cores, etc.,
 */
public class Arguments {
    private byte cores;
    private byte processors;

    private String inputDOTFilePath;
    private String outputDOTFilePath;

    private boolean isVisualiseSearch;

    /**
     * Instantiates a new Arguments instance.
     */
    public Arguments() {
        this.cores = 1;
        this.isVisualiseSearch = false;
    }

    /**
     * Gets the number of cores used in the scheduler generator.
     *
     * @return the cores used in the scheduler
     */
    public byte getCores() {
        return this.cores;
    }

    /**
     * Sets the number of cores to be used in the scheduler generator.
     *
     * @param cores the number of cores to be used
     */
    public void setCores(byte cores) {
        this.cores = cores;
    }

    /**
     * Gets the number of processors used in the scheduler.
     *
     * @return the number of processors used in the scheduler
     */
    public byte getProcessors() {
        return this.processors;
    }

    /**
     * Sets number of processors to be used in the scheduler.
     *
     * @param processors the number of processors
     */
    public void setProcessors(byte processors) {
        this.processors = processors;
    }

    /**
     * Gets input dot file path.
     *
     * @return the input dot file path
     */
    public String getInputDOTFilePath() {
        return inputDOTFilePath;
    }

    /**
     * Sets input dot file path.
     *
     * @param inputDOTFilePath the input dot file path
     */
    public void setInputDOTFilePath(String inputDOTFilePath) {
        this.inputDOTFilePath = inputDOTFilePath;
    }

    /**
     * Gets output dot file path.
     *
     * @return the output dot file path
     */
    public String getOutputDOTFilePath() {
        return outputDOTFilePath;
    }

    /**
     * Sets output dot file path.
     *
     * @param outputDOTFilePath the output dot file path
     */
    public void setOutputDOTFilePath(String outputDOTFilePath) {
        this.outputDOTFilePath = outputDOTFilePath;
    }

    /**
     * Checks if the user is visualising the search or not.
     *
     * @return the boolean value of the visualise search
     */
    public boolean isVisualiseSearch() {
        return isVisualiseSearch;
    }

    /**
     * Sets the visualising search mode.
     *
     * @param isVisualiseSearch should we visualise the search
     */
    public void setIsVisualiseSearch(boolean isVisualiseSearch) {
        this.isVisualiseSearch = isVisualiseSearch;
    }

    /**
     * Displays the path to the output DOT file.
     */
    public void displayOutputDOTFilePath() {
        System.out.println("\nOutput written to " + getOutputDOTFilePath());
    }
}
