package process.parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import process.entities.BookElementEntity;
import process.parser.dto.html.HtmlElement;
import process.parser.dto.html.TagElement;

public class BookElementSplitter {
    private HtmlParser parser;
    private HtmlRenderer renderer;

    public BookElementSplitter() {
        parser = new HtmlParser();
        renderer = new HtmlRenderer();
    }

    public List<BookElementEntity> split(String content, BookElementEntity rootEntity) throws ParseException {
        List<HtmlElement> elements = parser.parse(content);

        if (elements.size() != 1 || !(elements.get(0) instanceof TagElement)) {
            throw new ParseException("Text must contain only one root XML-Element", 0);
        }

        TagElement rootElement = (TagElement) elements.get(0);
        if (!"chapter".equals(rootElement.getName())) {
            throw new ParseException("Root Element must have name \"chapter\"", 0);
        }

        fillEntity(rootEntity, rootElement);

        List<BookElementEntity> entities = new ArrayList<>();
        splitRecursively(rootElement.getContent(), rootEntity, entities);
        return entities;
    }

    private void splitRecursively(List<HtmlElement> content, BookElementEntity parentEntity, List<BookElementEntity> entities) {
        for (HtmlElement element : content) {
            if (element == null || !(element instanceof TagElement)) {
                continue;
            }

            TagElement tagElement = (TagElement) element;
            if (!"chapter".equals(tagElement.getName()) && !"verse".equals(tagElement.getName())) {
                continue;
            }

            BookElementEntity entity = new BookElementEntity();
            entity.setParent(parentEntity);
            parentEntity.addChild(entity);
            entities.add(entity);
            fillEntity(entity, tagElement);

            if ("chapter".equals(tagElement.getName())) {
                splitRecursively(tagElement.getContent(), entity, entities);
            }
        }
    }

    private void fillEntity(BookElementEntity entity, TagElement chapterTag) {
        if (entity.getTitle() == null) {
            String title = chapterTag.getContentValue("title");
            if (title == null) {
                title = "No Title";
            }
            entity.setTitle(title);
        }

        TagElement parsedTag = copyTagElements(chapterTag);
        String content = renderer.render(Collections.singletonList(parsedTag), true);
        entity.setContent(content);
    }

    private TagElement copyTagElements(TagElement tagElement) {
        TagElement parsedTag = new TagElement();
        parsedTag.setName(tagElement.getName());
        for (HtmlElement childContentElement : tagElement.getContent()) {
            if (childContentElement != null && childContentElement instanceof TagElement) {
                TagElement childTag = (TagElement) childContentElement;
                if (!"chapter".equals(childTag.getName()) && !"verse".equals(childTag.getName())) {
                    parsedTag.getContent().add(childTag);
                }
            }
        }
        return parsedTag;
    }
}
