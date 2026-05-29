# Ejercicios — 32 Herramientas-Maven
## Difícil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Full dependency tree**
Construye el árbol completo de dependencias con deduplicación y conflict resolution.
Imprime el árbol con indentación al estilo `mvn dependency:tree`.
Las dependencias omitidas por conflicto se marcan con `(omitted for conflict with X.Y)`.

---

**Ejercicio 2 — Enforcer rules**
Implementa la interfaz `EnforcerRule` con método `validate(Pom)`.
Crea tres reglas concretas:
- `RequireJava(int version)` — falla si la propiedad `java.version` es menor que la requerida
- `BannedDependency(String coordinates)` — falla si el POM contiene esa dependencia
- `RequireUpperBoundDeps` — falla si alguna dep transitiva tiene versión superior a la declarada

Evalúa un POM con violaciones y muestra todas las violaciones encontradas.

---

**Ejercicio 3 — Custom lifecycle**
Define un lifecycle personalizado `"integration"` con fases:
`pre-integration, integration-test, post-integration`.
Registra plugins en esas fases.
Ejecuta `run("integration-test")`.
Demo completo con 3 plugins, uno por fase.

---

**Ejercicio 4 — Local repository cache**
Implementa `LocalRepo(Map<String,Artifact>)` y `RemoteRepo(Map<String,Artifact>)`.
`resolve(dep)` busca primero en el repo local; si no está, "descarga" del remoto y guarda en local.
Demo con 10 dependencias donde 6 ya están en caché local.
Muestra qué deps se resuelven localmente y cuáles requieren descarga.

---

**Ejercicio 5 — Multi-module con herencia de parent POM**
Implementa `ParentPom(groupId, artifactId, version, properties, dependencyManagement)` y `ChildModule(name, parent, ownDeps)`. Los módulos hijos heredan las propiedades y el `dependencyManagement` del parent. Al resolver las dependencias de un hijo, las versiones se toman del `dependencyManagement` heredado si no se declaran explícitamente. El `ReactorBuild` ordena los módulos (topológico) y construye en orden. Demo con parent + 4 módulos (`common`, `domain`, `service` depende de domain y common, `web` depende de service) y 3 dependencias gestionadas en el parent BOM.

---
