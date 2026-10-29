package com.foodordering.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Helper for switching JavaFX scenes.
 */
public final class NavigationUtil {

    private NavigationUtil() {}

    /**
     * Loads an FXML file and switches the primary stage to show it.
     *
     * @param stage    the stage to update.
     * @param fxmlPath the classpath path, e.g. "/fxml/Menu.fxml".
     */
    public static void navigateTo(Stage stage, String fxmlPath) throws IOException {
        if (stage == null) {
            stage = (Stage) javafx.stage.Window.getWindows().stream()
                .filter(w -> w instanceof Stage && w.isShowing())
                .findFirst()
                .orElse(null);
        }
        if (stage == null) {
            throw new IOException("Cannot navigate: No active JavaFX stage found.");
        }
        URL url = NavigationUtil.class.getResource(fxmlPath);
        if (url == null) {
            throw new IOException("FXML not found: " + fxmlPath);
        }
        Parent root = FXMLLoader.load(url);
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1100, 720);
            URL cssUrl = NavigationUtil.class.getResource("/css/style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        stage.show();
    }

    /**
     * Loads an FXML and returns the loaded controller so the caller can pass data.
     */
    public static <T> T loadFXML(String fxmlPath) throws IOException {
        URL url = NavigationUtil.class.getResource(fxmlPath);
        if (url == null) throw new IOException("FXML not found: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(url);
        loader.load();
        return loader.getController();
    }
}
