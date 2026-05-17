<div align="center">
  <a href="#"><img src="../../assets/modules/banner-18-solid-v1.svg" width="100%" alt=""/></a>
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

**¿Cómo detectas que una clase viola el SRP?**
Si puedes describir la clase con "y también" — "gestiona usuarios y también envía emails y también genera PDF" — cada "y también" es una responsabilidad extra. Otra señal: la clase cambia por razones distintas (la lógica de negocio cambia, pero también cambia cuando cambias el proveedor de email).

---

**¿Qué relación tiene OCP con los patrones de diseño?**
OCP se implementa mediante polimorfismo y patrones. Strategy permite añadir comportamientos sin modificar el contexto. Template Method define el esqueleto y deja los detalles a subclases. Decorator añade responsabilidades sin modificar la clase original. El patrón Factory permite extender la creación de objetos sin modificar el código cliente.

---

**¿Puedes dar un ejemplo de violación de LSP?**
La violación clásica: `Cuadrado extends Rectángulo`. Si `setAltura()` en Cuadrado también cambia el ancho (para mantener la invariante de cuadrado), el código que opera sobre `Rectángulo` (y espera que alto y ancho sean independientes) se rompe al recibir un `Cuadrado`.

---

**¿Por qué es mejor tener muchas interfaces pequeñas que una grande?**
Porque los implementadores solo implementan lo que necesitan. Una interfaz "gorda" fuerza a implementar métodos irrelevantes (con `throws UnsupportedOperationException` o vacíos). Eso rompe el contrato y lleva a comportamientos inesperados — viola también LSP.

---

**¿Cómo se relaciona DIP con la inyección de dependencias?**
DIP dice que el módulo de alto nivel no debe depender de módulos de bajo nivel — ambos deben depender de abstracciones (interfaces). La Inyección de Dependencias (DI) es el mecanismo que provee esas abstracciones en runtime: Spring construye el grafo de dependencias inyectando implementaciones concretas donde se declaran interfaces.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
