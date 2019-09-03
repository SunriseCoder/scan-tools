package process.parser.dto.html;

import java.util.List;

public abstract class AbstractHtmlElement implements HtmlElement {
    private TagElement parentTag;
    private List<HtmlElement> parentContent;

    @Override
    public TagElement getParentTag() {
        return parentTag;
    }

    @Override
    public void setParentTag(TagElement parentTag) {
        this.parentTag = parentTag;
    }

    @Override
    public List<HtmlElement> getParentContent() {
        return parentContent;
    }

    @Override
    public void setParentContent(List<HtmlElement> parentContent) {
        this.parentContent = parentContent;
    }
}
