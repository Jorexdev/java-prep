<div align="center">
  <a href="#"><img src="../../assets/modules/banner-23-spring-config-v1.svg" width="100%" alt=""/></a>
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

**¿Cuál es la diferencia entre `@Value` y `@ConfigurationProperties`?**
`@Value` inyecta una propiedad individual — simple pero difícil de mantener con muchas propiedades. `@ConfigurationProperties` mapea un grupo de propiedades a un POJO: type-safe, soporta validación con `@Validated`, genera metadata para autocompletado IDE, y maneja conversión de tipos automáticamente (kebab-case → camelCase).

---

**¿En qué orden tiene precedencia la configuración en Spring Boot?**
De mayor a menor: argumentos CLI (`--prop=val`), variables de entorno, `application-{profile}.properties`, `application.properties`, valores por defecto en `@Value`. Esto permite sobrescribir cualquier configuración en producción con variables de entorno sin tocar el código.

---

**¿Cómo externalizas configuración sensible (passwords)?**
Nunca en application.properties en texto claro si se sube a git. Opciones: variables de entorno (`${DB_PASSWORD}`), secrets del sistema operativo, gestores de secretos (HashiCorp Vault, AWS Secrets Manager, Kubernetes Secrets). Spring Cloud Vault integra Vault directamente.

---

**¿Qué diferencia hay entre application.properties y application.yml?**
Son equivalentes en capacidades — solo difieren en formato. YAML es más legible para configuraciones anidadas y admite listas de forma más natural. Properties es más simple y compatible con más herramientas. Spring Boot soporta ambos y puede coexistir (aunque es más claro usar uno solo).

---

**¿Cómo pasas una propiedad por línea de comandos?**
`java -jar app.jar --server.port=9090 --spring.datasource.url=jdbc:...`. Spring Boot las procesa con la mayor precedencia. También puedes usar la notación de sistema Java: `-Dserver.port=9090`, aunque tiene menor precedencia que los argumentos de aplicación.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
