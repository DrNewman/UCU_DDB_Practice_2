package ucu.ddb.practice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServerController {

    private static final Logger log = LoggerFactory.getLogger(ServerController.class);

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public ServerController(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    }

    @PostMapping("/reset")
    @Transactional
    public String resetCounter() {
        try {
            jdbcTemplate.update(
                    "UPDATE user_counter SET counter = 0, version = 0 WHERE user_id = 'user_1'"
            );
            log.info("Лічильник скинуто до 0 для user_1");
            return "Ok";
        } catch (Exception e) {
            log.error("Помилка при скиданні лічильника", e);
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/inc_p1")
    @Transactional
    public String incP1() {
        return inc1and2();
    }

    private String inc1and2() {
        Integer counter = jdbcTemplate.queryForObject(
                "SELECT counter FROM user_counter WHERE user_id = 'user_1'",
                Integer.class
        );
        jdbcTemplate.update(
                "UPDATE user_counter SET counter = ? WHERE user_id = 'user_1'",
                (counter == null ? 0 : counter) + 1
        );
        return "OK";
    }

    @PostMapping("/inc_p2")
    public String incP2() {
        int attempt = 0;
        java.util.Random random = new java.util.Random();

        while (attempt < 100) {
            try {
                transactionTemplate.execute(status -> {
                    return inc1and2();
                });
                return "OK";
            } catch (DataAccessException e) {
                if (isRetryableError(e)) {
                    attempt++;
                    try {
                        Thread.sleep(random.nextInt(15) + 5);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Error";
                    }
                    continue;
                }
                log.error("Критична помилка БД: ", e);
                throw e;
            }
        }
        return "Error";
    }

    private boolean isRetryableError(DataAccessException e) {
        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof java.sql.SQLException sqlEx) {
            String state = sqlEx.getSQLState();
            return "40001".equals(state) || "40P01".equals(state);
        }
        return false;
    }

    @PostMapping("/inc_p3")
    public String incP3() {
        jdbcTemplate.update(
                "UPDATE user_counter SET counter = counter + 1 WHERE user_id = 'user_1'"
        );
        return "OK";
    }

    @PostMapping("/inc_p4")
    @Transactional
    public String incP4() {
        Integer current = jdbcTemplate.queryForObject(
                "SELECT counter FROM user_counter WHERE user_id = 'user_1' FOR UPDATE",
                Integer.class
        );
        jdbcTemplate.update(
                "UPDATE user_counter SET counter = ? WHERE user_id = 'user_1'",
                (current != null ? current : 0) + 1
        );
        return "OK";
    }

    @PostMapping("/inc_p5")
    @Transactional
    public String incP5() {
        int attempt = 0;
        java.util.Random random = new java.util.Random();

        while (attempt < 100) {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                    "SELECT counter, version FROM user_counter WHERE user_id = 'user_1'"
            );

            int currentCounter = (int) data.get("counter");
            int currentVersion = (int) data.get("version");

            int rowsUpdated = jdbcTemplate.update(
                    "UPDATE user_counter SET counter = ?, version = ? WHERE user_id = 'user_1' AND version = ?",
                    currentCounter + 1,
                    currentVersion + 1,
                    currentVersion
            );

            if (rowsUpdated > 0) {
                return "OK";
            }

            attempt++;
            try {
                Thread.sleep(random.nextInt(15) + 5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error";
            }
        }
        return "Error";
    }

    @PostMapping("/inc_p6")
    public String incP6() {
        return incP3();
    }

    @GetMapping("/count")
    public int count() {
        try {
            String sql = "SELECT counter FROM user_counter WHERE user_id = 'user_1'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Помилка при отриманні лічильника з БД", e);
            return -1;
        }
    }
}