# Traducteur Automatique JavaFX

Une application JavaFX moderne de traduction automatique avec surveillance du presse-papiers en temps réel, sélection manuelle des langues, inversion bidirectionnelle et système de logging avancé.

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 📋 Fonctionnalités

### ✨ Fonctionnalités Principales
- **Sélection intelligente de la langue source** - Détection automatique OU choix manuel parmi 20 langues
- **Traduction en temps réel** - Traduction instantanée dès la saisie ou la sélection de texte
- **Inversion bidirectionnelle** - Bouton ⇄ pour échanger instantanément les langues et textes
- **Surveillance intelligente du presse-papiers** - Traduction automatique de tout texte copié depuis d'autres applications (désactivée quand l'app a le focus)
- **Changement de langue instantané** - Retraduction automatique lors du changement de langue source ou destination
- **Raccourci clavier intelligent** - `Ctrl+C` contextuel (copie sélection dans zone source, copie traduction ailleurs)
- **Double API de traduction** - MyMemory API (priorité) + Google Translate (fallback) pour une fiabilité maximale
- **Logging automatique** - Enregistrement de toutes les traductions dans des fichiers CSV quotidiens
- **Interface intuitive** - Design moderne et ergonomique avec feedback visuel

### 🔧 Nouvelles Améliorations (v2.0)
- **Sélecteur de langue source** - Choix entre "Détection automatique" et sélection manuelle
- **Système de traduction dual** - MyMemory API (plus fiable) + Google Translate en fallback
- **Parsing JSON robuste** - Utilisation de Gson pour traiter les réponses complexes
- **Inversion complète** - Échange bidirectionnel des langues et contenus
- **Détection linguistique améliorée** - Algorithmes basés sur patterns + caractères spéciaux
- **Interface reorganisée** - Layout "De → Vers" plus intuitif
- **Gestion d'erreurs renforcée** - Timeouts, fallbacks et messages d'état

### 📊 Système de Logging Avancé
- **Fichiers CSV quotidiens** - Un nouveau fichier chaque jour (`traductions_YYYYMMDD.csv`)
- **Horodatage précis** - Date et heure de chaque traduction
- **Données complètes** - Texte source, traduction, langues source et destination
- **Format standard** - Compatible Excel, LibreOffice, Google Sheets
- **Dossier organisé** - Tous les logs dans le dossier `logs/`

### 🌍 Langues Supportées (20 langues)
- Français (par défaut destination)
- Anglais (par défaut source auto)
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
- Connexion Internet (pour les traductions)

### Installation

1. **Cloner le repository**
   ```bash
   git clone https://github.com/OlivierMarcou/auto-translate.git
   cd auto-translate
   ```

2. **Compiler le projet**
   ```bash
   mvn clean compile
   ```

3. **Exécuter l'application**
   ```bash
   mvn javafx:run
   ```

### Alternative : JAR exécutable

```bash
# Créer un JAR avec toutes les dépendances
mvn clean package

# Exécuter le JAR
java -jar target/auto-translate-1.0-SNAPSHOT-shaded.jar
```

## 🖥️ Guide d'Utilisation

### Interface Utilisateur

1. **Sélecteur de langue source** - Choisissez "Détection automatique" ou une langue spécifique
2. **Bouton d'inversion ⇄** - Échange instantané des langues et des textes
3. **Sélecteur de langue de destination** - Langue cible de la traduction (français par défaut)
4. **Indicateur de langue** - Affiche la langue détectée/sélectionnée
5. **Zone de texte source** - Saisissez ou collez votre texte à traduire
6. **Zone de traduction** - Consultez la traduction en temps réel
7. **Bouton "Copier la traduction (Ctrl+C)"** - Copiez facilement la traduction
8. **Case "Surveiller le presse-papiers"** - Activez/désactivez la surveillance automatique
9. **Astuce d'utilisation** - Guide contextuel affiché en bas

### Modes d'utilisation

#### 🔍 Détection automatique (par défaut)
- **Sélectionnez** : "Détection automatique" dans le menu de langue source
- **Fonctionnement** : L'app détecte automatiquement la langue du texte saisi
- **Affichage** : "Langue détectée : [Langue]"
- **Idéal pour** : Usage général avec textes de langues variées

#### 🎯 Sélection manuelle de langue
- **Sélectionnez** : Une langue spécifique dans le menu source
- **Fonctionnement** : Force la traduction depuis cette langue
- **Affichage** : "Langue sélectionnée : [Langue]"
- **Idéal pour** : Textes ambigus ou correction de mauvaise détection

#### 🔄 Inversion bidirectionnelle
- **Cliquez** : Sur le bouton ⇄ entre les sélecteurs de langues
- **Résultat** :
   - La traduction devient le texte source
   - Les langues source et destination s'échangent
   - Nouvelle traduction dans l'autre sens
- **Feedback** : "🔄 Langues inversées !" dans la barre de titre

#### 🖊️ Saisie manuelle
- Tapez directement dans la zone source
- La traduction apparaît automatiquement après 1 seconde

