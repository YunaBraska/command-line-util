package berlin.yuna.clu.logic;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CommandLineReaderTest {

    private final String input = " myCommand1    myCommand2 --help  -v=\"true\" --verbose=\"true\"   -DArgs=\"true\" -param 42   54   -DArgList=\"item 1\" --DArgList=\"item 2\" --DArgList=\"-item 3\"  ";

    @Test
    public void parse_withCommands_shouldBeSuccessful() {
        final CommandLineReader cmdLines = new CommandLineReader(input);

        assertThat(cmdLines.getCommand(), is(notNullValue()));
        assertThat(cmdLines.getCommand(), is(equalTo("myCommand1")));
        assertThat(cmdLines.getCommand(0), is(equalTo("myCommand1")));
        assertThat(cmdLines.getCommand(1), is(equalTo("myCommand2")));
        assertThat(cmdLines.getCommand(2), is(nullValue()));
    }

    @Test
    public void parse_withOutCommands_shouldBeSuccessful() {
        final CommandLineReader cmdLines = new CommandLineReader("--help -v -DArgs=\"true\" param 42 54");

        assertThat(cmdLines.getCommand(), is(nullValue()));
        assertThat(cmdLines.getCommand(0), is(nullValue()));
    }

    @Test
    public void parse_arguments_shouldBeSuccessful() {
        final CommandLineReader cmdLines = new CommandLineReader(input);

        assertThat(cmdLines.size(), is(6));
        assertThat(cmdLines.isPresent("notPresentKey"), is(false));
        assertThat(cmdLines.isPresent("help"), is(true));
        assertThat(cmdLines.isPresent("hElp"), is(true));
        assertThat(cmdLines.get("hElp").size(), is(1));
        assertThat(cmdLines.getValue("hElp"), is(nullValue()));
        assertThat(cmdLines.getValue(1, "hElp"), is(nullValue()));

        assertThat(cmdLines.isPresent("v"), is(true));
        assertThat(cmdLines.isPresent("verbose"), is(true));
        assertThat(cmdLines.getValue("v", "verbose"), is(equalTo("true")));
        assertThat(cmdLines.getValue(0, "v", "verbose"), is(equalTo("true")));
        assertThat(cmdLines.getValue(1, "v", "verbose"), is(nullValue()));
        assertThat(cmdLines.getValues("v", "verbose").size(), is(1));

        assertThat(cmdLines.getValues("DArgList").size(), is(3));
        assertThat(cmdLines.getValues("DArgList"), hasItem("item 1"));
        assertThat(cmdLines.getValues("DArgList"), hasItem("item 2"));
        assertThat(cmdLines.getValues("DArgList"), hasItem("-item 3"));
        assertThat(cmdLines.getValue(3, "DArgList"), is(nullValue()));

        assertThat(cmdLines.getValue("param"), is(notNullValue()));
        assertThat(cmdLines.getValue("param"), is(equalTo("42   54")));
    }

}
