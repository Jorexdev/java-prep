package base.abstractas;

public class Intro {

    /*
       En Java, una clase abstracta es una clase que no puede instanciarse directamente
       y que sirve como plantilla para que otras clases hereden de ella. Puede tener
       métodos abstractos (sin implementación) y métodos normales (con implementación).
    */


    /*                             KEY FEATURES                            */

    /*
        - Se declaran con la palabra clave "abstract".
        - No se pueden instanciar directamente.
        - Pueden contener:
            * Métodos abstractos (sin cuerpo).
            * Métodos concretos (con cuerpo).
            * Atributos, constructores y bloques estáticos.
        - Una subclase concreta DEBE implementar todos los métodos abstractos.
        - Una clase abstracta puede heredar de otra clase abstracta o normal.
        - Puede implementar interfaces.
    */


    /*                            VENTAJAS                                 */

    /*
        - Permiten definir un comportamiento común y dejar detalles a las subclases.
        - Reutilización de código (métodos implementados en la clase abstracta).
        - Encapsulan lógica parcial + contrato para herencia.
    */
}
