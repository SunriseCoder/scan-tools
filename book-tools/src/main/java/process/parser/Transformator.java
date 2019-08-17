package process.parser;

import java.util.List;
import java.util.Map;

import process.parser.dto.Action;
import process.parser.dto.Chain;
import process.parser.dto.Command;
import process.parser.dto.Command.Condition;
import process.parser.dto.Command.Positions;
import process.parser.dto.HtmlElement;
import process.parser.dto.TagAttribute;
import process.parser.dto.Variable;

public class Transformator {

    public void transform(Chain<HtmlElement> htmlElements, List<Command> commands, Map<String, Variable> definedVariables) {
        Chain<HtmlElement> currentChain = htmlElements;

        while (currentChain != null) {
            Chain<HtmlElement> previousChain = currentChain.getPreviousChain();
            Chain<HtmlElement> nextChain = currentChain.getNextChain();

            HtmlElement previousElement = previousChain == null ? null : previousChain.getValue();
            HtmlElement currentElement = currentChain.getValue();
            String currentContent = currentElement.getContent();

            String arguments;
            commands: for (Command command : commands) {
                // Null Commands can be the result of Empty Lines or Comments
                if (command == null) {
                    continue;
                }

                // Checking that Command Matches current Element
                if (!isElementMatches(currentChain, command)) {
                    continue;
                }

                actions: for (Action action : command.getActions()) {
                    switch (action.getName()) {

                    // Change Element TagName to an Argument
                    case "changeTagTo":
                        currentElement.setTagName(action.getArguments());
                        break;

                    // ConcatToPrevious
                    case "concatToPrevious":
                        // If NO previous Element
                        if (previousChain == null) {
                            String message = "Cannot perform command \"" + command + "\" on element \"" + currentElement
                                    + "\", because it is first element, and action is \"" + action + "\"";
                            throw new IllegalArgumentException(message);
                        }

                        // Concatenating Content
                        String concatedContent = previousElement.getContent() + currentElement.getContent();
                        previousElement.setContent(concatedContent);

                        // Deleting Chain and Linking the parts of the Chain
                        previousChain.linkNext(nextChain);
                        currentChain = nextChain;
                        break commands;

                    // Delete Current Element
                    case "delete":
                        // If NOT First Element
                        if (previousChain != null) {
                            previousChain.linkNext(nextChain);
                            // Otherwise If First, but NOT Last
                        } else if (nextChain != null) {
                            currentChain = nextChain;
                            currentChain.setPreviousChain(null);
                            // Otherwise If First AND Last
                        } else {
                            currentChain = null;
                        }
                        break commands;

                    case "insertAtStart":
                        arguments = action.getArguments();
                        currentContent = arguments + currentContent;
                        currentElement.setContent(currentContent);
                        break;

                    case "insertAtEnd":
                        arguments = action.getArguments();
                        currentContent = currentContent + arguments;
                        currentElement.setContent(currentContent);
                        break;

                    // Replace BR Tag With Space
                    case "replaceBrTagWithSpace":
                        currentContent = currentContent.replaceAll("<br>", " ");
                        currentElement.setContent(currentContent);
                        break;

                     // Replace BR Tag With New Line
                    case "replaceBrTagWithNewLine":
                        currentContent = currentContent.replaceAll("<br>", "\n");
                        currentElement.setContent(currentContent);
                        break;

                    // Replace Substrings (need to define variable and put as first argument,
                    //  like:
                    //      define table = StringArray["a", "b"]
                    //      curr{span}->replaceSubstrings(table)
                    case "replaceSubstrings":
                        arguments = action.getArguments();
                        Variable definedVariable = definedVariables.get(arguments);
                        if (definedVariable == null) {
                            throw new IllegalArgumentException("Variable \"" + arguments + "\" is not defined");
                        }
                        String[] replacementTable = (String[]) definedVariable.getValue();

                        currentContent = replaceSubstrings(currentContent, replacementTable);
                        currentElement.setContent(currentContent);
                        break;

                    // ToLowerCase
                    case "toLowerCase":
                        currentContent = currentContent.toLowerCase();
                        currentElement.setContent(currentContent);
                        break;

                    default:
                        throw new IllegalArgumentException("Action \"" + action + "\" is not supported");
                    }
                }
            }

            currentChain = nextChain;
        }
    }

    protected boolean isElementMatches(Chain<HtmlElement> chain, Command command) {
        boolean matches = true;

        if (command.getCondition(Positions.Previous) != null) {
            matches &= isElementMatches(chain.getPreviousChain(), command.getCondition(Positions.Previous));
        }

        if (command.getCondition(Positions.Current) != null) {
            matches &= isElementMatches(chain, command.getCondition(Positions.Current));
        }

        if (command.getCondition(Positions.Next) != null) {
            matches &= isElementMatches(chain.getNextChain(), command.getCondition(Positions.Next));
        }

        return matches;
    }

    private boolean isElementMatches(Chain<HtmlElement> chain, Condition condition) {
        boolean matches = true;

        if (chain == null || chain.getValue() == null) {
            return false;
        }

        HtmlElement element = chain.getValue();

        if (!condition.getTagName().equals(element.getTagName())) {
            matches = false;
        }

        // If Command doesn't contains any Attributes
        if (condition.getAttributes() == null && !element.getAttributes().isEmpty()) {
            matches = false;
        } else {
            // If command contains Attributes and any of them are not in Element
            for (TagAttribute commandAttribute : condition.getAttributes().values()) {
                if (element.containsAttribute(commandAttribute.getName())) {
                    String elementAttributeValue = element.getAttributeValue(commandAttribute.getName());
                    String commandAttributeValue = commandAttribute.getValue();
                    if (commandAttributeValue == null && elementAttributeValue != null) {
                        matches = false;
                    }
                    if (commandAttribute != null && !commandAttributeValue.equals(elementAttributeValue)) {
                        matches = false;
                    }
                } else {
                    matches = false;
                }
            }
            for (TagAttribute elementAttribute : element.getAttributes().values()) {
                if (condition.containsAttribute(elementAttribute.getName())) {
                    String commandAttributeValue = condition.getAttributeValue(elementAttribute.getName());
                    String elementAttributeValue = elementAttribute.getValue();
                    if (elementAttributeValue == null && commandAttributeValue != null) {
                        matches = false;
                    }
                    if (elementAttribute != null && !elementAttributeValue.equals(commandAttributeValue)) {
                        matches = false;
                    }
                } else {
                    matches = false;
                }
            }
        }

        return matches;
    }

    private String replaceSubstrings(String currentContent, String[] replacementTable) {
        String replacedString = currentContent;
        for (int i = 0; i < replacementTable.length; i += 2) {
            String regex = replacementTable[i];
            String replacement = replacementTable[i + 1];
            replacedString = replacedString.replaceAll(regex, replacement);
        }
        return replacedString;
    }
}
