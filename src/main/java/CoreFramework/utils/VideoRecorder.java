package CoreFramework.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * VideoRecorder using ffmpeg to record MP4.
 *
 * Requirements:
 *  - ffmpeg must be installed and available on PATH
 *  - Tests must run on a machine with a desktop (not a headless OS with no display)
 *
 * Behavior:
 *  - startRecording(testName): starts ffmpeg screen capture into an MP4
 *  - stopRecording(): sends "q" to ffmpeg, waits for it to finish, returns MP4 file path
 *  - deleteIfExists(path): deletes a video file (useful for passed tests)
 */
public class VideoRecorder {

    // Your desired folder:
    // src/test/resources/Artifacts/VideoRecords
    private static final Path VIDEOS_DIR =
            Paths.get("src", "test", "resources", "Artifacts", "VideoRecords");

    private static final ThreadLocal<Process> currentProcess = new ThreadLocal<>();
    private static final ThreadLocal<String> currentVideoPath = new ThreadLocal<>();

    static {
        try {
            Files.createDirectories(VIDEOS_DIR);
        } catch (Exception ignored) {
        }
    }

    /**
     * Start screen recording for this test.
     * Uses ffmpeg with gdigrab to capture the entire desktop.
     */
    public static void startRecording(String testName) {
        try {
            String fileName = testName + "_" + System.currentTimeMillis() + ".AVI";
            Path videoPath = VIDEOS_DIR.resolve(fileName);
            currentVideoPath.set(videoPath.toString());

            // Example ffmpeg command (Windows, desktop capture):
            // ffmpeg -y -f gdigrab -framerate 15 -i desktop output.mp4
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",                    // overwrite output if exists
                    "-f", "gdigrab",         // screen capture on Windows
                    "-framerate", "15",      // FPS
                    "-i", "desktop",         // capture entire desktop
                    videoPath.toAbsolutePath().toString()
            );

            // Merge stderr into stdout so ffmpeg output doesn’t block
            pb.redirectErrorStream(true);

            Process process = pb.start();
            currentProcess.set(process);

        } catch (IOException e) {
            // If ffmpeg fails to start, clear thread-locals
            currentProcess.remove();
            currentVideoPath.remove();
            // You can log this if you want
        }
    }

    /**
     * Stop recording and return the MP4 file path (if it exists).
     * This sends "q" to ffmpeg to gracefully finish the file.
     */
    public static String stopRecording() {
        Process process = currentProcess.get();
        String videoPath = currentVideoPath.get();

        try {
            if (process != null) {
                try {
                    // Send 'q' to ffmpeg to stop and finalize the file
                    OutputStream os = process.getOutputStream();
                    os.write('q');
                    os.write('\n');
                    os.flush();
                } catch (IOException ignored) {
                }

                // Wait for ffmpeg to exit
                process.waitFor();
            }

            if (videoPath == null) {
                return null;
            }

            Path p = Paths.get(videoPath);
            if (Files.exists(p)) {
                return p.toAbsolutePath().toString();
            } else {
                return null;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            currentProcess.remove();
            currentVideoPath.remove();
        }
    }

    /**
     * Delete the given video file if it exists.
     * Useful for passed / skipped tests to save disk space.
     */
    public static void deleteIfExists(String videoPath) {
        if (videoPath == null || videoPath.isEmpty()) {
            return;
        }
        try {
            Path p = Paths.get(videoPath);
            if (Files.exists(p)) {
                Files.delete(p);
            }
        } catch (Exception ignored) {
            // Do not fail the test because deleting a file failed
        }
    }
}
