REM ===== run.bat (Windows) =====
@echo off
echo Lancement du Traducteur Automatique...

REM Méthode 1 : JAR avec dépendances séparées
if exist target\lib\javafx-controls-*.jar (
    echo Utilisation du JAR avec dépendances séparées...
    java --module-path target\lib ^
         --add-modules javafx.controls,javafx.fxml,javafx.swing ^
         --add-exports java.desktop/sun.awt=ALL-UNNAMED ^
         --add-exports java.desktop/sun.java2d=ALL-UNNAMED ^
         -jar target\auto-translate-1.0-SNAPSHOT.jar
    goto :end
)

REM Méthode 2 : JAR avec toutes les dépendances
if exist target\auto-translate-1.0-SNAPSHOT-jar-with-dependencies.jar (
    echo Utilisation du JAR avec toutes les dépendances...
    java --add-exports java.desktop/sun.awt=ALL-UNNAMED ^
         --add-exports java.desktop/sun.java2d=ALL-UNNAMED ^
         -jar target\auto-translate-1.0-SNAPSHOT-jar-with-dependencies.jar
    goto :end
)

REM Méthode 3 : Via Maven
echo Aucun JAR trouvé, utilisation de Maven...
mvn javafx:run

:end
pause

REM ===== run.sh (Linux/Mac) =====
#!/bin/bash
echo "Lancement du Traducteur Automatique..."

# Méthode 1 : JAR avec dépendances séparées
if [ -f target/lib/javafx-controls-*.jar ]; then
    echo "Utilisation du JAR avec dépendances séparées..."
    java --module-path target/lib \
         --add-modules javafx.controls,javafx.fxml,javafx.swing \
         --add-exports java.desktop/sun.awt=ALL-UNNAMED \
         --add-exports java.desktop/sun.java2d=ALL-UNNAMED \
         -jar target/auto-translate-1.0-SNAPSHOT.jar
    exit 0
fi

# Méthode 2 : JAR avec toutes les dépendances
if [ -f target/auto-translate-1.0-SNAPSHOT-jar-with-dependencies.jar ]; then
    echo "Utilisation du JAR avec toutes les dépendances..."
    java --add-exports java.desktop/sun.awt=ALL-UNNAMED \
         --add-exports java.desktop/sun.java2d=ALL-UNNAMED \
         -jar target/auto-translate-1.0-SNAPSHOT-jar-with-dependencies.jar
    exit 0
fi

# Méthode 3 : Via Maven
echo "Aucun JAR trouvé, utilisation de Maven..."
mvn javafx:run