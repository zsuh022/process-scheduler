package scheduler.schedulers.parallel;

import static org.junit.jupiter.api.Assertions.*;
import static scheduler.constants.Constants.TEST_INPUT_DOT_FILE_PATH;
import static scheduler.constants.Constants.TEST_OUTPUT_DOT_FILE_PATH;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import org.graphstream.graph.Graph;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import scheduler.models.GraphModel;
import scheduler.models.StateModel;
import scheduler.parsers.InputOutputParser;
import scheduler.schedulers.BaseSchedulerIT;
import scheduler.schedulers.sequential.AStarScheduler;

@Disabled
public class ParallelSchedulerIT extends BaseSchedulerIT {
  private void setInputAndOutputPaths(String filename) {
    arguments.setInputDOTFilePath(TEST_INPUT_DOT_FILE_PATH.concat(filename));
    arguments.setOutputDOTFilePath(TEST_OUTPUT_DOT_FILE_PATH.concat(filename));
  }

  private void arrangeTestCase(byte processors, byte cores) throws IOException {
    arguments.setProcessors(processors);
    arguments.setCores(cores);
    createGraph();
    createScheduler();
  }

  private void createGraph() throws IOException {
    graph = new GraphModel(arguments.getInputDOTFilePath());
  }

  private void createScheduler() {
    scheduler =
        new ParallelSchedulerDynamic(graph, arguments.getProcessors(), arguments.getCores());
  }

  private void assertTestCase(int expectedFinishTime) {
    StateModel bestState = scheduler.getMetrics().getBestState();

    assertAll(
        () -> assertNotNull(bestState),
        () -> assertEquals(expectedFinishTime, bestState.getMaximumFinishTime()));
  }

  @Test
  public void testNodes_7_OutTree() throws IOException {
    // Arrange
    int[][] processorsAndExpectedValues = {{2, 28}, {4, 22}};
    setInputAndOutputPaths("Nodes_7_OutTree.dot");

    for (int[] processorsAndExpectedValue : processorsAndExpectedValues) {

      // Set the number of number of cores as a random number between 2 and 9
      byte randomCores = (byte) ThreadLocalRandom.current().nextInt(2, 9);
      arrangeTestCase((byte) processorsAndExpectedValue[0], randomCores);

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
      // Set the number of number of cores as a random number between 2 and 9
      byte randomCores = (byte) ThreadLocalRandom.current().nextInt(2, 9);
      arrangeTestCase((byte) processorsAndExpectedValue[0], randomCores);

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

      // Set the number of number of cores as a random number between 2 and 9
      byte randomCores = (byte) ThreadLocalRandom.current().nextInt(2, 9);
      arrangeTestCase((byte) processorsAndExpectedValue[0], randomCores);

      // Act
      scheduler.schedule();

      // Assert
      assertTestCase(processorsAndExpectedValue[1]);
    }
  }

  @Test
  public void TestNodes_10_Random() throws IOException {
    // Arrange
    int[][] processorsAndExpectedValues = {{1, 63}, {2, 50}, {4, 50}};
    setInputAndOutputPaths("Nodes_10_Random.dot");

    for (int[] processorsAndExpectedValue : processorsAndExpectedValues) {

      // Set the number of number of cores as a random number between 2 and 9
      byte randomCores = (byte) ThreadLocalRandom.current().nextInt(2, 9);
      arrangeTestCase((byte) processorsAndExpectedValue[0], randomCores);

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

      // Set the number of number of cores as a random number between 2 and 9
      byte randomCores = (byte) ThreadLocalRandom.current().nextInt(2, 9);
      arrangeTestCase((byte) processorsAndExpectedValue[0], randomCores);

      // Act
      scheduler.schedule();

      // Assert
      assertTestCase(processorsAndExpectedValue[1]);
    }
  }

  /**
   * Set the number of processors based on the target system
   *
   * @param graph
   */
  public void setProcessors(Graph graph) {
    String targetSystem = (String) graph.getAttribute("TargetSystem");

    String[] splits = targetSystem.split("-");

    arguments.setProcessors(Byte.parseByte(splits[1]));
  }

  @Disabled
  @Test
  public void testCrawledDOTFiles() throws IOException {
    for (String crawledDOTFile : crawledDOTFiles) {
      testCrawledDOTFile(crawledDOTFile);
    }
  }

  public void testCrawledDOTFile(String filename) throws IOException {
    Graph graph = InputOutputParser.readDOTFile(filename);

    setProcessors(graph);

    int expectedValue = (int) Math.round((double) graph.getAttribute("Total schedule length"));

    this.graph = new GraphModel(graph);

    this.scheduler = new AStarScheduler(this.graph, arguments.getProcessors());
    this.scheduler.schedule();

    assertTestCase(expectedValue);
  }
}