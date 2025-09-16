package net.arkaine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TraducteurAutomatique extends Application {

    private TextArea zoneTexteSource;
    private TextArea zoneTexteDestination;
    private ComboBox<String> comboLangueDestination;
    private Label labelLangueDetectee;
    private ProgressIndicator indicateurProgres;
    private String dernierTexteClipboard = "";

    // Mapping des langues
    private Map<String, String> langues = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        initLangues();

        primaryStage.setTitle("Traducteur Automatique");

        // Interface utilisateur
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Zone de sélection de la langue de destination
        HBox boxLangueDestination = new HBox(10);
        Label labelDestination = new Label("Traduire vers :");
        comboLangueDestination = new ComboBox<>();
        comboLangueDestination.getItems().addAll(langues.keySet());
        comboLangueDestination.setValue("Français"); // Par défaut
        boxLangueDestination.getChildren().addAll(labelDestination, comboLangueDestination);

        // Zone d'affichage de la langue détectée
        labelLangueDetectee = new Label("Langue détectée : Aucune");
        labelLangueDetectee.setStyle("-fx-font-style: italic;");

        // Zone de texte source
        Label labelSource = new Label("Texte à traduire :");
        zoneTexteSource = new TextArea();
        zoneTexteSource.setPrefRowCount(8);
        zoneTexteSource.setPromptText("Tapez ou collez votre texte ici...");

        // Bouton de traduction manuelle
        Button boutonTraduire = new Button("Traduire");
        boutonTraduire.setOnAction(e -> traduireTexte());

        // Indicateur de progression
        indicateurProgres = new ProgressIndicator();
        indicateurProgres.setVisible(false);
        indicateurProgres.setPrefSize(30, 30);

        HBox boxBouton = new HBox(10);
        boxBouton.getChildren().addAll(boutonTraduire, indicateurProgres);

        // Zone de texte destination
        Label labelDestination2 = new Label("Traduction :");
        zoneTexteDestination = new TextArea();
        zoneTexteDestination.setPrefRowCount(8);
        zoneTexteDestination.setEditable(false);
        zoneTexteDestination.setStyle("-fx-background-color: #f5f5f5;");

        // Bouton copier
        Button boutonCopier = new Button("Copier la traduction");
        boutonCopier.setOnAction(e -> copierTraduction());

        // Checkbox pour activer/désactiver la surveillance du presse-papiers
        CheckBox checkboxSurveillance = new CheckBox("Surveiller le presse-papiers");
        checkboxSurveillance.setSelected(true);

        // Assemblage de l'interface
        root.getChildren().addAll(
                boxLangueDestination,
                labelLangueDetectee,
                labelSource,
                zoneTexteSource,
                boxBouton,
                labelDestination2,
                zoneTexteDestination,
                boutonCopier,
                new Separator(),
                checkboxSurveillance
        );

        Scene scene = new Scene(new ScrollPane(root), 600, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Démarrer la surveillance du presse-papiers
        demarrerSurveillanceClipboard(checkboxSurveillance);

        // Traduction automatique quand le texte change
        zoneTexteSource.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty() && !newValue.equals(oldValue)) {
                // Délai pour éviter de traduire à chaque caractère
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> traduireTexte());
                    }
                }, 1000);
            }
        });

        // Traduction automatique quand la langue de destination change
        comboLangueDestination.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                String texte = zoneTexteSource.getText().trim();
                if (!texte.isEmpty()) {
                    // Traduire immédiatement avec la nouvelle langue
                    Platform.runLater(() -> traduireTexte());
                }
            }
        });
    }

    private void initLangues() {
        langues.put("Français", "fr");
        langues.put("Anglais", "en");
        langues.put("Espagnol", "es");
        langues.put("Allemand", "de");
        langues.put("Italien", "it");
        langues.put("Portugais", "pt");
        langues.put("Russe", "ru");
        langues.put("Chinois", "zh");
        langues.put("Japonais", "ja");
        langues.put("Coréen", "ko");
        langues.put("Arabe", "ar");
        langues.put("Néerlandais", "nl");
        langues.put("Suédois", "sv");
        langues.put("Norvégien", "no");
        langues.put("Danois", "da");
        langues.put("Polonais", "pl");
        langues.put("Tchèque", "cs");
        langues.put("Hongrois", "hu");
        langues.put("Roumain", "ro");
        langues.put("Bulgare", "bg");
    }

    private void demarrerSurveillanceClipboard(CheckBox checkboxSurveillance) {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (checkboxSurveillance.isSelected()) {
                    Platform.runLater(() -> {
                        try {
                            Clipboard clipboard = Clipboard.getSystemClipboard();
                            if (clipboard.hasString()) {
                                String contenu = clipboard.getString();
                                if (contenu != null && !contenu.equals(dernierTexteClipboard)
                                        && contenu.trim().length() > 0) {
                                    dernierTexteClipboard = contenu;
                                    zoneTexteSource.setText(contenu);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la lecture du presse-papiers: " + e.getMessage());
                        }
                    });
                }
            }
        }, 1000, 1000); // Vérifier toutes les secondes
    }

    private void traduireTexte() {
        String texte = zoneTexteSource.getText().trim();
        if (texte.isEmpty()) {
            return;
        }

        String langueDestination = langues.get(comboLangueDestination.getValue());

        // Créer une tâche en arrière-plan pour la traduction
        Task<String[]> tacheTraduction = new Task<String[]>() {
            @Override
            protected String[] call() throws Exception {
                // Détecter la langue source
                String langueSource = detecterLangue(texte);

                // Traduire le texte
                String traduction = traduireAvecGoogleTranslate(texte, langueSource, langueDestination);

                return new String[]{langueSource, traduction};
            }

            @Override
            protected void succeeded() {
                String[] resultat = getValue();
                String langueSource = resultat[0];
                String traduction = resultat[1];

                // Mettre à jour l'interface
                Platform.runLater(() -> {
                    labelLangueDetectee.setText("Langue détectée : " + obtenirNomLangue(langueSource));
                    zoneTexteDestination.setText(traduction);
                    indicateurProgres.setVisible(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    zoneTexteDestination.setText("Erreur lors de la traduction : " + getException().getMessage());
                    indicateurProgres.setVisible(false);
                });
            }
        };

        indicateurProgres.setVisible(true);
        Thread threadTraduction = new Thread(tacheTraduction);
        threadTraduction.setDaemon(true);
        threadTraduction.start();
    }

    private String detecterLangue(String texte) throws Exception {
        // Utilisation de l'API Google Translate pour détecter la langue
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=fr&dt=t&q="
                + URLEncoder.encode(texte, "UTF-8");

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parser la réponse JSON pour extraire la langue détectée
        String jsonResponse = response.toString();
        // La langue détectée se trouve généralement à la fin de la réponse JSON
        String[] parts = jsonResponse.split(",");
        for (String part : parts) {
            if (part.contains("\"") && part.length() == 4) {
                return part.replace("\"", "");
            }
        }

        return "auto"; // Langue par défaut si détection échoue
    }

    private String traduireAvecGoogleTranslate(String texte, String langueSource, String langueDestination) throws Exception {
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + langueSource
                + "&tl=" + langueDestination + "&dt=t&q=" + URLEncoder.encode(texte, "UTF-8");

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parser la réponse JSON
        String jsonResponse = response.toString();
        return extraireTraduction(jsonResponse);
    }

    private String extraireTraduction(String jsonResponse) {
        try {
            // Parsing simple de la réponse JSON de Google Translate
            String[] parts = jsonResponse.split("\\[\\[\\[\"");
            if (parts.length > 1) {
                String traduction = parts[1].split("\"")[0];
                return traduction.replace("\\n", "\n");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction de la traduction: " + e.getMessage());
        }
        return "Erreur lors de la traduction";
    }

    private String obtenirNomLangue(String codeLangue) {
        for (Map.Entry<String, String> entry : langues.entrySet()) {
            if (entry.getValue().equals(codeLangue)) {
                return entry.getKey();
            }
        }
        return codeLangue.toUpperCase();
    }

    private void copierTraduction() {
        String traduction = zoneTexteDestination.getText();
        if (!traduction.trim().isEmpty()) {
            ClipboardContent contenu = new ClipboardContent();
            contenu.putString(traduction);
            Clipboard.getSystemClipboard().setContent(contenu);

            // Feedback visuel
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Copié");
            alert.setHeaderText(null);
            alert.setContentText("La traduction a été copiée dans le presse-papiers !");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}