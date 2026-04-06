package patronesdiseno;

/*
    PATRONES DE DISEÑO - Introducción

    ¿Qué son?
    Soluciones probadas a problemas recurrentes en el diseño de software orientado a objetos.
    No son código concreto sino guías reutilizables para estructurar mejor tus clases.

    ¿Para qué sirven?
    - Mejorar la extensibilidad, mantenibilidad y legibilidad del código.
    - Hablar un lenguaje común entre desarrolladores ("usa un Strategy aquí").
    - Reducir la duplicación y el acoplamiento entre clases.

    Clasificación GoF (Gang of Four):

    Creacionales - controlan cómo se crean los objetos
        - Singleton:  una sola instancia global.
        - Factory:    centraliza la lógica de creación.
        - Builder:    construye objetos complejos paso a paso.

    Estructurales - definen cómo se componen las clases
        - Adapter:    traduce interfaces incompatibles.
        - Decorator:  añade comportamiento sin modificar la clase original.

    De comportamiento - gestionan la comunicación entre objetos
        - Strategy:   elige el algoritmo en tiempo de ejecución.
        - Observer:   notifica a suscriptores cuando algo cambia.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre Factory y Abstract Factory?
    - ¿En qué se parece Decorator a herencia y en qué se diferencia?
    - ¿Cuándo usarías Strategy en lugar de un if/else?
    - ¿Qué patrones usa Spring internamente? (Singleton, Factory, Proxy, Template Method)
*/
