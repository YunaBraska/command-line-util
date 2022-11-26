package berlin.yuna.clu.logic;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static berlin.yuna.clu.logic.ArgumentReader.parseArgs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Tag("UnitTest")
class ArgumentReaderTest {

    private final String input = " myCommand1    myCommand2 --help  -v2=\"true\" -v=\"true\" -v=\"true\" --verbose=\"true\"   -DArgs=\"true\" -param 42   54   -DArgList=\"item 1\" --DArgList=\"item 2\" -v2=\"false\" --DArgList=\"-item 3\"  ";
    private ArgumentReader arguments;

    @BeforeEach
    void setUp() {
        arguments = parseArgs(input);
    }

    @Test
    void readMeExample() {
        //false == without environment variables
        final ArgumentReader args = parseArgs(false, "mvn clean install --Dencoding=\"UTF-8\" --javaVersion 8 -v=true --args=1,2,3", "--args=4,5");
        args.size(); // = 4
        args.getCommands(); // = 3
        args.hasCommand("install"); // = true

        //ARGUMENTS & TYPES
        args.getString("Dencoding"); // = "UTF-8"
        args.getBoolean("v"); // = true
        args.getLong("javaVersion"); // = 8
        args.getDouble("javaVersion"); // = 8.0

        //ARGUMENTS & SPLITTING & INDEXING
        args.getLong("-", 1, "Dencoding"); // = 8

        //DUPLICATES & LISTING & INDEXING
        args.getString("args"); // = "1,2,3"
        args.getString(1, "args"); // = "4,5"
        args.getStrings(',', "args"); // = ["1","2","3","4","5"]
        args.getLongs(',', "args"); // = [1,2,3,4,5]
        validateReadmeExample(args);
    }

    private void validateReadmeExample(final ArgumentReader args) {
        assertThat(args.size(), is(4));
        assertThat(args.getCommands().size(), is(3));
        assertThat(args.hasCommand("install"), is(true));
        assertThat(args.getBoolean("v"), is(true));
        assertThat(args.getLong("javaVersion"), is(equalTo(Optional.of(8L))));
        assertThat(args.getDouble("javaVersion"), is(equalTo(Optional.of(8D))));
        assertThat(args.getString("Dencoding"), is(equalTo(Optional.of("UTF-8"))));
        assertThat(args.getLong("-", 1, "Dencoding"), is(equalTo(Optional.of(8L))));
        assertThat(args.getString("args"), is(equalTo(Optional.of("1,2,3"))));
        assertThat(args.getString(1, "args"), is(equalTo(Optional.of("4,5"))));
        assertThat(args.getStrings(',', "args"), is(equalTo(Arrays.asList("1", "2", "3", "4", "5"))));
        assertThat(args.getLongs(',', "args"), is(equalTo(Arrays.asList(1L, 2L, 3L, 4L, 5L))));
        assertThat(args.getDoubles(',', "args"), is(equalTo(Arrays.asList(1D, 2D, 3D, 4D, 5D))));
    }

    @Test
    void parse_withEmptyString_shouldBeSuccessful() {
        assertThat(parseArgs("").size(), is(0));
    }

    @Test
    void parse_EnvVariables_shouldBeSuccessful() {
        System.setProperty("myEnvKey", "myEnvValue");
        final ArgumentReader arguments = parseArgs(true, "--myKey=myValue");

        assertThat(arguments.size(), is(greaterThan(2)));
        assertThat(arguments.getString("myKey"), is(equalTo(Optional.of("myValue"))));
        assertThat(arguments.getString("myEnvKey"), is(equalTo(Optional.of("myEnvValue"))));
    }

    @Test
    void parse_withCommands_shouldBeSuccessful() {
        assertThat(arguments.getCommand(), is(notNullValue()));
        assertThat(arguments.getCommand(), is(equalTo("myCommand1")));
        assertThat(arguments.getCommand(0), is(equalTo("myCommand1")));
        assertThat(arguments.getCommand(1), is(equalTo("myCommand2")));
        assertThat(arguments.getCommand(2), is(nullValue()));
        assertThat(arguments.getCommands().size(), is(2));
        assertThat(arguments.hasCommand("myCommand2"), is(true));
        assertThat(arguments.hasCommand("myCommand3"), is(false));
    }

