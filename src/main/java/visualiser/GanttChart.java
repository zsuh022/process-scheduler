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



public class GanttChart<X,Y> extends XYChart<X,Y> {

    private double blockHeight = 30;
    private List<Text> taskTexts = new ArrayList<>();

    public static class ExtraData {
        private long length;
        private String styleClass;
        private String taskName;


        public ExtraData(long length, String styleClass, String taskName) {
            this.length = length;
            this.styleClass = styleClass;
            this.taskName = taskName;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public String getStyleClass() {
            return styleClass;
        }

        public void setStyleClass(String styleClass) {
            this.styleClass = styleClass;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }
    }

    public GanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
        setData(FXCollections.observableArrayList());

        this.setAnimated(false);
        
    }

    public GanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X,Y>> data) {
        super(xAxis, yAxis);
        setData(data);
    }

    private static double getLength(Object obj) {
        return ((ExtraData) obj).getLength();
    }

    private static String getStyleClass(Object obj) {
        return ((ExtraData) obj).getStyleClass();
    }

    private static String getTaskName(Object obj) {
        return ((ExtraData) obj).getTaskName();
    }

    public double getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(double blockHeight) {
        this.blockHeight = blockHeight;
    }

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

    private double heightScaling() {
        if (getYAxis() instanceof NumberAxis) {
            return Math.abs(((NumberAxis)getYAxis()).getScale());
        } else if(getYAxis() instanceof CategoryAxis){
            return (double)1/((CategoryAxis) getYAxis()).getCategories().size();
        } else {
            return 1;
        }
    }

    private double widthScaling() {
        if (getXAxis() instanceof NumberAxis) {
            return Math.abs(((NumberAxis)getXAxis()).getScale());
        } else {
            return 1;
        }
    }

    @Override
    protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
        Node block = item.getNode();
        block.getStyleClass().add(getStyleClass(item.getExtraValue()));
        getPlotChildren().add(block);
    }

    @Override
    protected void dataItemRemoved(Data<X, Y> item, Series<X, Y> series) {
        Node block = item.getNode();
        getPlotChildren().remove(block);
    }

    @Override
    protected void dataItemChanged(Data<X, Y> item) {
        
    }

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

    @Override
    protected void seriesRemoved(Series<X, Y> series) {
        for (Data<X, Y> d : series.getData()) {
            Node block = d.getNode();
            getPlotChildren().remove(block);
        }
    }


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
