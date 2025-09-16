package net.arkaine;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
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
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TraducteurAutomatique extends JFrame {

    // Composants UI
    private JTextArea zoneTexteSource;
    private JTextArea zoneTexteDestination;
    private JComboBox<String> comboLangueSource;
    private JComboBox<String> comboLangueDestination;
    private JLabel labelLangueDetectee;
    private JProgressBar barreProgression;
    private JButton boutonTraduire;
    private JButton boutonInverser;
    private JButton boutonCapture;
    private JButton boutonCopier;
    private JCheckBox checkboxSurveillance;

    // Variables d'état
    private String derniereLangueSourceDetectee = "en";
    private String dernierTexteClipboard = "";
    private String sauvegardeClipboard = "";
    private boolean applicationALeFocus = true;
    private boolean ignorerProchainClipboard = false;

    // Système de logging
    private static final DateTimeFormatter FORMAT_FICHIER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FORMAT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String dernierJourFichier = "";

    // Timer pour la surveillance du clipboard
    private Timer timerSurveillance;
    private Timer timerTraductionDelai;

    // Mapping des langues
    private Map<String, String> langues = new HashMap<>();

    public TraducteurAutomatique() {
        initLangues();
        initUI();
        setupEvents();
        creerDossierLogs();
        demarrerSurveillanceClipboard();
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

    private void initUI() {
        setTitle("Traducteur Automatique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLocationRelativeTo(null);

        // Configuration du layout principal
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel principal avec scrolling
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));

        // === Panel de sélection des langues ===
        JPanel panelLangues = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelLangues.setBorder(new CompoundBorder(
                new TitledBorder("Sélection des langues"),
                new EmptyBorder(5, 5, 5, 5)
        ));

        // Langue source
        panelLangues.add(new JLabel("De :"));
        comboLangueSource = new JComboBox<>();
        comboLangueSource.addItem("Détection automatique");
        for (String langue : langues.keySet()) {
            comboLangueSource.addItem(langue);
        }
        comboLangueSource.setSelectedItem("Détection automatique");
        comboLangueSource.setPreferredSize(new Dimension(170, 30));
        panelLangues.add(comboLangueSource);

        // Bouton d'inversion
        boutonInverser = new JButton("⇄");
        boutonInverser.setPreferredSize(new Dimension(40, 30));
        boutonInverser.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        boutonInverser.setToolTipText("Inverser les langues (source ↔ destination)");
        panelLangues.add(boutonInverser);

        // Langue destination
        panelLangues.add(new JLabel("Vers :"));
        comboLangueDestination = new JComboBox<>();
        for (String langue : langues.keySet()) {
            comboLangueDestination.addItem(langue);
        }
        comboLangueDestination.setSelectedItem("Français");
        comboLangueDestination.setPreferredSize(new Dimension(170, 30));
        panelLangues.add(comboLangueDestination);

        panelPrincipal.add(panelLangues);

        // === Label de langue détectée ===
        labelLangueDetectee = new JLabel("Langue détectée : Aucune");
        labelLangueDetectee.setFont(labelLangueDetectee.getFont().deriveFont(Font.ITALIC));
        labelLangueDetectee.setBorder(new EmptyBorder(5, 10, 10, 10));
        panelPrincipal.add(labelLangueDetectee);

        // === Panel texte source ===
        JPanel panelTexteSource = new JPanel(new BorderLayout(5, 5));
        panelTexteSource.setBorder(new CompoundBorder(
                new TitledBorder("Texte à traduire"),
                new EmptyBorder(5, 5, 5, 5)
        ));

        zoneTexteSource = new JTextArea(8, 50);
        zoneTexteSource.setLineWrap(true);
        zoneTexteSource.setWrapStyleWord(true);
        zoneTexteSource.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        zoneTexteSource.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Auto-scroll vers le bas quand on tape
        DefaultCaret caretSource = (DefaultCaret) zoneTexteSource.getCaret();
        caretSource.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollSource = new JScrollPane(zoneTexteSource);
        scrollSource.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollSource.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelTexteSource.add(scrollSource, BorderLayout.CENTER);

        panelPrincipal.add(panelTexteSource);

        // === Panel boutons d'action ===
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        boutonTraduire = new JButton("Traduire");
        boutonTraduire.setPreferredSize(new Dimension(100, 35));
        panelBoutons.add(boutonTraduire);

        boutonCapture = new JButton("📷 Capturer écran");
        boutonCapture.setPreferredSize(new Dimension(150, 35));
        boutonCapture.setToolTipText("Capturer une zone de l'écran et traduire le texte (OCR)");
        panelBoutons.add(boutonCapture);

        // Barre de progression
        barreProgression = new JProgressBar();
        barreProgression.setIndeterminate(true);
        barreProgression.setVisible(false);
        barreProgression.setPreferredSize(new Dimension(100, 25));
        panelBoutons.add(barreProgression);

        panelPrincipal.add(panelBoutons);

        // === Panel texte destination ===
        JPanel panelTexteDestination = new JPanel(new BorderLayout(5, 5));
        panelTexteDestination.setBorder(new CompoundBorder(
                new TitledBorder("Traduction"),
                new EmptyBorder(5, 5, 5, 5)
        ));

        zoneTexteDestination = new JTextArea(8, 50);
        zoneTexteDestination.setLineWrap(true);
        zoneTexteDestination.setWrapStyleWord(true);
        zoneTexteDestination.setEditable(false);
        zoneTexteDestination.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        zoneTexteDestination.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Couleur de fond pour indiquer que c'est non-éditable
        zoneTexteDestination.setBackground(UIManager.getColor("TextField.inactiveBackground"));

        DefaultCaret caretDest = (DefaultCaret) zoneTexteDestination.getCaret();
        caretDest.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollDestination = new JScrollPane(zoneTexteDestination);
        scrollDestination.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDestination.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelTexteDestination.add(scrollDestination, BorderLayout.CENTER);

        panelPrincipal.add(panelTexteDestination);

        // === Bouton copier ===
        JPanel panelCopier = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        boutonCopier = new JButton("Copier la traduction (Ctrl+C)");
        boutonCopier.setPreferredSize(new Dimension(220, 35));
        panelCopier.add(boutonCopier);
        panelPrincipal.add(panelCopier);

        // === Panel configuration ===
        JPanel panelConfig = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelConfig.setBorder(BorderFactory.createEtchedBorder());

        checkboxSurveillance = new JCheckBox("Surveiller le presse-papiers", true);
        panelConfig.add(checkboxSurveillance);

        // Bouton changement de thème
        JButton boutonTheme = new JButton("🌙 Thème");
        boutonTheme.setToolTipText("Changer le thème de l'interface");
        boutonTheme.addActionListener(e -> changerTheme());
        panelConfig.add(boutonTheme);

        panelPrincipal.add(panelConfig);

        // === Instructions ===
        JTextArea instructions = new JTextArea(
                "💡 Astuce: Sélectionnez du texte → Ctrl+C → Traduction automatique\n" +
                        "📷 Capture d'écran: Cliquez sur 'Capturer écran' puis sélectionnez la zone\n" +
                        "🚫 Code source et textes > 5000 caractères filtrés automatiquement");
        instructions.setEditable(false);
        instructions.setOpaque(false);
        instructions.setFont(instructions.getFont().deriveFont(Font.ITALIC, 11f));
        instructions.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelPrincipal.add(instructions);

        // Ajouter le panel principal dans un scroll pane
        JScrollPane scrollPrincipal = new JScrollPane(panelPrincipal);
        scrollPrincipal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPrincipal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPrincipal.setBorder(null);

        add(scrollPrincipal, BorderLayout.CENTER);
    }

    private void setupEvents() {
        // Événements de la fenêtre
        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                applicationALeFocus = true;
                System.out.println("Application focus: true");
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                applicationALeFocus = false;
                System.out.println("Application focus: false");
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Fermeture de l'application...");
                arreterApplication();
                System.exit(0);
            }
        });

        // Événements des boutons
        boutonTraduire.addActionListener(e -> traduireTexte());
        boutonInverser.addActionListener(e -> inverserLangues());
        boutonCapture.addActionListener(e -> demarrerCaptureEcran());
        boutonCopier.addActionListener(e -> copierTraduction());

        // Traduction automatique avec délai
        zoneTexteSource.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { planifierTraduction(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { planifierTraduction(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { planifierTraduction(); }
        });

        // Traduction quand les langues changent
        comboLangueSource.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                String nouveauChoix = (String) comboLangueSource.getSelectedItem();
                if (nouveauChoix != null) {
                    String texte = zoneTexteSource.getText().trim();
                    if (!texte.isEmpty()) {
                        // Mettre à jour l'affichage
                        if (nouveauChoix.equals("Détection automatique")) {
                            labelLangueDetectee.setText("Langue détectée : Auto");
                        } else {
                            labelLangueDetectee.setText("Langue sélectionnée : " + nouveauChoix);
                        }
                        traduireTexte();
                    }
                }
            });
        });

        comboLangueDestination.addActionListener(e -> {
            String texte = zoneTexteSource.getText().trim();
            if (!texte.isEmpty()) {
                SwingUtilities.invokeLater(this::traduireTexte);
            }
        });

        // Raccourci clavier Ctrl+C global
        KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlC, "copier");
        getRootPane().getActionMap().put("copier", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner == zoneTexteDestination || focusOwner == boutonCopier ||
                        focusOwner == null || focusOwner == getRootPane()) {
                    copierTraduction();
                }
            }
        });
    }

    private void planifierTraduction() {
        if (timerTraductionDelai != null) {
            timerTraductionDelai.stop();
        }

        timerTraductionDelai = new Timer(1000, e -> {
            String texte = zoneTexteSource.getText().trim();
            if (!texte.isEmpty()) {
                traduireTexte();
            }
            timerTraductionDelai.stop();
        });
        timerTraductionDelai.setRepeats(false);
        timerTraductionDelai.start();
    }

    private void changerTheme() {
        String[] themes = {
                "FlatLaf Light", "FlatLaf Dark", "FlatLaf IntelliJ",
                "Arc", "Arc Orange", "Carbon", "Cobalt 2", "Cyan Light",
                "Dark Flat", "Dark Purple", "Dracula", "GitHub", "Gruvbox Dark",
                "High Contrast", "Light Flat", "Material Theme UI Lite",
                "Monokai Pro", "Nord", "One Dark", "Solarized Dark", "Solarized Light"
        };

        String choix = (String) JOptionPane.showInputDialog(
                this, "Choisissez un thème :", "Sélection du thème",
                JOptionPane.QUESTION_MESSAGE, null, themes, themes[0]);

        if (choix != null) {
            appliquerTheme(choix);
        }
    }

    private void appliquerTheme(String nomTheme) {
        try {
            LookAndFeel nouveauTheme = switch (nomTheme) {
                case "FlatLaf Light" -> new FlatLightLaf();
                case "FlatLaf Dark" -> new FlatDarculaLaf();
                case "FlatLaf IntelliJ" -> new FlatIntelliJLaf();
                case "Arc" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatArcIJTheme").getDeclaredConstructor().newInstance();
                case "Arc Orange" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme").getDeclaredConstructor().newInstance();
                case "Carbon" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme").getDeclaredConstructor().newInstance();
                case "Cobalt 2" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme").getDeclaredConstructor().newInstance();
                case "Cyan Light" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme").getDeclaredConstructor().newInstance();
                case "Dark Flat" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme").getDeclaredConstructor().newInstance();
                case "Dark Purple" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme").getDeclaredConstructor().newInstance();
                case "Dracula" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme").getDeclaredConstructor().newInstance();
                case "GitHub" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatGitHubIJTheme").getDeclaredConstructor().newInstance();
                case "Gruvbox Dark" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkMediumIJTheme").getDeclaredConstructor().newInstance();
                case "High Contrast" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme").getDeclaredConstructor().newInstance();
                case "Light Flat" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme").getDeclaredConstructor().newInstance();
                case "Material Theme UI Lite" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme").getDeclaredConstructor().newInstance();
                case "Monokai Pro" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme").getDeclaredConstructor().newInstance();
                case "Nord" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatNordIJTheme").getDeclaredConstructor().newInstance();
                case "One Dark" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme").getDeclaredConstructor().newInstance();
                case "Solarized Dark" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme").getDeclaredConstructor().newInstance();
                case "Solarized Light" -> (LookAndFeel) Class.forName("com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme").getDeclaredConstructor().newInstance();
                default -> new FlatIntelliJLaf();
            };

            FlatAnimatedLafChange.showSnapshot();
            UIManager.setLookAndFeel(nouveauTheme);
            FlatLaf.updateUI();
            FlatAnimatedLafChange.hideSnapshotWithAnimation();

            // Mettre à jour la couleur de fond de la zone de destination
            zoneTexteDestination.setBackground(UIManager.getColor("TextField.inactiveBackground"));

        } catch (Exception e) {
            System.err.println("Erreur lors du changement de thème: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Impossible d'appliquer le thème " + nomTheme + "\n" + e.getMessage(),
                    "Erreur de thème", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void demarrerSurveillanceClipboard() {
        if (timerSurveillance != null) {
            timerSurveillance.stop();
        }

        timerSurveillance = new Timer(1000, e -> {
            if (checkboxSurveillance.isSelected() && !applicationALeFocus && !ignorerProchainClipboard) {
                try {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                        String contenu = (String) clipboard.getData(DataFlavor.stringFlavor);
                        if (contenu != null && !contenu.equals(dernierTexteClipboard) &&
                                contenu.trim().length() > 0) {

                            // Vérifications de sécurité pour le clipboard (plus strict)
                            if (contenu.length() <= 2000 && !ressembleADuCode(contenu)) {
                                sauvegardeClipboard = contenu;
                                dernierTexteClipboard = contenu;

                                SwingUtilities.invokeLater(() -> {
                                    zoneTexteSource.setText(contenu);
                                    System.out.println("Nouveau texte détecté: " +
                                            contenu.substring(0, Math.min(50, contenu.length())) + "...");
                                });
                            } else {
                                System.out.println("Texte clipboard ignoré - " +
                                        (contenu.length() > 2000 ? "trop long (" + contenu.length() + " caractères)" : "code source détecté"));
                                dernierTexteClipboard = contenu; // Pour éviter de retraiter
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Erreur lors de la lecture du presse-papiers: " + ex.getMessage());
                }
            } else if (ignorerProchainClipboard) {
                ignorerProchainClipboard = false;
                System.out.println("Clipboard ignoré après copie interne");
            }
        });
        timerSurveillance.start();
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

        String langueDestination = langues.get((String) comboLangueDestination.getSelectedItem());

        // Désactiver les contrôles pendant la traduction
        boutonTraduire.setEnabled(false);
        barreProgression.setVisible(true);

        // Traduction asynchrone
        CompletableFuture.supplyAsync(() -> {
            try {
                String langueSource;
                String langueSourceSelectionnee = (String) comboLangueSource.getSelectedItem();

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

            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la traduction: " + e.getMessage(), e);
            }
        }).whenComplete((resultat, exception) -> {
            SwingUtilities.invokeLater(() -> {
                boutonTraduire.setEnabled(true);
                barreProgression.setVisible(false);

                if (exception != null) {
                    zoneTexteDestination.setText("Erreur lors de la traduction : " + exception.getMessage());
                    System.err.println("Erreur de traduction: " + exception.getMessage());
                } else {
                    String langueSource = resultat[0];
                    String traduction = resultat[1];

                    // Affichage différencié selon le mode
                    String langueSourceSelectionnee = (String) comboLangueSource.getSelectedItem();
                    if (langueSourceSelectionnee != null && !langueSourceSelectionnee.equals("Détection automatique")) {
                        labelLangueDetectee.setText("Langue sélectionnée : " + langueSourceSelectionnee);
                    } else {
                        labelLangueDetectee.setText("Langue détectée : " + obtenirNomLangue(langueSource));
                    }

                    zoneTexteDestination.setText(traduction);

                    // Sauvegarder la dernière langue source pour l'inversion
                    derniereLangueSourceDetectee = langueSource;

                    enregistrerTraduction(zoneTexteSource.getText().trim(), traduction, langueSource, langueDestination);
                }
            });
        });
    }

    /**
     * Démarrer la capture d'écran avec sélection de zone
     */
    private void demarrerCaptureEcran() {
        try {
            // Minimiser la fenêtre principale temporairement
            setExtendedState(JFrame.ICONIFIED);

            // Attendre un peu que la fenêtre se minimise
            Timer timer = new Timer(500, e -> creerOverlaySelection());
            timer.setRepeats(false);
            timer.start();

        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de la capture: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Impossible de démarrer la capture d'écran: " + e.getMessage(),
                    "Erreur de capture", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Créer l'overlay de sélection - UNE SEULE fenêtre couvrant TOUS les écrans
     */
    private void creerOverlaySelection() {
        try {
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

            // Variables pour la sélection
            final Rectangle[] zoneSelection = {null};
            final int[] startX = {0};
            final int[] startY = {0};
            final boolean[] isSelecting = {false};

            // Créer UNE SEULE fenêtre qui couvre tous les écrans
            JWindow overlayWindow = new JWindow();
            overlayWindow.setAlwaysOnTop(true);
            overlayWindow.setBounds(zoneTotale);
            overlayWindow.setBackground(new Color(0, 0, 0, 100)); // Semi-transparent

            // Panel personnalisé pour la sélection
            Rectangle finalZoneTotale = zoneTotale;
            JPanel overlayPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();

                    // Dessiner l'image de fond assombrie
                    g2d.drawImage(captureComplete, -finalZoneTotale.x, -finalZoneTotale.y, null);
                    g2d.setColor(new Color(0, 0, 0, 80));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    // Dessiner la zone de sélection si elle existe
                    if (zoneSelection[0] != null) {
                        Rectangle sel = zoneSelection[0];

                        // Zone claire (sélectionnée)
                        int localX = sel.x - finalZoneTotale.x;
                        int localY = sel.y - finalZoneTotale.y;

                        if (localX >= 0 && localY >= 0 &&
                                localX + sel.width <= getWidth() && localY + sel.height <= getHeight()) {

                            // Effacer l'assombrissement dans la zone sélectionnée
                            g2d.setComposite(AlphaComposite.Clear);
                            g2d.fillRect(localX, localY, sel.width, sel.height);

                            // Redessiner l'image originale dans la zone
                            g2d.setComposite(AlphaComposite.SrcOver);
                            g2d.drawImage(captureComplete.getSubimage(sel.x, sel.y, sel.width, sel.height),
                                    localX, localY, null);

                            // Bordure de sélection
                            g2d.setColor(Color.RED);
                            g2d.setStroke(new BasicStroke(3));
                            g2d.drawRect(localX, localY, sel.width, sel.height);

                            // Afficher les dimensions
                            g2d.setColor(Color.WHITE);
                            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                            String info = sel.width + " × " + sel.height + " px";

                            int textX = localX + 5;
                            int textY = localY - 10;
                            if (textY < 20) textY = localY + 20;

                            // Outline noir pour le texte
                            g2d.setColor(Color.BLACK);
                            for (int dx = -1; dx <= 1; dx++) {
                                for (int dy = -1; dy <= 1; dy++) {
                                    if (dx != 0 || dy != 0) {
                                        g2d.drawString(info, textX + dx, textY + dy);
                                    }
                                }
                            }
                            g2d.setColor(Color.WHITE);
                            g2d.drawString(info, textX, textY);

                            // Croix au centre
                            g2d.setColor(Color.RED);
                            g2d.setStroke(new BasicStroke(1));
                            int centerX = localX + sel.width / 2;
                            int centerY = localY + sel.height / 2;
                            g2d.drawLine(centerX - 10, centerY, centerX + 10, centerY);
                            g2d.drawLine(centerX, centerY - 10, centerX, centerY + 10);
                        }
                    }

                    g2d.dispose();
                }
            };

            // Gestion des événements souris
            Rectangle finalZoneTotale1 = zoneTotale;
            overlayPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startX[0] = e.getX() + finalZoneTotale1.x;
                    startY[0] = e.getY() + finalZoneTotale1.y;
                    isSelecting[0] = true;
                    System.out.println("Début sélection globale: " + startX[0] + ", " + startY[0]);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isSelecting[0] && zoneSelection[0] != null) {
                        isSelecting[0] = false;

                        System.out.println("Zone sélectionnée globale: " + zoneSelection[0]);

                        // Fermer l'overlay
                        overlayWindow.dispose();

                        // Traiter la capture si la sélection est suffisante
                        if (zoneSelection[0].width > 10 && zoneSelection[0].height > 10) {
                            traiterCaptureZone(zoneSelection[0]);
                        } else {
                            System.out.println("Sélection trop petite ignorée");
                            restaurerFenetrePrincipale();
                        }
                    }
                }
            });

            Rectangle finalZoneTotale2 = zoneTotale;
            overlayPanel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isSelecting[0]) {
                        int endX = e.getX() + finalZoneTotale2.x;
                        int endY = e.getY() + finalZoneTotale2.y;

                        int x = Math.min(startX[0], endX);
                        int y = Math.min(startY[0], endY);
                        int w = Math.abs(endX - startX[0]);
                        int h = Math.abs(endY - startY[0]);

                        zoneSelection[0] = new Rectangle(x, y, w, h);
                        overlayPanel.repaint();
                    }
                }
            });

            // Gestion du clavier pour annuler (Escape)
            overlayPanel.setFocusable(true);
            overlayPanel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        overlayWindow.dispose();
                        restaurerFenetrePrincipale();
                    }
                }
            });

            overlayWindow.add(overlayPanel);
            overlayWindow.setVisible(true);
            overlayPanel.requestFocusInWindow();

            System.out.println("Overlay unifié créé couvrant tous les écrans: " + zoneTotale);

        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'overlay: " + e.getMessage());
            restaurerFenetrePrincipale();
        }
    }

    /**
     * Traiter la capture de la zone sélectionnée
     */
    private void traiterCaptureZone(Rectangle zone) {
        CompletableFuture.supplyAsync(() -> {
            try {
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

            } catch (Exception e) {
                throw new RuntimeException("Erreur OCR: " + e.getMessage(), e);
            }
        }).whenComplete((texte, exception) -> {
            SwingUtilities.invokeLater(() -> {
                if (exception != null) {
                    System.err.println("Erreur OCR: " + exception.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors de l'extraction du texte: " + exception.getMessage(),
                            "Erreur OCR", JOptionPane.ERROR_MESSAGE);
                } else if (texte != null && !texte.trim().isEmpty()) {
                    System.out.println("Texte OCR extrait: " + texte);
                    zoneTexteSource.setText(texte.trim());
                    // La traduction se déclenchera automatiquement
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Aucun texte détecté dans la capture.\nEssayez avec une image plus nette ou une zone plus grande.",
                            "OCR", JOptionPane.INFORMATION_MESSAGE);
                }
                restaurerFenetrePrincipale();
            });
        });

        // Afficher l'indicateur de progression
        SwingUtilities.invokeLater(() -> {
            barreProgression.setVisible(true);
            setTitle("🔍 Extraction du texte en cours...");
        });
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
            writer.append("eng").append("\r\n");

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
        SwingUtilities.invokeLater(() -> {
            try {
                setExtendedState(JFrame.NORMAL);
                toFront();
                requestFocus();
                setTitle("Traducteur Automatique");
                barreProgression.setVisible(false);
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
            String titreOriginal = getTitle();
            setTitle("⚠️ Rien à inverser - Traduisez d'abord du texte");

            Timer timer = new Timer(2000, e -> setTitle(titreOriginal));
            timer.setRepeats(false);
            timer.start();
            return;
        }

        // Obtenir les langues actuelles
        String langueSourceActuelle = (String) comboLangueSource.getSelectedItem();
        String langueDestinationActuelle = (String) comboLangueDestination.getSelectedItem();

        // Si la langue source était en détection automatique, utiliser la langue détectée
        if (langueSourceActuelle.equals("Détection automatique")) {
            langueSourceActuelle = obtenirNomLangue(derniereLangueSourceDetectee);
        }

        // Inverser : mettre la traduction dans la zone source
        zoneTexteSource.setText(traduction);

        // Inverser les sélecteurs de langues
        comboLangueSource.setSelectedItem(langueDestinationActuelle);
        comboLangueDestination.setSelectedItem(langueSourceActuelle);

        // Vider la zone de destination (elle se remplira automatiquement)
        zoneTexteDestination.setText("");
        labelLangueDetectee.setText("Langue sélectionnée : " + langueDestinationActuelle);

        // Feedback visuel
        String titreOriginal = getTitle();
        setTitle("🔄 Langues inversées !");

        Timer timer = new Timer(1500, e -> setTitle(titreOriginal));
        timer.setRepeats(false);
        timer.start();

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

            StringSelection stringSelection = new StringSelection(traduction);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

            dernierTexteClipboard = traduction;

            String titreOriginal = getTitle();
            setTitle("✅ Traduction copiée!");

            Timer timer = new Timer(2000, e -> setTitle(titreOriginal));
            timer.setRepeats(false);
            timer.start();

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
            timerSurveillance.stop();
            timerSurveillance = null;
            System.out.println("Timer de surveillance arrêté");
        }

        if (timerTraductionDelai != null) {
            timerTraductionDelai.stop();
            timerTraductionDelai = null;
            System.out.println("Timer de traduction arrêté");
        }

        System.out.println("Application fermée proprement");
    }

    public static void main(String[] args) {
        // Configurer le Look and Feel avant de créer l'interface
        try {
            // Activer les propriétés système pour FlatLaf
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");

            // Utiliser le thème sombre par défaut
            UIManager.setLookAndFeel(new FlatIntelliJLaf());

            // Configuration des couleurs personnalisées
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 6);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        } catch (Exception e) {
            System.err.println("Impossible d'initialiser FlatLaf, utilisation du thème par défaut: " + e.getMessage());
            try {
                // Utiliser le Look and Feel système par défaut
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Impossible d'initialiser le thème système: " + ex.getMessage());
                // Garder le Look and Feel par défaut de Java
            }
        }

        // Créer et afficher l'interface utilisateur
        SwingUtilities.invokeLater(() -> {
            try {
                new TraducteurAutomatique().setVisible(true);
                System.out.println("Application Swing lancée avec succès!");
            } catch (Exception e) {
                System.err.println("Erreur lors du lancement de l'application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}