    @Test
    void parse_withOutCommands_shouldBeSuccessful() {
        final ArgumentReader cmdLines = parseArgs("--help -v -DArgs=\"true\" param 42 54");

        assertThat(cmdLines.getCommand(), is(nullValue()));
        assertThat(cmdLines.getCommand(0), is(nullValue()));
    }

    @Test
    void invalidKey_isNotPresent() {
        assertThat(arguments.isPresent("notPresentKey"), is(false));
        assertThat(arguments.get("notPresentKey").size(), is(0));
        assertThat(arguments.getStrings("notPresentKey").size(), is(0));
        assertThat(arguments.getString("notPresentKey"), is(equalTo(Optional.empty())));
        assertThat(arguments.getLong("notPresentKey"), is(equalTo(Optional.empty())));
        assertThat(arguments.getLongs("notPresentKey").size(), is(0));
        assertThat(arguments.getDouble("notPresentKey"), is(equalTo(Optional.empty())));
        assertThat(arguments.getDoubles("notPresentKey").size(), is(0));
        assertThat(arguments.getBoolean("notPresentKey"), is(false));
        assertThat(arguments.getBooleans("notPresentKey").size(), is(0));
    }

    @Test
    void keyWithoutValue_isPresent() {
        assertThat(arguments.isPresent("help"), is(true));
        assertThat(arguments.get("help").size(), is(1));
        assertThat(arguments.getStrings("help").size(), is(1));
        assertThat(arguments.getString("help"), is(equalTo(Optional.empty())));
        assertThat(arguments.getLong("help"), is(equalTo(Optional.empty())));
        assertThat(arguments.getLongs("help").size(), is(0));
        assertThat(arguments.getDouble("help"), is(equalTo(Optional.empty())));
        assertThat(arguments.getDoubles("help").size(), is(0));
        assertThat(arguments.getBoolean("help"), is(true));
        assertThat(arguments.getBooleans("help").size(), is(1));
    }

    @Test
    void duplicated_KV_areCountedAs_one() {
        assertThat(count(input, "-v="), is(2));
        assertThat(arguments.isPresent("v"), is(true));
        assertThat(arguments.get("v").size(), is(1));
        assertThat(arguments.getString("v"), is(equalTo(Optional.of("true"))));
        assertThat(arguments.getLong("v"), is(equalTo(Optional.of(1L))));
        assertThat(arguments.getLongs("v").size(), is(1));
        assertThat(arguments.getDouble("v"), is(equalTo(Optional.of(1D))));
        assertThat(arguments.getDoubles("v").size(), is(1));
        assertThat(arguments.getBoolean("v"), is(true));
        assertThat(arguments.getBooleans("v").size(), is(1));
    }

    @Test
    void getBoolean_withDifferentValues_takes_firstValue() {
        assertThat(count(input, "-v2="), is(2));
        assertThat(arguments.isPresent("v2"), is(true));
        assertThat(arguments.get("v2").size(), is(2));
        assertThat(arguments.getString("v2"), is(equalTo(Optional.of("true"))));
        assertThat(arguments.getLong("v2"), is(equalTo(Optional.of(1L))));
        assertThat(arguments.getLongs("v2").size(), is(2));
        assertThat(arguments.getDouble("v2"), is(equalTo(Optional.of(1D))));
        assertThat(arguments.getDoubles("v2").size(), is(2));
        assertThat(arguments.getBooleans("v2").size(), is(2));
        assertThat(arguments.getBooleans("v2").get(0), is(true));
        assertThat(arguments.getBooleans("v2").get(1), is(false));
        assertThat(arguments.getBoolean("v2"), is(true));
        assertThat(arguments.getBoolean(1, "v2"), is(false));
    }