#### 📋 Surveillance intelligente du presse-papiers
- **Quand l'app est en arrière-plan** : Copiez du texte depuis n'importe quelle application → Traduction automatique
- **Quand l'app a le focus** : Surveillance désactivée pour éviter les conflits
- **Workflow optimal** : Sélection → `Ctrl+C` → Traduction auto → Cliquez sur l'app → `Ctrl+C` → Colle la traduction

#### ⌨️ Raccourcis clavier intelligents
- **`Ctrl+C` dans la zone source** : Copie le texte sélectionné (comportement standard)
- **`Ctrl+C` ailleurs dans l'app** : Copie toute la traduction
- **Feedback visuel** : Titre de la fenêtre indique "✅ Traduction copiée!" pendant 2 secondes

## 🛠️ Architecture et APIs

### 🔗 Système de Traduction Double
1. **MyMemory API** (priorité) - Plus fiable et stable
2. **Google Translate API** (fallback) - Secours en cas d'échec MyMemory

### 📊 Parsing JSON Robuste
- **Gson** - Parsing principal des réponses JSON
- **Regex de fallback** - En cas d'échec Gson
- **Split traditionnel** - Dernière option de secours

### 🧠 Détection de Langue Avancée
- **Patterns linguistiques** - Analyse de mots-clés spécifiques par langue
- **Caractères spéciaux** - Détection d'accents, ñ, ß, etc.
- **Structure des mots** - Analyse longueur et capitalisation
- **API Google** - Fallback pour les cas complexes

## 🔧 Structure du Projet

```
auto-translate/
├── pom.xml                          # Configuration Maven
├── README.md                        # Documentation
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java     # Configuration des modules Java
│       │   └── net/arkaine/
│       │       └── TraducteurAutomatique.java  # Classe principale
│       └── resources/               # Ressources (icônes, etc.)
├── logs/                           # Dossier des logs (généré automatiquement)
│   ├── traductions_20250115.csv   # Logs du 15 janvier 2025
│   ├── traductions_20250116.csv   # Logs du 16 janvier 2025
│   └── ...                        # Un fichier par jour
└── target/                         # Fichiers générés par Maven
```

## 📊 Système de Logging

### Format des fichiers CSV
Chaque jour, un nouveau fichier CSV est créé dans le dossier `logs/` :

```csv
"Timestamp","Texte Source","Traduction","Langue Source","Langue Destination"
"2025-01-15 14:30:25","Hello world","Bonjour le monde","Anglais","Français"
"2025-01-15 14:32:10","Como estas","Comment allez-vous","Espagnol","Français"
"2025-01-15 14:35:42","Guten Tag","Bonjour","Allemand","Français"
```

### Caractéristiques du logging
- **Fichier quotidien** : `traductions_YYYYMMDD.csv`
- **En-têtes automatiques** : Ajoutés lors de la création du fichier
- **Échappement CSV** : Gestion correcte des guillemets et virgules
- **Limitation de taille** : Textes tronqués à 1000 caractères si nécessaire
- **Horodatage précis** : Format `yyyy-MM-dd HH:mm:ss`

### Utilisation des logs
- **Analyse d'usage** : Statistiques sur vos traductions
- **Recherche** : Retrouvez d'anciennes traductions
- **Export** : Ouvrez avec Excel, LibreOffice, Google Sheets
- **Archivage** : Historique complet de votre activité

## 🔧 Configuration

### Variables d'environnement (optionnel)
```bash
# Pour utiliser une API de traduction personnalisée (futur)
export TRANSLATION_API_KEY="votre-clé-api"
```

### Personnalisation
Vous pouvez modifier les langues supportées en éditant la méthode `initLangues()` dans `TraducteurAutomatique.java`.

## 🐛 Résolution de Problèmes

### Problèmes courants

#### ❌ Erreur "module not found: javafx.controls"
```bash
# Vérifier que JavaFX est bien installé
mvn dependency:resolve
mvn dependency:tree | grep javafx
```

#### ❌ Erreur de connexion réseau
- Vérifiez votre connexion Internet
- Certains firewalls d'entreprise peuvent bloquer les requêtes vers les APIs de traduction
- Essayez de changer temporairement de réseau

#### ❌ L'application ne surveille pas le presse-papiers
- Vérifiez que la case "Surveiller le presse-papiers" est cochée
- **Important** : La surveillance est automatiquement désactivée quand l'application a le focus (comportement normal)
- Cliquez sur une autre application pour réactiver la surveillance
- Sur certains systèmes, des permissions spéciales peuvent être requises

#### ❌ Le raccourci Ctrl+C ne fonctionne pas comme attendu
- **Dans la zone source** : `Ctrl+C` copie le texte sélectionné (normal)
- **Ailleurs dans l'app** : `Ctrl+C` copie toute la traduction
- Assurez-vous que l'application a le focus pour utiliser les raccourcis

#### ❌ Traductions partielles ou incorrectes
- **Essayez le mode manuel** : Sélectionnez la langue source au lieu de "Détection automatique"
- **Vérifiez la langue détectée** : Parfois la détection peut se tromper
- **Utilisez l'inversion** : Traduisez dans l'autre sens pour vérifier
- **Textes complexes** : Les noms de produits peuvent être mal traduits (normal)

