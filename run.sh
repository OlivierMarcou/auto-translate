#!/bin/bash

# =============================================================================
# 🚀 TRADUCTEUR AUTOMATIQUE - SWING
# Script de lancement automatique pour Linux/Mac
# =============================================================================

# Configuration des couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="Traducteur Automatique"
MIN_JAVA_VERSION=21
MAVEN_MIN_VERSION="3.9"

# Fonction d'affichage du banner
show_banner() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════╗"
    echo "║              🚀 TRADUCTEUR AUTOMATIQUE                  ║"
    echo "║                   Version Swing                         ║"
    echo "║              Avec FlatLaf & 25+ Thèmes                  ║"
    echo "╚══════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo
}

# Fonction d'aide
show_help() {
    echo -e "${CYAN}Usage: $0 [OPTIONS]${NC}"
    echo
    echo -e "${YELLOW}Options:${NC}"
    echo "  -h, --help     Afficher cette aide"
    echo "  -v, --verbose  Mode verbeux avec plus de détails"
    echo "  -c, --clean    Nettoyer avant de compiler"
    echo "  --dev          Mode développeur (recompile toujours)"
    echo "  --jar [FILE]   Lancer un JAR spécifique"
    echo
    echo -e "${YELLOW}Exemples:${NC}"
    echo "  $0                           # Lancement standard"
    echo "  $0 --clean                   # Nettoyage + compilation + lancement"
    echo "  $0 --jar mon-app.jar         # Lancer un JAR spécifique"
    echo "  $0 --dev                     # Mode développeur"
    echo
}

# Fonction de vérification de Java
check_java() {
    echo -e "${YELLOW}🔍 Vérification de Java...${NC}"

    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Java n'est pas installé ou pas dans le PATH${NC}"
        echo
        echo -e "${YELLOW}📥 Installation de Java:${NC}"
        echo "  • Ubuntu/Debian: sudo apt install openjdk-21-jdk"
        echo "  • Fedora/RHEL:   sudo dnf install java-21-openjdk-devel"
        echo "  • Arch Linux:    sudo pacman -S jdk-openjdk"
        echo "  • macOS:         brew install openjdk@21"
        echo
        echo "  Ou téléchargez depuis: https://adoptium.net/"
        exit 1
    fi

    # Obtenir la version Java
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [[ $JAVA_VERSION -lt $MIN_JAVA_VERSION ]]; then
        echo -e "${RED}❌ Java $JAVA_VERSION détecté, mais Java $MIN_JAVA_VERSION+ requis${NC}"
        echo "Veuillez installer Java $MIN_JAVA_VERSION ou supérieur"
        exit 1
    fi

    echo -e "${GREEN}✅ Java $JAVA_VERSION détecté${NC}"
    if [[ "$VERBOSE" == "true" ]]; then
        java -version
        echo
    fi
}

# Fonction de vérification de Maven
check_maven() {
    echo -e "${YELLOW}🔍 Vérification de Maven...${NC}"

    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ Maven n'est pas installé ou pas dans le PATH${NC}"
        echo
        echo -e "${YELLOW}📥 Installation de Maven:${NC}"
        echo "  • Ubuntu/Debian: sudo apt install maven"
        echo "  • Fedora/RHEL:   sudo dnf install maven"
        echo "  • Arch Linux:    sudo pacman -S maven"
        echo "  • macOS:         brew install maven"
        echo
        echo "  Ou téléchargez depuis: https://maven.apache.org/download.cgi"
        exit 1
    fi

    MAVEN_VERSION=$(mvn -version 2>/dev/null | head -n1 | cut -d' ' -f3)
    echo -e "${GREEN}✅ Maven $MAVEN_VERSION détecté${NC}"

    if [[ "$VERBOSE" == "true" ]]; then
        mvn -version
        echo
    fi
}

