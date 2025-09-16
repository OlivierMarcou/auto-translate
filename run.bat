@echo off
setlocal enabledelayedexpansion

REM =============================================================================
REM 🚀 TRADUCTEUR AUTOMATIQUE - SWING
REM Script de lancement automatique pour Windows
REM =============================================================================

REM Configuration
set "APP_NAME=Traducteur Automatique"
set "MIN_JAVA_VERSION=21"
set "MAVEN_MIN_VERSION=3.9"
set "VERBOSE=false"
set "CLEAN_BUILD=false"
set "DEV_MODE=false"
set "SPECIFIC_JAR="

REM Couleurs pour Windows (si supportées)
set "ESC="
for /f %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
set "RED=%ESC%[91m"
set "GREEN=%ESC%[92m"
set "YELLOW=%ESC%[93m"
set "BLUE=%ESC%[94m"
set "PURPLE=%ESC%[95m"
set "CYAN=%ESC%[96m"
set "WHITE=%ESC%[97m"
set "NC=%ESC%[0m"

REM Fonction d'affichage du banner
:show_banner
echo.
echo %BLUE%╔══════════════════════════════════════════════════════════╗%NC%
echo %BLUE%║              🚀 TRADUCTEUR AUTOMATIQUE                  ║%NC%
echo %BLUE%║                   Version Swing                         ║%NC%
echo %BLUE%║              Avec FlatLaf ^& 25+ Thèmes                  ║%NC%
echo %BLUE%╚══════════════════════════════════════════════════════════╝%NC%
echo.
goto :eof

REM Fonction d'aide
:show_help
echo %CYAN%Usage: %~nx0 [OPTIONS]%NC%
echo.
echo %YELLOW%Options:%NC%
echo   /h, /help      Afficher cette aide
echo   /v, /verbose   Mode verbeux avec plus de détails
echo   /c, /clean     Nettoyer avant de compiler
echo   /dev           Mode développeur (recompile toujours)
echo   /jar [FILE]    Lancer un JAR spécifique
echo.
echo %YELLOW%Exemples:%NC%
echo   %~nx0                           # Lancement standard
echo   %~nx0 /clean                    # Nettoyage + compilation + lancement
echo   %~nx0 /jar mon-app.jar          # Lancer un JAR spécifique
echo   %~nx0 /dev                      # Mode développeur
echo.
goto :eof

REM Fonction de vérification de Java
:check_java
echo %YELLOW%🔍 Vérification de Java...%NC%

java -version >nul 2>&1
if errorlevel 1 (
    echo %RED%❌ Java n'est pas installé ou pas dans le PATH%NC%
    echo.
    echo %YELLOW%📥 Installation de Java:%NC%
    echo   • Téléchargez Java 21+ depuis: https://adoptium.net/
    echo   • Ou utilisez: winget install EclipseAdoptium.Temurin.21.JDK
    echo   • Ou utilisez: choco install temurin21
    echo.
    pause
    exit /b 1
)

REM Obtenir la version Java
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION_STR=%%g
    set JAVA_VERSION_STR=!JAVA_VERSION_STR:"=!
)

REM Extraire le numéro de version majeure
for /f "delims=." %%a in ("%JAVA_VERSION_STR%") do set JAVA_MAJOR=%%a

if !JAVA_MAJOR! LSS %MIN_JAVA_VERSION% (
    echo %RED%❌ Java !JAVA_MAJOR! détecté, mais Java %MIN_JAVA_VERSION%+ requis%NC%
    echo Veuillez installer Java %MIN_JAVA_VERSION% ou supérieur
    pause
    exit /b 1
)

echo %GREEN%✅ Java !JAVA_MAJOR! détecté%NC%

if "%VERBOSE%"=="true" (
    java -version
    echo.
)
goto :eof

REM Fonction de vérification de Maven
:check_maven
echo %YELLOW%🔍 Vérification de Maven...%NC%

mvn -version >nul 2>&1
if errorlevel 1 (
    echo %RED%❌ Maven n'est pas installé ou pas dans le PATH%NC%
    echo.
    echo %YELLOW%📥 Installation de Maven:%NC%
    echo   • Téléchargez depuis: https://maven.apache.org/download.cgi
    echo   • Ou utilisez: winget install Apache.Maven
    echo   • Ou utilisez: choco install maven
    echo.
    pause
    exit /b 1
)

for /f "tokens=3" %%g in ('mvn -version 2^>nul ^| findstr "Apache Maven"') do set MAVEN_VERSION=%%g
echo %GREEN%✅ Maven !MAVEN_VERSION! détecté%NC%

