package base.abstractas.curiosidades;

public class AbstractVsInterface {

    /*
       En Java, tanto las clases abstractas como las interfaces permiten definir contratos
       para que otras clases los implementen, pero existen diferencias importantes entre ellas.

       Una clase abstracta en Java funciona como una plantilla inicial: puede tener atributos normales
       (con cualquier modificador de acceso), métodos ya implementados que serán reutilizados por
       todas las subclases, y métodos abstractos que obligan a las subclases a dar su propia
       implementación. De esta forma, la clase abstracta define la base común (estado y
       comportamiento compartido), mientras que deja abiertas ciertas partes para que cada
       subclase concrete los detalles.


       Una interfaz en Java actúa como un contrato: define un conjunto de métodos que las clases
       que la implementen deben cumplir. A diferencia de las clases abstractas, no pueden tener
       estado (todos sus atributos son public static final, es decir, constantes) ni constructores.
       Su objetivo es marcar capacidades o comportamientos comunes que pueden aplicarse a clases de
       jerarquías distintas. Desde Java 8, las interfaces también permiten métodos default y
       static (con implementación), y desde Java 9 incluso métodos privados, pero siempre sin
       perder su naturaleza principal: ser un contrato flexible que una clase puede implementa
       junto a otras interfaces.
    */


    /*                             KEY FEATURES                            */

    /*
        ► Clase abstracta:
        - Puede tener métodos abstractos y métodos implementados.
        - Puede tener atributos (variables de instancia).
        - Puede tener constructores.
        - Una clase solo puede extender de UNA clase abstracta (herencia simple).

        ► Interface:
        - Hasta Java 8, solo métodos abstractos.
        - Desde Java 8: puede tener métodos default y static.
        - Desde Java 9: puede tener métodos privados.
        - No puede tener constructores.
        - Todos los atributos son public static final (constantes).
        - Una clase puede implementar varias interfaces (herencia múltiple de comportamiento).
    */


    /*                            VENTAJAS                                 */

    /*
        - Clase abstracta:
            * Ideal cuando necesitas una base común con lógica parcial.
            * Reutilización de código en subclases.
        - Interface:
            * Ideal para contratos puros y polimorfismo flexible.
            * Permiten herencia múltiple de comportamiento.
    */


    /*                            EJEMPLOS                                 */

    /*
        abstract class Animal {
            abstract void hacerSonido();
            void dormir() { System.out.println("Zzz..."); }
        }

        interface Volador {
            void volar();
        }

        class Pajaro extends Animal implements Volador {
            @Override
            void hacerSonido() { System.out.println("Pío"); }
            @Override
            public void volar() { System.out.println("Estoy volando"); }
        }
    */
}
