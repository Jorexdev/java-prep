package base.excepciones;


public class Intro {

    /*
       En Java, las excepciones son eventos que interrumpen el flujo normal
       de ejecución de un programa cuando ocurre un error. Se representan
       como objetos que heredan de la clase Throwable.
    */


    /*                             KEY FEATURES                            */

    /*
        - Jerarquía de Throwable:
            * Error → problemas graves del sistema (OutOfMemoryError, StackOverflowError).
            * Exception → errores que pueden manejarse en la aplicación.
                - Checked Exceptions: el compilador obliga a capturarlas o declararlas
                  (IOException, SQLException).
                - Unchecked Exceptions (RuntimeException): no obligatorias, surgen
                  por errores de lógica (NullPointerException, IndexOutOfBoundsException).

        - try-catch-finally:
            * try: código que puede lanzar excepción.
            * catch: bloque que maneja la excepción.
            * finally: siempre se ejecuta, ocurra o no la excepción.

        - throw: lanza una excepción.
        - throws: declara que un métod0 puede lanzar excepciones.
        - Desde Java 7: try-with-resources para cerrar recursos automáticamente.
    */


    /*                            VENTAJAS                                 */

    /*
        - Separan la lógica normal del manejo de errores.
        - Permiten programas más robustos y mantenibles.
        - Checked exceptions obligan a pensar en casos de error.
    */
}
