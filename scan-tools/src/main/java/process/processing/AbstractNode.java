package process.processing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

public abstract class AbstractNode {

    protected <T> void initComboBox(ComboBox<T> comboBox, Class<? extends ListCell<T>> cl, T[] values) throws Exception {
        comboBox.setCellFactory((o) -> {
            try {
                return cl.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        comboBox.setButtonCell(cl.newInstance());

        ObservableList<T> imageCropItems = FXCollections.observableArrayList(values);
        comboBox.setItems(imageCropItems);
        comboBox.getSelectionModel().selectFirst();
    }
}
