# Java Moderno — Ejercicios Fácil

Ejercicios de nivel básico para practicar la sintaxis de Records, Sealed Classes,
Pattern Matching, Switch Expressions y Text Blocks.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Record Punto

Crea un record `Punto(double x, double y)` con:
- Campo estático `Punto ORIGEN = new Punto(0, 0)`
- Método `distanciaAlOrigen()` que devuelve `Math.sqrt(x*x + y*y)`
- Compact constructor que lance `IllegalArgumentException` si alguna coordenada es `Double.NaN`

Prueba que `equals` funciona estructuralmente (dos `Punto(3,4)` distintos deben ser iguales),
que el `toString` generado es legible y que el accessor usa `p.x()` sin prefijo `get`.

## Ejercicio 2 — Sealed Figura

Define una sealed interface `Figura` con `permits Circulo, Rectangulo, Triangulo`.
Implementa cada variante como record:
- `Circulo(double radio)`
- `Rectangulo(double ancho, double alto)`
- `Triangulo(double base, double altura)`

Crea una lista con una instancia de cada tipo e imprímela.

## Ejercicio 3 — Pattern matching instanceof

Dado una lista de `Object` con elementos de tipo `String`, `Integer`, `Double` y `List<?>`,
usa pattern matching con `instanceof` (sin cast explícito) para imprimir una descripción de cada elemento:
- String: `"String: <valor> (len=<n>)"`
- Integer: `"Integer: <valor> (<positivo|negativo|cero>)"`
- Double: `"Double: <valor> (redondeado a <int>)"`
- List: `"List de <n> elementos"`

## Ejercicio 4 — Switch expression con Figura

Usando la sealed interface `Figura` del ejercicio 2, escribe un método `area(Figura f)`
que use switch expression (sin `default`) para calcular el área de cada figura:
- Circulo: `PI * r²`
- Rectangulo: `ancho * alto`
- Triangulo: `0.5 * base * altura`

El switch debe ser exhaustivo gracias a la sealed class (sin `default`).

## Ejercicio 5 — Text block JSON

Crea un text block que represente este JSON de configuración, interpolando
las variables `String host = "api.ejemplo.com"`, `int puerto = 443`, `String env = "prod"`:

```json
{
    "host": "api.ejemplo.com",
    "port": 443,
    "environment": "prod",
    "ssl": true
}
```

Imprime el resultado y verifica que no hay escapes `\"` visibles en el código fuente.

## Ejercicio 6 — Record con validación

Crea un record `Rango(int min, int max)` con:
- Compact constructor que valide que `min <= max` (lanza `IllegalArgumentException`)
- Método `contiene(int valor)` que devuelva `true` si `min <= valor <= max`
- Método `longitud()` que devuelva `max - min`
- Método `solapaCon(Rango otro)` que devuelva `true` si los rangos se solapan

Prueba con rangos válidos, inválidos (min > max) y el método `solapaCon`.
