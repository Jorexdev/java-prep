<div align="center">
  <a href="#"><img src="../../assets/modules/banner-09-colecciones-conjunto-v1.svg" width="100%" alt=""/></a>
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

**¿Cómo sabe un HashSet si un elemento ya existe?**
Calcula el `hashCode()` del elemento para encontrar el bucket. Si hay algo en ese bucket, usa `equals()` para comparar. Si `equals()` devuelve true, el elemento ya existe y no se inserta. Por eso implementar ambos métodos consistentemente es obligatorio para que Set funcione correctamente.

---

**¿Cuándo usarías TreeSet vs HashSet?**
HashSet para O(1) cuando no importa el orden. TreeSet cuando necesitas los elementos siempre ordenados o hacer consultas de rango (`headSet`, `tailSet`, `subSet`, `floor`, `ceiling`). TreeSet es O(log n) para todas las operaciones.

---

**¿Por qué es importante implementar correctamente `equals()` y `hashCode()` al usar un Set?**
Porque Set usa ambos para detectar duplicados. Si `hashCode()` es inconsistente, dos objetos "iguales" podrían acabar en buckets distintos y ambos insertarse. El contrato exige: si `equals()` devuelve true, `hashCode()` debe devolver el mismo valor.

---

**¿Puede un Set contener `null`?**
HashSet y LinkedHashSet permiten exactamente un null. TreeSet no permite null porque necesita comparar (lanzaría NullPointerException). EnumSet tampoco permite null.

---

**¿Qué es EnumSet y cuándo lo usarías?**
Una implementación de Set especializada para enums. Usa una representación de bits (bit vector), lo que la hace extremadamente rápida y compacta. Ideal para conjuntos de flags o permisos: `EnumSet.of(LEER, ESCRIBIR)`. Solo acepta elementos del mismo enum.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
