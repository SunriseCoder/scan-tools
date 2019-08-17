package process.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import process.parser.dto.Action;
import process.parser.dto.Command;
import process.parser.dto.Command.Condition;
import process.parser.dto.Command.Positions;
import process.parser.dto.TagAttribute;
import process.parser.dto.Variable;

public class CommandParserTest {
    private CommandParser commandParser;

    @Before
    public void beforeTest() {
        commandParser = new CommandParser();
    }

    @Test
    public void testIgnoreEmptyLine() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{a}->stub\n\ncurr{b}->stub");

        assertEquals(3, parsedCommands.size());
        assertEquals("a", parsedCommands.get(0).getCondition(Positions.Current).getTagName());
        assertNull(parsedCommands.get(1));
        assertEquals("b", parsedCommands.get(2).getCondition(Positions.Current).getTagName());
    }

    @Test
    public void testIgnoreCommentLine() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{a}->stub\n# Comment\ncurr{b}->stub");

        assertEquals(3, parsedCommands.size());
        assertEquals("a", parsedCommands.get(0).getCondition(Positions.Current).getTagName());
        assertNull(parsedCommands.get(1));
        assertEquals("b", parsedCommands.get(2).getCondition(Positions.Current).getTagName());
    }

    @Test
    public void testBeginningAndTrailingSpaces() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{a}->stub\n\n \n # Comment\n curr { b } -> stub ");

        assertEquals(5, parsedCommands.size());
        assertEquals("a", parsedCommands.get(0).getCondition(Positions.Current).getTagName());
        assertNull(parsedCommands.get(1));
        assertNull(parsedCommands.get(2));
        assertNull(parsedCommands.get(3));
        assertEquals("b", parsedCommands.get(4).getCondition(Positions.Current).getTagName());
    }

    @Test
    public void testSimpleCommand() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{span}->stub");
        assertEquals(1, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(1, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Current);
        assertEquals("span", condition1.getTagName());
        assertEquals(0, condition1.getAttributes().size());

        assertEquals(1, command1.getActions().size());
        assertEquals("stub", command1.getActions().get(0).getName());
        assertNull(command1.getActions().get(0).getArguments());
    }

    @Test
    public void testCommandWithSingleAttribute() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{span font=\"15a\"}->stub");
        assertEquals(1, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(1, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Current);
        assertEquals("span", condition1.getTagName());

        assertEquals(1, condition1.getAttributes().size());
        TagAttribute attribute1 = condition1.getAttributes().values().iterator().next();
        assertEquals("font", attribute1.getName());
        assertEquals("15a", attribute1.getValue());

        assertEquals(1, command1.getActions().size());
        assertEquals("stub", command1.getActions().get(0).getName());
        assertNull(command1.getActions().get(0).getArguments());
    }

    @Test
    public void testCommandWithMultipleAttributes() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{span font=\"15a\" style=\"bold\"}->stub");
        assertEquals(1, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(1, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Current);
        assertEquals("span", condition1.getTagName());

        assertEquals(2, condition1.getAttributes().size());

        TagAttribute attribute1 = condition1.getAttributes().get("font");
        assertEquals("font", attribute1.getName());
        assertEquals("15a", attribute1.getValue());

        TagAttribute attribute2 = condition1.getAttributes().get("style");
        assertEquals("style", attribute2.getName());
        assertEquals("bold", attribute2.getValue());

        assertEquals(1, command1.getActions().size());
        assertEquals("stub", command1.getActions().get(0).getName());
        assertNull(command1.getActions().get(0).getArguments());
    }

    @Test
    public void testCommandWithMultipleConditions() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{span font=\"15a\" style=\"bold\"},next{span}->stub");
        assertEquals(1, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(2, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Current);
        assertEquals("span", condition1.getTagName());

        assertEquals(2, condition1.getAttributes().size());

        TagAttribute attribute1 = condition1.getAttributes().get("font");
        assertEquals("font", attribute1.getName());
        assertEquals("15a", attribute1.getValue());

        TagAttribute attribute2 = condition1.getAttributes().get("style");
        assertEquals("style", attribute2.getName());
        assertEquals("bold", attribute2.getValue());

        Condition condition2 = command1.getCondition(Positions.Next);
        assertEquals("span", condition2.getTagName());
        assertEquals(0, condition2.getAttributes().size());

        assertEquals(1, command1.getActions().size());
        assertEquals("stub", command1.getActions().get(0).getName());
        assertNull("stub", command1.getActions().get(0).getArguments());
    }

    @Test
    public void testCommandWithMultipleActions() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{span}->stub1;stub2");
        assertEquals(1, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(1, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Current);
        assertEquals("span", condition1.getTagName());

        assertEquals(0, condition1.getAttributes().size());

        List<Action> actions = command1.getActions();
        assertEquals(2, actions.size());
        assertEquals("stub1", actions.get(0).getName());
        assertNull(actions.get(0).getArguments());
        assertEquals("stub2", actions.get(1).getName());
        assertNull(actions.get(1).getArguments());
    }

    @Test
    public void testCommandWithMultipleCommands() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{span}->stub1\ncurr{a}->stub2-1;stub2-2");
        assertEquals(2, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(1, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Current);
        assertEquals("span", condition1.getTagName());

        assertEquals(0, condition1.getAttributes().size());

        assertEquals(1, command1.getActions().size());
        assertEquals("stub1", command1.getActions().get(0).getName());
        assertNull(command1.getActions().get(0).getArguments());

        Command command2 = parsedCommands.get(1);
        assertEquals(1, command2.getConditions().size());

        Condition condition2 = command2.getCondition(Positions.Current);
        assertEquals("a", condition2.getTagName());

        assertEquals(0, condition2.getAttributes().size());

        assertEquals(2, command2.getActions().size());
        assertEquals("stub2-1", command2.getActions().get(0).getName());
        assertNull(command2.getActions().get(0).getArguments());
        assertEquals("stub2-2", command2.getActions().get(1).getName());
        assertNull(command2.getActions().get(1).getArguments());
    }

    @Test
    public void testCommandWithAllPositions() {
        List<Command> parsedCommands = commandParser.parseCommands("prev{a},curr{b},next{c}->stub");
        assertEquals(1, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(3, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Previous);
        assertEquals("a", condition1.getTagName());
        assertEquals(0, condition1.getAttributes().size());

        Condition condition2 = command1.getCondition(Positions.Current);
        assertEquals("b", condition2.getTagName());
        assertEquals(0, condition2.getAttributes().size());

        Condition condition3 = command1.getCondition(Positions.Next);
        assertEquals("c", condition3.getTagName());
        assertEquals(0, condition3.getAttributes().size());

        assertEquals(1, command1.getActions().size());
        assertEquals("stub", command1.getActions().get(0).getName());
        assertNull(command1.getActions().get(0).getArguments());
    }

    @Test
    public void testCommandWithOneActionArgument() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{span}->stub(1)");
        assertEquals(1, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(1, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Current);
        assertEquals("span", condition1.getTagName());
        assertEquals(0, condition1.getAttributes().size());

        assertEquals(1, command1.getActions().size());
        assertEquals("stub", command1.getActions().get(0).getName());
        assertEquals("1", command1.getActions().get(0).getArguments());
    }

    @Test
    public void testCommandWithMultipleActionArguments() {
        List<Command> parsedCommands = commandParser.parseCommands("curr{span}->stub(1, \"hello\", 3)");
        assertEquals(1, parsedCommands.size());

        Command command1 = parsedCommands.get(0);
        assertEquals(1, command1.getConditions().size());

        Condition condition1 = command1.getCondition(Positions.Current);
        assertEquals("span", condition1.getTagName());
        assertEquals(0, condition1.getAttributes().size());

        assertEquals(1, command1.getActions().size());
        assertEquals("stub", command1.getActions().get(0).getName());
        assertEquals("1, \"hello\", 3", command1.getActions().get(0).getArguments());
    }

    @Test
    public void testDefineVariableStringArray() {
        Map<String, Variable> variables = commandParser.parseVariables("define a = StringArray[\"a\" , \"b\",\"c\"]");

        assertEquals(1, variables.size());
        assertArrayEquals(new String[] {"a", "b", "c"}, (String[]) variables.get("a").getValue());
    }
}
