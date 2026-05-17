<div align="center">
  <a href="#"><img src="../../assets/modules/banner-24-spring-starters-v1.svg" width="100%" alt=""/></a>
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

**¿Qué hace `@SpringBootApplication`?**
Es una meta-anotación que combina tres: `@Configuration` (la clase define beans), `@EnableAutoConfiguration` (activa la autoconfiguración basada en el classpath) y `@ComponentScan` (escanea el paquete actual y subpaquetes para detectar beans con `@Component`, `@Service`, etc.).

---

**¿Cómo funciona la autoconfiguración de Spring Boot?**
Spring Boot incluye un archivo `spring.factories` (o `AutoConfiguration.imports` en Boot 3+) con listas de clases de autoconfiguración. Al arrancar, carga esas clases y las aplica condicionalmente según el classpath, las propiedades y los beans existentes. Si detecta `DataSource` en el classpath, configura automáticamente un pool de conexiones.

---

**¿Cómo deshabilitas una autoconfiguración específica?**
Con `@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})` o con la propiedad `spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration`.

---

**¿Qué diferencia hay entre un starter y una dependencia normal?**
Un starter es solo un POM que agrupa dependencias relacionadas sin código propio. Al añadir `spring-boot-starter-web`, transitivamente obtienes spring-web, spring-webmvc, tomcat-embed, jackson-databind, etc., ya con versiones compatibles entre sí probadas por Spring Boot.

---

**¿Qué es `@ConditionalOnMissingBean`?**
Una anotación de autoconfiguración que aplica la configuración solo si no hay ya un bean del tipo especificado. Es el mecanismo de extensión: Spring Boot configura un bean por defecto, pero si tú defines el tuyo propio, el de Spring Boot cede. Así funciona la personalización sin modificar la autoconfiguración.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
