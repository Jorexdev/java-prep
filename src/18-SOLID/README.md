<div align="center">
  <a href="#"><img src="../../assets/modules/banner-18-solid-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**SOLID** son cinco principios de diseño orientado a objetos formulados por Robert C. Martin. Su objetivo es crear software mantenible, extensible y con bajo acoplamiento.

| Letra | Principio | En una línea |
|---|---|---|
| **S** | Single Responsibility | Una clase, una razón para cambiar |
| **O** | Open/Closed | Abierto a extensión, cerrado a modificación |
| **L** | Liskov Substitution | Los subtipos deben sustituir a sus tipos base |
| **I** | Interface Segregation | Interfaces específicas, no "gordas" |
| **D** | Dependency Inversion | Depende de abstracciones, no de implementaciones |

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**S — Single Responsibility Principle:**
```java
// Mal: UserService gestiona usuarios Y envía emails
// Bien: UserService (lógica) + EmailService (notificaciones)
```

**O — Open/Closed Principle:**
```java
// Mal: switch gigante que hay que modificar por cada nuevo tipo
// Bien: interfaz + polimorfismo — añades nueva clase, no modificas código existente
```

**L — Liskov Substitution Principle:**
```java
// Si Pato extends Ave, y Ave tiene volar()...
// Un PingüinoAve que lanza UnsupportedOperationException en volar() → viola LSP
```

**I — Interface Segregation Principle:**
```java
// Mal: interface Trabajador { trabajar(); comer(); dormir(); }  ← los robots no comen
// Bien: interface Trabajable { trabajar(); } + interface Humano extends Trabajable { comer(); dormir(); }
```

**D — Dependency Inversion Principle:**
```java
// Mal: UserService depende de MySQLRepository
// Bien: UserService depende de UserRepository (interfaz)
//       MySQLRepository implements UserRepository
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Código más fácil de mantener: cada clase tiene una razón para cambiar.
- Extensión sin modificación: nuevas funcionalidades no rompen código existente.
- Testabilidad: interfaces + DIP permiten mocking.
- Bajo acoplamiento: cambiar una implementación no afecta a quien la usa.

Ver [ExpSingleResponsibility.java](ExpSingleResponsibility.java), [ExpOpenClosed.java](ExpOpenClosed.java), [ExpLiskovSubstitution.java](ExpLiskovSubstitution.java), [ExpInterfaceSegregation.java](ExpInterfaceSegregation.java) y [ExpDependencyInversion.java](ExpDependencyInversion.java) para ejemplos ejecutables de cada principio SOLID.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
