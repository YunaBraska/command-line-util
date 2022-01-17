package berlin.yuna.clu.logic;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;


public class ArgumentReader extends ConcurrentHashMap<String, List<String>> {

    private final List<String> commandList = new ArrayList<>();

    /**
     * Parses a string arguments into key values <br>
     * example: [mvn clean install --Dencoding="UTF-8", --javaVersion 8 -v=true --args=1,2,3] <br>
     * example (commands): [mvn, clean, install] <br>
     * example (kv): [Dencoding=UTF-8, javaVersion=8, v=true, args=1,2,3] <br>
     * See: <br>
     * {@link ArgumentReader#getString(String, int, String...)} <br>
     * {@link ArgumentReader#getLong(String, int, String...)} <br>
     * {@link ArgumentReader#getDouble(String, int, String...)} <br>
     * {@link ArgumentReader#getValue(String, int, String...)} <br>
     * {@link ArgumentReader#get(String, String)} <br>
     * {@link ArgumentReader#getCommands()} <br>
     * {@link ArgumentReader#hasCommand(String)} <br>
     *
     * @param args strings with arguments
     */
    public static ArgumentReader parseArgs(final String... args) {
        return new ArgumentReader(false, args);
    }

    /**
     * Parses a string arguments into key values <br>
     * example: [mvn clean install --Dencoding="UTF-8", --javaVersion 8 -v=true --args=1,2,3] <br>
     * example (commands): [mvn, clean, install] <br>
     * example (kv): [Dencoding=UTF-8, javaVersion=8, v=true, args=1,2,3] <br>
     * See: <br>
     * {@link ArgumentReader#getString(String, int, String...)} <br>
     * {@link ArgumentReader#getLong(String, int, String...)} <br>
     * {@link ArgumentReader#getDouble(String, int, String...)} <br>
     * {@link ArgumentReader#getValue(String, int, String...)} <br>
     * {@link ArgumentReader#get(String, String)} <br>
     * {@link ArgumentReader#getCommands()} <br>
     * {@link ArgumentReader#hasCommand(String)} <br>
     *
     * @param args     strings with arguments
     * @param parseEnv if true, will also parse environment variables
     */
    public static ArgumentReader parseArgs(final boolean parseEnv, final String... args) {
        return new ArgumentReader(parseEnv, args);
    }

    /**
     * Parses a string arguments into key value
     *
     * @param args strings with arguments
     */
    protected ArgumentReader(final boolean parseEnv, final String... args) {
        final StringBuilder builder = new StringBuilder();
        for (String current : args) {
            builder.append(" ").append(current);
        }

        parseCommandLine(builder.toString());
        if (parseEnv) {
            parseEnvironment();
        }
    }

    /**
     * get command
     *
     * @return command
     */
    public String getCommand() {
        return getCommand(0);
    }

    /**
     * get command
     *
     * @param index number of command
     * @return command or null if no command were found
     */
    public String getCommand(final int index) {
        return commandList.isEmpty() || commandList.size() - 1 < index ? null : commandList.get(index);
    }

    /**
     * get commands
     *
     * @return all commands
     */
    public List<String> getCommands() {
        return new ArrayList<>(commandList);
    }

    /**
     * has command
     *
     * @return true if command exists
     */
    public boolean hasCommand(final String command) {
        return commandList.contains(command);
    }

    /**
     * isPresent
     *
     * @param keys search for
     * @return true if one key is present
     */
    public boolean isPresent(final String... keys) {
        for (String key : keys) {
            if (this.containsKey(key)) return true;
        }
        return false;
    }

    /**
     * getString
     *
     * @param keys search for
     * @return returns {@link Optional} value or {@link Optional#empty()} if no value is present
     */
    public Optional<String> getString(final String... keys) {
        return getString(0, keys);
    }

    /**
     * getString
     *
     * @param keys  search for
     * @param index number of value
     * @return returns {@link Optional} value or {@link Optional#empty()} if no value is present
     */
    public Optional<String> getString(final int index, final String... keys) {
        return getString(null, index, keys);
    }

    /**
     * getString
     *
     * @param keys      search for
     * @param index     number of value
     * @param separator handles values as list if present
     * @return returns {@link Optional} value or {@link Optional#empty()} if no value is present
     */
    public Optional<String> getString(final String separator, final int index, final String... keys) {
        return Optional.ofNullable(getValue(separator, index, keys));
    }

