package process.parser;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import process.parser.dto.Chain;
import process.parser.dto.Command;
import process.parser.dto.HtmlElement;
import process.parser.dto.Variable;

public class RawBookParser {
    private HtmlParser htmlParser;
    private CommandParser commandParser;
    private Transformator transformator;

    public RawBookParser() {
        htmlParser = new HtmlParser();
        commandParser = new CommandParser();
        transformator = new Transformator();
    }

    public String parse(String sourceText, String transformationText) throws ParseException {
        // Parse HTML Source Text
        Chain<HtmlElement> parsedHtmlElements = htmlParser.parse(sourceText);

        // Parse Transformation Commands
        List<Command> transformationCommands = commandParser.parseCommands(transformationText);
        Map<String, Variable> definedVariables = commandParser.parseVariables(transformationText);

        // Transform the Source Text
        transformator.transform(parsedHtmlElements, transformationCommands, definedVariables);
        //transformElements(parsedHtmlElements, transformationCommands);

        // TODO Rewrite, better with tree-based HTMLElements
        // Actually combining back HTML Elements, need to replace this stub
        StringBuilder parsedText = new StringBuilder();
                for (HtmlElement element : parsedHtmlElements) {
                    String renderedHtmlTag = renderHtmlTag(element);
                    parsedText.append(renderedHtmlTag).append("\n");
                }

        return parsedText.toString();
    }

    private String renderHtmlTag(HtmlElement element) {
        StringBuilder sb = new StringBuilder();

        sb.append("<").append(element.getTagName()).append(">");

        sb.append(element.getContent());

        sb.append("</").append(element.getTagName()).append(">");

        return sb.toString();
    }
}
