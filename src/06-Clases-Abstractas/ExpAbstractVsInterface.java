/*
    ABSTRACT CLASS VS INTERFACE

    Clase abstracta:
      - Plantilla base con estado (atributos de instancia) y comportamiento parcial.
      - Puede tener métodos implementados (reutilizables) y abstractos (obligatorios).
      - Permite constructores, atributos con cualquier modificador de acceso.
      - Una clase solo puede extender UNA clase abstracta (herencia simple).
      - Ideal cuando necesitas base común con lógica parcial.

    Interface:
      - Contrato puro: define qué debe hacer una clase, no cómo.
      - Todos los atributos son public static final (constantes).
      - Sin constructores, sin estado de instancia.
      - Desde Java 8: métodos default y static (con implementación).
      - Desde Java 9: métodos privados.
      - Una clase puede implementar múltiples interfaces (herencia múltiple de comportamiento).
      - Ideal para polimorfismo flexible entre jerarquías distintas.

    ¿Cuándo elegir cada uno?
      - Clase abstracta: cuando las subclases comparten estado o implementación base.
      - Interface: cuando solo defines un contrato y las clases pueden venir de jerarquías distintas.

    Ejemplo:
        abstract class Animal {
            abstract void hacerSonido();       // obligatorio en subclases
            void dormir() { System.out.println("Zzz..."); }  // compartido
        }

        interface Volador {
            void volar();
        }

        class Pajaro extends Animal implements Volador {
            @Override void hacerSonido() { System.out.println("Pío"); }
            @Override public void volar() { System.out.println("Volando"); }
        }

    Preguntas típicas de entrevista:
      - ¿Una clase abstracta puede implementar interfaces? (sí)
      - ¿Una interface puede extender otra interface? (sí, con extends)
      - ¿Qué pasa si dos interfaces tienen un método default con el mismo nombre?
        (la clase que las implementa debe sobreescribir el método)
      - ¿Pueden tener constructores las interfaces? (no)
*/
public class ExpAbstractVsInterface {}
