package devops.pipelines;

public class Intro {
/*
    PIPELINES CI/CD — Jenkins, GitHub Actions, GitLab CI

    ► ¿Qué es CI/CD?
      CI (Continuous Integration) = integrar cambios de código frecuentemente,
      ejecutando builds y tests automáticamente con cada push.
      CD (Continuous Delivery/Deployment) = entregar esos cambios al entorno
      de producción de forma automatizada y confiable.

      Objetivo: reducir el tiempo entre escribir código y tenerlo en producción,
      minimizando errores manuales y detectando problemas lo antes posible.

    ── GITHUB ACTIONS ─────────────────────────────────────────────────────────

    ► ¿Qué es?
      Sistema de CI/CD integrado en GitHub. Se define mediante archivos YAML
      en el directorio `.github/workflows/`.

    ► Conceptos clave
      - Workflow: archivo YAML que define el pipeline completo.
      - Event: disparador del workflow (push, pull_request, schedule, etc.).
      - Job: conjunto de steps que se ejecutan en un runner.
      - Step: comando o acción individual dentro de un job.
      - Action: unidad reutilizable de lógica (ej. actions/checkout@v4).
      - Runner: máquina virtual donde se ejecuta el job (ubuntu-latest, windows-latest...).

    ► Ejemplo básico (Spring Boot)
      # .github/workflows/ci.yml
      name: CI Pipeline

      on:
        push:
          branches: [main, develop]
        pull_request:
          branches: [main]

      jobs:
        build:
          runs-on: ubuntu-latest
          steps:
            - uses: actions/checkout@v4

            - name: Set up JDK 21
              uses: actions/setup-java@v4
              with:
                java-version: '21'
                distribution: 'temurin'

            - name: Build and test with Maven
              run: mvn clean verify

            - name: Upload artifact
              uses: actions/upload-artifact@v4
              with:
                name: app-jar
                path: target/*.jar

    ► Buenas prácticas GitHub Actions
      - Usar cache de dependencias (actions/cache) para acelerar builds.
      - Usar secrets del repositorio para credenciales (nunca en el YAML).
      - Separar jobs: build → test → deploy.
      - Usar environments para controlar despliegues con aprobaciones manuales.

    ── JENKINS ────────────────────────────────────────────────────────────────

    ► ¿Qué es?
      Servidor de automatización open source, muy extendido en entornos enterprise.
      Se puede instalar on-premise o en la nube. La lógica del pipeline se define
      en un archivo llamado Jenkinsfile (sintaxis Groovy).

    ► Tipos de pipeline
      - Declarative Pipeline: sintaxis más moderna y estructurada (recomendada).
      - Scripted Pipeline: sintaxis Groovy pura, más flexible pero más compleja.

    ► Ejemplo Declarative Pipeline
      pipeline {
          agent any

          tools {
              maven 'Maven 3.9'
              jdk 'JDK 21'
          }

          stages {
              stage('Checkout') {
                  steps {
                      git branch: 'main', url: 'https://github.com/org/repo.git'
                  }
              }
              stage('Build') {
                  steps {
                      sh 'mvn clean package -DskipTests'
                  }
              }
              stage('Test') {
                  steps {
                      sh 'mvn test'
                  }
                  post {
                      always {
                          junit 'target/surefire-reports/*.xml'
                      }
                  }
              }
              stage('Deploy') {
                  when {
                      branch 'main'
                  }
                  steps {
                      sh 'docker build -t miapp:latest . && docker push miregistry/miapp:latest'
                  }
              }
          }

          post {
              failure {
                  mail to: 'equipo@empresa.com', subject: 'Pipeline fallido'
              }
          }
      }

    ► Conceptos importantes Jenkins
      - Agent: máquina donde se ejecuta el pipeline (any, none, label, docker).
      - Stage: fase lógica del pipeline (build, test, deploy).
      - Step: acción concreta dentro de un stage.
      - Credentials: gestión segura de secretos (tokens, passwords).
      - Blue Ocean: interfaz visual moderna para Jenkins.
      - Multibranch Pipeline: crea pipelines automáticamente por cada rama del repo.

    ── GITLAB CI ──────────────────────────────────────────────────────────────

    ► ¿Qué es?
      CI/CD integrado en GitLab. El pipeline se define en `.gitlab-ci.yml`
      en la raíz del repositorio.

    ► Conceptos clave
      - Pipeline: conjunto de jobs definidos en .gitlab-ci.yml.
      - Stage: fase del pipeline; los jobs del mismo stage se ejecutan en paralelo.
      - Job: unidad de trabajo que ejecuta un script en un runner.
      - Runner: agente que ejecuta los jobs (shared, group o specific).
      - Artifact: ficheros que un job produce y pasa al siguiente.
      - Cache: dependencias que se reusan entre ejecuciones.

    ► Ejemplo básico
      # .gitlab-ci.yml
      stages:
        - build
        - test
        - deploy

      variables:
        MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2"

      cache:
        paths:
          - .m2/

      build:
        stage: build
        image: maven:3.9-eclipse-temurin-21
        script:
          - mvn clean package -DskipTests
        artifacts:
          paths:
            - target/*.jar

      test:
        stage: test
        image: maven:3.9-eclipse-temurin-21
        script:
          - mvn verify

      deploy:
        stage: deploy
        script:
          - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA .
          - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA
        only:
          - main

    ── COMPARATIVA ────────────────────────────────────────────────────────────

      Herramienta       | Hosting      | Configuración | Ideal para
      ------------------|--------------|---------------|------------------------
      GitHub Actions    | SaaS GitHub  | YAML          | Proyectos en GitHub
      GitLab CI         | SaaS/On-prem | YAML          | Proyectos en GitLab
      Jenkins           | Self-hosted  | Groovy/YAML   | Enterprise, on-premise

    ► Preguntas típicas de entrevista
      - ¿Qué diferencia hay entre CI y CD?
      - ¿Cómo proteges secretos en un pipeline?
      - ¿Qué hace el stage "verify" en Maven?
      - ¿Cómo separarías un pipeline en múltiples ambientes (dev, staging, prod)?
      - ¿Qué es un artifact en el contexto de CI/CD?
*/
}
