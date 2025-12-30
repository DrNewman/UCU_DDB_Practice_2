package ucu.ddb.practice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ServerController {

    private volatile AtomicInteger inMemmoryCounter = new AtomicInteger(0);
    private int p2 = 0;

    @PostMapping("/inc_p1")
    public String incP1() {
        return String.valueOf(inMemmoryCounter.incrementAndGet());
    }

    @GetMapping("/count_p1")
    public int countP1() {
        return inMemmoryCounter.get();
    }

    @PostMapping("/inc_p2")
    public String incP2() {
        p2++;
        return "P2 incremented";
    }

    @GetMapping("/count_p2")
    public int countP2() {
        return p2;
    }
}