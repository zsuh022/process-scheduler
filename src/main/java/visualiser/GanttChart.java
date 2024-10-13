package visualiser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;


/**
 * GanttChart is a custom implementation of XYChart that is used to create gantt charts.
 * The tasks are represented by rectangles that have a start time and a length. 
 */
public class GanttChart<X,Y> extends XYChart<X,Y> {

    private double blockHeight = 30;
    private List<Text> taskTexts = new ArrayList<>();

    /**
     * A class to hold extra data for each task.
     * The data consists of length, style, and its name.
     */
    public static class ExtraData {
        private long length;
        private String styleClass;
        private String taskName;


        /**
         * Constructor for ExtraData
         * 
         * @param length Length of the task
         * @param styleClass the style class for the task block
         * @param taskName the name of the task
         */
        public ExtraData(long length, String styleClass, String taskName) {
            this.length = length;
            this.styleClass = styleClass;
            this.taskName = taskName;
        }

        /**
         * Gets the length of the task
         * 
         * @return the length of the task
         */
        public long getLength() {
            return length;
        }

        /**
         * Sets the length of the task
         * 
         * @param length the length of the task
         */
        public void setLength(long length) {
            this.length = length;
        }

        /**
         * Gets the style class of the task
         * 
         * @return the style class of the task
         */
        public String getStyleClass() {
            return styleClass;
        }

        /**
         * Sets the style class of the task
         * 
         * @param styleClass the style class of the task
         */
        public void setStyleClass(String styleClass) {
            this.styleClass = styleClass;
        }

        /**
         * Gets the name of the task
         * 
         * @return the name of the task
         */
        public String getTaskName() {
            return taskName;
        }

