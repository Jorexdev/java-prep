<div align="center">
  <a href="#"><img src="../../assets/modules/banner-08-colecciones-mapa-v1.svg" width="100%" alt=""/></a>
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

`Map` almacena pares **clave-valor**. No permite claves duplicadas (una clave siempre apunta a exactamente un valor). Es la estructura fundamental para búsquedas por clave en O(1).

```java
Map<String, Integer> edades = new HashMap<>();
edades.put("Ana", 30);
edades.get("Ana");          // 30
edades.containsKey("Ana");  // true
edades.getOrDefault("Bob", 0);  // 0
edades.computeIfAbsent("Carlos", k -> calcularEdad(k));
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

| Implementación | Orden | Complejidad | Null key | Notas |
|---|---|---|---|---|
| `HashMap` | Ninguno | O(1) get/put | Sí (1) | Opción por defecto |
| `LinkedHashMap` | Inserción | O(1) | Sí (1) | Mantiene orden de inserción |
| `TreeMap` | Natural/Comparator | O(log n) | No | Siempre ordenado por clave |
| `WeakHashMap` | Ninguno | O(1) | Sí | Claves con referencias débiles (GC) |
| `IdentityHashMap` | Ninguno | O(1) | Sí | Compara claves con `==` en vez de `equals` |
| `Hashtable` | Ninguno | O(1) | No | Legacy, sincronizado. Evitar. |

**HashMap** es la opción por defecto. Funciona con `hashCode()` + `equals()`: llaves con el mismo `hashCode` van al mismo bucket, luego se diferencia con `equals`.

**Importante:** si usas objetos propios como claves, debes implementar correctamente `hashCode()` y `equals()`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Búsqueda por clave en O(1) con HashMap.
- Múltiples implementaciones para distintos requisitos (orden, concurrencia).
- API rica: `getOrDefault`, `computeIfAbsent`, `merge`, `forEach`, `entrySet`.
- Base de muchos algoritmos de agrupación y caché.

Ver cada implementación en los archivos `Exp*.java` para comparar comportamientos concretos.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
