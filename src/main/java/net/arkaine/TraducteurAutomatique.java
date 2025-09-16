package net.arkaine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TraducteurAutomatique extends Application {

    private TextArea zoneTexteSource;
    private TextArea zoneTexteDestination;
    private ComboBox<String> comboLangueSource;
    private ComboBox<String> comboLangueDestination;
    private Label labelLangueDetectee;
    private ProgressIndicator indicateurProgres;
    private String derniereLangueSourceDetectee = "en";
    private String dernierTexteClipboard = "";
    private String sauvegardeClipboard = "";
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

        // Zone de sélection des langues avec bouton d'inversion
        HBox boxLangues = new HBox(10);
        boxLangues.setStyle("-fx-alignment: center-left;");

        // Langue source
        Label labelSourceLangue = new Label("De :");
        comboLangueSource = new ComboBox<>();
        comboLangueSource.getItems().add("Détection automatique");
        comboLangueSource.getItems().addAll(langues.keySet());
        comboLangueSource.setValue("Détection automatique"); // Par défaut
        comboLangueSource.setPrefWidth(150);

        // Bouton d'inversion des langues
        Button boutonInverser = new Button("⇄");
        boutonInverser.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 40px;");
        boutonInverser.setTooltip(new Tooltip("Inverser les langues (source ↔ destination)"));
        boutonInverser.setOnAction(e -> inverserLangues());

        // Langue destination
        Label labelDestinationLangue = new Label("Vers :");
        comboLangueDestination = new ComboBox<>();
        comboLangueDestination.getItems().addAll(langues.keySet());
        comboLangueDestination.setValue("Français"); // Par défaut
        comboLangueDestination.setPrefWidth(150);

        boxLangues.getChildren().addAll(labelSourceLangue, comboLangueSource, boutonInverser,
                labelDestinationLangue, comboLangueDestination);

        // Zone d'affichage de la langue détectée
        labelLangueDetectee = new Label("Langue détectée : Aucune");
        labelLangueDetectee.setStyle("-fx-font-style: italic;");

        // Zone de texte source
        Label labelTexteSource = new Label("Texte à traduire :");
        zoneTexteSource = new TextArea();
        zoneTexteSource.setPrefRowCount(8);
        zoneTexteSource.setPromptText("Tapez ou collez votre texte ici...");

        // Bouton de traduction manuelle et capture d'écran
        Button boutonTraduire = new Button("Traduire");
        boutonTraduire.setOnAction(e -> traduireTexte());

        Button boutonCapture = new Button("📷 Capturer écran");
        boutonCapture.setStyle("-fx-font-size: 12px;");
        boutonCapture.setTooltip(new Tooltip("Capturer une zone de l'écran et traduire le texte (OCR)"));
        boutonCapture.setOnAction(e -> demarrerCaptureEcran());

        // Indicateur de progression
        indicateurProgres = new ProgressIndicator();
        indicateurProgres.setVisible(false);
        indicateurProgres.setPrefSize(30, 30);

        HBox boxBouton = new HBox(10);
        boxBouton.getChildren().addAll(boutonTraduire, boutonCapture, indicateurProgres);

        // Zone de texte destination
        Label labelTexteDestination = new Label("Traduction :");
        zoneTexteDestination = new TextArea();
        zoneTexteDestination.setPrefRowCount(8);
        zoneTexteDestination.setEditable(false);
        zoneTexteDestination.setStyle("-fx-background-color: #f5f5f5;");

        // Bouton copier avec raccourci clavier
        Button boutonCopier = new Button("Copier la traduction (Ctrl+C)");
        boutonCopier.setOnAction(e -> copierTraduction());

        // Instructions d'utilisation
        Label labelInstructions = new Label("💡 Astuce: Sélectionnez du texte → Ctrl+C → Traduction automatique\n" +
                "📷 Capture d'écran: Cliquez sur 'Capturer écran' puis sélectionnez la zone\n" +
                "🚫 Code source et textes > 5000 caractères filtrés automatiquement");
        labelInstructions.setStyle("-fx-font-size: 10px; -fx-text-fill: gray; -fx-font-style: italic;");

        // Checkbox pour activer/désactiver la surveillance du presse-papiers
        CheckBox checkboxSurveillance = new CheckBox("Surveiller le presse-papiers");
        checkboxSurveillance.setSelected(true);

        // Assemblage de l'interface
        root.getChildren().addAll(
                boxLangues,
                labelLangueDetectee,
                labelTexteSource,
                zoneTexteSource,
                boxBouton,
                labelTexteDestination,
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
                if (scene.getFocusOwner() == zoneTexteDestination ||
                        scene.getFocusOwner() == null ||
                        scene.getFocusOwner() == boutonCopier) {
                    copierTraduction();
                    e.consume();
                }
            }
        });

        primaryStage.show();

        // Détecter quand l'application a le focus ou le perd
        primaryStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            applicationALeFocus = newValue;
            System.out.println("Application focus: " + applicationALeFocus);
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
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> traduireTexte());
                    }
                }, 1000);
            }
        });

        // Traduction automatique quand la langue source change
        comboLangueSource.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                String texte = zoneTexteSource.getText().trim();
                if (!texte.isEmpty()) {
                    // Mettre à jour l'affichage de la langue détectée
                    if (newValue.equals("Détection automatique")) {
                        labelLangueDetectee.setText("Langue détectée : Auto");
                    } else {
                        labelLangueDetectee.setText("Langue sélectionnée : " + newValue);
                    }
                    // Traduire immédiatement avec la nouvelle langue
                    Platform.runLater(() -> traduireTexte());
                }
            }
        });

        // Traduction automatique quand la langue de destination change
        comboLangueDestination.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                String texte = zoneTexteSource.getText().trim();
                if (!texte.isEmpty()) {
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
        if (timerSurveillance != null) {
            timerSurveillance.cancel();
        }

        timerSurveillance = new Timer("ClipboardSurveillance", true);
        timerSurveillance.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (checkboxSurveillance.isSelected()) {
                    Platform.runLater(() -> {
                        try {
                            if (applicationALeFocus || ignorerProchainClipboard) {
                                if (ignorerProchainClipboard) {
                                    ignorerProchainClipboard = false;
                                    System.out.println("Clipboard ignoré après copie interne");
                                }
                                return;
                            }

                            Clipboard clipboard = Clipboard.getSystemClipboard();
                            if (clipboard.hasString()) {
                                String contenu = clipboard.getString();
                                if (contenu != null && !contenu.equals(dernierTexteClipboard)
                                        && contenu.trim().length() > 0) {
                                    sauvegardeClipboard = contenu;
                                    dernierTexteClipboard = contenu;
                                    zoneTexteSource.setText(contenu);
                                    System.out.println("Nouveau texte détecté: " + contenu.substring(0, Math.min(50, contenu.length())) + "...");
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la lecture du presse-papiers: " + e.getMessage());
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    private void traduireTexte() {
        String texte = zoneTexteSource.getText().trim();
        if (texte.isEmpty()) {
            return;
        }

        // Vérifications de sécurité et de qualité
        String messageErreur = validerTexteATraduire(texte);
        if (messageErreur != null) {
            zoneTexteDestination.setText(messageErreur);
            labelLangueDetectee.setText("⚠️ Texte non traduit");
            return;
        }

        String langueDestination = langues.get(comboLangueDestination.getValue());

        Task<String[]> tacheTraduction = new Task<String[]>() {
            @Override
            protected String[] call() throws Exception {
                String langueSource;

                // Vérifier si l'utilisateur a sélectionné une langue source manuellement
                String langueSourceSelectionnee = comboLangueSource.getValue();
                if (langueSourceSelectionnee != null && !langueSourceSelectionnee.equals("Détection automatique")) {
                    // Utiliser la langue sélectionnée manuellement
                    langueSource = langues.get(langueSourceSelectionnee);
                    System.out.println("Langue source manuelle: " + langueSourceSelectionnee + " (" + langueSource + ")");
                } else {
                    // Utiliser la détection automatique
                    System.out.println("Détection automatique de la langue source...");

                    // Essayer d'abord avec l'API MyMemory (plus fiable)
                    try {
                        String[] resultatMyMemory = traduireAvecMyMemory(texte, langueDestination);
                        if (resultatMyMemory[1] != null && !resultatMyMemory[1].trim().isEmpty()
                                && !resultatMyMemory[1].equals("NO QUERY SPECIFIED. EXAMPLE: GET?Q=HELLO&LANGPAIR=EN|IT")) {
                            return resultatMyMemory;
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur MyMemory, essai Google Translate: " + e.getMessage());
                    }

                    // Fallback vers Google Translate avec parsing amélioré pour la détection
                    langueSource = detecterLangueSimple(texte);
                    if (langueSource.equals("en")) { // Si détection simple donne anglais, vérifier avec Google
                        try {
                            String langueDetecteeGoogle = detecterLangueAvecGoogle(texte);
                            if (!langueDetecteeGoogle.equals("auto")) {
                                langueSource = langueDetecteeGoogle;
                            }
                        } catch (Exception e) {
                            System.err.println("Détection Google échouée: " + e.getMessage());
                        }
                    }
                }

                // Traduire le texte
                String traduction;
                if (langueSourceSelectionnee != null && !langueSourceSelectionnee.equals("Détection automatique")) {
                    // Forcer la traduction avec la langue choisie
                    try {
                        traduction = traduireAvecMyMemoryForce(texte, langueSource, langueDestination);
                    } catch (Exception e) {
                        traduction = traduireAvecGoogleTranslateAmeliore(texte, langueSource, langueDestination);
                    }
                } else {
                    // Utiliser le système normal
                    traduction = traduireAvecGoogleTranslateAmeliore(texte, langueSource, langueDestination);
                }

                return new String[]{langueSource, traduction};
            }

            @Override
            protected void succeeded() {
                String[] resultat = getValue();
                String langueSource = resultat[0];
                String traduction = resultat[1];

                Platform.runLater(() -> {
                    // Affichage différencié selon le mode
                    String langueSourceSelectionnee = comboLangueSource.getValue();
                    if (langueSourceSelectionnee != null && !langueSourceSelectionnee.equals("Détection automatique")) {
                        labelLangueDetectee.setText("Langue sélectionnée : " + langueSourceSelectionnee);
                    } else {
                        labelLangueDetectee.setText("Langue détectée : " + obtenirNomLangue(langueSource));
                    }

                    zoneTexteDestination.setText(traduction);
                    indicateurProgres.setVisible(false);

                    // Sauvegarder la dernière langue source pour l'inversion
                    derniereLangueSourceDetectee = langueSource;

                    enregistrerTraduction(zoneTexteSource.getText().trim(), traduction, langueSource, langueDestination);
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

    /**
     * Démarrer la capture d'écran avec sélection de zone
     */
    private void demarrerCaptureEcran() {
        try {
            // Minimiser la fenêtre principale temporairement
            Stage stagePrincipal = (Stage) zoneTexteSource.getScene().getWindow();
            stagePrincipal.setIconified(true);

            // Attendre un peu que la fenêtre se minimise
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        try {
                            creerOverlaySelection();
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la création de l'overlay: " + e.getMessage());
                            stagePrincipal.setIconified(false); // Restaurer en cas d'erreur
                        }
                    });
                }
            }, 500);

        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de la capture: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de capture");
            alert.setContentText("Impossible de démarrer la capture d'écran: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Créer l'overlay de sélection sur tous les écrans
     */
    private void creerOverlaySelection() throws Exception {
        // Obtenir les dimensions de tous les écrans
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        System.out.println("Nombre d'écrans détectés: " + screens.length);

        // Calculer la zone totale de tous les écrans (bounding box)
        Rectangle zoneTotale = new Rectangle();
        for (int i = 0; i < screens.length; i++) {
            GraphicsConfiguration config = screens[i].getDefaultConfiguration();
            Rectangle bounds = config.getBounds();
            System.out.println("Écran " + i + ": " + bounds);

            if (i == 0) {
                zoneTotale = new Rectangle(bounds);
            } else {
                zoneTotale = zoneTotale.union(bounds);
            }
        }

        System.out.println("Zone totale calculée: " + zoneTotale);

        // Créer une capture complète de tous les écrans
        Robot robot = new Robot();
        BufferedImage captureComplete = robot.createScreenCapture(zoneTotale);

        // Créer la liste finale des stages (pour les lambdas)
        final List<Stage> stagesSelection = new ArrayList<>();
        final List<Canvas> canvasList = new ArrayList<>();
        final List<Rectangle> ecranBounds = new ArrayList<>();

        // Variables partagées pour la sélection (finales pour les lambdas)
        final Rectangle[] zoneSelectionGlobale = {null};
        final double[] startX = {0};
        final double[] startY = {0};
        final boolean[] isSelecting = {false};
        final Rectangle zoneTotaleFinale = new Rectangle(zoneTotale);
        final BufferedImage captureFinale = captureComplete;

        // Créer une fenêtre de sélection pour chaque écran
        for (int i = 0; i < screens.length; i++) {
            GraphicsConfiguration config = screens[i].getDefaultConfiguration();
            Rectangle boundsEcran = config.getBounds();
            ecranBounds.add(new Rectangle(boundsEcran));

            System.out.println("Création overlay pour écran " + i + ": " + boundsEcran);

            // Créer la fenêtre de sélection pour cet écran
            Stage stageSelection = new Stage();
            stageSelection.initStyle(StageStyle.TRANSPARENT);
            stageSelection.setAlwaysOnTop(true);

            // Canvas pour cet écran
            Canvas canvas = new Canvas(boundsEcran.width, boundsEcran.height);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            canvasList.add(canvas);

            // Extraire la partie de l'image correspondant à cet écran
            int sourceX = boundsEcran.x - zoneTotale.x;
            int sourceY = boundsEcran.y - zoneTotale.y;

            BufferedImage partieEcran = captureComplete.getSubimage(
                    sourceX, sourceY, boundsEcran.width, boundsEcran.height);

            // Convertir pour JavaFX
            WritableImage fxImage = SwingFXUtils.toFXImage(partieEcran, null);

            // Dessiner l'image de fond assombrie
            gc.drawImage(fxImage, 0, 0);
            gc.setFill(Color.color(0, 0, 0, 0.3)); // Overlay semi-transparent
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            final int indexEcranActuel = i;
            final Rectangle boundsEcranFinal = new Rectangle(boundsEcran);

            // Gestion des événements de souris
            canvas.setOnMousePressed((MouseEvent e) -> {
                // Convertir les coordonnées locales en coordonnées globales
                startX[0] = e.getX() + boundsEcranFinal.x;
                startY[0] = e.getY() + boundsEcranFinal.y;
                isSelecting[0] = true;
                System.out.println("Début sélection globale: " + startX[0] + ", " + startY[0] + " (écran " + indexEcranActuel + ")");
            });

            canvas.setOnMouseDragged((MouseEvent e) -> {
                if (isSelecting[0]) {
                    // Coordonnées globales de la fin de sélection
                    double endX = e.getX() + boundsEcranFinal.x;
                    double endY = e.getY() + boundsEcranFinal.y;

                    // Calculer le rectangle de sélection en coordonnées globales
                    double x = Math.min(startX[0], endX);
                    double y = Math.min(startY[0], endY);
                    double w = Math.abs(endX - startX[0]);
                    double h = Math.abs(endY - startY[0]);

                    zoneSelectionGlobale[0] = new Rectangle((int)x, (int)y, (int)w, (int)h);

                    // Mettre à jour tous les écrans
                    mettreAJourTousLesOverlays(canvasList, ecranBounds, captureFinale, zoneTotaleFinale, zoneSelectionGlobale[0]);
                }
            });

            canvas.setOnMouseReleased((MouseEvent e) -> {
                if (isSelecting[0] && zoneSelectionGlobale[0] != null) {
                    isSelecting[0] = false;

                    System.out.println("Zone sélectionnée globale: " + zoneSelectionGlobale[0]);

                    // Fermer tous les overlays
                    for (Stage stage : stagesSelection) {
                        stage.close();
                    }

                    // Traiter la capture si la sélection est suffisante
                    if (zoneSelectionGlobale[0].width > 10 && zoneSelectionGlobale[0].height > 10) {
                        traiterCaptureZone(zoneSelectionGlobale[0]);
                    } else {
                        System.out.println("Sélection trop petite ignorée");
                        restaurerFenetrePrincipale();
                    }
                }
            });

            // Échapper pour annuler
            canvas.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode().getName().equals("ESCAPE")) {
                    for (Stage stage : stagesSelection) {
                        stage.close();
                    }
                    restaurerFenetrePrincipale();
                }
            });

            // Créer la scène pour cet écran
            VBox root = new VBox();
            root.getChildren().add(canvas);
            root.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(root, boundsEcran.width, boundsEcran.height);
            scene.setFill(Color.TRANSPARENT);
            stageSelection.setScene(scene);

            // Positionner la fenêtre exactement sur cet écran
            stageSelection.setX(boundsEcran.x);
            stageSelection.setY(boundsEcran.y);
            stageSelection.setWidth(boundsEcran.width);
            stageSelection.setHeight(boundsEcran.height);

            stagesSelection.add(stageSelection);
        }

        // Afficher tous les overlays
        for (Stage stage : stagesSelection) {
            stage.show();
            if (stage.getScene() != null && stage.getScene().getRoot() instanceof VBox) {
                VBox root = (VBox) stage.getScene().getRoot();
                if (!root.getChildren().isEmpty() && root.getChildren().get(0) instanceof Canvas) {
                    Canvas canvas = (Canvas) root.getChildren().get(0);
                    canvas.requestFocus();
                }
            }
        }

        System.out.println("Overlays de sélection créés pour " + screens.length + " écrans");
    }

    /**
     * Mettre à jour tous les overlays avec la sélection actuelle
     */
    private void mettreAJourTousLesOverlays(List<Canvas> canvasList, List<Rectangle> ecranBounds,
                                            BufferedImage captureComplete, Rectangle zoneTotale,
                                            Rectangle selectionGlobale) {

        for (int i = 0; i < canvasList.size(); i++) {
            Canvas canvas = canvasList.get(i);
            Rectangle boundsEcran = ecranBounds.get(i);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Extraire la partie d'image pour cet écran
            int sourceX = boundsEcran.x - zoneTotale.x;
            int sourceY = boundsEcran.y - zoneTotale.y;

            BufferedImage partieEcran = captureComplete.getSubimage(
                    sourceX, sourceY, boundsEcran.width, boundsEcran.height);
            WritableImage fxImage = SwingFXUtils.toFXImage(partieEcran, null);

            // Redessiner le fond assombri
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(fxImage, 0, 0);
            gc.setFill(Color.color(0, 0, 0, 0.3));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Calculer l'intersection entre la sélection et cet écran
            Rectangle intersection = selectionGlobale.intersection(boundsEcran);
            if (!intersection.isEmpty()) {
                // Convertir en coordonnées locales à cet écran
                double localX = intersection.x - boundsEcran.x;
                double localY = intersection.y - boundsEcran.y;

                // Zone claire (sélectionnée)
                gc.clearRect(localX, localY, intersection.width, intersection.height);
                gc.drawImage(fxImage, localX, localY, intersection.width, intersection.height,
                        localX, localY, intersection.width, intersection.height);

                // Bordure de sélection
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeRect(localX, localY, intersection.width, intersection.height);
            }
        }
    }

    /**
     * Traiter la capture de la zone sélectionnée
     */
    private void traiterCaptureZone(Rectangle zone) {
        Task<String> tacheOCR = new Task<String>() {
            @Override
            protected String call() throws Exception {
                // Capturer la zone spécifique
                Robot robot = new Robot();
                BufferedImage capture = robot.createScreenCapture(zone);

                // Sauvegarder temporairement l'image
                File tempFile = File.createTempFile("capture_ocr_", ".png");
                ImageIO.write(capture, "PNG", tempFile);

                System.out.println("Image capturée sauvée: " + tempFile.getAbsolutePath());

                // Effectuer l'OCR
                String texteExtrait = effectuerOCR(tempFile);

                // Nettoyer le fichier temporaire
                tempFile.delete();

                return texteExtrait;
            }

            @Override
            protected void succeeded() {
                String texte = getValue();

                Platform.runLater(() -> {
                    if (texte != null && !texte.trim().isEmpty()) {
                        System.out.println("Texte OCR extrait: " + texte);
                        zoneTexteSource.setText(texte.trim());
                        // La traduction se déclenchera automatiquement
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("OCR");
                        alert.setContentText("Aucun texte détecté dans la capture.\nEssayez avec une image plus nette ou une zone plus grande.");
                        alert.showAndWait();
                    }
                    restaurerFenetrePrincipale();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    System.err.println("Erreur OCR: " + getException().getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur OCR");
                    alert.setContentText("Erreur lors de l'extraction du texte: " + getException().getMessage());
                    alert.showAndWait();
                    restaurerFenetrePrincipale();
                });
            }
        };

        // Afficher l'indicateur de progression
        Platform.runLater(() -> {
            indicateurProgres.setVisible(true);
            Stage stage = (Stage) zoneTexteSource.getScene().getWindow();
            stage.setTitle("🔍 Extraction du texte en cours...");
        });

        Thread threadOCR = new Thread(tacheOCR);
        threadOCR.setDaemon(true);
        threadOCR.start();
    }

    /**
     * Effectuer l'OCR sur l'image capturée
     */
    private String effectuerOCR(File imageFile) throws Exception {
        // Utiliser l'API OCR.space (gratuite) pour l'extraction de texte
        String apiKey = "K87899142388957"; // Clé publique de démonstration
        String url = "https://api.ocr.space/parse/image";

        // Préparer la requête multipart
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("apikey", apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        try (OutputStream os = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

            // Paramètres OCR
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"language\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n");
            writer.append("\r\n");
            writer.append("eng").append("\r\n"); // Langue par défaut anglais

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"isOverlayRequired\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n");
            writer.append("\r\n");
            writer.append("false").append("\r\n");

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"detectOrientation\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n");
            writer.append("\r\n");
            writer.append("true").append("\r\n");

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"scale\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n");
            writer.append("\r\n");
            writer.append("true").append("\r\n");

            // Fichier image
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(imageFile.getName()).append("\"").append("\r\n");
            writer.append("Content-Type: image/png").append("\r\n");
            writer.append("\r\n").flush();

            // Copier le fichier
            Files.copy(imageFile.toPath(), os);
            os.flush();

            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--").append("\r\n").flush();
        }

        // Lire la réponse
        int responseCode = connection.getResponseCode();
        System.out.println("Code de réponse OCR: " + responseCode);

        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parser la réponse JSON
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                System.out.println("Réponse OCR: " + jsonResponse.toString());

                if (jsonResponse.has("ParsedResults") && jsonResponse.get("ParsedResults").isJsonArray()) {
                    JsonArray results = jsonResponse.getAsJsonArray("ParsedResults");
                    if (results.size() > 0) {
                        JsonObject result = results.get(0).getAsJsonObject();
                        if (result.has("ParsedText")) {
                            return result.get("ParsedText").getAsString();
                        }
                    }
                }

                // Vérifier les erreurs
                if (jsonResponse.has("ErrorMessage") && !jsonResponse.get("ErrorMessage").isJsonNull()) {
                    throw new Exception("Erreur OCR: " + jsonResponse.get("ErrorMessage").getAsString());
                }

                return "";
            }
        } else {
            throw new Exception("Erreur HTTP: " + responseCode + " " + connection.getResponseMessage());
        }
    }

    /**
     * Restaurer la fenêtre principale
     */
    private void restaurerFenetrePrincipale() {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) zoneTexteSource.getScene().getWindow();
                stage.setIconified(false);
                stage.toFront();
                stage.requestFocus();
                stage.setTitle("Traducteur Automatique");
                indicateurProgres.setVisible(false);
                System.out.println("Fenêtre principale restaurée");
            } catch (Exception e) {
                System.err.println("Erreur lors de la restauration: " + e.getMessage());
            }
        });
    }

    /**
     * Traduction forcée avec MyMemory (langue source spécifiée)
     */
    private String traduireAvecMyMemoryForce(String texte, String langueSource, String langueDestination) throws Exception {
        // Construire l'URL pour MyMemory avec la langue source spécifiée
        String langpair = langueSource + "|" + langueDestination;
        String url = "https://api.mymemory.translated.net/get?q="
                + URLEncoder.encode(texte, "UTF-8")
                + "&langpair=" + langpair;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (JavaFX Translation App)");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parser la réponse JSON avec Gson
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

        if (jsonResponse.has("responseData") && !jsonResponse.get("responseData").isJsonNull()) {
            JsonObject responseData = jsonResponse.getAsJsonObject("responseData");
            if (responseData.has("translatedText")) {
                String traduction = responseData.get("translatedText").getAsString();
                System.out.println("Traduction MyMemory forcée réussie: " + traduction.substring(0, Math.min(50, traduction.length())));
                return traduction;
            }
        }

        throw new Exception("Réponse MyMemory invalide pour traduction forcée");
    }

    /**
     * Valider le texte avant traduction (éviter code source, textes trop longs, etc.)
     */
    private String validerTexteATraduire(String texte) {
        // Limite de longueur (5000 caractères)
        if (texte.length() > 5000) {
            return "⚠️ Texte trop long pour la traduction (max 5000 caractères).\n" +
                    "Longueur actuelle : " + texte.length() + " caractères.\n" +
                    "Veuillez raccourcir le texte ou le diviser en plusieurs parties.";
        }

        // Détecter du code source potentiel
        if (ressembleADuCode(texte)) {
            return "🚫 Ce texte ressemble à du code source ou à un format technique.\n" +
                    "Les traductions de code peuvent causer des erreurs.\n" +
                    "Si vous souhaitez vraiment traduire ce contenu, " +
                    "copiez seulement les commentaires ou la documentation.";
        }

        // Détecter trop de caractères spéciaux (peut être du binaire, logs, etc.)
        if (tropDeCaracteresSpeciaux(texte)) {
            return "⚠️ Ce texte contient trop de caractères spéciaux ou de symboles.\n" +
                    "Il pourrait s'agir de données binaires, logs système, ou format technique.\n" +
                    "Vérifiez que c'est bien du texte naturel à traduire.";
        }

        return null; // Texte valide
    }

    /**
     * Détecter si le texte ressemble à du code source
     */
    private boolean ressembleADuCode(String texte) {
        // Compter les indicateurs de code
        int indicateursCode = 0;

        // Patterns de code courants
        if (texte.matches(".*\\b(public|private|protected|class|interface|import|package|function|def|var|let|const|return|if|else|while|for|try|catch|throw)\\b.*")) {
            indicateursCode += 3;
        }

        // Balises HTML/XML
        if (texte.matches(".*<[a-zA-Z][^>]*>.*") || texte.matches(".*</[a-zA-Z][^>]*>.*")) {
            indicateursCode += 2;
        }

        // JSON/YAML
        if (texte.matches(".*\\{[\"']\\w+[\"']\\s*:.*") || texte.matches(".*:\\s*[\"'].*[\"'].*")) {
            indicateursCode += 2;
        }

        // Expressions régulières ou patterns
        if (texte.matches(".*\\\\[nrtbf].*") || texte.matches(".*\\[\\^.*\\].*")) {
            indicateursCode += 1;
        }

        // Accolades et parenthèses nombreuses (fonctions, objets)
        long accolades = texte.chars().filter(c -> c == '{' || c == '}').count();
        long parentheses = texte.chars().filter(c -> c == '(' || c == ')').count();
        if (accolades > 3 || parentheses > 10) {
            indicateursCode += 1;
        }

        // Points-virgules multiples (instructions)
        if (texte.chars().filter(c -> c == ';').count() > 3) {
            indicateursCode += 1;
        }

        // Mots techniques fréquents
        String[] motsTechniques = {"null", "undefined", "boolean", "string", "array", "object",
                "href", "src", "onclick", "getElementById", "querySelector",
                "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE",
                "git", "commit", "push", "pull", "branch", "merge"};
        for (String mot : motsTechniques) {
            if (texte.toLowerCase().contains(mot.toLowerCase())) {
                indicateursCode += 1;
            }
        }

        // Extensions de fichiers
        if (texte.matches(".*\\.(js|java|py|php|html|css|xml|json|yml|yaml|sql|sh|bat)\\b.*")) {
            indicateursCode += 2;
        }

        // URLs nombreuses
        if (texte.split("https?://").length > 3) {
            indicateursCode += 1;
        }

        // Seuil de détection : si 4+ indicateurs, probablement du code
        return indicateursCode >= 4;
    }

    /**
     * Détecter trop de caractères spéciaux
     */
    private boolean tropDeCaracteresSpeciaux(String texte) {
        if (texte.length() < 50) return false; // Ignorer les textes courts

        // Compter les caractères non-alphabétiques (hors espaces et ponctuation courante)
        long caracteresSpeciaux = texte.chars()
                .filter(c -> !Character.isLetterOrDigit(c) &&
                        c != ' ' && c != '.' && c != ',' && c != '!' && c != '?' &&
                        c != ':' && c != ';' && c != '\n' && c != '\r' && c != '\t' &&
                        c != '-' && c != '_' && c != '\'' && c != '"' && c != '(' && c != ')' &&
                        c != '[' && c != ']')
                .count();

        // Si plus de 25% de caractères spéciaux, probablement pas du texte naturel
        double ratioSpeciaux = (double) caracteresSpeciaux / texte.length();
        return ratioSpeciaux > 0.25;
    }

    /**
     * Traduction avec l'API MyMemory (plus fiable et gratuite)
     */
    private String[] traduireAvecMyMemory(String texte, String langueDestination) throws Exception {
        // Détecter la langue source d'abord - MyMemory ne supporte pas "auto"
        String langueSource = detecterLangueSimple(texte);

        // Si la détection simple échoue, essayer avec Google
        if (langueSource.equals("auto")) {
            try {
                langueSource = detecterLangueAvecGoogle(texte);
                System.out.println("Langue détectée par Google: " + langueSource);
            } catch (Exception e) {
                System.err.println("Détection Google échouée, utilisation de 'en' par défaut");
                langueSource = "en"; // Par défaut anglais
            }
        }

        // Vérifier que la langue source n'est pas "auto"
        if (langueSource.equals("auto")) {
            langueSource = "en"; // Fallback vers anglais
        }

        System.out.println("Langue source pour MyMemory: " + langueSource + " -> " + langueDestination);

        // Construire l'URL pour MyMemory
        String langpair = langueSource + "|" + langueDestination;
        String url = "https://api.mymemory.translated.net/get?q="
                + URLEncoder.encode(texte, "UTF-8")
                + "&langpair=" + langpair;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (JavaFX Translation App)");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parser la réponse JSON avec Gson
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

        if (jsonResponse.has("responseData") && !jsonResponse.get("responseData").isJsonNull()) {
            JsonObject responseData = jsonResponse.getAsJsonObject("responseData");
            if (responseData.has("translatedText")) {
                String traduction = responseData.get("translatedText").getAsString();
                System.out.println("Traduction MyMemory réussie: " + traduction.substring(0, Math.min(50, traduction.length())));
                return new String[]{langueSource, traduction};
            }
        }

        throw new Exception("Réponse MyMemory invalide");
    }

    /**
     * Détection simple de langue basée sur des patterns
     */
    private String detecterLangueSimple(String texte) {
        texte = texte.toLowerCase().trim();

        // Patterns pour détecter les langues courantes
        if (texte.matches(".*\\b(the|and|or|but|in|on|at|to|for|with|by)\\b.*")) {
            return "en";
        }
        if (texte.matches(".*\\b(le|la|les|et|ou|mais|dans|sur|pour|avec|par|de|du|des)\\b.*")) {
            return "fr";
        }
        if (texte.matches(".*\\b(el|la|los|las|y|o|pero|en|con|por|para|de|del)\\b.*")) {
            return "es";
        }
        if (texte.matches(".*\\b(der|die|das|und|oder|aber|in|auf|mit|von|zu)\\b.*")) {
            return "de";
        }

        // Pattern pour détecter si c'est probablement de l'anglais (beaucoup de mots courts)
        String[] words = texte.split("\\s+");
        int shortWords = 0;
        for (String word : words) {
            if (word.length() <= 3) shortWords++;
        }
        if (shortWords > words.length * 0.3) {
            return "en";
        }

        return "auto"; // Langue inconnue
    }

    /**
     * Version améliorée de la traduction Google avec parsing JSON robuste
     */
    private String traduireAvecGoogleTranslateAmeliore(String texte, String langueSource, String langueDestination) throws Exception {
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + langueSource
                + "&tl=" + langueDestination + "&dt=t&q=" + URLEncoder.encode(texte, "UTF-8");

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (JavaFX Translation App)");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        String jsonResponse = response.toString();
        return extraireTraductionAmeliore(jsonResponse);
    }

    /**
     * Extraction améliorée de la traduction avec parsing JSON robuste
     */
    private String extraireTraductionAmeliore(String jsonResponse) {
        try {
            System.out.println("Réponse JSON brute: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())) + "...");

            // Essayer d'abord le parsing JSON avec Gson
            try {
                JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();
                if (jsonArray.size() > 0 && jsonArray.get(0).isJsonArray()) {
                    JsonArray translationsArray = jsonArray.get(0).getAsJsonArray();
                    StringBuilder traduction = new StringBuilder();

                    for (JsonElement element : translationsArray) {
                        if (element.isJsonArray()) {
                            JsonArray translationPart = element.getAsJsonArray();
                            if (translationPart.size() > 0) {
                                traduction.append(translationPart.get(0).getAsString());
                            }
                        }
                    }

                    if (traduction.length() > 0) {
                        String result = traduction.toString().trim();
                        System.out.println("Traduction extraite avec Gson: " + result);
                        return result;
                    }
                }
            } catch (Exception e) {
                System.err.println("Parsing Gson échoué, essai regex: " + e.getMessage());
            }

            // Fallback avec regex plus robuste
            Pattern pattern = Pattern.compile("\\[\\[\\[\"([^\"]+)\"", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(jsonResponse);

            StringBuilder traduction = new StringBuilder();
            while (matcher.find()) {
                traduction.append(matcher.group(1));
            }

            if (traduction.length() > 0) {
                String result = traduction.toString()
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
                System.out.println("Traduction extraite avec regex: " + result);
                return result;
            }

            // Dernière tentative avec split amélioré
            if (jsonResponse.contains("[[\"")) {
                String[] parts = jsonResponse.split("\\[\\[\\[\"");
                if (parts.length > 1) {
                    String[] subParts = parts[1].split("\"");
                    if (subParts.length > 0) {
                        String result = subParts[0].replace("\\n", "\n");
                        System.out.println("Traduction extraite avec split: " + result);
                        return result;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction de la traduction: " + e.getMessage());
            e.printStackTrace();
        }

        return "Erreur lors de l'extraction de la traduction";
    }

    private String detecterLangueAvecGoogle(String texte) throws Exception {
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q="
                + URLEncoder.encode(texte.substring(0, Math.min(100, texte.length())), "UTF-8");

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Extraction améliorée de la langue détectée
        String jsonResponse = response.toString();
        Pattern langPattern = Pattern.compile("\"([a-z]{2})\"(?=,null,null,)");
        Matcher matcher = langPattern.matcher(jsonResponse);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Fallback
        return detecterLangueSimple(texte);
    }

    /**
     * Inverser les langues source et destination
     */
    private void inverserLangues() {
        // Vérifier qu'il y a du contenu à inverser
        String texteSource = zoneTexteSource.getText().trim();
        String traduction = zoneTexteDestination.getText().trim();

        if (texteSource.isEmpty() || traduction.isEmpty()) {
            // Afficher un message temporaire si pas de contenu
            Stage stage = (Stage) zoneTexteSource.getScene().getWindow();
            String titreOriginal = stage.getTitle();
            stage.setTitle("⚠️ Rien à inverser - Traduisez d'abord du texte");

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> stage.setTitle(titreOriginal));
                }
            }, 2000);
            return;
        }

        // Obtenir les langues actuelles
        String langueSourceActuelle = comboLangueSource.getValue();
        String langueDestinationActuelle = comboLangueDestination.getValue();

        // Si la langue source était en détection automatique, utiliser la langue détectée
        if (langueSourceActuelle.equals("Détection automatique")) {
            langueSourceActuelle = obtenirNomLangue(derniereLangueSourceDetectee);
        }

        // Inverser : mettre la traduction dans la zone source
        zoneTexteSource.setText(traduction);

        // Inverser les sélecteurs de langues
        comboLangueSource.setValue(langueDestinationActuelle);
        comboLangueDestination.setValue(langueSourceActuelle);

        // Vider la zone de destination (elle se remplira automatiquement)
        zoneTexteDestination.clear();
        labelLangueDetectee.setText("Langue sélectionnée : " + langueDestinationActuelle);

        // Feedback visuel
        Stage stage = (Stage) zoneTexteSource.getScene().getWindow();
        String titreOriginal = stage.getTitle();
        stage.setTitle("🔄 Langues inversées !");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> stage.setTitle(titreOriginal));
            }
        }, 1500);

        System.out.println("Inversion: " + langueDestinationActuelle + " -> " + langueSourceActuelle);
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
            ignorerProchainClipboard = true;

            ClipboardContent contenu = new ClipboardContent();
            contenu.putString(traduction);
            Clipboard.getSystemClipboard().setContent(contenu);

            dernierTexteClipboard = traduction;

            Stage stage = (Stage) zoneTexteDestination.getScene().getWindow();
            String titreOriginal = stage.getTitle();
            stage.setTitle("✅ Traduction copiée!");

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> stage.setTitle(titreOriginal));
                }
            }, 2000);

            System.out.println("Traduction copiée: " + traduction.substring(0, Math.min(50, traduction.length())) + "...");
        }
    }

    private void creerDossierLogs() {
        try {
            Files.createDirectories(Paths.get("logs"));
            System.out.println("Dossier de logs créé/vérifié: logs/");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du dossier logs: " + e.getMessage());
        }
    }

    private void enregistrerTraduction(String texteSource, String traduction, String langueSource, String langueDestination) {
        try {
            LocalDateTime maintenant = LocalDateTime.now();
            String jourActuel = maintenant.format(FORMAT_FICHIER);
            String timestamp = maintenant.format(FORMAT_TIMESTAMP);

            String nomFichier = "logs/traductions_" + jourActuel + ".csv";

            boolean nouveauFichier = !jourActuel.equals(dernierJourFichier);
            dernierJourFichier = jourActuel;

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

            if (nouveauFichier && !Files.exists(Paths.get(nomFichier))) {
                String entete = "\"Timestamp\",\"Texte Source\",\"Traduction\",\"Langue Source\",\"Langue Destination\"" + System.lineSeparator();
                Files.write(Paths.get(nomFichier), entete.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println("Nouveau fichier de logs créé: " + nomFichier);
            }

            Files.write(Paths.get(nomFichier), ligne.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("Traduction enregistrée dans: " + nomFichier);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement des logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String echapperCSV(String texte) {
        if (texte == null) return "";

        String resultat = texte.replace("\"", "\"\"");
        resultat = resultat.replace("\n", " ").replace("\r", " ");

        if (resultat.length() > 1000) {
            resultat = resultat.substring(0, 997) + "...";
        }

        return resultat;
    }

    private void arreterApplication() {
        System.out.println("Arrêt des services en cours...");

        if (timerSurveillance != null) {
            timerSurveillance.cancel();
            timerSurveillance = null;
            System.out.println("Timer de surveillance arrêté");
        }

        System.out.println("Application fermée proprement");
    }

    public static void main(String[] args) {
        launch(args);
    }
}