package com.foodordering;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Main entry point for the Online Food Ordering System JavaFX application.
 *
 * <p>This class bootstraps JavaFX, loads the Login FXML screen, and
 * applies the global CSS stylesheet.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlUrl = getClass().getResource("/fxml/Login.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /fxml/Login.fxml on the classpath.");
        }
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root, 1100, 720);

        URL cssUrl = getClass().getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("Online Food Ordering System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        // Run auto-database check in background thread so UI launch is instantaneous
        new Thread(com.foodordering.database.DatabaseInitializer::initializeIfNeeded).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
