package herramientas.maven;


public class Comandos {

/*
    MAVEN — Comandos y Flags

    ► Maven se controla mediante comandos ejecutados desde la terminal, aunque si
      tienes el plugin instalado, puedes hacerlo mediante la interfaz :)
      Cada comando ejecuta una o varias fases del ciclo de vida del build.

    -----------------------------------------------------------------------
    COMANDOS PRINCIPALES
    -----------------------------------------------------------------------

    1. mvn clean
       - Elimina el directorio /target (archivos compilados, JAR, logs, etc.)
       - Se usa antes de compilar o empaquetar para empezar “limpio”.

       Ejemplo: mvn clean

    2. mvn compile
       - Compila el código fuente ubicado en src/main/java.
       - Genera las clases .class dentro de /target/classes.

       Ejemplo: mvn compile

    3. mvn test
       - Ejecuta los tests JUnit (src/test/java).
       - Usa el plugin maven-surefire-plugin.
       - Detiene el build si alguna prueba falla.

       Ejemplo: mvn test

    4. mvn package
       - Empaqueta el código compilado y los recursos en un artefacto (JAR o WAR).
       - El archivo se guarda en /target.
       - Requiere que compile y test se hayan ejecutado antes.

       Ejemplo: mvn package

    5. mvn verify
       - Realiza validaciones adicionales después del empaquetado.
       - Se usa para ejecutar análisis de calidad o validaciones personalizadas.

    6. mvn install
       - Instala el artefacto (JAR/WAR) en el repositorio local (~/.m2/repository).
       - Permite que otros proyectos Maven lo utilicen como dependencia.

       Ejemplo: mvn install

    7. mvn deploy
       - Publica el artefacto en un repositorio remoto (ej. Nexus, Artifactory).
       - Suele ejecutarse en entornos de CI/CD o publicación.

       Ejemplo: mvn deploy

    8. mvn validate
       - Verifica que la estructura del proyecto y el pom.xml son válidos.
       - No compila ni ejecuta nada.

       Ejemplo: mvn validate

    9. mvn site
       - Genera documentación HTML del proyecto (en /target/site).
       - Requiere configuración del plugin maven-site-plugin.

       Ejemplo: mvn site

    -----------------------------------------------------------------------
    COMANDOS ÚTILES ADICIONALES
    -----------------------------------------------------------------------

    10. mvn dependency:tree
        - Muestra un árbol de dependencias y versiones usadas.
        - Muy útil para depurar conflictos entre dependencias.

        Ejemplo: mvn dependency:tree

    11. mvn dependency:analyze
        - Detecta dependencias declaradas no utilizadas o usadas sin declarar.

    12. mvn help:effective-pom
        - Muestra el pom.xml “efectivo”, es decir, el resultado final tras herencias y perfiles.

    13. mvn help:active-profiles
        - Lista los perfiles Maven actualmente activos.

    -----------------------------------------------------------------------
    FLAGS (MODIFICADORES)
    -----------------------------------------------------------------------

    -f, --file <path>
      Especifica un pom.xml distinto al predeterminado.
      Ejemplo: mvn -f /ruta/a/otro/pom.xml clean install

    -P, --activate-profiles <id>
      Activa un perfil concreto definido en el pom.xml.
      Ejemplo: mvn clean package -Pdev

    -D<propiedad>=<valor>
      Define una propiedad del sistema o del pom.
      Ejemplo: mvn clean package -DskipTests=true

    -q, --quiet
      Modo silencioso: solo muestra errores.

    -X, --debug
      Modo depuración: muestra información detallada de la ejecución.

    -U
      Fuerza a Maven a actualizar dependencias desde los repositorios remotos.

    -pl, --projects <módulo>
      Compila o ejecuta solo los módulos indicados (en proyectos multi-módulo).
      Ejemplo: mvn install -pl api,core

    -am, --also-make
      Compila también las dependencias requeridas por el módulo indicado.

    -----------------------------------------------------------------------
    COMANDOS COMBINADOS MÁS FRECUENTES
    -----------------------------------------------------------------------

    mvn clean install
      Limpia, compila, ejecuta los tests e instala el JAR/WAR en el repositorio local.

    mvn clean package -DskipTests
      Empaqueta la aplicación sin ejecutar los tests.

    mvn test -Pdev
      Ejecuta los tests con el perfil de desarrollo activo.

    mvn verify -Denv=staging
      Verifica el build con propiedades personalizadas para un entorno.

    mvn spring-boot:run
      Ejecuta directamente una aplicación Spring Boot (si tiene el plugin).
*/

}
