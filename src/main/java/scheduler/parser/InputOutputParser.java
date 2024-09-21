package scheduler.parser;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDOT;

public class InputOutputParser {
    public static Graph readDotFile(String filename) throws IOException {
        Graph graph = new SingleGraph("digraph");

        FileSourceDOT fileSourceDOT = new FileSourceDOT();
        fileSourceDOT.addSink(graph);

        try {
            fileSourceDOT.readAll(filename);
        } finally {
            fileSourceDOT.removeSink(graph);
        }

        return graph;
    }
}
