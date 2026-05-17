# Preguntas de entrevista — Lambdas

**¿Qué es una interfaz funcional y por qué las lambdas dependen de ellas?**
Una interfaz funcional tiene exactamente un método abstracto. Las lambdas necesitan un tipo destino para existir, y ese tipo es siempre una interfaz funcional. El compilador infiere qué método implementa la lambda a partir del contexto.

---

**¿Cuál es la diferencia entre `Function<T,R>`, `Consumer<T>` y `Supplier<T>`?**
`Function` transforma (recibe y devuelve), `Consumer` solo consume (recibe, no devuelve), `Supplier` solo produce (no recibe, devuelve).

---

**¿Cuándo es válido usar una referencia a método (`::`) en lugar de una lambda?**
Cuando el cuerpo de la lambda es exclusivamente una llamada a un método existente y la firma coincide con la del método abstracto de la interfaz funcional. El compilador hace la sustitución automáticamente.

---

**¿Una lambda puede modificar variables del scope externo?**
Solo puede leer variables locales externas si son efectivamente `final` (declaradas `final` o nunca reasignadas). Puede leer y modificar campos de instancia o variables de clase sin restricciones.

---

**¿Cuál es la diferencia entre una lambda y una clase anónima?**
La principal es el `this`: dentro de una clase anónima `this` se refiere a la propia instancia anónima; dentro de una lambda `this` se refiere a la clase envolvente. Además las lambdas no generan un `.class` separado en tiempo de compilación, son más ligeras.

---

**¿Qué relación tienen las lambdas con los Streams?**
Los Streams se construyen sobre lambdas: cada operación (`filter`, `map`, `forEach`...) recibe una interfaz funcional como argumento. Sin lambdas, el código de Streams sería tan verboso como el de colecciones clásicas.