if "%VERBOSE%"=="true" (
    mvn -version
    echo.
)
goto :eof

REM Fonction de détection du système
:detect_system
echo %YELLOW%🖥️ Détection du système...%NC%

REM Obtenir les informations système
for /f "tokens=2 delims==" %%a in ('wmic os get Caption /value ^| find "="') do set OS_NAME=%%a
for /f "tokens=2 delims==" %%a in ('wmic os get Version /value ^| find "="') do set OS_VERSION=%%a
for /f "tokens=2 delims==" %%a in ('wmic computersystem get SystemType /value ^| find "="') do set ARCH=%%a

echo %GREEN%✅ Système: !OS_NAME! (!OS_VERSION!) - !ARCH!%NC%

REM Vérifier l'environnement graphique (Windows a toujours un environnement graphique)
echo %GREEN%✅ Environnement graphique disponible%NC%
set "HEADLESS_MODE=false"
goto :eof

REM Fonction de recherche des JARs
:find_jars
echo %YELLOW%🔍 Recherche des JARs disponibles...%NC%

set "JAR_WITH_DEPS="
set "JAR_SHADED="
set "JAR_REGULAR="
set "SELECTED_JAR="

if exist "target\" (
    REM Chercher JAR avec dépendances
    for %%f in ("target\*jar-with-dependencies.jar") do (
        if exist "%%f" set "JAR_WITH_DEPS=%%f"
    )

    REM Chercher JAR shaded
    for %%f in ("target\*shaded.jar") do (
        if exist "%%f" set "JAR_SHADED=%%f"
    )

    REM Chercher JAR standard
    for %%f in ("target\auto-translate-swing*.jar") do (
        echo %%f | findstr /v "dependencies" | findstr /v "shaded" >nul
        if not errorlevel 1 set "JAR_REGULAR=%%f"
    )

    echo %BLUE%📦 JARs trouvés:%NC%
    if defined JAR_WITH_DEPS (
        for %%f in ("!JAR_WITH_DEPS!") do echo   ✅ JAR avec dépendances: %%~nxf
    )
    if defined JAR_SHADED (
        for %%f in ("!JAR_SHADED!") do echo   ✅ JAR shaded: %%~nxf
    )
    if defined JAR_REGULAR (
        for %%f in ("!JAR_REGULAR!") do echo   ✅ JAR standard: %%~nxf
    )

    REM Choisir le meilleur JAR
    if defined JAR_WITH_DEPS (
        set "SELECTED_JAR=!JAR_WITH_DEPS!"
        set "JAR_TYPE=avec dépendances"
    ) else if defined JAR_SHADED (
        set "SELECTED_JAR=!JAR_SHADED!"
        set "JAR_TYPE=shaded"
    ) else if defined JAR_REGULAR (
        set "SELECTED_JAR=!JAR_REGULAR!"
        set "JAR_TYPE=standard (nécessite les dépendances)"
    ) else (
        goto :find_jars_failed
    )

    for %%f in ("!SELECTED_JAR!") do echo %GREEN%🎯 JAR sélectionné: %%~nxf (!JAR_TYPE!)%NC%
    exit /b 0
) else (
    echo %YELLOW%⚠️ Dossier target\ non trouvé%NC%
)

:find_jars_failed
exit /b 1

REM Fonction de compilation
:compile_project
echo %YELLOW%📦 Compilation du projet...%NC%

if "%CLEAN_BUILD%"=="true" (
    echo %BLUE%🧹 Nettoyage...%NC%
    mvn clean
    if errorlevel 1 (
        echo %RED%❌ Échec du nettoyage%NC%
        pause
        exit /b 1
    )
)

echo %BLUE%🔨 Compilation...%NC%
if "%VERBOSE%"=="true" (
    mvn compile
) else (
    mvn compile -q
)

if errorlevel 1 (
    echo %RED%❌ Échec de la compilation%NC%
    pause
    exit /b 1
)

echo %GREEN%✅ Compilation réussie%NC%

REM Créer les packages
echo %BLUE%📦 Création des packages...%NC%
if "%VERBOSE%"=="true" (
    mvn package -DskipTests
) else (
    mvn package -DskipTests -q
)

if errorlevel 1 (
    echo %RED%❌ Échec de la création des packages%NC%
    pause
    exit /b 1
)

echo %GREEN%✅ Packages créés avec succès%NC%
goto :eof

