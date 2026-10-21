import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

// ============================================================
// Global checker for the Java Basics project (75 points).
//
// From the root of the project (the folder that contains tests/):
//     java tests/Grader.java          -> summary + details of the failing tests
//     java tests/Grader.java -v       -> details of ALL tests
//     java tests/Grader.java 3 4      -> only tasks 3 and 4
//
// Remember to compile your tasks with javac before running the checker.
// ============================================================
public class Grader {
    record Task(int number, String dir, int points) {}

    static final Task[] TASKS = {
        new Task(0, "0-hello", 3),
        new Task(1, "1-args", 4),
        new Task(2, "2-variables", 6),
        new Task(3, "3-strings", 5),
        new Task(4, "4-string-traps", 5),
        new Task(5, "5-conditions", 5),
        new Task(6, "6-loops", 5),
        new Task(7, "7-arrays", 6),
        new Task(8, "8-methods", 6),
        new Task(9, "9-by-value", 3),
        new Task(10, "10-access", 4),
        new Task(11, "11-static", 5),
        new Task(12, "12-input", 5),
        new Task(13, "13-guess", 5),
        new Task(14, "14-calculator", 8),
    };

    public static void main(String[] args) throws Exception {
        boolean verbose = false;
        Set<Integer> only = new TreeSet<>();
        for (String a : args) {
            if (a.equals("-v")) {
                verbose = true;
            } else {
                try {
                    only.add(Integer.parseInt(a));
                } catch (NumberFormatException e) {
                    System.err.println("Unknown argument: " + a + " (expected: -v or a task number)");
                    System.exit(2);
                }
            }
        }
        if (!Files.isDirectory(Path.of("tests"))) {
            System.err.println("tests/ folder not found: run the checker from the root of the project.");
            System.exit(2);
        }

        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        int total = 0;
        int max = 0;
        List<String> failures = new ArrayList<>();

        System.out.println("Java Basics: automatic grading");
        System.out.println("=".repeat(60));

        for (Task task : TASKS) {
            if (!only.isEmpty() && !only.contains(task.number())) {
                continue;
            }
            max += task.points();
            String file = String.format("tests/Task%02dTest.java", task.number());
            List<String> ok = new ArrayList<>();
            List<String> ko = new ArrayList<>();
            List<String> other = new ArrayList<>();
            try {
                ProcessBuilder pb = new ProcessBuilder(java, file).redirectErrorStream(true);
                pb.environment().remove("JAVA_TOOL_OPTIONS");
                pb.environment().remove("JDK_JAVA_OPTIONS");
                pb.environment().remove("_JAVA_OPTIONS");
                Process p = pb.start();
                p.getOutputStream().close();
                String output;
                try (InputStream in = p.getInputStream()) {
                    output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                if (!p.waitFor(180, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                    other.add("the test exceeds 180 s (infinite loop?)");
                }
                for (String line : output.lines().toList()) {
                    if (line.startsWith("[OK] ")) {
                        ok.add(line.substring(5));
                    } else if (line.startsWith("[KO] ")) {
                        ko.add(line.substring(5));
                    } else if (!line.isBlank() && !line.endsWith("tests passed")) {
                        other.add(line);
                    }
                }
            } catch (IOException e) {
                other.add("cannot run " + file + ": " + e.getMessage());
            }

            int score = Math.min(ok.size(), task.points());
            total += score;
            System.out.printf("%2d  %-16s %d/%d%n", task.number(), task.dir(), score, task.points());
            if (verbose) {
                ok.forEach(name -> System.out.println("      [OK] " + name));
            }
            for (String line : ko) {
                System.out.println("      [KO] " + line);
                failures.add("task " + task.number() + ": " + line);
            }
            if (ok.size() + ko.size() == 0 && !other.isEmpty()) {
                System.out.println("      The test file could not run:");
                other.stream().limit(6).forEach(l -> System.out.println("      " + l));
            }
        }

        System.out.println("=".repeat(60));
        System.out.printf("TOTAL: %d/%d%n", total, max);
        if (failures.isEmpty() && total == max) {
            System.out.println("All tests pass.");
        }
        System.exit(total == max ? 0 : 1);
    }
}
