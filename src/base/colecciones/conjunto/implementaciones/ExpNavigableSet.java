package base.colecciones.conjunto.implementaciones;

import java.util.NavigableSet;
import java.util.TreeSet;

public class ExpNavigableSet {







    public static void main(String[] args) {
        // Crear un NavigableSet con TreeSet
        NavigableSet<Integer> navSet = new TreeSet<>();

        navSet.add(10);
        navSet.add(20);
        navSet.add(30);
        navSet.add(40);
        navSet.add(50);
        System.out.println("NavigableSet → " + navSet); // [10,20,30,40,50]

        // lower(e): estrictamente menor que e
        System.out.println("lower(30): " + navSet.lower(30)); // 20

        // floor(e): menor o igual que e
        System.out.println("floor(30): " + navSet.floor(30)); // 30

        // higher(e): estrictamente mayor que e
        System.out.println("higher(30): " + navSet.higher(30)); // 40

        // ceiling(e): mayor o igual que e
        System.out.println("ceiling(25): " + navSet.ceiling(25)); // 30

        // descendingSet(): vista en orden inverso
        System.out.println("descendingSet(): " + navSet.descendingSet()); // [50,40,30,20,10]

        // pollFirst(): obtiene y elimina el primero
        System.out.println("pollFirst(): " + navSet.pollFirst()); // 10

        // pollLast(): obtiene y elimina el último
        System.out.println("pollLast(): " + navSet.pollLast()); // 50

        System.out.println("NavigableSet tras polls → " + navSet); // [20,30,40]
    }
}
