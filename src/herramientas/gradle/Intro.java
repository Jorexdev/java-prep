package herramientas.gradle;

public class Intro {

/*
    GRADLE — Herramienta de construcción moderna para proyectos Java

    ► ¿Qué es Gradle?
      Gradle es una herramienta de automatización de construcción (build automation)
      que combina la flexibilidad de Ant con la gestión de dependencias de Maven.
      Está escrita en Java y Groovy (o Kotlin DSL) y permite scripts más dinámicos
      y personalizables que los archivos XML de Maven.

    ► Objetivo principal
      Automatizar el ciclo de vida de construcción de proyectos, gestionar dependencias,
      ejecutar tests, generar artefactos y facilitar integraciones con CI/CD.

    ► Características principales
      - Utiliza un *Domain Specific Language (DSL)* basado en Groovy o Kotlin.
      - Compila más rápido que Maven gracias a su *build cache* e *incremental build*.
      - Usa un único archivo de configuración: build.gradle (o build.gradle.kts).
      - Compatible con el repositorio Maven Central y con proyectos Maven existentes.

    ► Estructura típica del proyecto Gradle
      Igual que Maven:
        src/
          main/java/        → código fuente principal
          main/resources/   → recursos
          test/java/        → código de pruebas
          test/resources/   → configuración de pruebas
        build.gradle        → configuración del build
        settings.gradle     → configuración del proyecto (nombre, módulos, etc.)
        gradlew / gradlew.bat → wrapper de Gradle
        gradle/wrapper/     → configuración del wrapper

    ► build.gradle (ejemplo básico)
      ---------------------------------------------------------
      plugins {
          id 'java'
          id 'application'
      }

      group = 'com.jorex.app'
      version = '1.0.0'
      sourceCompatibility = '21'

      repositories {
          mavenCentral()
      }

      dependencies {
          implementation 'org.springframework.boot:spring-boot-starter-web:3.2.2'
          testImplementation 'org.springframework.boot:spring-boot-starter-test:3.2.2'
      }

      application {
          mainClass = 'com.jorex.MainApp'
      }
      ---------------------------------------------------------

    ► Gradle Wrapper
      El *wrapper* permite ejecutar Gradle sin necesidad de tenerlo instalado globalmente.
      Cada proyecto usa su propia versión de Gradle para evitar incompatibilidades.

      - gradlew → script Linux/Mac
      - gradlew.bat → script Windows

      Ejemplo:
        ./gradlew build
        ./gradlew test

    ► Repositorios
      Igual que Maven, Gradle descarga dependencias de:
        - Maven Central
        - Repositorios privados (Nexus, Artifactory)
        - Repositorios locales

    ► Integración con CI/CD
      Gradle se integra fácilmente con Jenkins, GitHub Actions y GitLab CI.
      Ejemplo de pipeline Jenkins:
        steps {
          sh './gradlew clean test build'
        }

    ► Ventajas principales
      - Compilación incremental (solo recompila lo necesario).
      - Soporte para multi-módulo y proyectos híbridos (Java + Kotlin + Groovy).
      - Caché de compilación local y remota.
      - Configuración flexible mediante scripting.
      - Wrapper para builds consistentes en distintos entornos.
*/
}
