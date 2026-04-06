package herramientas.git;

public class Intro {

/*
    GIT — Sistema de control de versiones distribuido

    ► ¿Qué es Git?
      Git es un sistema de control de versiones distribuido creado por Linus Torvalds
      en 2005. Permite llevar un registro histórico de los cambios en el código fuente
      y facilita el trabajo colaborativo entre desarrolladores.

      Cada copia de un repositorio Git es completa y contiene tod0 el historial,
      lo que permite trabajar sin conexión y realizar merges o commits locales.

    ► Objetivos principales
      - Rastrear versiones del código a lo largo del tiempo.
      - Permitir la colaboración simultánea de varios desarrolladores.
      - Resolver conflictos de cambios en ramas diferentes.
      - Mantener un historial detallado y recuperable del proyecto.

    ► Terminología básica
      - **Repositorio (repo):** carpeta con el historial de commits de un proyecto.
      - **Commit:** un punto en el tiempo con los cambios confirmados.
      - **Branch (rama):** línea independiente de desarrollo dentro del proyecto.
      - **Merge:** integración de cambios entre ramas.
      - **Remote:** repositorio alojado en un servidor (GitHub, GitLab, Bitbucket…).
      - **Clone:** copia local de un repositorio remoto.
      - **Push/Pull:** sincronización entre la copia local y el remoto.

    ► Ventajas de Git
      - Distribuido: cada desarrollador tiene una copia completa del repositorio.
      - Rápido: las operaciones son locales (commit, diff, branch, merge).
      - Seguro: protege la integridad del historial mediante hashing (SHA-1).
      - Flexible: permite múltiples flujos de trabajo (GitFlow, Trunk-Based, etc.).
      - Integración nativa con plataformas CI/CD (GitHub Actions, Jenkins…).

    ► Estructura interna de Git
      Git maneja tres zonas:
        1. **Working Directory** → donde editas los archivos.
        2. **Staging Area (Index)** → zona de preparación para el commit.
        3. **Repository (Local)** → historial de commits confirmados.

      Flujo típico:
        Modificas un archivo  →  git add → git commit → git push

    ► Ciclo básico de trabajo
      1. Clonar un repositorio remoto:
         git clone https://github.com/jorex/java-prep.git

      2. Crear una nueva rama:
         git checkout -b feature/nueva-funcionalidad

      3. Realizar cambios y agregarlos al staging:
         git add .

      4. Confirmar los cambios:
         git commit -m "Implementada nueva funcionalidad"

      5. Subir la rama al repositorio remoto:
         git push origin feature/nueva-funcionalidad

    ► Archivos especiales
      - `.gitignore` → define qué archivos o carpetas se deben excluir del control.
        Ejemplo:
          target/
          .idea/
          *.log
          *.class

      - `.gitattributes` → configura comportamientos específicos (fin de línea, merges, etc.)

    ► Integración con herramientas
      Git se integra directamente con:
        - GitHub (repos públicos y privados)
        - GitLab (repos corporativos y CI/CD)
        - Bitbucket, Azure DevOps, etc.
      También se usa en pipelines de integración continua.

    ► Buenas prácticas generales
      - Usar mensajes de commit descriptivos.
      - Agrupar commits relacionados (no mezclar temas).
      - Actualizar ramas con frecuencia (`git pull origin main`).
      - Crear ramas específicas por feature o bugfix.
      - Revisar código mediante Pull Requests antes de hacer merge.
*/
}
