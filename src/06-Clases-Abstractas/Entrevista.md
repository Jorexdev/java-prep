<div align="center">
  <a href="#"><img src="../../assets/modules/banner-06-clases-abstractas-v1.svg" width="100%" alt=""/></a>
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

**¿Cuándo usarías una clase abstracta vs una interfaz?**
Clase abstracta cuando las clases hijas comparten estado (campos) o implementación común y tienen una relación "es-un" fuerte. Interfaz cuando defines un contrato que clases no relacionadas pueden cumplir, o necesitas herencia múltiple de comportamiento. En la práctica moderna, con los métodos `default` en interfaces (Java 8+), la línea se difumina, pero la clase abstracta sigue siendo la opción cuando necesitas campos de instancia o constructores.

---

**¿Puede una clase abstracta tener constructor?**
Sí. Aunque no se puede instanciar directamente, el constructor existe para ser invocado por las subclases con `super(...)`. Es útil para inicializar campos comunes.

---

**¿Puede una clase abstracta no tener métodos abstractos?**
Sí. Una clase puede declararse `abstract` sin tener ningún método abstracto. Esto impide instanciarla directamente aunque tenga toda la implementación. Es útil cuando quieres que solo las subclases concretas sean instanciables.

---

**¿Cuántos niveles de herencia puedes tener con clases abstractas?**
No hay límite técnico. Puedes tener `A abstract → B abstract → C concrete`. Pero más de 2-3 niveles suele indicar un diseño complejo. El compilador exige que la primera clase concreta de la cadena implemente todos los métodos abstractos acumulados.

---

**¿Qué pasa si no implementas todos los métodos abstractos en una subclase?**
La subclase debe declararse también `abstract`. El compilador no permite una clase concreta con métodos abstractos sin implementar.

---

**¿Qué cambió con los métodos `default` en interfaces (Java 8+) respecto a la elección entre abstract class e interface?**

Antes de Java 8, la interfaz solo podía tener contratos sin implementación, por lo que una clase abstracta era la única forma de compartir código reutilizable. Con `default` methods las interfaces pueden tener implementación, lo que borra parte de la ventaja. La diferencia clave que persiste: las interfaces no pueden tener estado (campos de instancia no estáticos), mientras que las clases abstractas sí. Elige interfaz cuando defines un contrato sin estado compartido; elige clase abstracta cuando necesitas campos, un ciclo de vida controlado por constructor o quieres imponer herencia de una sola jerarquía.

---

**¿Cuándo genera un conflicto tener `default` methods en múltiples interfaces y cómo lo resuelve Java?**

Si una clase implementa dos interfaces que declaran un `default` method con la misma firma, el compilador obliga a que la clase lo sobreescriba explícitamente para resolver la ambigüedad — de lo contrario no compila. La clase puede delegar a uno de los defaults con la sintaxis `InterfazA.super.metodo()`. Este mecanismo evita el "problema del diamante" que existía en herencia múltiple de clases.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
