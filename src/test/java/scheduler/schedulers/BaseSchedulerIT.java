package scheduler.schedulers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import scheduler.models.GraphModel;
import scheduler.parsers.Arguments;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static scheduler.constants.Constants.TEST_CRAWLED_DOT_FILE_PATH;

public abstract class BaseSchedulerIT {
    protected GraphModel graph;

    protected Scheduler scheduler;

    protected Arguments arguments;

    protected static List<String> crawledDOTFiles;

    @BeforeAll
    public static void getCrawledDOTFiles() {
        crawledDOTFiles = new ArrayList<>();

        String path = TEST_CRAWLED_DOT_FILE_PATH;

        File directory = new File(path);
        int index=0;

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            String filename = file.getName();

            if (filename.startsWith("Fork_Join")) {
                crawledDOTFiles.add(path.concat(file.getName()));
            }
        }

        System.out.println(crawledDOTFiles.size());
    }

    @BeforeEach
    public void initialise() {
        this.arguments = new Arguments();
    }

    @AfterEach
    public void cleanUp() {
        graph = null;
        scheduler = null;
        arguments = null;
    }
}
