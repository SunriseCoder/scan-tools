package process.parser.dto.html;

import java.util.List;

public interface HtmlElement {
    TagElement getParentTag();
    void setParentTag(TagElement parentTag);

    List<HtmlElement> getParentContent();
    void setParentContent(List<HtmlElement> parentContent);
}
