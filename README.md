# Traducteur Automatique - Version Swing avec FlatLaf

Une application Java Swing moderne de traduction automatique avec surveillance du presse-papiers en temps réel, sélection manuelle des langues, inversion bidirectionnelle, capture d'écran OCR et système de logging avancé.

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Swing](https://img.shields.io/badge/Swing-Built--in-blue.svg)
![FlatLaf](https://img.shields.io/badge/FlatLaf-3.4.1-green.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 🆕 Version Swing - Nouveautés

### ✨ Migration de JavaFX vers Swing + FlatLaf
- **Look moderne** avec FlatLaf (Look and Feel moderne inspiré d'IntelliJ IDEA)
- **25+ thèmes disponibles** : Arc, Darcula, GitHub, Monokai, Nord, One Dark, Solarized, etc.
- **Changement de thème à chaud** avec animations fluides
- **Meilleure compatibilité** avec tous les systèmes d'exploitation
- **Performance améliorée** et démarrage plus rapide
- **Interface responsive** qui s'adapte à toutes les tailles d'écran

### 🎨 Interface Utilisateur Moderne
- **Design épuré** avec bordures arrondies et animations
- **Thème sombre par défaut** (IntelliJ) avec possibilité de basculer
- **Icônes et emojis** pour une navigation intuitive
- **Barres de progression** avec indicateurs visuels
- **Zones de texte** avec auto-scroll et coloration syntaxique
- **Tooltips informatifs** sur tous les contrôles

## 📋 Fonctionnalités

### ✨ Fonctionnalités Principales
- **Sélection de langue source** - Détection automatique OU choix manuel de la langue à traduire
- **Détection automatique intelligente** - Reconnaissance précise de la langue source avec fallbacks multiples
- **Traduction en temps réel** - Traduction instantanée dès la saisie ou la sélection de texte
- **Filtres de sécurité intelligents** - Protection contre la traduction de code source et textes inappropriés
- **Inversion des langues (⇄)** - Bouton pour inverser instantanément source ↔ destination avec échange des textes
- **Surveillance intelligente du presse-papiers** - Traduction automatique de tout texte copié avec filtrage automatique
- **Capture d'écran OCR** - Sélection de zone à l'écran pour extraire et traduire du texte
- **Changement de thème instantané** - Plus de 25 thèmes modernes disponibles
- **Raccourci clavier intelligent** - `Ctrl+C` contextuel (copie sélection dans zone source, copie traduction ailleurs)
- **Double API de traduction** - MyMemory (priorité) + Google Translate (fallback) pour une fiabilité maximale
- **Logging automatique** - Enregistrement de toutes les traductions dans des fichiers CSV quotidiens
- **Interface responsive** - S'adapte à toutes les tailles d'écran et résolutions

### 🎨 Thèmes Disponibles
- **FlatLaf Light/Dark/IntelliJ** - Thèmes de base modernes
- **Arc/Arc Orange** - Design inspiré d'Arc Linux
- **Carbon** - Thème sombre élégant
- **Cobalt 2** - Couleurs vives sur fond sombre
- **Cyan Light** - Thème clair avec accents cyan
- **Dark/Light Flat** - Variantes plates modernes
- **Dark Purple** - Nuances violettes élégantes
- **Dracula** - Le célèbre thème vampire
- **GitHub** - Style GitHub clair/sombre
- **Gruvbox Dark** - Couleurs chaudes et contrastées
- **High Contrast** - Accessibilité maximale
- **Material Theme UI Lite** - Design Material de Google
- **Monokai Pro** - Couleurs vives sur fond sombre
- **Nord** - Palette arctique apaisante
- **One Dark** - Thème Atom populaire
- **Solarized Light/Dark** - Palette scientifiquement optimisée

### 🛡️ Filtres de Sécurité Intelligents
- **Protection contre le code source** : Détection automatique et refus de traduire du code (JavaScript, Java, Python, HTML, CSS, SQL, JSON, etc.)
- **Limite de longueur intelligente** : Maximum 5000 caractères pour la traduction manuelle, 2000 pour le clipboard
- **Filtre de caractères spéciaux** : Évite la traduction de données binaires, logs système, ou formats techniques
- **Messages explicatifs** : Interface claire indiquant pourquoi un texte n'est pas traduit
- **Surveillance adaptée** : Le clipboard ignore automatiquement le code source sans notification intrusive

### 🔧 Sélection de Langue Source
- **Mode automatique** (par défaut) : Détection intelligente avec patterns linguistiques + Google Translate
- **Mode manuel** : Choix explicite de la langue source parmi 20+ langues supportées
- **Affichage intelligent** : "Langue détectée" vs "Langue sélectionnée" selon le mode
- **Traduction forcée** : Respecte le choix manuel même si le texte semble être dans une autre langue

### ↔️ Inversion Bidirectionnelle
- **Bouton ⇄ intuitif** : Situé entre les sélecteurs de langues
- **Inversion complète** : Échange automatique des textes ET des langues sélectionnées
- **Gestion intelligente** : Prend en compte le mode de détection (auto/manuel)
- **Feedback visuel** : Message temporaire "🔄 Langues inversées !" dans la barre de titre

### 📷 Capture d'Écran OCR
- **Sélection multi-écrans** : Support complet des configurations multi-moniteurs
- **Interface de sélection** : Overlay semi-transparent avec aperçu en temps réel
- **Informations visuelles** : Dimensions, coordonnées et croix de centrage
- **OCR intelligent** : Extraction de texte avec API OCR.space gratuite
- **Intégration transparente** : Texte extrait automatiquement traduit

### 🌐 APIs de Traduction Robustes
- **MyMemory API** (priorité) : Plus stable et fiable pour les textes complexes
- **Google Translate** (fallback) : Parsing JSON amélioré avec Gson + regex de secours
- **Gestion d'erreurs avancée** : Système de fallback automatique entre APIs
- **Parsing robuste** : Extraction complète des traductions longues et complexes

### 📊 Système de Logging Avancé
- **Fichiers CSV quotidiens** - Un nouveau fichier chaque jour (`traductions_YYYYMMDD.csv`)
- **Horodatage précis** - Date et heure de chaque traduction
- **Données complètes** - Texte source, traduction, langues détectées/sélectionnées
- **Format standard** - Compatible Excel, LibreOffice, Google Sheets
- **Dossier organisé** - Tous les logs dans le dossier `logs/`

### 🌍 Langues Supportées (20+)
- Français (par défaut)
- Anglais
- Espagnol
- Allemand
- Italien
- Portugais
- Russe
- Chinois
- Japonais
- Coréen
- Arabe
- Néerlandais
- Suédois
- Norvégien
- Danois
- Polonais
- Tchèque
- Hongrois
- Roumain
- Bulgare

## 🚀 Installation et Utilisation

### Prérequis
- **Java 21** ou supérieur
- **Maven 3.9** ou supérieur
- Connexion Internet (pour les traductions et OCR)

### Installation Rapide

1. **Cloner le repository**
   ```bash
   git clone https://github.com/OlivierMarcou/auto-translate-swing.git
   cd auto-translate-swing
   ```

2. **Lancement automatique (Windows)**
   ```bash
   run.bat
   ```

3. **Lancement automatique (Linux/Mac)**
   ```bash
   chmod +x run.sh
   ./run.sh
   ```

### Installation Manuelle

1. **Compiler le projet**
   ```bash
   mvn clean compile
   ```

2. **Exécuter directement**
   ```bash
   mvn exec:java -Dexec.mainClass="net.arkaine.TraducteurAutomatique"
   ```

3. **Créer un JAR exécutable**
   ```bash
   mvn clean package
   java -jar target/auto-translate-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

## 🖥️ Interface Utilisateur

### Capture d'Écran Moderne

```
┌────────────────────────────────────────────────────────────────────┐
│ 🌙 Traducteur Automatique                                    ⚙️ 🌙 │
├────────────────────────────────────────────────────────────────────┤
│ ┌── Sélection des langues ──────────────────────────────────────── │
│ │ De: [Détection automatique ▼] ⇄ Vers: [Français ▼]    │
│ └─────────────────────────────────────────────────────────────────┘ │
│ Langue détectée : Anglais                                           │
│                                                                    │
│ ┌── Texte à traduire ──────────────────────────────────────────── │
│ │ ┌──────────────────────────────────────────────────────────────┐ │
│ │ │ Hello, how are you today? I hope you're doing well.         │ │
│ │ │                                                              │ │
│ │ └──────────────────────────────────────────────────────────────┘ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                    │
│ [Traduire] [📷 Capturer écran] ●○○○○ (en cours...)                │
│                                                                    │
│ ┌── Traduction ────────────────────────────────────────────────── │
│ │ ┌──────────────────────────────────────────────────────────────┐ │
│ │ │ Bonjour, comment allez-vous aujourd'hui ? J'espère que      │ │
│ │ │ vous allez bien.                                             │ │
│ │ └──────────────────────────────────────────────────────────────┘ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                    │
│ [Copier la traduction (Ctrl+C)]                                   │
│ ────────────────────────────────────────────────────────────────── │
│ ☑ Surveiller le presse-papiers    [🌙 Thème]                     │
│                                                                    │
│ 💡 Sélectionnez du texte → Ctrl+C → Traduction automatique        │
│ 📷 Capture : Cliquez 'Capturer écran' puis sélectionnez la zone   │
│ 🚫 Code source et textes > 5000 caractères filtrés auto           │
└────────────────────────────────────────────────────────────────────┘
```

### Modes d'Utilisation

#### 🎨 Changement de thème
- **Menu déroulant** : Plus de 25 thèmes modernes disponibles
- **Changement à chaud** : Pas besoin de redémarrer l'application
- **Animations fluides** : Transition en douceur entre les thèmes
- **Sauvegarde automatique** : Votre thème préféré est mémorisé

#### 🎯 Sélection de langue source
- **Détection automatique** : Laissez l'IA détecter la langue (recommandé)
- **Sélection manuelle** : Choisissez explicitement la langue source
- **Cas d'usage manuel** : Textes ambigus, noms propres, ou forcer une interprétation

#### 🔄 Inversion des langues
- **Utilisation** : Cliquez sur le bouton ⇄ pour inverser source ↔ destination
- **Effet** : Échange automatique des textes ET des sélecteurs de langues
- **Exemple** : EN→FR ("Hello") devient FR→EN ("Bonjour")

#### 📷 Capture d'écran OCR
- **Multi-écrans** : Fonctionne parfaitement avec plusieurs moniteurs
- **Sélection précise** : Interface overlay avec dimensions en temps réel
- **OCR automatique** : Extraction et traduction automatiques du texte
- **Gestion d'erreurs** : Messages clairs en cas de problème

#### 🛡️ Filtres de sécurité automatiques
- **Protection contre le code** : JavaScript, Java, Python, HTML, SQL, JSON automatiquement détectés et filtrés
- **Limite de texte** : 5000 caractères max (saisie) / 2000 caractères max (clipboard)
- **Messages explicatifs** : Interface claire expliquant pourquoi certains textes ne sont pas traduits
- **Exemples de textes filtrés** :
    - ❌ `function hello() { return "world"; }`
    - ❌ `<div class="container">Hello</div>`
    - ❌ `{"name": "John", "age": 30}`
    - ❌ Fichiers logs, données binaires
    - ✅ `Hello, how are you today? I'm fine, thanks!`

#### 🖊️ Saisie manuelle
- Tapez directement dans la zone source
- La traduction apparaît automatiquement après 1 seconde
- Changement de langue = retraduction instantanée

#### 📋 Surveillance intelligente du presse-papiers
- **Quand l'app est en arrière-plan** : Copiez du texte depuis n'importe quelle application → Traduction automatique
- **Quand l'app a le focus** : Surveillance désactivée pour éviter les conflits
- **Filtrage automatique** : Code source et textes longs ignorés silencieusement
- **Limite clipboard** : 2000 caractères maximum (plus strict que la saisie manuelle)
- **Messages de débogage** : Console indique les textes ignorés avec leurs raisons
- **Workflow optimal** : Sélection → `Ctrl+C` → Traduction auto → Cliquez sur l'app → `Ctrl+C` → Colle la traduction

#### ⌨️ Raccourcis clavier intelligents
- **`Ctrl+C` dans la zone source** : Copie le texte sélectionné (comportement standard)
- **`Ctrl+C` ailleurs dans l'app** : Copie toute la traduction
- **`Échap` dans la capture** : Annule la sélection de zone
- **Feedback visuel** : Titre de la fenêtre indique "✅ Traduction copiée!" pendant 2 secondes

## 🛠️ Structure du Projet

```
auto-translate-swing/
├── pom.xml                          # Configuration Maven avec FlatLaf
├── README.md                        # Documentation complète
├── run.bat                          # Script de lancement Windows
├── run.sh                           # Script de lancement Linux/Mac
├── src/
│   └── main/
│       └── java/
│           └── net/arkaine/
│               └── TraducteurAutomatique.java  # Classe principale Swing
├── logs/                           # Dossier des logs (généré automatiquement)
│   ├── traductions_20250116.csv   # Logs du 16 janvier 2025
│   └── ...                        # Un fichier par jour
└── target/                         # Fichiers générés par Maven
    ├── auto-translate-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
    └── auto-translate-swing-1.0-SNAPSHOT-shaded.jar
```

## 📊 Système de Logging

### Format des fichiers CSV
Chaque jour, un nouveau fichier CSV est créé dans le dossier `logs/` :

```csv
"Timestamp","Texte Source","Traduction","Langue Source","Langue Destination"
"2025-01-16 14:30:25","Hello world","Bonjour le monde","Anglais","Français"
"2025-01-16 14:32:10","Como estas","Comment allez-vous","Espagnol","Français"
"2025-01-16 14:35:42","Guten Tag","Bonjour","Allemand","Français"
"2025-01-16 14:40:15","Summer Sisters Party...","Fête des Sœurs d'Été...","Anglais","Français"
```

### Caractéristiques du logging
- **Fichier quotidien** : `traductions_YYYYMMDD.csv`
- **Mode de langue** : Indique si la langue était détectée automatiquement ou sélectionnée manuellement
- **Textes longs supportés** : Gestion correcte des traductions complexes
- **Échappement CSV** : Gestion correcte des guillemets, virgules et retours à la ligne
- **Limitation de taille** : Textes tronqués à 1000 caractères si nécessaire
- **Horodatage précis** : Format `yyyy-MM-dd HH:mm:ss`

## 🔧 Configuration

### Personnalisation des thèmes
Vous pouvez ajouter de nouveaux thèmes en modifiant la méthode `appliquerTheme()` dans `TraducteurAutomatique.java`.

### Ajout de langues
Modifiez la méthode `initLangues()` pour ajouter de nouvelles langues supportées.

### Configuration OCR
Par défaut, l'application utilise l'API OCR.space gratuite. Vous pouvez modifier la clé API dans la méthode `effectuerOCR()`.

## 🐛 Résolution de Problèmes

### Problèmes courants

#### ❌ Erreur "Could not find or load main class"
```bash
# Vérifier que le projet est bien compilé
mvn clean compile

# Ou utiliser les scripts fournis
run.bat    # Windows
./run.sh   # Linux/Mac
```

#### ❌ Interface qui ne s'affiche pas correctement
- ✅ **Résolu** avec FlatLaf : Look moderne et cohérent sur tous les systèmes
- Essayez un autre thème via le bouton "🌙 Thème"
- Vérifiez que Java 21+ est installé

#### ❌ Capture d'écran qui ne fonctionne pas
```bash
# Vérifier les permissions sur Linux
xhost +local:

# Sur macOS, autoriser l'accès à l'écran dans Préférences Système
```

#### ❌ OCR qui ne fonctionne pas
- Vérifiez votre connexion Internet
- L'API OCR.space gratuite a des limites de taux
- Les textes très petits ou flous peuvent ne pas être détectés

#### ❌ L'application traduit du code source ou des données techniques
- ✅ **Résolu** avec les nouveaux filtres intelligents
- Le système détecte automatiquement et refuse de traduire :
    - Code source (JavaScript, Java, Python, HTML, CSS, SQL, JSON, etc.)
    - Données binaires ou logs système
    - Textes avec trop de caractères spéciaux
- Messages explicatifs pour informer l'utilisateur
- Filtrage silencieux pour la surveillance du clipboard

#### ❌ Textes trop longs qui causent des erreurs
- ✅ **Résolu** avec les limites intelligentes
- Maximum 5000 caractères pour la saisie manuelle
- Maximum 2000 caractères pour la surveillance du clipboard
- Message clair indiquant la longueur actuelle et suggestions

#### ❌ Traductions partielles ou incomplètes
- ✅ **Résolu** avec le nouveau système dual API (MyMemory + Google)
- ✅ **Résolu** avec le parsing JSON robuste utilisant Gson
- La version Swing gère correctement les textes complexes

#### ❌ Détection de langue incorrecte
- Utilisez le **mode manuel** : sélectionnez explicitement la langue source
- La détection automatique est maintenant plus précise avec des patterns linguistiques étendus

#### ❌ L'application ne surveille pas le presse-papiers
- Vérifiez que la case "Surveiller le presse-papiers" est cochée
- **Important** : La surveillance est automatiquement désactivée quand l'application a le focus (comportement normal)
- Cliquez sur une autre application pour réactiver la surveillance

#### ❌ Le bouton d'inversion ⇄ ne fonctionne pas
- Assurez-vous qu'il y a du texte traduit à inverser
- Le bouton affichera un message d'avertissement si aucune traduction n'est disponible
- L'inversion fonctionne avec les deux modes (détection auto et sélection manuelle)

#### ❌ Thème qui ne s'applique pas
- Tous les thèmes ne sont pas disponibles sur tous les systèmes
- Essayez "FlatLaf Light", "FlatLaf Dark" ou "FlatLaf IntelliJ" qui fonctionnent partout
- Redémarrez l'application si nécessaire

### Logs de débogage
```bash
# Exécuter avec logs détaillés depuis Maven
mvn exec:java -Dexec.mainClass="net.arkaine.TraducteurAutomatique" -X

# Ou directement avec Java
java -jar target/*.jar 2>&1 | tee debug.log

# Observer les messages dans la console :
# - "Traduction MyMemory réussie: ..."
# - "Langue source pour MyMemory: en -> fr"
# - "Inversion: Français -> Anglais"
# - "Texte ignoré (code source détecté): class MyClass..."
# - "Texte ignoré (trop long): 7543 caractères"
# - "Thème appliqué: FlatLaf Dark"
# - "Overlay unifié créé couvrant tous les écrans"
```

## 🚀 Nouvelles Fonctionnalités v2.0 Swing

### 🆕 Ce qui a été ajouté par rapport à la version JavaFX :

1. **Interface Swing moderne** 🎨
    - FlatLaf pour un look contemporain
    - 25+ thèmes disponibles avec changement à chaud
    - Animations fluides et transitions

2. **Meilleure compatibilité système** 🖥️
    - Fonctionne sur tous les OS sans dépendances externes
    - Support natif des DPI élevés
    - Gestion améliorée des multi-écrans

3. **Performance optimisée** ⚡
    - Démarrage plus rapide
    - Consommation mémoire réduite
    - Threading asynchrone pour toutes les opérations

4. **Interface utilisateur améliorée** 📱
    - Layout responsive qui s'adapte à la taille de fenêtre
    - Barres de progression avec indicateurs visuels
    - Tooltips informatifs sur tous les contrôles
    - Messages d'état dans la barre de titre

5. **Gestion d'erreurs robuste** 🛡️
    - Try-catch complets avec messages utilisateur clairs
    - Fallbacks automatiques entre APIs
    - Récupération d'erreurs sans crash

## 🔒 Sécurité et Confidentialité

- ⚠️ **Attention** : Cette application utilise des APIs gratuites de traduction (MyMemory, Google Translate) et OCR (OCR.space)
- Le texte traduit transit par des serveurs externes
- **Ne pas utiliser** pour des données sensibles ou confidentielles
- Pour un usage professionnel, considérez l'utilisation d'APIs de traduction privées ou on-premise

## 🤝 Contribution

Les contributions sont les bienvenues ! Voici comment contribuer :

1. **Fork** le projet
2. Créez votre **feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Committez** vos changements (`git commit -m 'Add some AmazingFeature'`)
4. **Push** vers la branch (`git push origin feature/AmazingFeature`)
5. Ouvrez une **Pull Request**

### Idées d'améliorations futures
- [ ] Support d'APIs de traduction alternatives (DeepL, Azure, etc.)
- [ ] Interface de consultation des historiques de traductions avec recherche
- [ ] Statistiques visuelles des langues les plus utilisées
- [ ] Raccourcis clavier globaux (système)
- [ ] Thèmes personnalisables avec éditeur de couleurs
- [ ] Détection de la langue par analyse de fichier
- [ ] Export personnalisé des logs (JSON, XML)
- [ ] Configuration personnalisable des raccourcis
- [ ] Notifications système pour les traductions
- [ ] Cache local pour les traductions fréquentes
- [ ] Plugin système pour intégration OS
- [ ] Support de la traduction de documents (PDF, DOCX)
- [ ] Mode plein écran pour les longues traductions
- [ ] Dictionnaire personnel avec synonymes
- [ ] Historique des traductions avec recherche
- [ ] API REST locale pour intégration dans d'autres apps

## 📄 License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🙏 Remerciements

- [FlatLaf](https://www.formdev.com/flatlaf/) - Look and Feel moderne pour Swing
- [Gson](https://github.com/google/gson) - Parsing JSON robuste
- [MyMemory API](https://mymemory.translated.net/) - Service de traduction principal
- [Google Translate](https://translate.google.com/) - Service de traduction de fallback
- [OCR.space](https://ocr.space/) - Service OCR gratuit
- [Maven](https://maven.apache.org/) - Gestionnaire de dépendances

## 🎯 Workflow Recommandé

```
🌐 Navigation web : Sélection + Ctrl+C → ✨ Traduction auto
📝 Dans l'app : Ctrl+C → 📋 Copie la traduction  
🔄 Inversion : Clic sur ⇄ → 🔃 Sens inversé
🎯 Langue forcée : Sélection manuelle → 🎯 Traduction précise
📷 Capture OCR : Clic sur 📷 → Sélection zone → ✨ Texte extrait et traduit
🎨 Personnalisation : Clic sur 🌙 → Choix du thème → 🎨 Interface adaptée
📤 Destination : Ctrl+V → ✅ Colle la traduction
📊 Analyse : Consultez logs/ pour vos statistiques
```

## 📞 Support

- 🐛 **Issues** : [GitHub Issues](https://github.com/OlivierMarcou/auto-translate-swing/issues)
- 💬 **Discussions** : [GitHub Discussions](https://github.com/OlivierMarcou/auto-translate-swing/discussions)
- 📧 **Email** : marcou.olivier@gmail.com
- 📋 **Logs** : Consultez le dossier `logs/` pour le debugging

---

⭐ **N'oubliez pas de mettre une étoile si ce projet vous a été utile !**

## 📈 Changelog v2.0 Swing

### 🔄 Migration JavaFX → Swing + FlatLaf :
- **Interface moderne** : Remplacement de JavaFX par Swing + FlatLaf pour un look contemporain
- **25+ thèmes** : Arc, Darcula, GitHub, Monokai, Nord, One Dark, Solarized, Material, etc.
- **Animations fluides** : Changement de thème avec transitions en douceur
- **Meilleure compatibilité** : Fonctionne sur tous les OS sans dépendances externes

### ✅ Corrections majeures conservées :
- **Fix traductions partielles** : Textes complexes maintenant traduits complètement
- **Fix détection de langue** : MyMemory n'accepte plus "auto", gestion corrigée
- **Fix parsing JSON** : Remplacement des split() fragiles par Gson + regex de fallback
- **Fix sécurité** : Protection contre traduction de code source et données techniques
- **Fix performance** : Limites intelligentes pour éviter les surcharges réseau

### 🆕 Nouvelles fonctionnalités Swing :
- **Threading asynchrone** : CompletableFuture pour toutes les opérations réseau
- **Interface responsive** : Layout qui s'adapte à toutes les tailles d'écran
- **Indicateurs visuels** : Barres de progression, messages d'état, tooltips
- **Gestion d'erreurs robuste** : Messages utilisateur clairs sans crash
- **Scripts de lancement** : run.bat/run.sh pour démarrage automatique

### 🔧 Améliorations techniques :
- **Composants Swing natifs** : JTextArea, JComboBox, JButton avec FlatLaf
- **Gestion du focus** : WindowFocusListener pour surveillance clipboard
- **Timers Swing** : Remplacement des Timeline JavaFX par javax.swing.Timer
- **Robot API** : Capture d'écran multi-moniteurs avec JWindow overlay
- **Configuration Maven** : Plugins exec, assembly et shade pour tous usages