        /**
         * Sets the name of the task
         * 
         * @param taskName the name of the task
         */
        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }
    }

    /**
     * Constructor for GanttChart
     * 
     * @param xAxis the x-axis
     * @param yAxis the y-axis
     */
    public GanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
        setData(FXCollections.observableArrayList());

        this.setAnimated(false);
        
    }

    /**
     * Constructor for GanttChart
     * 
     * @param xAxis the x-axis
     * @param yAxis the y-axis
     * @param data the data for the chart
     */
    public GanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X,Y>> data) {
        super(xAxis, yAxis);
        setData(data);
    }

    /**
     * Gets the length of the task
     * 
     * @param obj the object to get the length from
     * @return the length of the task
     */
    private static double getLength(Object obj) {
        return ((ExtraData) obj).getLength();
    }

    /**
     * Gets the style class of the task
     * 
     * @param obj the object to get the style class from
     * @return the style class of the task
     */
    private static String getStyleClass(Object obj) {
        return ((ExtraData) obj).getStyleClass();
    }

    /**
     * Gets the name of the task
     * 
     * @param obj the object to get the name from
     * @return the name of the task
     */
    private static String getTaskName(Object obj) {
        return ((ExtraData) obj).getTaskName();
    }

    /**
     * Gets the height of the task block
     * 
     * @return the height of the task block
     */
    public double getBlockHeight() {
        return blockHeight;
    }

    /**
     * Sets the height of the task block
     * 
     * @param blockHeight the height of the task block
     */
    public void setBlockHeight(double blockHeight) {
        this.blockHeight = blockHeight;
    }

    /**
     * Lays out the task blocks and their labels on the graph. 
     */
    @Override
    protected void layoutPlotChildren() {
        this.getPlotChildren().removeAll(taskTexts);
        taskTexts.clear();
    // Iterate through all the data in the chart
        for (int i = 0; i < getData().size(); i++) {
            Series<X, Y> series = getData().get(i);
            Iterator<Data<X, Y>> iter = getDisplayedDataIterator(series);

            // Iterate through all the tasks in the series
            while (iter.hasNext()) {
                Data<X, Y> item = iter.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(item.getYValue());

                // If x and y are NaN, skip this iteration
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }

                // Get or create the node for the task
                Rectangle rect = (Rectangle) item.getNode();

                if (rect == null) {
                    rect = new Rectangle();
                    item.setNode(rect);  // Set the newly created Rectangle as the node
                    getPlotChildren().add(rect);  // Add the Rectangle to the plot
                }

                // Adds the styling to the task.
                rect.getStyleClass().add(getStyleClass(item.getExtraValue()));

                // Set the size of the rectangle (task block)
                double length = getLength(item.getExtraValue()) * widthScaling();
                double height = Math.min(170 * heightScaling(), getBlockHeight());

                // Center block vertically
                y -= height / 2;

                // Set the position and size of the rectangle
                rect.setX(x);
                rect.setY(y);
                rect.setWidth(length);
                rect.setHeight(height);

                // Create a text object for the task name
                String taskName = getTaskName(item.getExtraValue());
                Text text = new Text(taskName);

                // Calculate position of the text 
                double textX = x + (length-10) / 2;

                // Set the position of the text
                text.setX(textX);
                text.setY(y + (height+10) / 2);

                // Add the text to the plot
                taskTexts.add(text);
                getPlotChildren().add(text);
            }
        }
    }

    /**
     * Scales the height of the task block based on the y-axis
     * 
     * @return the scaling factor for the height
     */
    private double heightScaling() {
        if (getYAxis() instanceof NumberAxis) {
            return Math.abs(((NumberAxis)getYAxis()).getScale());
        } else if(getYAxis() instanceof CategoryAxis){
            return (double)1/((CategoryAxis) getYAxis()).getCategories().size();
        } else {
            return 1;
        }
    }

    /**
     * Scales the width of the task block based on the x-axis
     * 
     * @return the scaling factor for the width
     */
    private double widthScaling() {
        if (getXAxis() instanceof NumberAxis) {
            return Math.abs(((NumberAxis)getXAxis()).getScale());
        } else {
            return 1;
        }
    }

    /**
     * Adds a task to the chart
     * 
     * @param series the series to add the task to
     * @param itemIndex the index of the task
     * @param item the task to add
     */
    @Override
    protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
        Node block = item.getNode();
        block.getStyleClass().add(getStyleClass(item.getExtraValue()));
        getPlotChildren().add(block);
    }

    /**
     * Removes a task from the chart
     * 
     * @param item the task to remove
     * @param series the series to remove the task from
     */
    @Override
    protected void dataItemRemoved(Data<X, Y> item, Series<X, Y> series) {
        Node block = item.getNode();
        getPlotChildren().remove(block);
    }

    /**
     * Updates the data of a task.
     * This method is not used in the implementation of a gantt chart
     * 
     * @param item the task to update
     */
    @Override
    protected void dataItemChanged(Data<X, Y> item) {}

    /**
     * Adds a series of tasks to the chart
     * 
     * @param series the series of task to add
     * @param seriesIndex the index of the series
     */
    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        for (int j=0; j<series.getData().size(); j++) {
            Data<X, Y> item = series.getData().get(j);
            Node block = item.getNode();

            if (block == null) {
                block = new Rectangle();
                item.setNode(block);
            }
            block.getStyleClass().add(getStyleClass(item.getExtraValue()));

            getPlotChildren().add(block);
        }
    }

    /**
     * Removes a series of tasks from the chart
     * 
     * @param series the series of tasks to remove
     */
    @Override
    protected void seriesRemoved(Series<X, Y> series) {
        for (Data<X, Y> d : series.getData()) {
            Node block = d.getNode();
            getPlotChildren().remove(block);
        }
    }

    /**
     * Updates the range of the x-axis and y-axis
     */
    @Override
    protected void updateAxisRange() {
        final Axis<X> xAxis = getXAxis();
        final Axis<Y> yAxis = getYAxis();

        List<X> xData = null;
        List<Y> yData = null;

        if (xAxis.isAutoRanging()) {
            xData = new ArrayList<>();
        }
        if (yAxis.isAutoRanging()) {
            yData = new ArrayList<>();
        }

        if (xData != null || yData != null) {
            for (Series<X, Y> series : getData()) {
                for (Data<X, Y> data : series.getData()) {
                    if (xData != null) {
                        // Add start and end positions of each task (x axis)
                        xData.add(data.getXValue());
                        xData.add(xAxis.toRealValue(xAxis.toNumericValue(data.getXValue()) + getLength(data.getExtraValue())));
                    }
                    if (yData != null) {
                        // Add y-values (task categories)
                        yData.add(data.getYValue());
                    }
                }
            }
            if (xData != null) {
                xAxis.invalidateRange(xData);  // Update the range of the X axis
            }
            if (yData != null) {
                yAxis.invalidateRange(yData);  // Update the range of the Y axis
            }
        }
    }

    public void clear() {
        this.getData().clear();
    }
}
