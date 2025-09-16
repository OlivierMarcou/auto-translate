# Traducteur Automatique JavaFX

Une application JavaFX moderne de traduction automatique avec surveillance du presse-papiers en temps réel, sélection manuelle des langues, inversion bidirectionnelle et système de logging avancé.

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 📋 Fonctionnalités

### ✨ Fonctionnalités Principales
- **Sélection de langue source** - Détection automatique OU choix manuel de la langue à traduire
- **Détection automatique intelligente** - Reconnaissance précise de la langue source avec fallbacks multiples
- **Traduction en temps réel** - Traduction instantanée dès la saisie ou la sélection de texte
- **Filtres de sécurité intelligents** - Protection contre la traduction de code source et textes inappropriés
- **Inversion des langues (⇄)** - Bouton pour inverser instantanément source ↔ destination avec échange des textes
- **Surveillance intelligente du presse-papiers** - Traduction automatique de tout texte copié avec filtrage automatique
- **Changement de langue instantané** - Retraduction automatique lors du changement de langue
- **Raccourci clavier intelligent** - `Ctrl+C` contextuel (copie sélection dans zone source, copie traduction ailleurs)
- **Double API de traduction** - MyMemory (priorité) + Google Translate (fallback) pour une fiabilité maximale
- **Logging automatique** - Enregistrement de toutes les traductions dans des fichiers CSV quotidiens
- **Interface moderne** - Design ergonomique et intuitif avec feedback visuel

### 🛡️ Filtres de Sécurité Intelligents
- **Protection contre le code source** : Détection automatique et refus de traduire du code (JavaScript, Java, Python, HTML, CSS, SQL, JSON, etc.)
- **Limite de longueur intelligente** : Maximum 5000 caractères pour la traduction manuelle, 2000 pour le clipboard
- **Filtre de caractères spéciaux** : Évite la traduction de données binaires, logs système, ou formats techniques
- **Messages explicatifs** : Interface claire indiquant pourquoi un texte n'est pas traduit
- **Surveillance adaptée** : Le clipboard ignore automatiquement le code source sans notification intrusive
- **Mode automatique** (par défaut) : Détection intelligente avec patterns linguistiques + Google Translate
- **Mode manuel** : Choix explicite de la langue source parmi 20+ langues supportées
- **Affichage intelligent** : "Langue détectée" vs "Langue sélectionnée" selon le mode
- **Traduction forcée** : Respecte le choix manuel même si le texte semble être dans une autre langue

### 🔧 Sélection de Langue Source
- **Bouton ⇄ intuitif** : Situé entre les sélecteurs de langues
- **Inversion complète** : Échange automatique des textes ET des langues sélectionnées
- **Gestion intelligente** : Prend en compte le mode de détection (auto/manuel)
- **Feedback visuel** : Message temporaire "🔄 Langues inversées !" dans la barre de titre

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

## 🖥️ Utilisation

### Interface Utilisateur Moderne

```
┌──────────────────────────────────────────────────────────────────┐
│ De: [Détection automatique ▼] ⇄ Vers: [Français ▼]              │
│ Langue détectée : Anglais                                        │
│                                                                  │
│ Texte à traduire :                                               │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Bonjour, comment allez-vous aujourd'hui ?                   │ │
│ └──────────────────────────────────────────────────────────────┘ │
│ [Traduire] ◐                                                    │
│                                                                  │
│ Traduction :                                                     │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Hello, how are you today?                                    │ │
│ └──────────────────────────────────────────────────────────────┘ │
│ [Copier la traduction (Ctrl+C)]                                 │
│                                                                  │
│ ─────────────────────────────────────────────────────────────    │
│ ☑ Surveiller le presse-papiers                                  │
│ 💡 Astuce: Sélectionnez du texte → Ctrl+C → Traduction auto     │
│ 🚫 Code source et textes > 5000 caractères filtrés auto         │
└──────────────────────────────────────────────────────────────────┘
```

### Modes d'utilisation