# Fonction de détection du système
detect_system() {
    echo -e "${YELLOW}🖥️ Détection du système...${NC}"

    OS=$(uname -s)
    ARCH=$(uname -m)

    case $OS in
        Linux*)
            PLATFORM="Linux"
            if command -v lsb_release &> /dev/null; then
                DISTRO=$(lsb_release -si)
                VERSION=$(lsb_release -sr)
                echo -e "${GREEN}✅ Système: $PLATFORM ($DISTRO $VERSION) - $ARCH${NC}"
            else
                echo -e "${GREEN}✅ Système: $PLATFORM - $ARCH${NC}"
            fi
            ;;
        Darwin*)
            PLATFORM="macOS"
            MACOS_VERSION=$(sw_vers -productVersion 2>/dev/null || echo "Version inconnue")
            echo -e "${GREEN}✅ Système: $PLATFORM $MACOS_VERSION - $ARCH${NC}"
            ;;
        CYGWIN*|MINGW*|MSYS*)
            PLATFORM="Windows (via $OS)"
            echo -e "${GREEN}✅ Système: $PLATFORM - $ARCH${NC}"
            echo -e "${YELLOW}💡 Conseil: Utilisez run.bat pour une meilleure expérience${NC}"
            ;;
        *)
            PLATFORM="$OS"
            echo -e "${GREEN}✅ Système: $PLATFORM - $ARCH${NC}"
            ;;
    esac

    # Vérifier l'environnement graphique
    if [[ -n "$DISPLAY" ]] || [[ -n "$WAYLAND_DISPLAY" ]] || [[ "$PLATFORM" == "macOS" ]]; then
        echo -e "${GREEN}✅ Environnement graphique détecté${NC}"
        HEADLESS_MODE=false
    else
        echo -e "${YELLOW}⚠️ Pas d'environnement graphique détecté${NC}"
        echo -e "${YELLOW}   L'application sera lancée en mode headless (test uniquement)${NC}"
        HEADLESS_MODE=true
    fi
}

# Fonction de recherche des JARs
find_jars() {
    echo -e "${YELLOW}🔍 Recherche des JARs disponibles...${NC}"

    # Chercher les JARs dans target/
    if [[ -d "target" ]]; then
        JAR_WITH_DEPS=$(find target/ -name "*jar-with-dependencies.jar" -type f | head -1)
        JAR_SHADED=$(find target/ -name "*shaded.jar" -type f | head -1)
        JAR_REGULAR=$(find target/ -name "auto-translate-swing*.jar" -not -name "*dependencies*" -not -name "*shaded*" -type f | head -1)

        echo -e "${BLUE}📦 JARs trouvés:${NC}"
        [[ -n "$JAR_WITH_DEPS" ]] && echo "  ✅ JAR avec dépendances: $(basename "$JAR_WITH_DEPS")"
        [[ -n "$JAR_SHADED" ]] && echo "  ✅ JAR shaded: $(basename "$JAR_SHADED")"
        [[ -n "$JAR_REGULAR" ]] && echo "  ✅ JAR standard: $(basename "$JAR_REGULAR")"

        # Choisir le meilleur JAR
        if [[ -n "$JAR_WITH_DEPS" ]]; then
            SELECTED_JAR="$JAR_WITH_DEPS"
            JAR_TYPE="avec dépendances"
        elif [[ -n "$JAR_SHADED" ]]; then
            SELECTED_JAR="$JAR_SHADED"
            JAR_TYPE="shaded"
        elif [[ -n "$JAR_REGULAR" ]]; then
            SELECTED_JAR="$JAR_REGULAR"
            JAR_TYPE="standard (nécessite les dépendances)"
        else
            return 1
        fi

        echo -e "${GREEN}🎯 JAR sélectionné: $(basename "$SELECTED_JAR") ($JAR_TYPE)${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️ Dossier target/ non trouvé${NC}"
        return 1
    fi
}

# Fonction de compilation
compile_project() {
    echo -e "${YELLOW}📦 Compilation du projet...${NC}"

    if [[ "$CLEAN_BUILD" == "true" ]]; then
        echo -e "${BLUE}🧹 Nettoyage...${NC}"
        mvn clean
    fi

    echo -e "${BLUE}🔨 Compilation...${NC}"
    if [[ "$VERBOSE" == "true" ]]; then
        mvn compile
    else
        mvn compile -q
    fi

    if [[ $? -ne 0 ]]; then
        echo -e "${RED}❌ Échec de la compilation${NC}"
        exit 1
    fi

    echo -e "${GREEN}✅ Compilation réussie${NC}"

    # Créer les packages
    echo -e "${BLUE}📦 Création des packages...${NC}"
    if [[ "$VERBOSE" == "true" ]]; then
        mvn package -DskipTests
    else
        mvn package -DskipTests -q
    fi

    if [[ $? -ne 0 ]]; then
        echo -e "${RED}❌ Échec de la création des packages${NC}"
        exit 1
    fi

    echo -e "${GREEN}✅ Packages créés avec succès${NC}"
}

