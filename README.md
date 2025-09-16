# Traducteur Automatique JavaFX

Une application JavaFX moderne de traduction automatique avec surveillance du presse-papiers en temps réel.

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 📋 Fonctionnalités

### ✨ Fonctionnalités Principales
- **Détection automatique de la langue source** - Reconnaissance intelligente de la langue d'origine
- **Traduction en temps réel** - Traduction instantanée dès la saisie ou la sélection de texte
- **Surveillance du presse-papiers** - Traduction automatique de tout texte copié, même depuis d'autres applications
- **Changement de langue instantané** - Retraduction automatique lors du changement de langue de destination
- **Interface intuitive** - Design moderne et ergonomique

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
   git clone https://github.com/votre-username/traducteur-automatique.git
   cd traducteur-automatique
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
java -jar target/traducteur-automatique-1.0.0.jar
```

## 🖥️ Utilisation

### Interface Utilisateur

1. **Zone de texte source** - Saisissez ou collez votre texte à traduire
2. **Sélecteur de langue** - Choisissez la langue de destination (français par défaut)
3. **Affichage langue détectée** - Visualisez la langue source détectée automatiquement
4. **Zone de traduction** - Consultez la traduction en temps réel
5. **Bouton "Copier"** - Copiez facilement la traduction dans le presse-papiers
6. **Case "Surveiller le presse-papiers"** - Activez/désactivez la surveillance automatique

### Modes d'utilisation

#### 🖊️ Saisie manuelle
- Tapez directement dans la zone source
- La traduction apparaît automatiquement après 1 seconde

#### 📋 Copie depuis d'autres applications
- Copiez du texte depuis n'importe quelle application (navigateur, Word, etc.)
- Le texte est automatiquement traduit dans l'application

#### 🔄 Changement de langue
- Sélectionnez une nouvelle langue de destination
- La traduction se met à jour instantanément

## 🛠️ Structure du Projet

```
traducteur-automatique/
├── pom.xml                          # Configuration Maven
├── README.md                        # Documentation
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java     # Configuration des modules Java
│       │   └── com/exemple/
│       │       └── TraducteurAutomatique.java  # Classe principale
│       └── resources/               # Ressources (icônes, etc.)
└── target/                          # Fichiers générés par Maven
```

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
- Sur certains systèmes, des permissions spéciales peuvent être requises

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
- [ ] Historique des traductions
- [ ] Raccourcis clavier globaux
- [ ] Mode sombre
- [ ] Détection de la langue par fichier
- [ ] Export des traductions
- [ ] Configuration personnalisable

## 📄 License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🙏 Remerciements

- [OpenJFX](https://openjfx.io/) - Framework JavaFX
- [Google Translate](https://translate.google.com/) - Service de traduction
- [Gson](https://github.com/google/gson) - Parsing JSON
- [Maven](https://maven.apache.org/) - Gestionnaire de dépendances

## 📞 Support

- 🐛 **Issues** : [GitHub Issues](https://github.com/votre-username/traducteur-automatique/issues)
- 💬 **Discussions** : [GitHub Discussions](https://github.com/votre-username/traducteur-automatique/discussions)
- 📧 **Email** : votre-email@example.com

---

⭐ **N'oubliez pas de mettre une étoile si ce projet vous a été utile !**