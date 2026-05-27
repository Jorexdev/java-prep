# Ejercicios — SOLID (Fácil)

## Ejercicio 1 — SRP: Clase con una sola responsabilidad
Tienes una clase `Factura` que calcula el total, imprime la factura y guarda en base de datos.
Refactorízala aplicando SRP: separa en `Factura`, `ImpresoreDeFactura` y `RepositorioFactura`.
Demuestra que cada clase tiene una sola razón para cambiar.

## Ejercicio 2 — OCP: Extender sin modificar
Crea una jerarquía de `Forma` (abstract) con `Circulo` y `Rectangulo`.
Añade una clase `CalculadoraArea` con método `calcular(Forma f)` que funcione sin modificarse
cuando se añade `Triangulo`. Demuestra que OCP se respeta.

## Ejercicio 3 — LSP: Sustitución de Liskov
Crea una clase base `Ave` con método `volar()`. Crea `Aguila` y `Pinguino`.
Muestra el problema LSP y corrígelo: separa `AveVoladora` de `Ave`.
Prueba que `AveVoladora` puede sustituirse en cualquier contexto.

## Ejercicio 4 — ISP: Interfaces específicas
Define una interfaz `Trabajador` con `trabajar()`, `comer()`, `dormir()`.
Muestra el problema con un robot que no come ni duerme.
Aplica ISP: separa en `Trabajable`, `Descansable`. Implementa `Humano` y `Robot`.

## Ejercicio 5 — DIP: Inversión de dependencias
Crea un `Notificador` que depende directamente de `EmailService`.
Refactorízalo: define interfaz `ServicioNotificacion`, implementa `EmailService` y `SmsService`.
`Notificador` recibe la implementación por constructor. Demuestra intercambiabilidad.

## Ejercicio 6 — Composición: todos los principios juntos
Diseña un sistema de notificaciones que aplique los 5 principios simultáneamente: `Notificacion` (SRP: solo datos), `EnviadorNotificacion` interface (DIP), `EnviadorEmail`/`EnviadorSMS` (OCP: extendible), `Destinatario` interface con solo `obtenerContacto()` (ISP), `Usuario`/`Empresa` (LSP: intercambiables). Demo que demuestre que añadir un nuevo canal (Push) no requiere modificar nada existente.
