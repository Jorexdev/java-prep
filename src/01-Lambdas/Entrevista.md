<div align="center">
  <a href="#"><img src="../../assets/modules/banner-01-lambdas-v2.svg" width="100%" alt="01 - Lambdas"/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

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
La principal es el `this`: dentro de una clase anónima `this` se refiere a la propia instancia anónima; dentro de una lambda `this` se refiere a la clase envolvente. Además las lambdas no generan un `.class` separado en compilación, son más ligeras.

---

**¿Qué relación tienen las lambdas con los Streams?**
Los Streams se construyen sobre lambdas: cada operación (`filter`, `map`, `forEach`...) recibe una interfaz funcional como argumento. Sin lambdas, el código de Streams sería tan verboso como el de colecciones clásicas.

---

**¿Qué restricción impone Java sobre las variables locales capturadas por una lambda?**

Las variables locales del scope externo deben ser efectivamente `final` (effectively final): pueden omitir la palabra clave `final` siempre que nunca se reasignen. El motivo es que las lambdas pueden ejecutarse en otro hilo y el compilador no puede garantizar que la variable siga siendo la misma si es mutable. Si necesitas acumular un valor mutable, usa un array de un elemento (`int[] count = {0}`) o un `AtomicInteger`, aunque ambos patrones implican efectos secundarios que deben evitarse en lambdas puras.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
