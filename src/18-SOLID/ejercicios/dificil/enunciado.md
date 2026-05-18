# Ejercicios — SOLID (Difícil)

## Ejercicio 1 — Violaciones SOLID: detectar y corregir
Dada una clase `GestorUsuarios` que valida, persiste, envía email de bienvenida y
genera informe en PDF — todo en una clase — identifica cada violación SOLID y refactoriza
en una arquitectura limpia: repositorio, servicio de email, generador de informes, servicio principal.

## Ejercicio 2 — OCP con Strategy + Factory
Diseña un sistema de descuentos: `CalculadorDescuento` debe calcular el descuento final
según tipo de cliente: `Regular` (0%), `Premium` (10%), `VIP` (20%), `Empleado` (30%).
Usa Strategy para cada tipo y Factory para instanciar la estrategia correcta.
Añadir un nuevo tipo `Mayorista` (15%) no debe tocar clases existentes.

## Ejercicio 3 — LSP con contratos (Design by Contract ligero)
Diseña una jerarquía de colecciones: `Coleccion<T>` con `agregar(T)`, `obtener(int)`, `tamaño()`.
Implementa `ColeccionOrdenada<T extends Comparable<T>>` que mantiene elementos ordenados,
y `ColeccionInmutable<T>` que lanza `UnsupportedOperationException` en `agregar`.
Discute por qué `ColeccionInmutable` viola LSP y cómo resolverlo con interfaces separadas.

## Ejercicio 4 — DIP con inyección de dependencias manual
Implementa un mini-contenedor IoC manual: `Contenedor` que registra y resuelve dependencias.
`Contenedor.registrar(Class<?>, Supplier<?>)` asocia un tipo a su factory.
`Contenedor.resolver(Class<?>)` devuelve la instancia.
Demuestra que `ServicioPedidos` → `RepositorioPedidos` → `ConexionBD` se pueden
resolver sin que ninguna clase conozca sus dependencias concretas.
