# Colecciones Deque — Ejercicios Medio

Ventana deslizante, paréntesis balanceados, historial navegación, evaluador RPN.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Ventana deslizante básica

Dado un array y un tamaño de ventana k, calcula la suma de cada ventana deslizante usando Deque para mantener el índice del máximo en O(n).

## Ejercicio 2 — Paréntesis balanceados

Usa Deque como stack para verificar si una expresión tiene paréntesis, corchetes y llaves balanceados. Ej: "({[]})" → true, "([)]" → false.

## Ejercicio 3 — Navegación back/forward

Implementa navegación de browser con dos Deques: historial y forward. Métodos: visit(url), back(), forward(). Muestra la URL actual en cada operación.

## Ejercicio 4 — Cola con prioridad manual

Usa Deque para implementar una cola donde los elementos de alta prioridad se insertan al frente (addFirst) y los normales al final (addLast).

## Ejercicio 5 — Evaluador de expresiones RPN

Implementa un evaluador de notación polaca inversa (postfija) usando Deque como stack. Ej: "3 4 + 2 * 7 /" → (3+4)*2/7.

## Ejercicio 6 — ArrayDeque vs Stack rendimiento

Mide con nanoTime el tiempo de 100.000 push/pop en ArrayDeque vs java.util.Stack. Muestra que ArrayDeque es más rápido.
