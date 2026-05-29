# Ejercicios — 32 Herramientas-Maven
## Medio
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Dependency conflict resolution**
Implementa "nearest wins" con BFS para calcular la distancia de cada versión al proyecto raíz.
Demo con un árbol de 4 niveles donde D aparece en múltiples paths con distintas versiones.
Muestra el árbol, los caminos candidatos y la versión ganadora con justificación.

---

**Ejercicio 2 — BOM import**
Implementa `BOM(Map<String,String> versions)`.
Un POM puede importar un BOM en el bloque `dependencyManagement`.
Al declarar una dependencia sin versión, esta se resuelve desde el BOM.
Demo con 5 dependencias, algunas con versión explícita (que tiene precedencia) y otras resueltas por BOM.

---

**Ejercicio 3 — Maven profiles**
Implementa `Profile(id, activationProperty, dependencies, properties)`.
Activa perfiles con `-P<id>` (lista de IDs activos).
Fusiona dependencias y propiedades del perfil activo con las del POM base.
Demo con perfiles "dev" y "prod" y activación de cada uno por separado.

---

**Ejercicio 4 — Multi-module reactor**
Implementa `ReactorProject(List<Module> modules)`.
Cada módulo puede depender de otros módulos del proyecto.
Ordena los módulos topológicamente (respetando dependencias inter-módulo).
Demo con 5 módulos con dependencias entre sí. Muestra el orden de build.

---

**Ejercicio 5 — Plugin configuration**
Implementa `PluginConfig(Map<String,String> config)` que el plugin lee del POM.
Los valores pueden referenciar propiedades del POM con `${property}`.
Demo: configurar `maven-compiler-plugin` con `source=${java.version}`,
`target=${java.version}`, `parameters=true`. Mostrar la config resuelta.

---

**Ejercicio 6 — BOM con nearest-wins**
Crea `BomManager` que combina un BOM importado con declaraciones explícitas en `dependencyManagement`. La regla "nearest wins" se aplica: una versión declarada directamente en el POM tiene precedencia sobre la del BOM; y si hay dos BOMs importados, el primero en declararse gana. Demo con BOM-A que declara `jackson:2.15`, BOM-B que declara `jackson:2.14` y `spring:6.1`, y el POM que declara explícitamente `jackson:2.16`. Muestra qué versión se resuelve para cada dependencia y por qué.

---
