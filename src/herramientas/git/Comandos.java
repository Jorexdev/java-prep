package herramientas.git;

public class Comandos {

/*
    GIT — Comandos, flags y flujos de trabajo

    ► Git se basa en tres zonas:
        1. Working Directory (tu carpeta actual)
        2. Staging Area (archivos preparados para commit)
        3. Repository (historial local de commits)

    -----------------------------------------------------------------------
    CONFIGURACIÓN INICIAL
    -----------------------------------------------------------------------

    git config --global user.name "Tu Nombre"
    git config --global user.email "tuemail@dominio.com"
       → Define tu identidad global (para todos los repositorios).

    git config --list
       → Muestra la configuración actual de Git.

    git init
       → Crea un nuevo repositorio Git local (genera la carpeta .git/).

    git clone <url>
       → Clona un repositorio remoto (GitHub, GitLab, etc.) en tu máquina.

    -----------------------------------------------------------------------
    ESTADO Y CAMBIOS
    -----------------------------------------------------------------------

    git status
       → Muestra el estado del repositorio (archivos modificados, sin seguimiento, etc.).

    git diff
       → Muestra las diferencias entre el código modificado y el último commit.

    git add <archivo>
       → Añade un archivo al *staging area* (preparado para commit).
       Ejemplo: git add src/Main.java

    git add .
       → Añade todos los archivos modificados.

    git restore <archivo>
       → Revierte cambios en el archivo desde el último commit.

    git reset <archivo>
       → Quita un archivo del staging area sin borrar los cambios.

    -----------------------------------------------------------------------
    COMMITS Y HISTORIAL
    -----------------------------------------------------------------------

    git commit -m "Mensaje descriptivo"
       → Guarda los cambios del staging en el historial local.

    git commit --amend
       → Permite modificar el último commit (mensaje o contenido).

    git log
       → Muestra el historial de commits con hash, autor y mensaje.

    git log --oneline --graph --all
       → Muestra un resumen gráfico y simplificado de todas las ramas.

    git show <hash>
       → Muestra los cambios realizados en un commit específico.

    git blame <archivo>
       → Muestra quién y cuándo modificó cada línea de un archivo.

    -----------------------------------------------------------------------
    RAMAS Y MERGE
    -----------------------------------------------------------------------

    git branch
       → Lista todas las ramas locales.

    git branch <nombre>
       → Crea una nueva rama.

    git checkout <rama>
       → Cambia a una rama existente.

    git checkout -b <rama>
       → Crea y cambia a una nueva rama.

    git merge <rama>
       → Combina los cambios de otra rama en la actual.

    git rebase <rama>
       → Reaplica tus commits sobre la rama indicada (historial más limpio).

    git branch -d <rama>
       → Elimina una rama local ya fusionada.

    git push origin --delete <rama>
       → Elimina una rama remota.

    -----------------------------------------------------------------------
    SINCRONIZACIÓN REMOTA
    -----------------------------------------------------------------------

    git remote -v
       → Muestra los repositorios remotos configurados.

    git fetch
       → Descarga los cambios remotos sin aplicarlos.

    git pull
       → Descarga y aplica los cambios remotos a la rama actual.

    git push
       → Envía tus commits locales al repositorio remoto.

    git push -u origin <rama>
       → Asocia una rama local con su rama remota (upstream).

    -----------------------------------------------------------------------
    STASH Y REVERTIR CAMBIOS
    -----------------------------------------------------------------------

    git stash
       → Guarda temporalmente cambios no confirmados y limpia el directorio.

    git stash list
       → Lista todos los stashes guardados.

    git stash apply
       → Restaura los últimos cambios guardados sin borrarlos.

    git stash pop
       → Restaura los cambios y elimina el stash.

    git revert <hash>
       → Crea un commit nuevo que revierte los cambios de un commit anterior.

    git reset --hard <hash>
       → Restaura tod0 el proyecto al estado de un commit anterior (se pierden cambios).

    -----------------------------------------------------------------------
    GESTIÓN DE CONFLICTOS
    -----------------------------------------------------------------------

    Durante un merge, si Git no puede combinar los cambios automáticamente:
       - Los archivos en conflicto se marcan con <<<<<< HEAD y >>>>>>.
       - Debes editar manualmente, guardar y ejecutar:

         git add <archivo>
         git commit

    Para cancelar un merge:
         git merge --abort

    -----------------------------------------------------------------------
    GITFLOW (flujo de trabajo estructurado)
    -----------------------------------------------------------------------

    GitFlow define ramas específicas para cada etapa del ciclo de desarrollo:

        main / master → contiene el código estable (en producción)
        develop       → rama principal de desarrollo
        feature/*     → nuevas funcionalidades
        release/*     → preparación de versiones
        hotfix/*      → correcciones urgentes en producción

    Ejemplo de flujo:
        git checkout develop
        git checkout -b feature/nueva-funcion
        git add .
        git commit -m "Nueva función completada"
        git push origin feature/nueva-funcion
        git checkout develop
        git merge feature/nueva-funcion
        git push origin develop

    -----------------------------------------------------------------------
    PULL REQUESTS (PRs)
    -----------------------------------------------------------------------

    ► Concepto
       Un Pull Request (también llamado Merge Request) es una solicitud para
       integrar los cambios de una rama (por ejemplo, feature/xyz) en otra
       (por ejemplo, develop o main).

    ► Buenas prácticas en PRs
       - Pequeños y específicos (1 feature o bugfix por PR).
       - Revisión de código antes del merge (por otro desarrollador).
       - Mensajes de commit descriptivos.
       - Integración continua (CI) ejecutando tests automáticos.

    ► Flujo general en GitHub
       1. Crea una rama → feature/api-endpoint
       2. Realiza commits locales
       3. git push origin feature/api-endpoint
       4. En GitHub: “Compare & pull request”
       5. Esperar revisión → Merge → Rama eliminada

    -----------------------------------------------------------------------
    COMANDOS ÚTILES ADICIONALES
    -----------------------------------------------------------------------

    git tag <nombre>
       → Crea una etiqueta (versión) en un commit.
       Ejemplo: git tag v1.0.0

    git show-ref --tags
       → Lista todas las etiquetas existentes.

    git fetch --all --prune
       → Actualiza todas las ramas y elimina referencias obsoletas.

    git gc
       → Limpia y optimiza el repositorio local.

    git shortlog -sn
       → Muestra los autores con número de commits.

    git archive -o build.zip HEAD
       → Empaqueta el estado actual del repositorio en un ZIP.

*/
}