    /**
     * getStrings
     *
     * @param keys search for
     * @return returns values or empty list if no value is present
     */
    public List<String> getStrings(final String... keys) {
        return getStrings(null, keys);
    }

    /**
     * getStrings
     *
     * @param keys      search for
     * @param separator handles values as list if present
     * @return returns values or empty list if no value is present
     */
    public List<String> getStrings(final char separator, final String... keys) {
        return getValues(String.valueOf(separator), keys);
    }

    /**
     * getStrings
     *
     * @param keys      search for
     * @param separator handles values as list if present
     * @return returns values or empty list if no value is present
     */
    public List<String> getStrings(final String separator, final String[] keys) {
        return getValues(separator, keys);
    }

    /**
     * getBoolean
     *
     * @param keys search for
     * @return true if [value == 1], [value == true], [key != && value == null]
     */
    public boolean getBoolean(final String... keys) {
        return getBoolean(0, keys);
    }

    /**
     * getBoolean
     *
     * @param keys  search for
     * @param index number of value
     * @return true if [value == 1], [value == true], [key != && value == null]
     */
    public boolean getBoolean(final int index, final String... keys) {
        return getBoolean(null, index, keys);
    }

    /**
     * getBoolean
     *
     * @param keys      search for
     * @param index     number of value
     * @param separator handles values as list if present
     * @return true if [value == 1], [value == true], [key != && value == null]
     */
    public boolean getBoolean(final String separator, final int index, final String... keys) {
        return getString(separator, index, keys).map(toBoolean()).orElseGet(() -> isPresent(keys));
    }

    /**
     * getBoolean
     *
     * @param keys search for
     * @return boolean list if [value == 1], [value == true]
     */
    public List<Boolean> getBooleans(final String... keys) {
        return getBooleans(null, keys);
    }

