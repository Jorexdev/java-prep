# Colecciones Deque — Ejercicios Difícil

Ventana deslizante máxima, buffer circular, evaluador de expresiones infijas, validador HTML.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Ventana deslizante máxima

Dado array y tamaño k, retorna el máximo de cada ventana usando Deque de índices. Complejidad O(n). Ej: [1,3,-1,-3,5,3,6,7] k=3 → [3,3,5,5,6,7].

## Ejercicio 2 — Buffer circular de capacidad fija

Implementa un buffer circular de tamaño N usando ArrayDeque. offer() añade. poll() extrae. Si lleno, offer() lanza excepción. Método isFull().

## Ejercicio 3 — Evaluador de expresiones infijas

Extiende el evaluador RPN para soportar notación infija con paréntesis usando el algoritmo Shunting-yard: convierte infija a postfija y luego evalúa. Ej: "(3 + 4) * 2".

## Ejercicio 4 — Validador de HTML simplificado

Usa Deque como stack para validar que las etiquetas HTML estén correctamente anidadas. Ej: "<div><p></p></div>" → válido. "<div><p></div></p>" → inválido.

---

## Ejercicio 5 — LRU Cache con Deque en O(1)

Implementa una `LRUCache<K, V>` con operaciones `get` y `put` en O(1). Usa un `HashMap<K, Node<K,V>>` para acceso directo y un `ArrayDeque` simulado como lista doblemente enlazada manual (clase interna `Node` con `prev`/`next`). Al hacer `get`, mueve el nodo al frente. Al hacer `put`, si la clave existe actualiza y mueve al frente; si no existe inserta al frente y, si se supera la capacidad, elimina el nodo del final. Demo con capacidad 3: inserta 5 entradas, hace gets intercalados y verifica el orden de evicción imprimiendo el estado de la caché tras cada operación.
