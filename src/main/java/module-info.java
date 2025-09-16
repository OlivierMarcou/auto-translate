module net.arkaine {
    // Modules JavaFX requis
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;  // Pour SwingFXUtils (conversion BufferedImage <-> WritableImage)

    // Modules Java standard requis
    requires java.net.http;        // Pour les requêtes HTTP vers les APIs de traduction
    requires java.desktop;         // Pour Robot, GraphicsEnvironment, BufferedImage (capture d'écran)

    // Bibliothèques tierces
    requires com.google.gson;      // Pour le parsing JSON des réponses APIs

    // Exports pour permettre l'accès aux classes du package
    exports net.arkaine;

    // Opens pour permettre l'accès par réflexion (JavaFX FXML et Gson)
    opens net.arkaine to javafx.fxml, com.google.gson;
}