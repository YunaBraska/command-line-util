package berlin.yuna.clu.logic;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class CommandLineReader extends ConcurrentHashMap<String, List<String>> {

    private final List<String> commandList = new ArrayList<>();

    /**
     * Parses a string arguments into key value
     *
     * @param input strings with arguments
     */
    public CommandLineReader(final String... input) {
        final StringBuilder builder = new StringBuilder();
        for (String current : input) {
            builder.append(" ").append(current);
        }
        parseCommandLine(builder.toString());
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
     * IgnoreCase isPresent
     *
     * @param key search for
     * @return true if key is present
     */
    public boolean isPresent(final String key) {
        for (String current : this.keySet()) {
            if (key.equalsIgnoreCase(current)) return true;
        }
        return false;
    }


    /**
     * IgnoreCase getValue
     *
     * @param keys search for
     * @return value or null if no value was found
     */
    public String getValue(final String... keys) {
        return getValue(0, keys);
    }

    /**
     * IgnoreCase getValue
     *
     * @param keys  search for
     * @param index number of value
     * @return value or null if no value was found
     */
    public String getValue(final int index, final String... keys) {
        final List<String> result = getValues(keys);
        return result.isEmpty() || result.size() - 1 < index ? null : result.get(index);
    }

    /**
     * IgnoreCase getValue
     *
     * @param keys search for
     * @return list of values
     */
    public List<String> getValues(final String... keys) {
        List<String> result = new ArrayList<>();
        for (String key : keys) {
            result.addAll(get(key));
        }

        //noinspection unchecked
        result = removeDuplicates(result);
        return result;
    }

    /**
     * IgnoreCase get
     *
     * @param key search for
     * @return list of values or empty list if not found
     */
    public List<String> get(final String key) {
        for (String current : this.keySet()) {
            if (key.equalsIgnoreCase(current)) return super.get(current);
        }
        return new ArrayList<>();
    }


    @SuppressWarnings("unchecked")
    private List removeDuplicates(final List<String> result) {
        return new ArrayList<>(new HashSet(result));
    }

    private void parseCommandLine(final String input) {
        final String process = parseCommands(input);

        for (String argument : (" " + process).split(" --| -")) {
            argument = argument.trim();

            if(argument.isEmpty()){
                continue;
            }

            final String[] arg = parseToKeyValue(argument);

            final String key = arg[0].trim().toLowerCase();
            final String value = arg[1] == null? null : getStripedValue(arg[1]);

            final List<String> valueList = new ArrayList<>(get(key));
            if (!containsIgnoreCase(valueList, value)) {
                valueList.add(value);
            }
            this.put(key, valueList);
        }
    }

    private String parseCommands(final String input) {
        String result = input.trim();
        if (result.indexOf('-') > 0) {
            final String[] cmdList = result.substring(0, result.indexOf('-')).trim().split(" ");
            result = result.substring(result.indexOf('-')).trim();
            addCommands(cmdList);
        }
        return result.trim();
    }

    private void addCommands(final String[] cmdList) {
        for (String cmd : cmdList) {
            if (!cmd.isEmpty()) {
                commandList.add(cmd.trim());
            }
        }
    }

    private String getStripedValue(final String value) {
        String result = value == null?  "" : value.trim();
        if ((result.startsWith("\'") && result.endsWith("\'")) || result.startsWith("\"") && result.endsWith("\"")) {
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

    private boolean containsIgnoreCase(final List<String> list, final String value) {
        for (String current : list) {
            if (value.equalsIgnoreCase(current)) return true;
        }
        return false;
    }
}
