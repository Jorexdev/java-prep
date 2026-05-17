<div align="center">
  <a href="#"><img src="../../assets/modules/banner-33-herramientas-gradle-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Gradle** es una herramienta de build automation moderna que combina la flexibilidad de scripts con la gestión de dependencias de Maven. Usa un **DSL** basado en Groovy o Kotlin en lugar de XML.

```
build.gradle (Groovy) / build.gradle.kts (Kotlin DSL)
settings.gradle            ← nombre del proyecto, módulos
gradlew / gradlew.bat      ← Gradle Wrapper
gradle/wrapper/            ← configuración del wrapper
src/main/java/             ← misma estructura que Maven
```

El **Gradle Wrapper** es el mecanismo que garantiza que todos usan la misma versión de Gradle sin instalación previa — `./gradlew build` descarga la versión correcta automáticamente.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**build.gradle básico (Kotlin DSL):**
```kotlin
plugins {
    id("java")
    id("org.springframework.boot") version "3.3.4"
}

group = "com.ejemplo"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

**Comandos principales:**
```bash
./gradlew build          # compila, testa y empaqueta
./gradlew test           # ejecuta tests
./gradlew clean build    # limpia y recompila
./gradlew dependencies   # árbol de dependencias
./gradlew tasks          # lista todas las tareas disponibles
```

**Ventajas vs Maven:**

| | Gradle | Maven |
|---|---|---|
| Velocidad | Más rápido (incremental + caché) | Más lento |
| Flexibilidad | Alta (scripting) | Media (XML) |
| Curva aprendizaje | Mayor | Menor |
| Estándar industria | Creciendo (Android, Kotlin) | Alto (Java enterprise) |
| Multi-módulo | Excelente | Bueno |

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Compilación incremental: solo recompila lo que cambió — builds más rápidos.
- Build cache local y remota: evita recompilar cuando el input no ha cambiado.
- Gradle Wrapper: mismo resultado en cualquier entorno sin instalación manual.
- Es el estándar de facto en proyectos Android y Kotlin.

Ver [Comandos.java](Comandos.java) para el cheatsheet de tareas Gradle y [GradleVsMaven.java](GradleVsMaven.java) para la comparativa detallada.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
