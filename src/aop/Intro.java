package aop;

/*
    AOP - Aspect Oriented Programming

    ¿Qué es?
    Paradigma de programación que separa la lógica transversal del resto de la aplicación.
    La lógica transversal son funcionalidades que afectan a varias capas pero no forman
    parte de la lógica de negocio: logging, seguridad, métricas, transacciones, auditoría.

    ¿Para qué sirve?
    Para centralizar comportamientos que de otro modo estarían duplicados en muchos métodos.

    Sin AOP:
        método -> log de entrada -> validación -> auditoría -> lógica -> log de salida
    Con AOP:
        método -> lógica
        (el resto se aplica automáticamente desde el aspecto)

    ¿Cuándo usarlo?
    - Logging transversal sin ensuciar los servicios.
    - Medición de rendimiento de métodos.
    - Manejo centralizado de excepciones.
    - Validaciones de seguridad antes de ejecutar operaciones.
    - Gestión de transacciones (@Transactional usa AOP internamente).

    ¿Cuándo NO usarlo?
    - Para lógica de negocio: los aspectos son difíciles de depurar y rastrear.
    - AOP no intercepta llamadas internas dentro del mismo bean (limitación de proxies).

    Conceptos clave:

    Aspect - clase que contiene el código transversal. Se marca con @Aspect.

    Join Point - punto de la ejecución donde se puede aplicar el aspecto.
        En Spring AOP, siempre es la invocación de un método.

    Pointcut - expresión que selecciona qué métodos interceptar.
        Ejemplo: execution(* com.app.service.*.*(..))

    Advice - código que se ejecuta en el Join Point. Tipos:
        @Before         - antes del método
        @After          - después del método, siempre (con o sin excepción)
        @AfterReturning - después de retornar un valor correctamente
        @AfterThrowing  - si el método lanza una excepción
        @Around         - rodea la ejecución completa (control total)

    Weaving - proceso de aplicar los aspectos al código.
        En Spring se hace en tiempo de ejecución mediante proxies.

    Preguntas típicas de entrevista:
    - ¿Cuál es la diferencia entre @Before y @Around?
    - ¿Por qué AOP no intercepta llamadas internas al mismo bean?
    - ¿Qué usa Spring internamente para implementar AOP? (proxies JDK o CGLIB)
    - ¿Cómo funciona @Transactional por dentro? (es un aspecto @Around)
    - ¿Qué diferencia hay entre Spring AOP y AspectJ completo?
*/
