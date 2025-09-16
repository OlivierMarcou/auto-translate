@echo off
echo ========================================
echo    Traducteur Automatique - Swing
echo ========================================
echo.

REM Vérifier Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERREUR: Java n'est pas installé ou pas dans le PATH
    echo Téléchargez Java 21+ depuis: https://adoptium.net/
    pause
    exit /b 1
)

REM Vérifier Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERREUR: Maven n'est pas installé ou pas dans le PATH
    echo Téléchargez Maven depuis: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Java et Maven détectés ✓
echo.

REM Méthode 1 : JAR avec toutes les dépendances
if exist target\auto-translate-swing-1.0-SNAPSHOT-jar-with-dependencies.jar (
    echo Utilisation du JAR avec toutes les dépendances...
    echo Lancement de l'application...
    java -jar target\auto-translate-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
    goto :end
)

REM Méthode 2 : JAR shaded
if exist target\auto-translate-swing-1.0-SNAPSHOT-shaded.jar (
    echo Utilisation du JAR shaded...
    echo Lancement de l'application...
    java -jar target\auto-translate-swing-1.0-SNAPSHOT-shaded.jar
    goto :end
)

REM Méthode 3 : Compilation et lancement via Maven
echo Aucun JAR trouvé, compilation et lancement via Maven...
echo.
echo Compilation du projet...
mvn clean compile

if errorlevel 1 (
    echo ERREUR: Échec de la compilation
    pause
    exit /b 1
)

echo Compilation réussie ✓
echo.
echo Lancement de l'application...
mvn exec:java -Dexec.mainClass="net.arkaine.TraducteurAutomatique"

:end
echo.
echo Application fermée.
pause