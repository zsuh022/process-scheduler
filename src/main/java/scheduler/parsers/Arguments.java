package scheduler.parsers;

public class Arguments {
    private int cores;
    private int processors;
    private String inputDOTFilePath;
    private String outputDOTFilePath;
    private boolean isVisualiseSearch;

    public Arguments() {
        this.cores = 1;
        this.isVisualiseSearch = false;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getProcessors() {
        return processors;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public String getInputDOTFilePath() {
        return inputDOTFilePath;
    }

    public void setInputDOTFilePath(String inputDOTFilePath) {
        this.inputDOTFilePath = inputDOTFilePath;
    }

    public String getOutputDOTFilePath() {
        return outputDOTFilePath;
    }

    public void setOutputDOTFilePath(String outputDOTFilePath) {
        this.outputDOTFilePath = outputDOTFilePath;
    }

    public boolean isVisualiseSearch() {
        return isVisualiseSearch;
    }

    public void setIsVisualiseSearch(boolean isVisualiseSearch) {
        this.isVisualiseSearch = isVisualiseSearch;
    }
}
