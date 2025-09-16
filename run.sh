#!/bin/bash

echo "========================================"
echo "   Traducteur Automatique - Swing"
echo "========================================"
echo

# Vérifier Java
if ! command -v java &> /dev/null; then
    echo "ERREUR: Java n'est pas installé ou pas dans le PATH"
    echo "Installez Java 21+ depuis: https://adoptium.net/"
    exit 1
fi

# Vérifier Maven
if ! command -v mvn &> /dev/null; then
    echo "ERREUR: Maven n'est pas installé ou pas dans le PATH"
    echo "Installez Maven depuis: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "Java et Maven détectés ✓"
echo

# Méthode 1 : JAR avec toutes les dépendances
if [ -f "target/auto-translate-swing-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    echo "Utilisation du JAR avec toutes les dépendances..."
    echo "Lancement de l'application..."
    java -jar target/auto-translate-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
    exit 0
fi

# Méthode 2 : JAR shaded
if [ -f "target/auto-translate-swing-1.0-SNAPSHOT-shaded.jar" ]; then
    echo "Utilisation du JAR shaded..."
    echo "Lancement de l'application..."
    java -jar target/auto-translate-swing-1.0-SNAPSHOT-shaded.jar
    exit 0
fi

# Méthode 3 : Compilation et lancement via Maven
echo "Aucun JAR trouvé, compilation et lancement via Maven..."
echo
echo "Compilation du projet..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "ERREUR: Échec de la compilation"
    exit 1
fi

echo "Compilation réussie ✓"
echo
echo "Lancement de l'application..."
mvn exec:java -Dexec.mainClass="net.arkaine.TraducteurAutomatique"

echo
echo "Application fermée."