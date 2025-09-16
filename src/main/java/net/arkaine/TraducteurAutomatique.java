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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private String sauvegardeClipboard = ""; // Sauvegarder le contenu original
    private boolean applicationALeFocus = false;
    private boolean ignorerProchainClipboard = false;

    // Système de logging
    private static final DateTimeFormatter FORMAT_FICHIER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FORMAT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String dernierJourFichier = "";

    // Timer pour la surveillance du clipboard
    private Timer timerSurveillance;

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

        // Bouton copier avec raccourci clavier
        Button boutonCopier = new Button("Copier la traduction (Ctrl+C)");
        boutonCopier.setOnAction(e -> copierTraduction());

        // Instructions d'utilisation
        Label labelInstructions = new Label("💡 Astuce: Sélectionnez du texte → Ctrl+C → Traduction automatique");
        labelInstructions.setStyle("-fx-font-size: 10px; -fx-text-fill: gray; -fx-font-style: italic;");

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
                checkboxSurveillance,
                labelInstructions
        );

        Scene scene = new Scene(new ScrollPane(root), 600, 750);
        primaryStage.setScene(scene);

        // Raccourci clavier global pour copier la traduction
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode().toString().equals("C")) {
                // Vérifier si le focus est sur la zone de traduction ou nulle part en particulier
                if (scene.getFocusOwner() == zoneTexteDestination ||
                        scene.getFocusOwner() == null ||
                        scene.getFocusOwner() == boutonCopier) {
                    copierTraduction();
                    e.consume();
                }
                // Si le focus est sur la zone source, laisser le Ctrl+C normal fonctionner
            }
        });

        primaryStage.show();

        // Détecter quand l'application a le focus ou le perd
        primaryStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            applicationALeFocus = newValue;
            System.out.println("Application focus: " + applicationALeFocus); // Debug
        });

        // Gérer la fermeture de l'application proprement
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("Fermeture de l'application...");
            arreterApplication();
            Platform.exit();
            System.exit(0);
        });

        // Créer le dossier de logs s'il n'existe pas
        creerDossierLogs();

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
        // Arrêter le timer précédent s'il existe
        if (timerSurveillance != null) {
            timerSurveillance.cancel();
        }

        // Créer un nouveau timer daemon
        timerSurveillance = new Timer("ClipboardSurveillance", true);
        timerSurveillance.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (checkboxSurveillance.isSelected()) {
                    Platform.runLater(() -> {
                        try {
                            // NE PAS surveiller le clipboard si l'application a le focus
                            // ou si on vient de copier quelque chose depuis l'app
                            if (applicationALeFocus || ignorerProchainClipboard) {
                                if (ignorerProchainClipboard) {
                                    ignorerProchainClipboard = false; // Reset du flag
                                    System.out.println("Clipboard ignoré après copie interne"); // Debug
                                }
                                return;
                            }

                            Clipboard clipboard = Clipboard.getSystemClipboard();
                            if (clipboard.hasString()) {
                                String contenu = clipboard.getString();
                                if (contenu != null && !contenu.equals(dernierTexteClipboard)
                                        && contenu.trim().length() > 0) {
                                    // Sauvegarder le contenu original pour pouvoir le restaurer
                                    sauvegardeClipboard = contenu;
                                    dernierTexteClipboard = contenu;
                                    zoneTexteSource.setText(contenu);
                                    System.out.println("Nouveau texte détecté: " + contenu.substring(0, Math.min(50, contenu.length())) + "..."); // Debug
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

                    // Enregistrer dans les logs
                    enregistrerTraduction(texte, traduction, langueSource, langueDestination);
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
            // Marquer qu'on va modifier le clipboard depuis l'application
            ignorerProchainClipboard = true;

            ClipboardContent contenu = new ClipboardContent();
            contenu.putString(traduction);
            Clipboard.getSystemClipboard().setContent(contenu);

            // Mettre à jour le dernier contenu connu pour éviter la re-détection
            dernierTexteClipboard = traduction;

            // Feedback visuel discret dans la barre de titre
            Stage stage = (Stage) zoneTexteDestination.getScene().getWindow();
            String titreOriginal = stage.getTitle();
            stage.setTitle("✅ Traduction copiée!");

            // Restaurer le titre après 2 secondes
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> stage.setTitle(titreOriginal));
                }
            }, 2000);

            System.out.println("Traduction copiée: " + traduction.substring(0, Math.min(50, traduction.length())) + "..."); // Debug
        }
    }

    /**
     * Créer le dossier de logs s'il n'existe pas
     */
    private void creerDossierLogs() {
        try {
            Files.createDirectories(Paths.get("logs"));
            System.out.println("Dossier de logs créé/vérifié: logs/");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du dossier logs: " + e.getMessage());
        }
    }

    /**
     * Enregistrer la traduction dans le fichier CSV du jour
     */
    private void enregistrerTraduction(String texteSource, String traduction, String langueSource, String langueDestination) {
        try {
            LocalDateTime maintenant = LocalDateTime.now();
            String jourActuel = maintenant.format(FORMAT_FICHIER);
            String timestamp = maintenant.format(FORMAT_TIMESTAMP);

            // Nom du fichier avec timestamp du jour
            String nomFichier = "logs/traductions_" + jourActuel + ".csv";

            // Vérifier si c'est un nouveau jour (nouveau fichier)
            boolean nouveauFichier = !jourActuel.equals(dernierJourFichier);
            dernierJourFichier = jourActuel;

            // Préparer la ligne CSV (échapper les guillemets et virgules)
            String texteSourceEchappe = echapperCSV(texteSource);
            String traductionEchappee = echapperCSV(traduction);
            String langueSourceNom = obtenirNomLangue(langueSource);
            String langueDestinationNom = obtenirNomLangue(langueDestination);

            StringBuilder ligne = new StringBuilder();
            ligne.append("\"").append(timestamp).append("\",");
            ligne.append("\"").append(texteSourceEchappe).append("\",");
            ligne.append("\"").append(traductionEchappee).append("\",");
            ligne.append("\"").append(langueSourceNom).append("\",");
            ligne.append("\"").append(langueDestinationNom).append("\"");
            ligne.append(System.lineSeparator());

            // Écrire dans le fichier
            if (nouveauFichier && !Files.exists(Paths.get(nomFichier))) {
                // Nouveau fichier : ajouter l'en-tête CSV
                String entete = "\"Timestamp\",\"Texte Source\",\"Traduction\",\"Langue Source\",\"Langue Destination\"" + System.lineSeparator();
                Files.write(Paths.get(nomFichier), entete.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println("Nouveau fichier de logs créé: " + nomFichier);
            }

            // Ajouter la ligne de traduction
            Files.write(Paths.get(nomFichier), ligne.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            System.out.println("Traduction enregistrée dans: " + nomFichier);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement des logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Échapper les caractères spéciaux pour CSV (guillemets, virgules, retours à la ligne)
     */
    private String echapperCSV(String texte) {
        if (texte == null) return "";

        // Remplacer les guillemets par des guillemets doublés
        String resultat = texte.replace("\"", "\"\"");

        // Remplacer les retours à la ligne par des espaces
        resultat = resultat.replace("\n", " ").replace("\r", " ");

        // Limiter la longueur pour éviter les lignes trop longues
        if (resultat.length() > 1000) {
            resultat = resultat.substring(0, 997) + "...";
        }

        return resultat;
    }

    /**
     * Arrêter proprement tous les services en arrière-plan
     */
    private void arreterApplication() {
        System.out.println("Arrêt des services en cours...");

        // Arrêter le timer de surveillance du clipboard
        if (timerSurveillance != null) {
            timerSurveillance.cancel();
            timerSurveillance = null;
            System.out.println("Timer de surveillance arrêté");
        }

        // Arrêter tous les autres timers potentiels
        // (ceux créés pour les délais de traduction et feedback)
        // Note: Ces timers ont une durée limitée, mais on peut les nettoyer ici si besoin

        System.out.println("Application fermée proprement");
    }


    public static void main(String[] args) {
        launch(args);
    }
}