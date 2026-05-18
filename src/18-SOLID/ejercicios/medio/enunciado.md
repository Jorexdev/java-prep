# Ejercicios — SOLID (Medio)

## Ejercicio 1 — SRP + OCP combinados
Diseña un sistema de reportes: `ReporteVentas` con datos, `GeneradorHTML` y `GeneradorCSV`
que implementan interfaz `Generador`. Agrega `GeneradorJSON` sin modificar código existente.
Demuestra SRP (datos separados de formato) y OCP (nuevo formato sin cambios).

## Ejercicio 2 — LSP riguroso
Crea una jerarquía `Cuenta` → `CuentaCorriente`, `CuentaAhorro`, `CuentaPlazoFijo`.
`CuentaCorriente` permite sobregiro limitado, `CuentaAhorro` no puede quedar en negativo,
`CuentaPlazoFijo` no permite retiradas hasta vencimiento.
Aplica LSP correctamente usando precondiciones/postcondiciones correctas.

## Ejercicio 3 — ISP avanzado: dispositivos
Interfaz `Dispositivo` con `imprimir()`, `escanear()`, `faxear()`, `fotocopiar()`.
Aplica ISP: `Imprimible`, `Escaneable`, `Faxeable`, `Fotocopiable`.
Implementa `ImpresoraBasica` (solo imprime), `ImpresoraMultifuncion` (todo).
Usa composición para implementar `ImpresoraMultifuncion`.

## Ejercicio 4 — DIP con repositorios
Sistema de pedidos: `ServicioPedidos` depende de interfaz `RepositorioPedidos`.
Implementa `RepositorioMemoria` y `RepositorioArchivo` (simulado).
`ServicioPedidos` no sabe qué implementación usa. Demuestra sustitución.

## Ejercicio 5 — Todos los SOLID integrados
Diseña un sistema de pagos: `ProcesadorPago` usa interfaz `MetodoPago`.
Implementa `TarjetaCredito`, `PayPal`, `Cripto`.
Aplica: SRP (clases con una responsabilidad), OCP (nuevo método sin modificar procesador),
LSP (cualquier método es sustituible), ISP (interfaces específicas si necesario),
DIP (procesador depende de abstracción).
