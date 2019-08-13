package process.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity(name = "book_elements")
public class BookElementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private BookElementEntity parent;

    private String title;

    @Column(columnDefinition = "longtext")
    private String source;

    @Column(columnDefinition = "longtext")
    private String content;

    @ManyToOne
    private LanguageEntity language;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private List<BookElementEntity> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookElementEntity getParent() {
        return parent;
    }

    public void setParent(BookElementEntity parent) {
        this.parent = parent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LanguageEntity getLanguage() {
        return language;
    }

    public void setLanguage(LanguageEntity language) {
        this.language = language;
    }

    public List<BookElementEntity> getChildren() {
        return children;
    }

    public void setChildren(List<BookElementEntity> children) {
        this.children = children;
    }
}
