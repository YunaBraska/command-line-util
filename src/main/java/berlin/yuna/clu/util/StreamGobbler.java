package berlin.yuna.clu.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

public class StreamGobbler implements Runnable {
    private final InputStream inputStream;
    private final List<Consumer<String>> consumerList;

    public StreamGobbler(final InputStream inputStream, final List<Consumer<String>> consumerList) {
        this.inputStream = inputStream;
        this.consumerList = consumerList;
    }

    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(string -> consumerList.forEach(c -> c.accept(
                string)));
    }
}