#### 🎯 Sélection de langue source
- **Détection automatique** : Laissez l'IA détecter la langue (recommandé)
- **Sélection manuelle** : Choisissez explicitement la langue source
- **Cas d'usage manuel** : Textes ambigus, noms propres, ou forcer une interprétation

#### 🔄 Inversion des langues
- **Utilisation** : Cliquez sur le bouton ⇄ pour inverser source ↔ destination
- **Effet** : Échange automatique des textes ET des sélecteurs de langues
- **Exemple** : EN→FR ("Hello") devient FR→EN ("Bonjour")

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
- **Feedback visuel** : Titre de la fenêtre indique "✅ Traduction copiée!" pendant 2 secondes

## 🛠️ Structure du Projet

```
auto-translate/
├── pom.xml                          # Configuration Maven avec plugins améliorés
├── README.md                        # Documentation complète
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java     # Configuration des modules Java
│       │   └── net/arkaine/
│       │       └── TraducteurAutomatique.java  # Classe principale avec nouvelles fonctionnalités
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
"2025-01-15 14:40:15","Summer Sisters Party...","Fête des Sœurs d'Été...","Anglais","Français"
```

### Caractéristiques du logging
- **Fichier quotidien** : `traductions_YYYYMMDD.csv`
- **Mode de langue** : Indique si la langue était détectée automatiquement ou sélectionnée manuellement
- **Textes longs supportés** : Gestion correcte des traductions complexes
- **Échappement CSV** : Gestion correcte des guillemets, virgules et retours à la ligne
- **Limitation de taille** : Textes tronqués à 1000 caractères si nécessaire
- **Horodatage précis** : Format `yyyy-MM-dd HH:mm:ss`

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

#### ❌ L'application traduit du code source ou des données techniques
- ✅ **Résolu** avec les nouveaux filtres intelligents
- Le système détecte automatiquement et refuse de traduire :
    - Code source (JavaScript, Java, Python, HTML, CSS, SQL, JSON, etc.)
    - Données binaires ou logs système
    - Textes avec trop de caractères spéciaux
- Messages explicatifs pour informer l'utilisateur
- Filtrage silencieux pour la surveillance du clipboard

#### ❌ Textes trop longs qui causent des erreurs
- ✅ **Résolu** avec le nouveau système dual API (MyMemory + Google)
- ✅ **Résolu** avec le parsing JSON robuste utilisant Gson
- La version corrigée gère correctement les textes complexes comme "Summer Sisters Party Gradient..."

#### ❌ Textes trop longs qui causent des erreurs
- ✅ **Résolu** avec les limites intelligentes
- Maximum 5000 caractères pour la saisie manuelle
- Maximum 2000 caractères pour la surveillance du clipboard
- Message clair indiquant la longueur actuelle et suggestions

#### ❌ Traductions partielles ou incomplètes
- Utilisez le **mode manuel** : sélectionnez explicitement la langue source
- La détection automatique est maintenant plus précise avec des patterns linguistiques étendus

#### ❌ Traductions partielles ou incomplètes
- ✅ **Résolu** avec le nouveau système dual API (MyMemory + Google)
- ✅ **Résolu** avec le parsing JSON robuste utilisant Gson
- La version corrigée gère correctement les textes complexes

#### ❌ Détection de langue incorrecte
- Vérifiez votre connexion Internet
- Certains firewalls d'entreprise peuvent bloquer les requêtes vers les APIs de traduction
- Le système utilise maintenant deux APIs différentes pour plus de robustesse

#### ❌ L'application ne surveille pas le presse-papiers
- Vérifiez que la case "Surveiller le presse-papiers" est cochée
- **Important** : La surveillance est automatiquement désactivée quand l'application a le focus (comportement normal)
- Cliquez sur une autre application pour réactiver la surveillance

#### ❌ Le bouton d'inversion ⇄ ne fonctionne pas
- Assurez-vous qu'il y a du texte traduit à inverser
- Le bouton affichera un message d'avertissement si aucune traduction n'est disponible
- L'inversion fonctionne avec les deux modes (détection auto et sélection manuelle)

