<div align="center">
  <a href="#"><img src="../../assets/modules/banner-32-herramientas-maven-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Cuál es la diferencia entre `mvn install` y `mvn deploy`?**
`install` empaqueta el proyecto y lo instala en el repositorio local (`~/.m2/`) — disponible para otros proyectos Maven en la misma máquina. `deploy` hace lo mismo y además publica el artefacto en el repositorio remoto configurado (Nexus, Artifactory, Maven Central) — disponible para el equipo.

---

**¿Qué son las dependencias transitivas en Maven?**
Si tu proyecto depende de A, y A depende de B, Maven descarga B automáticamente aunque no lo declares. Esto simplifica la gestión de dependencias pero puede causar conflictos de versión. Se resuelven con `<exclusions>` para excluir una transitiva o declarando explícitamente la versión deseada con `<dependencyManagement>`.

---

**¿Para qué sirven los perfiles (profiles) en Maven?**
Para tener configuraciones distintas según el entorno: dependencias, propiedades, plugins activos. Se activan con `mvn package -Pprod`. Útil para tener una DB H2 en tests y PostgreSQL en producción, o diferentes configuraciones de logging sin cambiar el pom principal.

---

**¿Qué diferencia hay entre SNAPSHOT y RELEASE en Maven?**
`1.0.0-SNAPSHOT` es una versión en desarrollo: Maven siempre busca la versión más reciente en el repositorio (no se cachea). `1.0.0` (sin SNAPSHOT) es una versión inmutable: una vez publicada no se puede sobreescribir. En producción siempre se usan releases.

---

**¿Cómo excluyes una dependencia transitiva?**
Con `<exclusions>` dentro de la dependencia:
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

---

**¿Qué es un BOM (Bill of Materials) en Maven y para qué sirve?**
Un BOM es un POM especial con `<packaging>pom</packaging>` que declara versiones de dependencias en `<dependencyManagement>` sin añadirlas como dependencias directas. Se importa con `<scope>import</scope>` en tu `<dependencyManagement>`. Permite que varios módulos de un proyecto usen versiones consistentes sin repetirlas en cada POM. Spring Boot publica su propio BOM (`spring-boot-dependencies`) que gestiona las versiones de todas sus dependencias.

---

**¿Qué es el Maven Enforcer Plugin y qué problemas resuelve?**
El Enforcer Plugin añade reglas que se validan durante el build y lo abortan si no se cumplen. Reglas comunes: versión mínima de Java o Maven, ausencia de dependencias en conflicto (`dependencyConvergence`), prohibición de dependencias SNAPSHOT en releases. Evita que el proyecto compile localmente con JDK 17 pero falle en CI con JDK 11, o que versiones distintas de la misma dependencia coexistan en el classpath causando `NoSuchMethodError`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
