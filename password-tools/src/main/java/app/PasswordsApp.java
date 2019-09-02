package app;

import app.context.ApplicationContext;
import app.context.ApplicationParameters;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PasswordsApp extends Application {
    private static final String APPLICATION_CONTEXT_CONFIG_FILENAME = "password-tools-config.json";
    private static final String PASSWORDS_FILENAME = "passwords.json";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Application Context
        ApplicationContext applicationContext = new ApplicationContext(APPLICATION_CONTEXT_CONFIG_FILENAME);
        applicationContext.setStage(primaryStage);
        applicationContext.setParameterValue(ApplicationParameters.PasswordsFileName, PASSWORDS_FILENAME);

        // Graph Form
        PasswordsForm graphForm = new PasswordsForm();
        Parent root = graphForm.init(applicationContext);

        // Main Scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Passwords Tool");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
