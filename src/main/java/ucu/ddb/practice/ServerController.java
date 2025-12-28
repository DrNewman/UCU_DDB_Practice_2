package ucu.ddb.practice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {
    private int p1 = 0;
    private int p2 = 0;

    @PostMapping("/inc_p1")
    public String incP1() {
        p1++;
        return "P1 incremented";
    }

    @GetMapping("/count_p1")
    public int countP1() {
        return p1;
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