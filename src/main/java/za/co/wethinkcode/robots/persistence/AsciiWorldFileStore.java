package za.co.wethinkcode.robots.persistence;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Writes ASCII world dumps to disk.
 * Kept out of {@code world} classes so file access is not mixed with domain logic.
 */
public class AsciiWorldFileStore {

    public void write(String path, String content) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
        }
    }
}
