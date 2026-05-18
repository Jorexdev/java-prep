# Ejercicios — Patrones de Diseño (Difícil)

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Abstract Factory

Implementa una Abstract Factory para UI multiplataforma: `FabricaUI` (interfaz) con
`crearBoton()`, `crearCheckbox()`, `crearTextField()`.
Familias: `FabricaWeb` (produce componentes HTML) y `FabricaDesktop` (produce componentes text-mode).
La `App` recibe la fábrica por constructor y renderiza una pantalla de login sin saber qué familia usa.

## Ejercicio 2 — Proxy (Cache + Seguridad)

Implementa `ServicioArchivos` con `leer(String ruta)` y `escribir(String ruta, String contenido)`.
`ProxyCache`: almacena lecturas en caché, no consulta el servicio real si ya tiene el resultado.
`ProxySeguridad`: verifica que el usuario tenga permisos antes de delegar.
Combina ambos proxies en cadena: seguridad → caché → real.

## Ejercicio 3 — Prototype + Registry

Implementa `RegistroPrototipos` que almacena prototipos clonables por nombre.
`DocumentoBase` (abstract) implementa `Cloneable` con campos comunes.
Subclases: `DocumentoFactura`, `DocumentoContrato`.
El registro clona el prototipo al crear un nuevo documento — sin usar `new` directamente.

## Ejercicio 4 — Visitor

Sistema de AST simplificado: nodos `Numero`, `Suma`, `Producto`, `Negacion`.
`Nodo` con `aceptar(Visitor)`. `Visitor` con visitar() para cada tipo.
Implementa `EvaluadorVisitor` (calcula) e `ImpresorVisitor` (genera string infix).
Añade `ContadorNodosVisitor` sin modificar los nodos — demuestra extensibilidad.

## Ejercicio 5 — State

Máquina expendedora con estados: `SinMoneda`, `ConMoneda`, `Dispensando`, `Agotado`.
`Maquina` delega cada operación al estado actual: `insertarMoneda()`, `seleccionarProducto()`, `recogerProducto()`.
Transiciones: SinMoneda→ConMoneda, ConMoneda→Dispensando, Dispensando→SinMoneda (o Agotado si sin stock).
