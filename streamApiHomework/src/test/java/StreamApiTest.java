import org.example.common.dto.Task;
import org.example.common.enums.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.common.enums.Status.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamApiTest {

    List<Task> tasks = List.of(new Task(1, "Do your homework", READY),
            new Task(2, "Send your homework", IN_PROGRESS),
            new Task(3, "Being a cool programmer", READY),
            new Task(4, "Get some sleep", CREATED),
            new Task(5, "Watch netflix", STOPPED));

    @Test
    void testFilter() {
        List<Task> result = tasks.stream()
                .filter(t -> t.getStatus() == CREATED)
                .toList();

        List<Task> test = List.of(new Task(4, "Get some sleep", CREATED));

        assertTrue(result.size() == 1);
        assertTrue(test.containsAll(result) && result.containsAll(test));
    }

    @Test
    void testAny() {
        boolean result = tasks.stream()
                .anyMatch(t -> t.getId() == 5);

        assertTrue(result);
    }

    @Test
    void testSorted() {
        List<Task> result = tasks.stream()
                .sorted((t1, t2) -> t1.getStatus().ordinal() - t2.getStatus().ordinal())
                .toList();

        Task testTask = new Task(4, "Get some sleep", CREATED);
        Task testTask2 = new Task(5, "Watch netflix", STOPPED);

        assertEquals(testTask, result.get(0));
        assertEquals(testTask2, result.get(result.size()-1));
    }

    @Test
    void testCount() {
        long result = tasks.stream()
                .filter(t -> t.getStatus() == READY)
                .count();

        assertEquals(2, result);
    }
}
