public class ProfilesYPlugins {

/*
    MAVEN — Profiles y Plugins

    ► 1. Profiles (Perfiles Maven)
      Los perfiles permiten definir configuraciones específicas para distintos entornos
      (desarrollo, test, producción, etc.) sin modificar el pom principal.

      Un profile puede cambiar:
        - Propiedades (variables del build)
        - Dependencias
        - Plugins o configuraciones
        - Parámetros de compilación o ejecución

      Ejemplo:
      ---------------------------------------------------------
      <profiles>
        <!-- Perfil de desarrollo -->
        <profile>
          <id>dev</id>
          <properties>
            <env.name>desarrollo</env.name>
            <db.url>jdbc:h2:mem:testdb</db.url>
          </properties>
        </profile>

        <!-- Perfil de producción -->
        <profile>
          <id>prod</id>
          <properties>
            <env.name>produccion</env.name>
            <db.url>jdbc:mysql://localhost/miapp</db.url>
          </properties>
        </profile>
      </profiles>
      ---------------------------------------------------------

      Activar un perfil desde la consola:
        mvn clean install -Pdev
        mvn clean package -Pprod

      Los perfiles se pueden activar también:
        - Por variable de entorno (MAVEN_OPTS)
        - Por propiedad del sistema
        - Por archivo settings.xml

    ► 2. Plugins Maven
      Los plugins son componentes reutilizables que ejecutan tareas dentro de cada fase
      del ciclo de vida. Pueden ser estándar o personalizados.

      Los más comunes:
        - maven-compiler-plugin → compila el código fuente
        - maven-surefire-plugin → ejecuta tests
        - maven-jar-plugin → empaqueta el código en un JAR
        - spring-boot-maven-plugin → ejecuta aplicaciones Spring Boot

      Ejemplo básico de configuración de plugins:
      ---------------------------------------------------------
      <build>
        <plugins>

          <!-- Plugin de compilación -->
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
              <source>21</source>     <!-- versión de Java fuente -->
              <target>21</target>     <!-- versión de bytecode -->
            </configuration>
          </plugin>

          <!-- Plugin de empaquetado con Spring Boot -->
          <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
              <execution>
                <goals>
                  <goal>repackage</goal>
                </goals>
              </execution>
            </executions>
          </plugin>

        </plugins>
      </build>
      ---------------------------------------------------------

    ► 3. Ciclo de vida y plugins personalizados
      Maven asocia los plugins a fases específicas del ciclo de vida:

      validate  → validación de estructura
      compile   → compilación del código
      test      → ejecución de pruebas
      package   → empaquetado en JAR/WAR
      verify    → comprobaciones adicionales
      install   → instalación en el repositorio local (~/.m2)
      deploy    → publicación en repositorio remoto

      Ejemplo:
        mvn clean package
        mvn test
        mvn install

    ► 4. Crear un perfil con plugin específico
      Un perfil también puede incluir un plugin o una configuración distinta para
      un entorno determinado:

      ---------------------------------------------------------
      <profiles>
        <profile>
          <id>dev</id>
          <build>
            <plugins>
              <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                  <skipTests>true</skipTests> <!-- saltar tests en dev -->
                </configuration>
              </plugin>
            </plugins>
          </build>
        </profile>
      </profiles>
      ---------------------------------------------------------

    ► 5. Beneficio general
      - Los perfiles permiten adaptar el proyecto a distintos entornos sin cambiar código.
      - Los plugins proporcionan extensibilidad al proceso de build.
      - Ambos juntos hacen de Maven una herramienta altamente configurable.
*/
}
