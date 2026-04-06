package base.opcional;

/*
    OPTIONAL — Introducción

    ► ¿Qué es Optional?
      Optional es un contenedor introducido en Java 8 diseñado para representar
      un valor que puede existir o no. Su objetivo principal es evitar el uso
      del valor null y los NullPointerException.

      Es una alternativa segura a:
         - devolver null
         - recibir null como parámetro
         - comprobar null manualmente

      Optional<T> es simplemente un "envoltorio" que puede contener:
         - un valor de tipo T
         - o estar vacío (Optional.empty())

    ► ¿Por qué existe Optional?
      Antes de Java 8, la ausencia de valor se representaba con null, lo cual
      era inseguro y propenso a errores:

         User user = findUser();
         user.getName();  // puede lanzar NullPointerException

      Optional permite expresar claramente la intención:
         Optional<User> user = findUser();
         user.map(User::getName).orElse("Desconocido");

    ► ¿Cuándo usar Optional?
      - Como valor de retorno de métodos que pueden no devolver nada.
      - Para evitar if (obj != null).
      - Para operaciones encadenadas (streams, functional programming).

      No se recomienda:
      - En atributos de entidades (JPA, JSON), porque rompe serialización.
      - En parámetros de métodos (mejor sobrecargar o validar antes).
      - En colecciones (ya se pueden dejar vacías).

    ► Métodos principales de Optional
      - Optional.empty()                → crea un Optional vacío
      - Optional.of(valor)              → crea un Optional con valor (no puede ser null)
      - Optional.ofNullable(valor)      → crea Optional permitiendo null
      - isPresent() / isEmpty()         → comprobar estado (no recomendado en usos modernos)
      - ifPresent(Consumer)             → ejecuta código si hay valor
      - orElse(valor)                   → devuelve valor por defecto
      - orElseGet(Supplier)             → valor por defecto lazy
      - orElseThrow() / orElseThrow(...)→ lanza excepción si está vacío
      - map(Function)                   → transforma el valor interno
      - flatMap(Function)               → trabaja con Optional anidados

    ► Ventajas
      - Elimina NullPointerException comunes.
      - Código más declarativo e intencional.
      - Facilita programación funcional con Streams.
      - Hace explícito cuándo un valor puede estar ausente.

    ► Ejemplo conceptual
      En vez de:
         public User findUser(String id) { ... return null; }

      Usar:
         public Optional<User> findUser(String id) { ... return Optional.of(usuario); }

      Y consumirlo así:
         User u = findUser("1").orElseThrow(() -> new RuntimeException("No encontrado"));

    En resumen:
      Optional obliga a manejar la ausencia de un valor de forma explícita,
      evitando errores silenciosos y flujo incierto causado por null.
*/
public class Intro {
    public static void main(String[] args) {
        System.out.println("Introducción a Optional en Java");
    }
}
