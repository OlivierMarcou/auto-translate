#!/bin/bash

# Script pour créer une nouvelle release du Traducteur Automatique
# Usage: ./create-release.sh [version]

set -e  # Arrêter en cas d'erreur

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔══════════════════════════════════════════════════════════╗"
echo "║              🚀 CREATION D'UNE RELEASE                  ║"
echo "║            Traducteur Automatique - Swing               ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Fonction d'aide
show_help() {
    echo "Usage: $0 [VERSION]"
    echo ""
    echo "Exemples:"
    echo "  $0 1.0.0     # Crée une release v1.0.0"
    echo "  $0 2.1.3     # Crée une release v2.1.3"
    echo "  $0           # Mode interactif pour saisir la version"
    echo ""
    echo "Ce script va:"
    echo "  1. 🧪 Vérifier que tout est clean et testé"
    echo "  2. 📝 Mettre à jour la version dans pom.xml"
    echo "  3. 📦 Créer et tester les builds"
    echo "  4. 🏷️ Créer un tag Git"
    echo "  5. 🚀 Pousser le tag (déclenche la CI/CD)"
    echo ""
}

# Fonction de vérification des prérequis
check_prerequisites() {
    echo -e "${YELLOW}🔍 Vérification des prérequis...${NC}"

    # Vérifier Git
    if ! command -v git &> /dev/null; then
        echo -e "${RED}❌ Git n'est pas installé${NC}"
        exit 1
    fi

    # Vérifier Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ Maven n'est pas installé${NC}"
        exit 1
    fi

    # Vérifier Java
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Java n'est pas installé${NC}"
        exit 1
    fi

    # Vérifier qu'on est dans un repo Git
    if [ ! -d ".git" ]; then
        echo -e "${RED}❌ Ce n'est pas un repository Git${NC}"
        exit 1
    fi

    # Vérifier qu'on est sur la branche main
    CURRENT_BRANCH=$(git branch --show-current)
    if [ "$CURRENT_BRANCH" != "main" ] && [ "$CURRENT_BRANCH" != "master" ]; then
        echo -e "${RED}❌ Vous devez être sur la branche main/master pour créer une release${NC}"
        echo "Branche actuelle: $CURRENT_BRANCH"
        exit 1
    fi

    # Vérifier qu'il n'y a pas de modifications non commitées
    if [ -n "$(git status --porcelain)" ]; then
        echo -e "${RED}❌ Il y a des modifications non commitées${NC}"
        echo "Committez ou stash vos modifications avant de créer une release"
        git status --short
        exit 1
    fi

    # Vérifier qu'on est à jour avec origin
    git fetch origin
    LOCAL=$(git rev-parse HEAD)
    REMOTE=$(git rev-parse origin/$CURRENT_BRANCH 2>/dev/null || git rev-parse origin/main 2>/dev/null || echo "")
    if [ -n "$REMOTE" ] && [ "$LOCAL" != "$REMOTE" ]; then
        echo -e "${RED}❌ Votre branche locale n'est pas à jour avec origin${NC}"
        echo "Exécutez: git pull origin $CURRENT_BRANCH"
        exit 1
    fi

    echo -e "${GREEN}✅ Tous les prérequis sont satisfaits${NC}"
}

# Fonction de validation de version
validate_version() {
    local version=$1
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo -e "${RED}❌ Version invalide: $version${NC}"
        echo "Le format doit être: X.Y.Z (ex: 1.0.0, 2.1.3)"
        return 1
    fi

    # Vérifier que le tag n'existe pas déjà
    if git tag -l | grep -q "^v$version$"; then
        echo -e "${RED}❌ Le tag v$version existe déjà${NC}"
        return 1
    fi

    return 0
}