    /**
     * getBoolean
     *
     * @param keys      search for
     * @param separator handles values as list if present
     * @return boolean list if [value == 1], [value == true]
     */
    public List<Boolean> getBooleans(final String separator, final String[] keys) {
        return getStrings(separator, keys).stream().map(toBoolean()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Optional<Long> getLong(final String... keys) {
        return getLong(0, keys);
    }

    /**
     * getLong
     *
     * @param keys  search for
     * @param index number of value
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public Optional<Long> getLong(final int index, final String... keys) {
        return getLong(null, index, keys);
    }

    /**
     * getLong
     *
     * @param keys      search for
     * @param index     number of value
     * @param separator handles values as list if present
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public Optional<Long> getLong(final String separator, final int index, final String... keys) {
        return getString(separator, index, keys).map(toLong());
    }

    /**
     * getLongs
     *
     * @param keys search for
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public List<Long> getLongs(final String... keys) {
        return getLongs(null, keys);
    }

    /**
     * getLongs
     *
     * @param keys      search for
     * @param separator handles values as list if present
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public List<Long> getLongs(final char separator, final String... keys) {
        return getStrings(String.valueOf(separator), keys).stream().map(toLong()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * getLongs
     *
     * @param keys      search for
     * @param separator handles values as list if present
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public List<Long> getLongs(final String separator, final String[] keys) {
        return getStrings(separator, keys).stream().map(toLong()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * getDouble
     *
     * @param keys search for
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public Optional<Double> getDouble(final String... keys) {
        return getDouble(0, keys);
    }

    /**
     * getDouble
     *
     * @param keys  search for
     * @param index number of value
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public Optional<Double> getDouble(final int index, final String... keys) {
        return getDouble(null, index, keys);
    }

    /**
     * getDouble
     *
     * @param keys      search for
     * @param index     number of value
     * @param separator handles values as list if present
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public Optional<Double> getDouble(final String separator, final int index, final String... keys) {
        return getString(separator, index, keys).map(toDouble());
    }

    /**
     * getDoubles
     *
     * @param keys search for
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public List<Double> getDoubles(final String... keys) {
        return getDoubles(null, keys);
    }

    /**
     * getDoubles
     *
     * @param keys      search for
     * @param separator handles values as list if present
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public List<Double> getDoubles(final char separator, final String... keys) {
        return getStrings(String.valueOf(separator), keys).stream().map(toDouble()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * getDoubles
     *
     * @param keys      search for
     * @param separator handles values as list if present
     * @return {@link Optional} of {@link Long} if [value is an integer]
     */
    public List<Double> getDoubles(final String separator, final String[] keys) {
        return getStrings(separator, keys).stream().map(toDouble()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * get
     *
     * @param key search for
     * @return list of values or empty list if not found
     */
    public List<String> get(final String key) {
        return get(null, key);
    }

    /**
     * get
     *
     * @param key       search for
     * @param separator handles values as list if present
     * @return list of values or empty list if not found
     */
    public List<String> get(final String separator, final String key) {
        final List<String> result = super.getOrDefault(key, new ArrayList<>());
        return separator == null ? result : result.stream().flatMap(s -> stream(s.split(separator)).filter(sp -> !sp.isEmpty())).collect(Collectors.toList());
    }

    private List<String> removeDuplicates(final List<String> result) {
        return new ArrayList<>(new LinkedHashSet<>(result));
    }

    public ArgumentReader parseEnvironment() {
        System.getProperties().forEach((key, value) -> addKV(String.valueOf(key), String.valueOf(value)));
        return this;
    }

    private ArgumentReader parseCommandLine(final String input) {
        final String process = parseCommands(input);

        for (String argument : (" " + process).split(" --| -")) {
            argument = argument.trim();

            if (argument.isEmpty()) {
                continue;
            }

            final String[] arg = parseToKeyValue(argument);

            final String key = arg[0].trim();
            final String value = arg[1] == null ? null : getStripedValue(arg[1]);

            addKV(key, value);
        }
        return this;
    }

    private ArgumentReader addKV(final String key, final String value) {
        final List<String> valueList = new ArrayList<>(get(key));
        if (value == null || !valueList.contains(value)) {
            valueList.add(value);
        }
        this.put(key, valueList);
        return this;
    }

    private String parseCommands(final String input) {
        String result = input.trim();
        if (result.contains("-")) {
            final String[] cmdList = result.substring(0, result.indexOf('-')).trim().split(" ");
            result = result.substring(result.indexOf('-')).trim();
            addCommands(cmdList);
        }
        return result.trim();
    }

    private ArgumentReader addCommands(final String[] cmdList) {
        for (String cmd : cmdList) {
            if (!cmd.isEmpty()) {
                commandList.add(cmd.trim());
            }
        }
        return this;
    }

    private String getStripedValue(final String value) {
        String result = value == null ? "" : value.trim();
        if ((result.startsWith("'") && result.endsWith("'")) || result.startsWith("\"") && result.endsWith("\"")) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    private String[] parseToKeyValue(final String argument) {
        if (argument.indexOf('=') != -1) {
            return argument.split("=");
        } else if (argument.indexOf(' ') != -1) {
            final int spaceIndex = argument.indexOf(" ");
            return new String[]{argument.substring(0, spaceIndex), argument.substring(spaceIndex)};
        }
        return new String[]{argument, null};
    }

    private List<String> getValues(final String separator, final String[] keys) {
        List<String> result = new ArrayList<>();
        for (String key : keys) {
            result.addAll(get(separator, key));
        }

        result = removeDuplicates(result);
        return result;
    }

    /**
     * getValue
     *
     * @param keys      search for
     * @param index     number of value
     * @param separator handles values as list if present
     * @return value or null if no value was found
     */
    private String getValue(final String separator, final int index, final String... keys) {
        final List<String> result = getValues(separator, keys);
        return result.isEmpty() || result.size() - 1 < index ? null : result.get(index);
    }

    private Function<String, Boolean> toBoolean() {
        return s -> "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    @SuppressWarnings("java:S3358")
    private Function<String, Long> toLong() {
        return s -> {
            try {
                return s.equalsIgnoreCase("true")
                        ? 1
                        : s.equalsIgnoreCase("false")
                        ? 0
                        : Long.parseLong(s.replaceAll("\\s", ""));
            } catch (Exception ignored) {
                return null;
            }
        };
    }

    @SuppressWarnings("java:S3358")
    private Function<String, Double> toDouble() {
        return s -> {
            try {
                return s.equalsIgnoreCase("true")
                        ? 1
                        : s.equalsIgnoreCase("false")
                        ? 0
                        : Double.parseDouble(s.replaceAll("\\s", ""));
            } catch (Exception ignored) {
                return null;
            }
        };
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final ArgumentReader that = (ArgumentReader) o;
        return Objects.equals(commandList, that.commandList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), commandList);
    }
}
