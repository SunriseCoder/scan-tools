package process.forms.editor;

import java.io.IOException;

import javafx.scene.Node;
import javafx.scene.Parent;
import process.context.ApplicationContext;
import utils.FileUtils;

public class ViewForm {
    public Node createUI(ApplicationContext applicationContext) throws IOException {
        Parent root = FileUtils.loadFXML(this);

        return root;
    }
}
