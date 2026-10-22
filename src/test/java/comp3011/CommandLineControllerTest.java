package comp3011;

import java.beans.Transient;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import comp3011.CommandLineController.FPOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandLineControllerTest {
    File tempVideo;
    private String tempVideoPath;

    private String helpExpectedOutput = String.join(System.lineSeparator(),
        "Usage: VideoPlayer [options] [video-file]",
        "",
        "Options:",
        "  -h, --help         Show this help message",
        "  -a, --audio        Play audio",
        "  -x, --maximise     Open the player maximised",
        "  -1, --monitor-1    Open the player on display 1",
        "  -2, --monitor-2    Open the player on display 2",
        "",
        "Frame processors:",
        "  -n, --number-frames    Render the frame number onto each frame",
        "  -s, --scratch-frames   Render vertical film scratches",
        "  -f, --flicker-frames   Randomly dim frames",
        "  -w, --black-and-white  Convert frames to black and white",
        "  -y, --yellow-frames    Apply a warmer colour temperature",
        "  -v, --vignette-frames  Darken the frame edges",
        "  -d, --dust-frames      Render dust and hair marks",
        "  -j, --jitter-frames    Randomly displace frames by a few pixels",
        "  -m, --mottle-frames    Add cloudy emulsion mottling",
        "  -b, --bleed-frames     Bleed light into frames",
        "  -p, --pepper-frames    Pepper frames with dark spots/blotches",
        "",
        "Frame processors are applied in command-line order and may be repeated.",
        "Example: -nssnfwyvdjmbp numbers, scratches twice, numbers again, flickers,",
        "converts, warms, vignettes, dusts, jitters, mottles, bleeds, then peppers."
    ) + System.lineSeparator();

    @BeforeEach
    void createPlaceholderVideo() throws IOException {
        // Source - https://stackoverflow.com/a/26860215
        // Posted by Stefan, modified by community. See post 'Timeline' for change history
        // Retrieved 2026-10-22, License - CC BY-SA 4.0
        tempVideo = File.createTempFile("tempVideoFile", ".mp4");
        tempVideoPath = tempVideo.getAbsolutePath();
    }

    @AfterEach
    void deletePlaceholderVideo() throws IOException {
        tempVideo.deleteOnExit();
        tempVideoPath = "";
    }

    private String getOutput(Runnable action) {
        // Source - https://www.geeksforgeeks.org/advance-java/unit-testing-of-system-out-println-with-junit/
        // Retrieved 2026-10-22
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return outputStream.toString();
    }

    // Help tests
    @Test
    void helpOption_noVideo_printsHelp() {
        String[] longArgs = {"--help"};
        String[] shortArgs = {"-h"};

        String longFormOutput = getOutput(() -> new CommandLineController(longArgs));
        String shortFormOutput = getOutput(() -> new CommandLineController(shortArgs));

        String expected = helpExpectedOutput;

        assertEquals(expected, longFormOutput);
        assertEquals(expected, shortFormOutput);
    }

    // Audio tests
    @Test
    void audioOption_playsAudio() {
        String[] longArgs = {"--audio", tempVideoPath};
        String[] shortArgs = {"-a", tempVideoPath};

        CommandLineController longForm = new CommandLineController(longArgs);
        CommandLineController shortForm = new CommandLineController(shortArgs);

        assertTrue(longForm.isAudioRequested());
        assertTrue(shortForm.isAudioRequested());
    }

    // Maximise tests
    @Test
    void maximiseOption_maximiseWindow() {
        String[] longArgs = {"--maximise", tempVideoPath};
        String[] shortArgs = {"-x", tempVideoPath};

        CommandLineController longForm = new CommandLineController(longArgs);
        CommandLineController shortForm = new CommandLineController(shortArgs);

        assertTrue(longForm.isMaximiseRequested());
        assertTrue(shortForm.isMaximiseRequested());
    }

    // Display tests
    @Test
    void displayOption_openPlayerOnDisplay() {
        String[] longArgs1 = {"--monitor-1", tempVideoPath};
        String[] shortArgs1 = {"-1", tempVideoPath};

        String[] longArgs2 = {"--monitor-2", tempVideoPath};
        String[] shortArgs2 = {"-2", tempVideoPath};

        CommandLineController longForm1 = new CommandLineController(longArgs1);
        CommandLineController shortForm1 = new CommandLineController(shortArgs1);

        CommandLineController longForm2 = new CommandLineController(longArgs2);
        CommandLineController shortForm2 = new CommandLineController(shortArgs2);

        assertEquals(1, longForm1.getDisplayId());
        assertEquals(1, shortForm1.getDisplayId());

        assertEquals(2, longForm2.getDisplayId());
        assertEquals(2, shortForm2.getDisplayId());
    }
}