#!/bin/bash

# Script pour gérer les releases GitHub (supprimer, recréer, lister)
# Usage: ./manage-release.sh [COMMAND] [VERSION]

set -e

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
REPO_OWNER=$(git config --get remote.origin.url | sed 's/.*github.com[:/]\([^/]*\)\/\([^.]*\).*/\1/')
REPO_NAME=$(git config --get remote.origin.url | sed 's/.*github.com[:/]\([^/]*\)\/\([^.]*\).*/\2/')

show_help() {
    echo "Usage: $0 [COMMAND] [VERSION]"
    echo ""
    echo "Commands:"
    echo "  list                 Liste toutes les releases"
    echo "  delete VERSION       Supprime une release (ex: delete 1.0.0)"
    echo "  recreate VERSION     Supprime et recrée une release"
    echo "  info VERSION         Affiche les infos d'une release"
    echo ""
    echo "Examples:"
    echo "  $0 list"
    echo "  $0 delete 1.0.0"
    echo "  $0 recreate 1.0.0"
    echo "  $0 info 1.0.0"
}

# Vérifier que gh CLI est installé
check_gh_cli() {
    if ! command -v gh &> /dev/null; then
        echo -e "${RED}❌ GitHub CLI (gh) n'est pas installé${NC}"
        echo ""
        echo "Installation:"
        echo "  • Ubuntu/Debian: sudo apt install gh"
        echo "  • macOS: brew install gh"
        echo "  • Windows: winget install GitHub.cli"
        echo ""
        echo "Puis: gh auth login"
        exit 1
    fi

    # Vérifier l'authentification
    if ! gh auth status &>/dev/null; then
        echo -e "${RED}❌ Vous n'êtes pas connecté à GitHub${NC}"
        echo "Exécutez: gh auth login"
        exit 1
    fi
}

# Lister toutes les releases
list_releases() {
    echo -e "${BLUE}📋 Releases existantes pour $REPO_OWNER/$REPO_NAME:${NC}"
    echo ""

    gh release list --repo "$REPO_OWNER/$REPO_NAME" || {
        echo -e "${YELLOW}⚠️ Aucune release trouvée ou erreur d'accès${NC}"
        return 1
    }
}

# Obtenir les infos d'une release
get_release_info() {
    local version="$1"
    local tag="v$version"

    echo -e "${BLUE}ℹ️ Informations sur la release $tag:${NC}"
    echo ""

    gh release view "$tag" --repo "$REPO_OWNER/$REPO_NAME" || {
        echo -e "${RED}❌ Release $tag non trouvée${NC}"
        return 1
    }
}

# Supprimer une release
delete_release() {
    local version="$1"
    local tag="v$version"

    if [ -z "$version" ]; then
        echo -e "${RED}❌ Version manquante${NC}"
        echo "Usage: $0 delete VERSION"
        return 1
    fi

    echo -e "${YELLOW}🗑️ Suppression de la release $tag...${NC}"

    # Vérifier si la release existe
    if ! gh release view "$tag" --repo "$REPO_OWNER/$REPO_NAME" &>/dev/null; then
        echo -e "${YELLOW}⚠️ Release $tag n'existe pas${NC}"
        return 0
    fi

    # Demander confirmation
    echo -e "${RED}⚠️ Attention: Cette action est irréversible !${NC}"
    echo "Release à supprimer: $tag"
    echo "Repository: $REPO_OWNER/$REPO_NAME"
    echo ""
    read -p "Confirmer la suppression? (y/N) " -n 1 -r
    echo

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # Supprimer la release (garde le tag)
        if gh release delete "$tag" --repo "$REPO_OWNER/$REPO_NAME" --yes; then
            echo -e "${GREEN}✅ Release $tag supprimée${NC}"

            # Demander si on supprime aussi le tag
            echo ""
            read -p "Supprimer aussi le tag Git? (y/N) " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                git tag -d "$tag" 2>/dev/null || echo "Tag local déjà absent"
                git push origin ":refs/tags/$tag" 2>/dev/null || echo "Tag distant déjà absent"
                echo -e "${GREEN}✅ Tag $tag supprimé${NC}"
            fi
        else
            echo -e "${RED}❌ Échec de la suppression${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️ Suppression annulée${NC}"
    fi
}

# Recréer une release (supprimer puis recréer le tag)
recreate_release() {
    local version="$1"
    local tag="v$version"

    if [ -z "$version" ]; then
        echo -e "${RED}❌ Version manquante${NC}"
        echo "Usage: $0 recreate VERSION"
        return 1
    fi

    echo -e "${BLUE}🔄 Recréation de la release $tag...${NC}"
    echo ""

    # Supprimer l'ancienne release
    delete_release "$version"

    # Attendre un peu
    sleep 2

    # Recréer le tag et déclencher la CI/CD
    echo -e "${YELLOW}🏷️ Recréation du tag $tag...${NC}"

    # S'assurer qu'on est à jour
    git fetch origin

    # Supprimer le tag local s'il existe
    git tag -d "$tag" 2>/dev/null || true

    # Créer le nouveau tag
    git tag -a "$tag" -m "🚀 Release $tag (recréée)"

    # Pousser le tag
    git push origin "$tag"

    echo -e "${GREEN}✅ Tag $tag recréé et poussé${NC}"
    echo -e "${BLUE}🤖 La CI/CD va maintenant créer la release automatiquement${NC}"
    echo ""
    echo "Surveillez: https://github.com/$REPO_OWNER/$REPO_NAME/actions"
}

# Fonction principale
main() {
    local command="$1"
    local version="$2"

    if [ -z "$command" ]; then
        show_help
        exit 1
    fi

    check_gh_cli

    case "$command" in
        "list"|"ls")
            list_releases
            ;;
        "delete"|"del"|"remove"|"rm")
            delete_release "$version"
            ;;
        "recreate"|"redo"|"reset")
            recreate_release "$version"
            ;;
        "info"|"show"|"view")
            get_release_info "$version"
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            echo -e "${RED}❌ Commande inconnue: $command${NC}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Détection automatique du repository
if [ -z "$REPO_OWNER" ] || [ -z "$REPO_NAME" ]; then
    echo -e "${RED}❌ Impossible de détecter le repository GitHub${NC}"
    echo "Assurez-vous d'être dans un repository Git avec une origine GitHub"
    exit 1
fi

echo -e "${BLUE}🔧 Gestion des releases pour $REPO_OWNER/$REPO_NAME${NC}"
echo ""

main "$@"