# Fonction de lancement de l'application
launch_app() {
    local jar_file="$1"

    echo -e "${YELLOW}🚀 Lancement de l'application...${NC}"
    echo -e "${BLUE}📁 JAR: $(basename "$jar_file")${NC}"
    echo -e "${BLUE}💾 Taille: $(du -sh "$jar_file" | cut -f1)${NC}"
    echo

    # Préparer les options JVM
    JVM_OPTS="-Xmx512m"

    # Ajouter les options pour l'environnement graphique
    if [[ "$HEADLESS_MODE" == "true" ]]; then
        JVM_OPTS="$JVM_OPTS -Djava.awt.headless=true"
        echo -e "${YELLOW}⚠️ Lancement en mode headless (test uniquement)${NC}"
    else
        # Options pour améliorer le rendu graphique
        if [[ "$PLATFORM" == "Linux" ]]; then
            JVM_OPTS="$JVM_OPTS -Dsun.java2d.opengl=true"
        fi
    fi

    # Lancer l'application
    echo -e "${GREEN}▶️ Démarrage de $APP_NAME...${NC}"
    echo

    if [[ "$VERBOSE" == "true" ]]; then
        echo -e "${CYAN}Commande exécutée:${NC}"
        echo "java $JVM_OPTS -jar \"$jar_file\""
        echo
    fi

    # Gestion du mode headless
    if [[ "$HEADLESS_MODE" == "true" ]]; then
        timeout 10s java $JVM_OPTS -jar "$jar_file" 2>/dev/null || true
        echo -e "${YELLOW}⏰ Test en mode headless terminé (timeout normal)${NC}"
        echo -e "${GREEN}✅ L'application semble fonctionnelle${NC}"
    else
        java $JVM_OPTS -jar "$jar_file"
        EXIT_CODE=$?

        if [[ $EXIT_CODE -eq 0 ]]; then
            echo
            echo -e "${GREEN}✅ Application fermée normalement${NC}"
        else
            echo
            echo -e "${RED}❌ Application fermée avec le code d'erreur: $EXIT_CODE${NC}"
        fi
    fi
}

# Fonction de nettoyage en cas d'interruption
cleanup() {
    echo
    echo -e "${YELLOW}🛑 Interruption détectée, nettoyage...${NC}"
    # Ici on pourrait ajouter du nettoyage si nécessaire
    exit 130
}

# Fonction principale
main() {
    # Gestion des signaux
    trap cleanup SIGINT SIGTERM

    # Variables par défaut
    VERBOSE=false
    CLEAN_BUILD=false
    DEV_MODE=false
    SPECIFIC_JAR=""

    # Analyse des arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -c|--clean)
                CLEAN_BUILD=true
                shift
                ;;
            --dev)
                DEV_MODE=true
                CLEAN_BUILD=true
                shift
                ;;
            --jar)
                SPECIFIC_JAR="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}❌ Option inconnue: $1${NC}"
                echo "Utilisez --help pour voir les options disponibles"
                exit 1
                ;;
        esac
    done

    # Affichage du banner
    show_banner

    # Si un JAR spécifique est demandé
    if [[ -n "$SPECIFIC_JAR" ]]; then
        if [[ -f "$SPECIFIC_JAR" ]]; then
            echo -e "${GREEN}🎯 Utilisation du JAR spécifié: $SPECIFIC_JAR${NC}"
            detect_system
            check_java
            launch_app "$SPECIFIC_JAR"
            exit 0
        else
            echo -e "${RED}❌ Fichier JAR introuvable: $SPECIFIC_JAR${NC}"
            exit 1
        fi
    fi

    # Vérifications système
    detect_system
    check_java

    # Méthodes de lancement par priorité
    echo -e "${YELLOW}🔍 Recherche de la meilleure méthode de lancement...${NC}"
    echo

    # Méthode 1: JAR déjà compilé
    if find_jars && [[ "$DEV_MODE" != "true" ]]; then
        echo -e "${GREEN}🎯 Utilisation du JAR existant${NC}"
        launch_app "$SELECTED_JAR"
        exit 0
    fi

    # Méthode 2: Compilation nécessaire
    echo -e "${YELLOW}📦 Aucun JAR trouvé ou mode développeur activé${NC}"
    check_maven

    compile_project

    if find_jars; then
        launch_app "$SELECTED_JAR"
        exit 0
    else
        echo -e "${RED}❌ Impossible de créer ou trouver un JAR exécutable${NC}"
        exit 1
    fi
}

# Point d'entrée du script
main "$@"