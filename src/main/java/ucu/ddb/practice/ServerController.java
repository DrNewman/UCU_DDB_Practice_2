package ucu.ddb.practice;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ServerController {

    private static final Logger log = LoggerFactory.getLogger(ServerController.class);
    private static final Path COUNTER_FILE = Paths.get("counter_p2.txt");

    private final AtomicInteger inMemmoryCounter = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        try {
            Files.writeString(COUNTER_FILE, "0");
            log.info("Лічильник ініціалізовано у файлі: {}", COUNTER_FILE.toAbsolutePath());
        } catch (IOException e) {
            log.error("Не вдалося створити файл з лічильником", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }

    @PostMapping("/inc_p1")
    public String incP1() {
        return String.valueOf(inMemmoryCounter.incrementAndGet());
    }

    @GetMapping("/count_p1")
    public int countP1() {
        return inMemmoryCounter.get();
    }

    @PostMapping("/inc_p2")
    public synchronized String incP2() {
        try {
            String content = Files.readString(COUNTER_FILE).trim();
            int currentVal = Integer.parseInt(content);

            int newVal = currentVal + 1;

            Files.writeString(COUNTER_FILE, String.valueOf(newVal));

            return String.valueOf(newVal);
        } catch (IOException | NumberFormatException e) {
            log.error("Помилка при роботі з файлом (inc)", e);
            return "File error";
        }
    }

    @GetMapping("/count_p2")
    public synchronized int countP2() {
        try {
            String content = Files.readString(COUNTER_FILE).trim();
            return Integer.parseInt(content);
        } catch (IOException | NumberFormatException e) {
            log.error("Помилка при роботі з файлом (count)", e);
            return -1;
        }
    }
}