package process.forms.editor;

import java.io.IOException;

import javafx.scene.Node;
import javafx.scene.Parent;
import process.context.ApplicationContext;
import utils.FileUtils;

public class ViewForm {
    private ApplicationContext applicationContext;

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;

        Parent root = FileUtils.loadFXML(this);

        return root;
    }
}
