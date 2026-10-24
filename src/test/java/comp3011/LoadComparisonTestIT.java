package comp3011;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import javafx.util.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class LoadComparisonTestIT {
    private static final File SINGLE_THREAD_REFERENCE_DIR = new File("reference-player").getAbsoluteFile();
    private static final String TEST_CLIP_PATH = new File("src/test/resources/test-clip.mp4").getAbsolutePath();
    private static final String EFFECTS = "-sfwyvdjmbppbmjdvywfs --number-frames";

    private static final Pattern HEADROOM_PATTERN = Pattern.compile("Frame headroom is \\D*(-?\\d+)us");

    private record Headroom(
        int totalCount,
        int positive,
        int negative
    ) {}

    private Headroom countHeadroom(File projectDir) throws Exception {
        // Source - https://www.baeldung.com/run-shell-command-in-java
        // Retrieved 2026-10-23
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        String mvn = (isWindows) ? "mvn.cmd" : "mvn";

        ProcessBuilder processBuilder = new ProcessBuilder(
            mvn,
            "javafx:run",
            "-Djavafx.args=" + EFFECTS + " " + TEST_CLIP_PATH
        );
        processBuilder.directory(projectDir);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int totalCount = 0;
        int positive = 0;
        int negative = 0;

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            // Source - https://www.w3schools.com/java/java_regex.asp
            // Retrieved 2026-10-23
            Matcher matcher = HEADROOM_PATTERN.matcher(line);
            if (matcher.find()) {
                totalCount++;
                if (Integer.parseInt(matcher.group(1)) < 0) {
                    negative++;
                } else {
                    positive++;
                }
            }
        }

        process.destroyForcibly();
        process.waitFor(5, TimeUnit.SECONDS);

        return new Headroom(totalCount, positive, negative);
    }

    @Test
    void headroomComparison_singleVsMultiThread() throws Exception {
        File entryProjectDir = new File(".").getAbsoluteFile();

        Headroom entry = countHeadroom(entryProjectDir);
        Headroom ref = countHeadroom(SINGLE_THREAD_REFERENCE_DIR);

        System.out.printf("Entry: %d positive, %d negative of %d total.",
            ref.positive, ref.negative, ref.totalCount);
        System.out.println();
        System.out.printf("Reference: %d positive, %d negative of %d total.",
            entry.positive, entry.negative, entry.totalCount);

        assertTrue(entry.negative < ref.negative);
    }
}