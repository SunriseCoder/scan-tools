package process.parser;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import process.parser.dto.Chain;
import process.parser.dto.Command;
import process.parser.dto.Variable;
import process.parser.dto.html.HtmlElement;

public class RawBookParser {
    private HtmlParser htmlParser;
    private CommandParser commandParser;
    private Transformator transformator;
    private HtmlRenderer renderer;

    public RawBookParser() {
        htmlParser = new HtmlParser();
        commandParser = new CommandParser();
        transformator = new Transformator();
        renderer = new HtmlRenderer();
    }

    public String parse(String sourceText, String transformationText) throws ParseException, IOException {
        // Parse HTML Source Text
        List<HtmlElement> parsedHtmlElements = htmlParser.parse(sourceText);
        Chain<HtmlElement> parsedChain = htmlParser.toChain(parsedHtmlElements);

        // Parse Transformation Commands
        List<Command> transformationCommands = commandParser.parseCommands(transformationText);
        Map<String, Variable> definedVariables = commandParser.parseVariables(transformationText);

        // Transform the Source Text
        List<HtmlElement> transformationResult = transformator.transform(parsedChain, transformationCommands, definedVariables);

        // Rendering Transformation Result
        String renderedText = renderer.render(transformationResult, true);

        return renderedText.toString();
    }
}
