package schedulers.sequential;

import org.junit.jupiter.api.Test;
import scheduler.models.GraphModel;
import scheduler.models.MetricsModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;
import schedulers.BaseSchedulerIT;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static scheduler.constants.Constants.TEST_INPUT_DOT_FILE_PATH;
import static scheduler.constants.Constants.TEST_OUTPUT_DOT_FILE_PATH;

public class SequentialSchedulerIT extends BaseSchedulerIT {
    private void setInputAndOutputPaths(String filename) {
        arguments.setInputDOTFilePath(TEST_INPUT_DOT_FILE_PATH.concat(filename));
        arguments.setOutputDOTFilePath(TEST_OUTPUT_DOT_FILE_PATH.concat(filename));
    }

    private void arrangeTestCase(byte processors) throws IOException {
        arguments.setProcessors(processors);

        createGraph();
        createScheduler();
    }

    private void createGraph() throws IOException {
        graph = new GraphModel(arguments.getInputDOTFilePath());
    }

    private void createScheduler() {
        scheduler = new AStarScheduler(graph, arguments.getProcessors());
    }

    private void assertTestCase(int expectedFinishTime) {
        MetricsModel metrics = scheduler.getMetrics();
        StateModel bestState = metrics.getBestState();

        assertAll(
                () -> assertNotNull(bestState),
                () -> assertEquals(expectedFinishTime, bestState.getMaximumFinishTime())
        );
    }

    @Test
    public void testNodes_7_OutTree() throws IOException {
        // Arrange
        int[][] processorsAndExpectedValues = {{2, 28}, {4, 22}};
        setInputAndOutputPaths("Nodes_7_OutTree.dot");

        for (int[] processorsAndExpectedValue : processorsAndExpectedValues) {
            arrangeTestCase((byte) processorsAndExpectedValue[0]);

            // Act
            scheduler.schedule();

            // Assert
            assertTestCase(processorsAndExpectedValue[1]);
        }
    }

    @Test
    public void testNodes_8_Random() throws IOException {
        // Arrange
        int[][] processorsAndExpectedValues = {{2, 581}, {4, 581}};
        setInputAndOutputPaths("Nodes_8_Random.dot");

        for (int[] processorsAndExpectedValue : processorsAndExpectedValues) {
            arrangeTestCase((byte) processorsAndExpectedValue[0]);

            // Act
            scheduler.schedule();

            // Assert
            assertTestCase(processorsAndExpectedValue[1]);
        }
    }

    @Test
    public void TestNodes_9_SeriesParallel() throws IOException {
        // Arrange
        int[][] processorsAndExpectedValues = {{2, 55}, {4, 55}};
        setInputAndOutputPaths("Nodes_9_SeriesParallel.dot");

        for (int[] processorsAndExpectedValue : processorsAndExpectedValues) {
            arrangeTestCase((byte) processorsAndExpectedValue[0]);

            // Act
            scheduler.schedule();

            // Assert
            assertTestCase(processorsAndExpectedValue[1]);
        }
    }

    @Test
    public void TestNodes_10_Random() throws IOException {
        // Arrange
        int[][] processorsAndExpectedValues = {{2, 50}, {4, 50}};
        setInputAndOutputPaths("Nodes_10_Random.dot");

        for (int[] processorsAndExpectedValue : processorsAndExpectedValues) {
            arrangeTestCase((byte) processorsAndExpectedValue[0]);

            // Act
            scheduler.schedule();

            // Assert
            assertTestCase(processorsAndExpectedValue[1]);
        }
    }

    @Test
    public void testNodes_11_OutTree() throws IOException {
        // Arrange
        int[][] processorsAndExpectedValues = {{2, 350}, {4, 227}};
        setInputAndOutputPaths("Nodes_11_OutTree.dot");

        for (int[] processorsAndExpectedValue : processorsAndExpectedValues) {
            arrangeTestCase((byte) processorsAndExpectedValue[0]);

            // Act
            scheduler.schedule();

            // Assert
            assertTestCase(processorsAndExpectedValue[1]);
        }
    }
}