#### ❌ Le bouton d'inversion ne fonctionne pas
- Assurez-vous qu'il y a du texte dans les deux zones (source et traduction)
- Vérifiez qu'une traduction a été effectuée avant d'essayer d'inverser
- Le message "⚠️ Rien à inverser" apparaît s'il n'y a pas de contenu

#### ❌ Les logs ne se créent pas
- Vérifiez les permissions d'écriture dans le dossier du projet
- Le dossier `logs/` est créé automatiquement au premier lancement
- Consultez la console pour les messages d'erreur de logging

### Logs de débogage
```bash
# Exécuter avec logs détaillés
mvn javafx:run -X
```

## 🔒 Sécurité et Confidentialité

- ⚠️ **Attention** : Cette application utilise des APIs gratuites (MyMemory, Google Translate)
- Le texte traduit transit par des serveurs externes
- **Ne pas utiliser** pour des données sensibles ou confidentielles
- Pour un usage professionnel, considérez l'utilisation d'APIs de traduction privées

## 🆕 Nouveautés v2.0

### ✨ Fonctionnalités ajoutées :
- **Sélecteur de langue source** avec "Détection automatique"
- **Bouton d'inversion ⇄** pour traduction bidirectionnelle
- **Double API de traduction** (MyMemory + Google Translate)
- **Parsing JSON robuste** avec Gson
- **Détection linguistique améliorée** avec patterns avancés
- **Interface reorganisée** plus intuitive

### 🔧 Améliorations techniques :
- **Gestion d'erreurs renforcée** avec timeouts et fallbacks
- **Performances optimisées** avec cache de détection
- **Code restructuré** pour une meilleure maintenabilité
- **Feedback utilisateur** amélioré avec messages d'état

## 🤝 Contribution

Les contributions sont les bienvenues ! Voici comment contribuer :

1. **Fork** le projet
2. Créez votre **feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Committez** vos changements (`git commit -m 'Add some AmazingFeature'`)
4. **Push** vers la branch (`git push origin feature/AmazingFeature`)
5. Ouvrez une **Pull Request**

### Idées d'améliorations
- [ ] Support d'APIs de traduction alternatives (DeepL, Azure Translator)
- [ ] Interface de consultation des historiques de traductions avec recherche
- [ ] Statistiques visuelles des langues les plus utilisées (graphiques)
- [ ] Raccourcis clavier globaux (système) pour traduction instantanée
- [ ] Mode sombre et thèmes personnalisables
- [ ] Détection de la langue par analyse de fichiers
- [ ] Export personnalisé des logs (JSON, XML, PDF)
- [ ] Configuration personnalisable des raccourcis clavier
- [ ] Notifications système pour les traductions automatiques
- [ ] Cache local intelligent pour les traductions fréquentes
- [ ] Plugin système pour intégration OS (Windows/Linux/macOS)
- [ ] API REST locale pour intégration avec d'autres apps
- [ ] Support de traduction de fichiers (PDF, DOCX, etc.)
- [ ] Correcteur orthographique intégré
- [ ] Suggestions de traductions alternatives

## 📄 License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🙏 Remerciements

- [OpenJFX](https://openjfx.io/) - Framework JavaFX moderne
- [MyMemory API](https://mymemory.translated.net/) - Service de traduction principal
- [Google Translate](https://translate.google.com/) - Service de traduction de fallback
- [Gson](https://github.com/google/gson) - Parsing JSON robuste
- [Maven](https://maven.apache.org/) - Gestionnaire de dépendances et build

## 🎯 Workflows Recommandés

### 🌐 Navigation web classique
```
Sélection sur site web → Ctrl+C → ✨ Traduction auto → Ctrl+C dans l'app → Ctrl+V
```

### 🔄 Traduction bidirectionnelle
```
Texte EN → FR → Clic ⇄ → Texte FR → EN (vérification)
```

### 🎯 Langue spécifique
```
Texte ambigu → Sélection manuelle langue source → Traduction précise
```

### 📊 Analyse d'usage
```
Utilisation quotidienne → Consultation logs/ → Statistiques personnelles
```

## 📞 Support

- 🐛 **Issues** : [GitHub Issues](https://github.com/OlivierMarcou/auto-translate/issues)
- 💬 **wiki** : [GitHub Discussions](https://github.com/OlivierMarcou/auto-translate/wiki)
- 📧 **Email** : marcou.olivier@gmail.com
- 📋 **Logs** : Consultez le dossier `logs/` pour le debugging
- 🔧 **Debug** : Lancez avec `mvn javafx:run -X` pour logs détaillés

---

## 🚀 Exemple d'Usage Complet

```
1. Lancez l'application : mvn javafx:run
2. Copiez du texte depuis un site web → Traduction automatique
3. Si la langue détectée est incorrecte → Changez le sélecteur source
4. Pour traduire dans l'autre sens → Cliquez ⇄
5. Copiez le résultat → Ctrl+C
6. Consultez l'historique → Dossier logs/
```

⭐ **N'oubliez pas de mettre une étoile si ce projet vous a été utile !**