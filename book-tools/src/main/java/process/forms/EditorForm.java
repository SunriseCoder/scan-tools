package process.forms;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import process.context.ApplicationContext;
import process.forms.editor.EditForm;
import process.forms.editor.ParseForm;
import process.forms.editor.ViewForm;
import utils.FileUtils;

public class EditorForm {
    private ApplicationContext applicationContext;

    @FXML
    private Tab viewTab;
    @FXML
    private Tab editTab;
    @FXML
    private Tab parseTab;

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;

        Parent root = FileUtils.loadFXML(this);

        ViewForm viewForm = new ViewForm();
        Node viewFormNode = viewForm.createUI(applicationContext);
        viewTab.setContent(viewFormNode);

        EditForm editForm = new EditForm();
        Node editFormNode = editForm.createUI(applicationContext);
        editTab.setContent(editFormNode);

        ParseForm parseForm = new ParseForm();
        Node parseFormNode = parseForm.createUI(applicationContext);
        parseTab.setContent(parseFormNode);

        return root;
    }
}
