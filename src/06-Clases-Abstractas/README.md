<div align="center">
  <a href="#"><img src="../../assets/modules/banner-06-clases-abstractas-v1.svg" width="100%" alt=""/></a>
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

Una **clase abstracta** no puede instanciarse directamente. Define un contrato parcial: puede mezclar métodos abstractos (que las subclases deben implementar) con métodos concretos (implementación compartida).

```java
public abstract class Figura {
    private String color;  // campo de instancia ✓

    public Figura(String color) { this.color = color; }  // constructor ✓

    public abstract double calcularArea();    // obliga a implementar
    public abstract double calcularPerimetro();

    public String getColor() { return color; }  // implementación compartida
}
```

Diferencia clave con interfaces: una clase solo puede extender **una** clase abstracta, pero puede implementar **múltiples** interfaces.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

| Característica | Clase Abstracta | Interfaz |
|---|---|---|
| Instanciable | No | No |
| Herencia | Simple (una) | Múltiple |
| Métodos con estado | Sí (campos de instancia) | No (constantes) |
| Constructores | Sí | No |
| Métodos concretos | Sí | Sí (default, Java 8+) |
| Modificadores acceso | Cualquiera | public (implícito) |

**Cuándo usar clase abstracta:**
- Quieres compartir código y estado entre clases relacionadas.
- Necesitas constructores con lógica común.
- Las subclases comparten una relación "es-un" fuerte.

**Cuándo usar interfaz:**
- Quieres definir un contrato que clases no relacionadas pueden implementar.
- Necesitas herencia múltiple de comportamiento.
- Modelas capacidades (`Comparable`, `Serializable`, `Runnable`).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Compartir implementación entre clases con relación jerárquica.
- Forzar a las subclases a implementar métodos específicos del dominio.
- Permite campos de instancia y constructores (imposible en interfaces).
- Combina reutilización de código con polimorfismo.

Ver [AbstractVsInterface.java](AbstractVsInterface.java) para la comparativa entre clase abstracta e interfaz con ejemplos ejecutables.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
