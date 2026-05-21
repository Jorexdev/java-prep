public class ExpComandos {

/*
    GRADLE — ExpComandos y Flags

    ► ¿Qué son?
      Gradle se controla mediante comandos de consola, igual que Maven.
      Cada comando ejecuta tareas (*tasks*) definidas en el archivo build.gradle.

    -----------------------------------------------------------------------
    COMANDOS PRINCIPALES
    -----------------------------------------------------------------------

    1. gradle tasks
       - Lista todas las tareas disponibles en el proyecto.
       - Muestra las tareas agrupadas por tipo (build, verification, documentation, etc.)
       Ejemplo:
          ./gradlew tasks

    2. gradle clean
       - Elimina el directorio /build (equivalente a mvn clean).
       - Limpia los resultados previos del build.
       Ejemplo:
          ./gradlew clean

    3. gradle build
       - Compila el código fuente, ejecuta los tests y genera el artefacto (JAR o WAR).
       - Es la tarea más usada y ejecuta todas las dependencias necesarias.
       Ejemplo:
          ./gradlew build

    4. gradle assemble
       - Empaqueta el proyecto (JAR/WAR) sin ejecutar tests.
       - Útil cuando solo se necesita el artefacto.
       Ejemplo:
          ./gradlew assemble

    5. gradle test
       - Ejecuta todas las pruebas (JUnit, TestNG, etc.) del proyecto.
       - Genera reportes en build/reports/tests.
       Ejemplo:
          ./gradlew test

    6. gradle run
       - Ejecuta la aplicación si se usa el plugin `application`.
       - Usa la propiedad `mainClass` definida en build.gradle.
       Ejemplo:
          ./gradlew run

    7. gradle jar
       - Genera explícitamente el archivo .jar del proyecto.
       Ejemplo:
          ./gradlew jar

    8. gradle check
       - Ejecuta tareas de verificación (tests, análisis estáticos, etc.)
       Ejemplo:
          ./gradlew check

    9. gradle dependencies
       - Muestra el árbol completo de dependencias.
       - Muy útil para detectar conflictos o versiones duplicadas.
       Ejemplo:
          ./gradlew dependencies

    10. gradle help
        - Muestra ayuda general sobre Gradle y los comandos disponibles.

    11. gradle wrapper
        - Crea los archivos del *Gradle Wrapper* (gradlew, gradlew.bat, gradle/wrapper/).
        Ejemplo:
          gradle wrapper --gradle-version 8.7

    -----------------------------------------------------------------------
    COMANDOS ESPECÍFICOS PARA SPRING BOOT
    -----------------------------------------------------------------------

    12. gradle bootRun
        - Ejecuta una aplicación Spring Boot directamente sin empaquetar.
        Ejemplo:
          ./gradlew bootRun

    13. gradle bootJar
        - Genera un archivo JAR ejecutable para Spring Boot.
        Ejemplo:
          ./gradlew bootJar

    14. gradle bootBuildImage
        - Crea una imagen Docker de la aplicación usando el plugin de Spring Boot.
        Ejemplo:
          ./gradlew bootBuildImage

    -----------------------------------------------------------------------
    FLAGS Y PARÁMETROS ÚTILES
    -----------------------------------------------------------------------

    -q, --quiet
       Ejecuta en modo silencioso, mostrando solo resultados esenciales.

    -i, --info
       Muestra información adicional del proceso.

    -d, --debug
       Modo detallado, útil para depurar errores de compilación.

    --stacktrace
       Muestra la traza completa de errores (muy útil para debugging).

    -x <tarea>
       Excluye una tarea específica del build.
       Ejemplo:
          ./gradlew build -x test   → compila sin ejecutar tests

    --refresh-dependencies
       Fuerza la descarga de todas las dependencias de nuevo.

    -P<propiedad>=<valor>
       Define una propiedad para usar dentro del script build.gradle.
       Ejemplo:
          ./gradlew build -Penv=prod

    -D<propiedad>=<valor>
       Define propiedades del sistema Java (similar a Maven).
       Ejemplo:
          ./gradlew test -Dspring.profiles.active=dev

    --continue
       Continúa la ejecución del build incluso si una tarea falla.

    --profile
       Genera un informe HTML de rendimiento del build.

    --offline
       Ejecuta Gradle sin conexión, usando dependencias en caché.

    -----------------------------------------------------------------------
    COMANDOS COMBINADOS MÁS FRECUENTES
    -----------------------------------------------------------------------

    ./gradlew clean build
       Limpia, compila y genera el artefacto final.

    ./gradlew build -x test
       Genera el artefacto sin ejecutar las pruebas.

    ./gradlew test --info
       Ejecuta tests mostrando información adicional.

    ./gradlew bootRun
       Inicia una aplicación Spring Boot.

    ./gradlew clean bootJar
       Limpia y genera el JAR ejecutable para producción.

    ./gradlew dependencies --refresh-dependencies
       Actualiza dependencias y muestra su árbol completo.
*/
}