### Logs de débogage
```bash
# Exécuter avec logs détaillés
mvn javafx:run -X

# Observer les messages dans la console
# - "Traduction MyMemory réussie: ..."
# - "Langue source pour MyMemory: en -> fr"
# - "Inversion: Français -> Anglais"
# - "Texte ignoré (code source détecté): class MyClass..."
# - "Texte ignoré (trop long): 7543 caractères"
```

## 🚀 Nouvelles Fonctionnalités v2.0

### 🆕 Ce qui a été ajouté :

1. **Sélection manuelle de langue source** 📋
    - Dropdown "De:" avec "Détection automatique" + toutes les langues
    - Mode forcé qui respecte le choix utilisateur

2. **Inversion bidirectionnelle** 🔄
    - Bouton ⇄ entre les sélecteurs
    - Échange automatique textes + langues

3. **Double API robuste** 🌐
    - MyMemory (priorité) + Google Translate (fallback)
    - Parsing JSON avec Gson pour plus de fiabilité

4. **Interface repensée** 🎨
    - Layout horizontal pour les langues
    - Labels distincts et clairs
    - Feedback visuel amélioré

5. **Filtres de sécurité intelligents** 🛡️
    - Détection automatique du code source
    - Limites de longueur (5000/2000 caractères)
    - Protection contre les données techniques
    - Messages utilisateur explicatifs

## 🔒 Sécurité et Confidentialité

- ⚠️ **Attention** : Cette application utilise des APIs gratuites de traduction (MyMemory, Google Translate)
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
- [ ] Mode sombre / thèmes personnalisables
- [ ] Détection de la langue par analyse de fichier
- [ ] Export personnalisé des logs (JSON, XML)
- [ ] Configuration personnalisable des raccourcis
- [ ] Notifications système pour les traductions
- [ ] Cache local pour les traductions fréquentes
- [ ] Plugin système pour intégration OS
- [ ] Support de la traduction de documents (PDF, DOCX)

## 📄 License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🙏 Remerciements

- [OpenJFX](https://openjfx.io/) - Framework JavaFX
- [MyMemory API](https://mymemory.translated.net/) - Service de traduction principal
- [Google Translate](https://translate.google.com/) - Service de traduction de fallback
- [Gson](https://github.com/google/gson) - Parsing JSON robuste
- [Maven](https://maven.apache.org/) - Gestionnaire de dépendances

## 🎯 Workflow Recommandé

```
🌐 Navigation web : Sélection + Ctrl+C → ✨ Traduction auto
📝 Dans l'app : Ctrl+C → 📋 Copie la traduction  
🔄 Inversion : Clic sur ⇄ → 🔃 Sens inversé
🎯 Langue forcée : Sélection manuelle → 🎯 Traduction précise
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

## 📈 Changelog v2.0

### ✅ Corrections majeures :
- **Fix traductions partielles** : Textes complexes maintenant traduits complètement
- **Fix détection de langue** : MyMemory n'accepte plus "auto", gestion corrigée
- **Fix parsing JSON** : Remplacement des split() fragiles par Gson + regex de fallback
- **Fix sécurité** : Protection contre traduction de code source et données techniques
- **Fix performance** : Limites intelligentes pour éviter les surcharges réseau

### 🆕 Nouvelles fonctionnalités :
- **Sélection manuelle langue source** : Dropdown avec "Détection automatique" + 20+ langues
- **Inversion bidirectionnelle** : Bouton ⇄ pour échanger langues et textes
- **Double API** : MyMemory (priorité) + Google Translate (fallback)
- **Interface améliorée** : Layout repensé, labels distincts, feedback visuel
- **Filtres intelligents** : Détection code source, limite longueur, messages explicatifs

### 🔧 Améliorations techniques :
- **Détection linguistique renforcée** : Patterns étendus + analyse structurelle
- **Gestion d'erreurs robuste** : Fallbacks multiples entre APIs
- **Parsing JSON avec Gson** : Plus fiable que les méthodes artisanales
- **Configuration Maven améliorée** : Plugin shade pour JAR exécutables
- **Validation des entrées** : Filtres de sécurité et qualité automatiques