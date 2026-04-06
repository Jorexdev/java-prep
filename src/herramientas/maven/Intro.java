package herramientas.maven;


public class Intro {

/*
    MAVEN — Herramienta de construcción y gestión de proyectos Java

    ► ¿Qué es?
      Maven es una herramienta de build automation desarrollada por Apache.
      Se utiliza para:
        - Gestionar dependencias (librerías externas).
        - Compilar, testear y empaquetar el código.
        - Ejecutar plugins y fases de build.
        - Estandarizar la estructura de proyectos Java.

    ► ¿Por qué usarlo?
      - Simplifica la gestión de librerías (no más JARs manuales).
      - Estructura de proyecto estándar (src/main/java, src/test/java...).
      - Reproducibilidad: el mismo `pom.xml` genera siempre el mismo build.
      - Integración con CI/CD (Jenkins, GitHub Actions, etc.).

    ► Archivo principal: pom.xml
      (Project Object Model)
      Define el proyecto, sus dependencias y configuraciones.

    ► Coordinadas Maven
      Cada artefacto se identifica por:
        groupId:    nombre del grupo o empresa (ej. org.springframework.boot)
        artifactId: nombre del artefacto/librería (ej. spring-boot-starter-web)
        version:    versión exacta del artefacto

      → Ejemplo: org.springframework.boot:spring-boot-starter-web:3.3.4

    ► Ciclo de vida Maven (Build Lifecycle)
      Maven organiza tareas en fases predefinidas:
        - validate → verifica que el proyecto esté correcto
        - compile  → compila el código fuente
        - test     → ejecuta los tests
        - package  → empaqueta en JAR/WAR
        - verify   → valida calidad y tests adicionales
        - install  → instala en el repositorio local (~/.m2)
        - deploy   → publica en un repositorio remoto (empresa, Nexus, etc.)

      Ejemplo:
         mvn clean install
         → limpia la carpeta target y compila + testea + instala el JAR en local.

    ► Plugins
      Extienden el comportamiento de Maven (compilación, test, empaquetado...).
      Ejemplo:
        - maven-compiler-plugin  → compila código Java
        - maven-surefire-plugin  → ejecuta los tests
        - spring-boot-maven-plugin → ejecuta aplicaciones Spring Boot

    ► Repositorios
      - Local: ~/.m2/repository
      - Central: https://repo.maven.apache.org/maven2
      - Privados (Nexus, Artifactory...)

    ► Ventajas clave
      - Gestión automática de dependencias transitivas.
      - Estructura estándar para todos los proyectos.
      - Integración sencilla con Jenkins y otras herramientas CI/CD.
      - Fácil migración entre entornos gracias a los *profiles*.
*/
}
