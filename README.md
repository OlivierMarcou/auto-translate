# Traducteur Automatique JavaFX

Une application JavaFX moderne de traduction automatique avec surveillance du presse-papiers en temps réel et système de logging avancé.

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 📋 Fonctionnalités

### ✨ Fonctionnalités Principales
- **Détection automatique de la langue source** - Reconnaissance intelligente de la langue d'origine
- **Traduction en temps réel** - Traduction instantanée dès la saisie ou la sélection de texte
- **Surveillance intelligente du presse-papiers** - Traduction automatique de tout texte copié depuis d'autres applications (désactivée quand l'app a le focus)
- **Changement de langue instantané** - Retraduction automatique lors du changement de langue de destination
- **Raccourci clavier intelligent** - `Ctrl+C` contextuel (copie sélection dans zone source, copie traduction ailleurs)
- **Logging automatique** - Enregistrement de toutes les traductions dans des fichiers CSV quotidiens
- **Interface intuitive** - Design moderne et ergonomique

### 📊 Système de Logging Avancé
- **Fichiers CSV quotidiens** - Un nouveau fichier chaque jour (`traductions_YYYYMMDD.csv`)
- **Horodatage précis** - Date et heure de chaque traduction
- **Données complètes** - Texte source, traduction, langues détectées
- **Format standard** - Compatible Excel, LibreOffice, Google Sheets
- **Dossier organisé** - Tous les logs dans le dossier `logs/`

### 🌍 Langues Supportées
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
java -jar target/auto-translate-1.0.0.jar
```

## 🖥️ Utilisation

### Interface Utilisateur

1. **Zone de texte source** - Saisissez ou collez votre texte à traduire
2. **Sélecteur de langue** - Choisissez la langue de destination (français par défaut)
3. **Affichage langue détectée** - Visualisez la langue source détectée automatiquement
4. **Zone de traduction** - Consultez la traduction en temps réel
5. **Bouton "Copier la traduction (Ctrl+C)"** - Copiez facilement la traduction
6. **Case "Surveiller le presse-papiers"** - Activez/désactivez la surveillance automatique
7. **Astuce d'utilisation** - Guide contextuel affiché en bas

### Modes d'utilisation

#### 🖊️ Saisie manuelle
- Tapez directement dans la zone source
- La traduction apparaît automatiquement après 1 seconde

#### 📋 Surveillance intelligente du presse-papiers
- **Quand l'app est en arrière-plan** : Copiez du texte depuis n'importe quelle application → Traduction automatique
- **Quand l'app a le focus** : Surveillance désactivée pour éviter les conflits
- **Workflow optimal** : Sélection → `Ctrl+C` → Traduction auto → Cliquez sur l'app → `Ctrl+C` → Colle la traduction

#### 🔄 Changement de langue
- Sélectionnez une nouvelle langue de destination
- La traduction se met à jour instantanément

#### ⌨️ Raccourcis clavier intelligents
- **`Ctrl+C` dans la zone source** : Copie le texte sélectionné (comportement standard)
- **`Ctrl+C` ailleurs dans l'app** : Copie toute la traduction
- **Feedback visuel** : Titre de la fenêtre indique "✅ Traduction copiée!" pendant 2 secondes

## 🛠️ Structure du Projet

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
# Pour utiliser une API de traduction personnalisée
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
- Certains firewalls d'entreprise peuvent bloquer les requêtes vers Google Translate

#### ❌ L'application ne surveille pas le presse-papiers
- Vérifiez que la case "Surveiller le presse-papiers" est cochée
- **Important** : La surveillance est automatiquement désactivée quand l'application a le focus (comportement normal)
- Cliquez sur une autre application pour réactiver la surveillance
- Sur certains systèmes, des permissions spéciales peuvent être requises

#### ❌ Le raccourci Ctrl+C ne fonctionne pas comme attendu
- **Dans la zone source** : `Ctrl+C` copie le texte sélectionné (normal)
- **Ailleurs dans l'app** : `Ctrl+C` copie toute la traduction
- Assurez-vous que l'application a le focus pour utiliser les raccourcis

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

- ⚠️ **Attention** : Cette application utilise l'API gratuite de Google Translate
- Le texte traduit transit par les serveurs de Google
- **Ne pas utiliser** pour des données sensibles ou confidentielles
- Pour un usage professionnel, considérez l'utilisation d'APIs de traduction privées

## 🤝 Contribution

Les contributions sont les bienvenues ! Voici comment contribuer :

1. **Fork** le projet
2. Créez votre **feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Committez** vos changements (`git commit -m 'Add some AmazingFeature'`)
4. **Push** vers la branch (`git push origin feature/AmazingFeature`)
5. Ouvrez une **Pull Request**

### Idées d'améliorations
- [ ] Support d'APIs de traduction alternatives (DeepL, Azure, etc.)
- [ ] Interface de consultation des historiques de traductions
- [ ] Statistiques visuelles des langues les plus utilisées
- [ ] Raccourcis clavier globaux (système)
- [ ] Mode sombre
- [ ] Détection de la langue par fichier
- [ ] Export personnalisé des logs (JSON, XML)
- [ ] Configuration personnalisable des raccourcis
- [ ] Notifications système pour les traductions
- [ ] Cache local pour les traductions fréquentes

## 📄 License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🙏 Remerciements

- [OpenJFX](https://openjfx.io/) - Framework JavaFX
- [Google Translate](https://translate.google.com/) - Service de traduction
- [Gson](https://github.com/google/gson) - Parsing JSON
- [Maven](https://maven.apache.org/) - Gestionnaire de dépendances

## 🎯 Workflow Recommandé

```
🌐 Navigation web : Sélection + Ctrl+C → ✨ Traduction auto
📝 Dans l'app : Ctrl+C → 📋 Copie la traduction  
📤 Destination : Ctrl+V → ✅ Colle la traduction
📊 Analyse : Consultez logs/ pour vos statistiques
```

## 📞 Support

- 🐛 **Issues** : [GitHub Issues](https://github.com/OlivierMarcou/auto-translate/issues)
- 💬 **wiki** : [GitHub Discussions](https://github.com/OlivierMarcou/auto-translate/wiki)
- 📧 **Email** : marcou.olivier@gmail.com
- 📋 **Logs** : Consultez le dossier `logs/` pour le debugging

---

⭐ **N'oubliez pas de mettre une étoile si ce projet vous a été utile !**