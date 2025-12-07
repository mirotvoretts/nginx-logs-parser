package academy.export;

import academy.stats.Stats;
import java.io.IOException;

/** Export logs to file */
public interface ILogsExporter {
    void export(String filename, Stats stats) throws IOException;
}
