import java.util.*;

public class Ejercicio1 {

    static class GcNode {
        String name;
        boolean marked = false;
        List<GcNode> references = new ArrayList<>();

        GcNode(String name) {
            this.name = name;
        }

        void addRef(GcNode other) {
            references.add(other);
        }
    }

    static void mark(GcNode node) {
        if (node == null || node.marked) return;
        node.marked = true;
        for (GcNode ref : node.references) {
            mark(ref);
        }
    }

    public static void main(String[] args) {
        // Crear grafo de objetos
        GcNode a = new GcNode("A");
        GcNode b = new GcNode("B");
        GcNode c = new GcNode("C");
        GcNode d = new GcNode("D");
        GcNode e = new GcNode("E");
        GcNode f = new GcNode("F");

        // Relaciones entre objetos
        a.addRef(b);
        a.addRef(c);
        b.addRef(d);
        c.addRef(d);
        // E y F no son alcanzables desde ninguna root

        // GC roots: solo A es root en este ejemplo
        List<GcNode> roots = List.of(a);
        List<GcNode> allObjects = List.of(a, b, c, d, e, f);

        System.out.println("=== GC Mark Phase ===");
        System.out.println("GC Roots: " + roots.stream().map(n -> n.name).toList());
        System.out.println();

        // Fase de marcado
        for (GcNode root : roots) {
            mark(root);
        }

        // Clasificar objetos
        System.out.println("Resultado del marcado:");
        for (GcNode obj : allObjects) {
            String status = obj.marked ? "VIVO   " : "BASURA ";
            System.out.println("  " + status + " -> " + obj.name);
        }

        System.out.println();
        long alive = allObjects.stream().filter(n -> n.marked).count();
        long garbage = allObjects.stream().filter(n -> !n.marked).count();
        System.out.println("Objetos vivos:   " + alive);
        System.out.println("Objetos basura:  " + garbage + " (serán recolectados)");
    }
}
