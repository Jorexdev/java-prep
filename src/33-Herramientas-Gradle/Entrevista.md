<div align="center">
  <a href="#"><img src="../../assets/modules/banner-33-herramientas-gradle-v1.svg" width="100%" alt=""/></a>
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

**¿Cuál es la diferencia principal entre Gradle y Maven?**
Maven usa XML (pom.xml) y tiene un ciclo de vida fijo con convención estricta. Gradle usa un DSL Groovy/Kotlin más flexible y permite personalizar tareas con scripting. Gradle es generalmente más rápido gracias a compilación incremental y build cache. Maven es más estándar en entornos Java enterprise; Gradle domina en Android y Kotlin.

---

**¿Qué ventaja ofrece el Gradle Wrapper?**
El Wrapper (`gradlew`) descarga y usa la versión de Gradle especificada en `gradle/wrapper/gradle-wrapper.properties` sin requerir instalación previa. Esto garantiza que todos los desarrolladores y el CI usan exactamente la misma versión de Gradle, evitando incompatibilidades. El Wrapper se versiona junto con el código.

---

**¿Qué es la compilación incremental en Gradle?**
Gradle rastrea las entradas (código fuente, recursos, configuración) y salidas (clases compiladas, JARs) de cada tarea. Si las entradas no han cambiado desde el último build, Gradle marca la tarea como `UP-TO-DATE` y la salta. Esto puede reducir el tiempo de build en un orden de magnitud en proyectos grandes.

---

**¿Puedes usar repositorios Maven desde Gradle?**
Sí. Gradle es completamente compatible con Maven Central y cualquier repositorio Maven. Se configura con `repositories { mavenCentral() }` o `maven { url = uri("https://mi-nexus/...") }`. También puede leer y publicar artefactos en formato Maven.

---

**¿Cuándo elegirías Gradle sobre Maven?**
Para proyectos Android (es el único soportado oficialmente), proyectos multi-módulo grandes donde la velocidad importa, proyectos Kotlin (Kotlin DSL es más natural), o cuando necesitas personalización avanzada del build. Maven sigue siendo preferible en entornos Java enterprise tradicionales donde la convención y la simplicidad son más importantes.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
