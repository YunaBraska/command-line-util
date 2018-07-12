package berlin.yuna.system.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private List<Consumer<String>> consumerList;

    public StreamGobbler(InputStream inputStream, Consumer<String>... consumerList) {
        this.inputStream = inputStream;
        this.consumerList = Arrays.asList(consumerList);
    }

    public StreamGobbler(InputStream inputStream, List<Consumer<String>> consumerList) {
        this.inputStream = inputStream;
        this.consumerList = consumerList;
    }

    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(string -> {
            consumerList.forEach(c -> c.accept(string));
        });
    }
}