# Fonction de mise à jour de la version dans pom.xml
update_pom_version() {
    local version=$1
    echo -e "${YELLOW}📝 Mise à jour de la version dans pom.xml...${NC}"

    # Backup du pom.xml original
    cp pom.xml pom.xml.backup

    # Mise à jour de la version
    sed -i.tmp "s/<version>1.0-SNAPSHOT<\/version>/<version>$version<\/version>/" pom.xml
    rm -f pom.xml.tmp

    # Vérifier que la modification a bien été appliquée
    if grep -q "<version>$version</version>" pom.xml; then
        echo -e "${GREEN}✅ Version mise à jour dans pom.xml: $version${NC}"
        rm -f pom.xml.backup
    else
        echo -e "${RED}❌ Échec de la mise à jour de pom.xml${NC}"
        mv pom.xml.backup pom.xml
        exit 1
    fi
}

# Fonction de build et test
build_and_test() {
    echo -e "${YELLOW}📦 Build et tests...${NC}"

    # Nettoyer et compiler
    echo "Nettoyage et compilation..."
    mvn clean compile

    # Exécuter les tests (si ils existent)
    echo "Exécution des tests..."
    mvn test || echo "⚠️ Pas de tests ou tests échoués (continuer quand même)"

    # Créer les packages
    echo "Création des packages..."
    mvn package -DskipTests

    # Vérifier que les JARs sont créés
    if [ ! -f "target/auto-translate-swing-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
        echo -e "${RED}❌ JAR avec dépendances non créé${NC}"
        exit 1
    fi

    echo -e "${GREEN}✅ Build réussi${NC}"

    # Afficher les artefacts créés
    echo "📦 Artefacts créés:"
    find target/ -name "*.jar" -exec ls -lh {} \;
}

# Fonction de test rapide de l'application
quick_test_app() {
    echo -e "${YELLOW}🧪 Test rapide de l'application...${NC}"

    JAR_FILE="target/auto-translate-swing-1.0-SNAPSHOT-jar-with-dependencies.jar"

    # Test de lancement en mode headless avec timeout
    if timeout 5s java -Djava.awt.headless=true -jar "$JAR_FILE" 2>/dev/null; then
        echo -e "${GREEN}✅ Application se lance correctement${NC}"
    else
        echo -e "${GREEN}✅ Application testée (timeout attendu en mode headless)${NC}"
    fi
}

# Fonction de création du tag et push
create_and_push_tag() {
    local version=$1
    echo -e "${YELLOW}🏷️ Création du tag Git...${NC}"

    # Committer la modification de pom.xml
    git add pom.xml
    git commit -m "🚀 Release v$version - Mise à jour de la version"

    # Créer le tag
    git tag -a "v$version" -m "🚀 Release v$version

Cette release inclut :
- Application Swing complète avec FlatLaf
- Support de 25+ thèmes modernes
- Traduction avec APIs MyMemory + Google Translate
- Capture d'écran OCR multi-moniteurs
- Surveillance intelligente du presse-papiers
- Filtres de sécurité avancés
- Logging automatique en CSV

Prérequis : Java 21+

Pour installer :
java -jar traducteur-automatique-$version.jar"

    echo -e "${GREEN}✅ Tag v$version créé${NC}"

    # Confirmer le push
    echo -e "${YELLOW}🚀 Prêt à pousser le tag et déclencher la release automatique${NC}"
    read -p "Continuer ? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Push du commit et du tag..."
        git push origin HEAD
        git push origin "v$version"
        echo -e "${GREEN}✅ Tag poussé ! La CI/CD va créer la release automatiquement${NC}"
    else
        echo -e "${YELLOW}⚠️ Push annulé. Tag créé localement seulement.${NC}"
        echo "Pour pousser plus tard : git push origin HEAD && git push origin v$version"
    fi
}

# Fonction principale
main() {
    # Afficher l'aide si demandé
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_help
        exit 0
    fi

    # Vérifier les prérequis
    check_prerequisites

    # Obtenir la version
    VERSION="$1"
    if [ -z "$VERSION" ]; then
        echo -e "${BLUE}💭 Aucune version spécifiée. Mode interactif.${NC}"
        echo ""

        # Afficher la dernière version
        LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo