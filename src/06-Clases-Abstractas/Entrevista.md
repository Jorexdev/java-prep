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

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
