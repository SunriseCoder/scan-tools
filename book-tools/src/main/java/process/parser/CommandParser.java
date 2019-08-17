package process.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import process.parser.dto.Action;
import process.parser.dto.Command;
import process.parser.dto.Command.Condition;
import process.parser.dto.Command.Positions;
import process.parser.dto.Variable;
import process.parser.dto.html.TagAttribute;

public class CommandParser {

    public List<Command> parseCommands(String transformationText) {

        String[] transformationParts = transformationText.split("\n");

        List<Command> commands = Arrays.stream(transformationParts).map(part -> parseCommand(part))
                .collect(Collectors.toList());

        return commands;
    }

    private Command parseCommand(String commandText) {
        if (commandText == null || commandText.trim().isEmpty() || commandText.trim().startsWith("#")
                || commandText.trim().startsWith("define ")) {
            return null;
        }

        Command command = new Command();

        String[] commandParts = commandText.split("->");
        String conditionPart = commandParts[0].trim();
        String actionsPart = commandParts[1].trim();

        parseConditions(conditionPart, command);

        List<Action> actions = parseActions(actionsPart);
        command.setActions(actions);

        return command;
    }

    private void parseConditions(String conditionsText, Command command) {
        Condition condition = null;
        CommandModes mode = CommandModes.Free;
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < conditionsText.length(); i++) {
            String symbol = conditionsText.substring(i, i + 1);

            switch (mode) {

            case Free:
                if ("{".equals(symbol)) {
                    condition = new Condition();

                    String positionText = buffer.toString().trim();
                    Positions position = parsePosition(positionText);
                    condition.setPosition(position);

                    mode = CommandModes.ConditionText;
                    buffer = new StringBuilder();
                } else if (",".equals(symbol)) {
                    continue;
                } else {
                    buffer.append(symbol);
                }
                break;

            case ConditionText:
                if ("}".equals(symbol)) {
                    String conditionText = buffer.toString().trim();
                    parseCondition(conditionText, condition);
                    command.getConditions().put(condition.getPosition(), condition);

                    mode = CommandModes.Free;
                    buffer = new StringBuilder();
                } else {
                    buffer.append(symbol);
                }

            default:
                break;
            }
        }
    }

    private Positions parsePosition(String positionText) {
        switch (positionText) {

        case "prev":
            return Positions.Previous;

        case "curr":
            return Positions.Current;

        case "next":
            return Positions.Next;

        default:
            throw new IllegalArgumentException("Unknown position type \"" + positionText + "\"");
        }
    }

    private void parseCondition(String conditionText, Condition condition) {
        String[] conditionParts = conditionText.split(" ");
        String tagName = conditionParts[0];
        condition.setTagName(tagName);

        if (conditionParts.length > 1) {
            for (int i = 1; i < conditionParts.length; i++) {
                String tagAttributes = conditionParts[i];
                TagAttribute tagAttribute = parseAttribute(tagAttributes);
                condition.getAttributes().put(tagAttribute.getName(), tagAttribute);
            }
        }
    }

    private TagAttribute parseAttribute(String tagAttributeText) {
        TagAttribute attribute = new TagAttribute();

        String[] attributeParts = tagAttributeText.split("=");

        String attributeName = attributeParts[0];
        attribute.setName(attributeName);

        String attributeValue = attributeParts[1];
        if (attributeValue.length() >= 2 && attributeValue.startsWith("\"") && attributeValue.endsWith("\"")) {
            attributeValue = attributeValue.substring(1, attributeValue.length() - 1);
        }
        attribute.setValue(attributeValue);

        return attribute;
    }

    private List<Action> parseActions(String actionsText) {
        List<Action> actions = new ArrayList<>();

        for (String actionText : actionsText.split(";")) {
            Action action = parseAction(actionText);
            actions.add(action);
        }

        return actions;
    }

    private Action parseAction(String actionText) {
        Action action = new Action();

        String[] actionParts = actionText.split("\\(");
        String actionName = actionParts[0];
        action.setName(actionName);

        if (actionParts.length > 1) {
            String actionArguments = actionParts[1];
            // Removing closing Parenthesis
            actionArguments = actionArguments.substring(0, actionArguments.length() - 1);
            action.setArguments(actionArguments);
        }

        return action;
    }

    public Map<String, Variable> parseVariables(String transformationText) {
        Map<String, Variable> variables = new HashMap<>();

        String[] transformationParts = transformationText.split("\n");

        Arrays.stream(transformationParts).forEach(part -> parseVariable(part, variables));

        return variables;
    }

    private void parseVariable(String commandText, Map<String, Variable> variables) {
        if (commandText == null || !commandText.trim().startsWith("define ")) {
            return;
        }

        // Cutting off starting spaces and keyword "define " (with space)
        commandText = commandText.trim().substring(7).trim();

        String variableName = null, variableTypeText = null, variableValueText = null;

        VariableModes mode = VariableModes.VariableName;
        StringBuilder buffer = new StringBuilder();
        command: for (int i = 0; i < commandText.length(); i++) {
            String symbol = commandText.substring(i, i + 1);

            switch (mode) {

            case VariableName:
                if (" ".equals(symbol)) {
                    variableName = buffer.toString();

                    mode = VariableModes.LookingForAssignment;
                    buffer = new StringBuilder();
                } else if ("=".equals(symbol)) {
                    variableName = buffer.toString();

                    mode = VariableModes.LookingForVariableType;
                    buffer = new StringBuilder();
                } else {
                    buffer.append(symbol);
                }

                break;

            case LookingForAssignment:
                if ("=".equals(symbol)) {
                    mode = VariableModes.LookingForVariableType;
                }

                break;

            case LookingForVariableType:
                if ("[".equals(symbol)) {
                    variableTypeText = buffer.toString();

                    mode = VariableModes.ParseArrayValue;
                    buffer = new StringBuilder();
                } else if (" ".equals(symbol)) {
                    continue;
                } else {
                    buffer.append(symbol);
                }
                break;

            case ParseArrayValue:
                if ("]".equals(symbol)) {
                    variableValueText = buffer.toString();
                    break command;
                } else {
                    buffer.append(symbol);
                }
                break;

            default:
                break;
            }
        }

        Variable variable = new Variable();
        variable.setName(variableName);
        variables.put(variableName, variable);

        switch (variableTypeText) {

        case "StringArray":
            variable.setType(Variable.Types.StringArray);
            parseStringArrayValue(variableValueText, variable);
            break;

        default:
            break;
        }
    }

    private void parseStringArrayValue(String variableValueText, Variable variable) {
        List<String> values = new ArrayList<>();

        StringBuilder buffer = new StringBuilder();
        StringArrayModes mode = StringArrayModes.LookingForStartQuote;
        for (int i = 0; i < variableValueText.length(); i++) {
            String symbol = variableValueText.substring(i, i + 1);

            switch (mode) {

            case LookingForStartQuote:
                if ("\"".equals(symbol)) {
                    mode = StringArrayModes.ParseVariableValue;
                }
                break;

            case ParseVariableValue:
                if ("\\".equals(symbol)) {
                    mode = StringArrayModes.ParseEscapedSymbol;
                } else if ("\"".equals(symbol)) {
                    String value = buffer.toString();
                    values.add(value);

                    mode = StringArrayModes.LookingForStartQuote;
                    buffer = new StringBuilder();
                } else {
                    buffer.append(symbol);
                }
                break;

            case ParseEscapedSymbol:
                buffer.append(symbol);
                mode = StringArrayModes.ParseVariableValue;
                break;

            default:
                break;
            }
        }

        if (mode.equals(StringArrayModes.ParseVariableValue) || mode.equals(StringArrayModes.ParseEscapedSymbol)) {
            throw new IllegalArgumentException("Error due to Define Variable (StringArray) \"" + variable
                    + "\", there is no closing doublequote symbol");
        }

        String[] variableValue = values.toArray(new String[values.size()]);
        variable.setValue(variableValue);
    }

    private enum CommandModes {
        Free, ConditionText,
    }

    private enum VariableModes {
        VariableName, LookingForAssignment, LookingForVariableType, ParseArrayValue
    }

    private enum StringArrayModes {
        LookingForStartQuote, ParseVariableValue, ParseEscapedSymbol
    }
}
