package schedulers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import scheduler.models.GraphModel;
import scheduler.parsers.Arguments;
import scheduler.schedulers.Scheduler;
import scheduler.schedulers.sequential.AStarScheduler;

import java.io.IOException;

public abstract class BaseSchedulerIT {
    protected GraphModel graph;

    protected Scheduler scheduler;

    protected Arguments arguments;

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
