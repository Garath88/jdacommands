package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class TxtReader {
    private final URL file;
    private long timestamp;

    public TxtReader(URL file) {
        this.file = file;
    }

    public List<String> readTxtFile() throws IOException {
        try (InputStream is = file.openStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr)) {
            timestamp = getLastModified();
            return readAllLines(br);
        } catch (IOException | IllegalArgumentException e) {
            throw new IOException("Failed to load configuration", e);
        }
    }

    private long getLastModified() throws IOException {
        URLConnection connection = file.openConnection();
        long time = new Date(connection.getLastModified())
            .getTime();
        connection.getInputStream().close();
        return time;
    }

    private List<String> readAllLines(BufferedReader reader) throws IOException {
        List<String> result = new ArrayList<>();
        for (; ; ) {
            String line = reader.readLine();
            if (line == null)
                break;
            result.add(line);
        }
        return result;
    }

    boolean isFileUpdated() throws IOException {
        long latestTimestamp = getLastModified();
        if (this.timestamp != latestTimestamp) {
            this.timestamp = latestTimestamp;
            return true;
        }
        return false;
    }
}
