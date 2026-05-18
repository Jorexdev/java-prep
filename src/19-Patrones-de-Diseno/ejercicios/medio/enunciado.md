# Ejercicios — Patrones de Diseño (Medio)

## Ejercicio 1 — Decorator
Implementa un sistema de café: `Cafe` (interfaz) con `getDescripcion()` y `getCosto()`.
`CafeSimple` es la implementación base. Decoradores: `Leche`, `Azucar`, `Canela`.
Cada decorador añade descripción y coste. Apila varios decoradores y verifica resultado.

## Ejercicio 2 — Command
Sistema de editor de texto con historial: `Comando` (interfaz) con `ejecutar()` y `deshacer()`.
Comandos: `EscribirTexto`, `EliminarTexto`, `MayusculasTexto`.
`HistorialComandos` con `ejecutar()`, `deshacer()`, `rehacer()`.

## Ejercicio 3 — Composite
Sistema de archivos: `Componente` (interfaz) con `nombre()`, `tamaño()`, `mostrar(String indent)`.
`Archivo` (hoja) y `Directorio` (compuesto) que contiene otros `Componente`.
Construye un árbol de directorios y demuestra que `tamaño()` de un directorio suma recursivamente.

## Ejercicio 4 — Template Method
Algoritmo de exportación de datos: `ExportadorDatos` (abstract) con template method `exportar(List<String> datos)`.
Pasos: `abrirConexion()`, `validarDatos()`, `transformar(String)`, `cerrarConexion()`.
Implementa `ExportadorCSV` y `ExportadorXML`. Los pasos variables son `transformar()` y los delimitadores.

## Ejercicio 5 — Chain of Responsibility
Pipeline de validación de solicitudes HTTP: `Manejador` (abstract) con `siguiente` y `manejar(Solicitud)`.
Eslabones: `ValidadorAutenticacion` (verifica token), `ValidadorAutorizacion` (verifica rol),
`ValidadorTamano` (verifica body size), `ProcesadorFinal`.
Demuestra que una solicitud inválida se rechaza en el eslabón correcto.

## Ejercicio 6 — Facade
Sistema complejo de pedidos con subsistemas: `GestorInventario`, `ProcesadorPago`, `ServicioEnvio`, `NotificadorEmail`.
Crea `FachadaPedidos` con método `realizarPedido(String producto, int cantidad, String email)` que orquesta los 4 subsistemas.
El cliente solo interactúa con la fachada.
