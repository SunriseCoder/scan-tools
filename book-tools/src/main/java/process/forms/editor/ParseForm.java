package process.forms.editor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import process.context.ApplicationContext;
import process.context.ApplicationEvents;
import process.entities.BookElementEntity;
import process.services.BookElementService;
import utils.FileUtils;

@Component
public class ParseForm {
    @Autowired
    private BookElementService bookElementService;

    private BookElementEntity currentBookElement;

    @FXML
    private SplitPane parseSplitPane;

    @FXML
    private Button transformButton;
    @FXML
    private Button saveAsCurrentButton;
    @FXML
    private Button saveAsChildButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button deleteButton;

    @FXML
    private TextArea sourceField;
    @FXML
    private TextArea transformationField;
    @FXML
    private TextArea transformedField;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentField;

    private int counter;

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        applicationContext.addEventListener(ApplicationEvents.CurrentBookElementChanged,
                value -> currentBookElementChanged(value));

        Parent root = FileUtils.loadFXML(this);

        return root;
    }

    private void currentBookElementChanged(Object value) {
        if (value == null) {
            return;
        }

        currentBookElement = (BookElementEntity) value;

        titleField.setText(currentBookElement.getTitle());
        sourceField.setText(currentBookElement.getSource());
        contentField.setText(currentBookElement.getContent());
    }

    @FXML
    private void handleSaveAsCurrent() {
        if (currentBookElement == null) {
            handleSaveAsChild();
        } else {
            saveBookElement(currentBookElement);
        }
    }

    @FXML
    private void handleSaveAsChild() {
        BookElementEntity child = new BookElementEntity();
        child.setParent(currentBookElement);
        saveBookElement(child);
    }

    private void saveBookElement(BookElementEntity bookElementEntity) {
        bookElementEntity.setTitle(titleField.getText());
        bookElementEntity.setSource(sourceField.getText());
        bookElementEntity.setContent(contentField.getText());

        BookElementEntity savedEntity = bookElementService.save(bookElementEntity);
    }
}