    @Test
    void duplicatedArgument_hasMultipleValues() {
        assertThat(count(input, "-DArgList="), is(3));
        assertThat(arguments.isPresent("DArgList"), is(true));
        assertThat(arguments.get("DArgList").size(), is(3));
        assertThat(arguments.get("DArgList").get(0), is(equalTo("item 1")));
        assertThat(arguments.get("DArgList").get(1), is(equalTo("item 2")));
        assertThat(arguments.get("DArgList").get(2), is(equalTo("-item 3")));
        assertThat(arguments.getString("DArgList"), is(equalTo(Optional.of("item 1"))));
        assertThat(arguments.getString(0, "DArgList"), is(equalTo(Optional.of("item 1"))));
        assertThat(arguments.getString(1, "DArgList"), is(equalTo(Optional.of("item 2"))));
        assertThat(arguments.getString(2, "DArgList"), is(equalTo(Optional.of("-item 3"))));
        assertThat(arguments.getString(4, "DArgList"), is(Optional.empty()));
        assertThat(arguments.getString("DArgList"), is(equalTo(Optional.of("item 1"))));
        assertThat(arguments.getStrings("DArgList").size(), is(3));
        assertThat(arguments.getLong("DArgList"), is(equalTo(Optional.empty())));
        assertThat(arguments.getLongs("DArgList").size(), is(0));
        assertThat(arguments.getDouble("DArgList"), is(equalTo(Optional.empty())));
        assertThat(arguments.getDoubles("DArgList").size(), is(0));
        assertThat(arguments.getBoolean("DArgList"), is(false));
        assertThat(arguments.getBooleans("DArgList").size(), is(3));
    }

    @Test
    void argumentWithout_EqualSign_getsValuesUntilNextArgument() {
        assertThat(count(input, "-param="), is(0));
        assertThat(count(input, "-param"), is(1));
        assertThat(arguments.isPresent("param"), is(true));
        assertThat(arguments.get("param").size(), is(1));
        assertThat(arguments.get("param").get(0), is(equalTo("42   54")));
        assertThat(arguments.getString("param"), is(equalTo(Optional.of("42   54"))));
        assertThat(arguments.getString("\\s", 0, "param"), is(equalTo(Optional.of("42"))));
        assertThat(arguments.getString("\\s", 1, "param"), is(equalTo(Optional.of("54"))));
        assertThat(arguments.getString("param"), is(equalTo(Optional.of("42   54"))));
        assertThat(arguments.getString("\\s", 0, "param"), is(equalTo(Optional.of("42"))));
        assertThat(arguments.getString("\\s", 1, "param"), is(equalTo(Optional.of("54"))));
        assertThat(arguments.getLong("param"), is(equalTo(Optional.of(4254L))));
        assertThat(arguments.getLong("\\s", 0, "param"), is(equalTo(Optional.of(42L))));
        assertThat(arguments.getLong("\\s", 1, "param"), is(equalTo(Optional.of(54L))));
        assertThat(arguments.getLongs("param").size(), is(1));
        assertThat(arguments.getLongs("\\s", new String[]{"param"}).size(), is(2));
        assertThat(arguments.getDouble("param"), is(equalTo(Optional.of(4254D))));
        assertThat(arguments.getDouble("\\s", 0, "param"), is(equalTo(Optional.of(42D))));
        assertThat(arguments.getDouble("\\s", 1, "param"), is(equalTo(Optional.of(54D))));
        assertThat(arguments.getDoubles("param").size(), is(1));
        assertThat(arguments.getDoubles("\\s", new String[]{"param"}).size(), is(2));
        assertThat(arguments.getBoolean("param"), is(false));
        assertThat(arguments.getBooleans("param").size(), is(1));
    }

    @Test
    @SuppressWarnings("all")
    void coverage() {
        assertThat(arguments.equals(arguments), is(true));
        assertThat(arguments.equals(parseArgs("v1=true")), is(false));
        assertThat(arguments.equals(1), is(false));
        assertThat(arguments.hashCode(), is(notNullValue()));

    }

    @SuppressWarnings("all")
    private int count(final String text, final String key) {
        int result = 0;
        int lastIndex = 0;

        while (text != null && (lastIndex = text.indexOf(key, lastIndex)) != -1) {
            result++;
            lastIndex++;
        }
        return result;
    }

}
