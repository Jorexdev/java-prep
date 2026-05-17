<div align="center">
  <a href="#"><img src="../../assets/modules/banner-19-patrones-diseno-v1.svg" width="100%" alt=""/></a>
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

Los **patrones de diseño** son soluciones reutilizables a problemas comunes de diseño de software. El catálogo GoF (Gang of Four) define 23 patrones en 3 categorías:

- **Creacionales**: cómo se crean los objetos.
- **Estructurales**: cómo se componen los objetos.
- **Comportamentales**: cómo se comunican los objetos.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Creacionales:**

| Patrón | Propósito |
|---|---|
| **Singleton** | Una única instancia global accesible |
| **Factory Method** | Delega la creación a subclases |
| **Builder** | Construye objetos complejos paso a paso |

**Estructurales:**

| Patrón | Propósito |
|---|---|
| **Adapter** | Convierte una interfaz en otra (compatibilidad) |
| **Decorator** | Añade responsabilidades dinámicamente sin herencia |

**Comportamentales:**

| Patrón | Propósito |
|---|---|
| **Strategy** | Encapsula algoritmos intercambiables |
| **Observer** | Notifica cambios a múltiples suscriptores |

```java
// Builder — Java moderno con records o Lombok
Persona persona = new Persona.Builder()
    .nombre("Ana").edad(30).ciudad("Madrid")
    .build();

// Strategy — con lambdas
Ordenador ordenador = new Ordenador(lista -> Collections.sort(lista));
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

-  Vocabulario común entre desarrolladores.
- Soluciones probadas para problemas recurrentes.
- Bajo acoplamiento (Strategy, Observer), extensibilidad (Decorator, Factory).
- En Java moderno muchos se simplifican con lambdas e interfaces funcionales.

Ver [SingletonDemo.java](SingletonDemo.java), [FactoryDemo.java](FactoryDemo.java), [BuilderDemo.java](BuilderDemo.java), [AdapterDemo.java](AdapterDemo.java), [DecoratorDemo.java](DecoratorDemo.java), [StrategyDemo.java](StrategyDemo.java) y [ObserverDemo.java](ObserverDemo.java).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