REM Fonction de lancement de l'application
:launch_app
set "jar_file=%~1"

echo %YELLOW%🚀 Lancement de l'application...%NC%
for %%f in ("!jar_file!") do (
    echo %BLUE%📁 JAR: %%~nxf%NC%
    echo %BLUE%💾 Taille: %%~zf bytes%NC%
)
echo.

REM Préparer les options JVM
set "JVM_OPTS=-Xmx512m"

REM Options pour améliorer le rendu graphique sur Windows
set "JVM_OPTS=%JVM_OPTS% -Dsun.java2d.d3d=true -Dsun.java2d.ddoffscreen=false"

echo %GREEN%▶️ Démarrage de %APP_NAME%...%NC%
echo.

if "%VERBOSE%"=="true" (
    echo %CYAN%Commande exécutée:%NC%
    echo java %JVM_OPTS% -jar "%jar_file%"
    echo.
)

REM Lancer l'application
java %JVM_OPTS% -jar "%jar_file%"
set "EXIT_CODE=!errorlevel!"

if !EXIT_CODE! EQU 0 (
    echo.
    echo %GREEN%✅ Application fermée normalement%NC%
) else (
    echo.
    echo %RED%❌ Application fermée avec le code d'erreur: !EXIT_CODE!%NC%
)

goto :eof

REM Fonction de gestion des arguments
:parse_args
:parse_loop
if "%~1"=="" goto :parse_done
if /i "%~1"=="/h" goto :help_requested
if /i "%~1"=="/help" goto :help_requested
if /i "%~1"=="/?" goto :help_requested
if /i "%~1"=="/v" (
    set "VERBOSE=true"
    shift & goto :parse_loop
)
if /i "%~1"=="/verbose" (
    set "VERBOSE=true"
    shift & goto :parse_loop
)
if /i "%~1"=="/c" (
    set "CLEAN_BUILD=true"
    shift & goto :parse_loop
)
if /i "%~1"=="/clean" (
    set "CLEAN_BUILD=true"
    shift & goto :parse_loop
)
if /i "%~1"=="/dev" (
    set "DEV_MODE=true"
    set "CLEAN_BUILD=true"
    shift & goto :parse_loop
)
if /i "%~1"=="/jar" (
    set "SPECIFIC_JAR=%~2"
    shift & shift & goto :parse_loop
)

echo %RED%❌ Option inconnue: %~1%NC%
echo Utilisez /help pour voir les options disponibles
pause
exit /b 1

:help_requested
call :show_help
exit /b 0

:parse_done
goto :eof

REM Fonction principale
:main
REM Analyse des arguments
call :parse_args %*
if errorlevel 1 exit /b 1

REM Affichage du banner
call :show_banner

REM Si un JAR spécifique est demandé
if defined SPECIFIC_JAR (
    if exist "!SPECIFIC_JAR!" (
        echo %GREEN%🎯 Utilisation du JAR spécifié: !SPECIFIC_JAR!%NC%
        call :detect_system
        call :check_java
        if errorlevel 1 exit /b 1
        call :launch_app "!SPECIFIC_JAR!"
        goto :end_success
    ) else (
        echo %RED%❌ Fichier JAR introuvable: !SPECIFIC_JAR!%NC%
        pause
        exit /b 1
    )
)

REM Vérifications système
call :detect_system
call :check_java
if errorlevel 1 exit /b 1

REM Recherche de la meilleure méthode de lancement
echo %YELLOW%🔍 Recherche de la meilleure méthode de lancement...%NC%
echo.

REM Méthode 1: JAR déjà compilé
call :find_jars
if not errorlevel 1 (
    if not "%DEV_MODE%"=="true" (
        echo %GREEN%🎯 Utilisation du JAR existant%NC%
        call :launch_app "!SELECTED_JAR!"
        goto :end_success
    )
)

REM Méthode 2: Compilation nécessaire
echo %YELLOW%📦 Aucun JAR trouvé ou mode développeur activé%NC%
call :check_maven
if errorlevel 1 exit /b 1

call :compile_project
if errorlevel 1 exit /b 1

call :find_jars
if not errorlevel 1 (
    call :launch_app "!SELECTED_JAR!"
    goto :end_success
) else (
    echo %RED%❌ Impossible de créer ou trouver un JAR exécutable%NC%
    pause
    exit /b 1
)

:end_success
echo.
echo %GREEN%✅ Script terminé avec succès%NC%
pause
exit /b 0

REM Point d'entrée du script
call :main %*
exit /b %errorlevel%