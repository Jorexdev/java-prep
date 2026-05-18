import java.util.HashMap;
import java.util.Map;

public class Ejercicio3 {

    static abstract class DocumentoBase implements Cloneable {
        protected String titulo;
        protected String fuente;
        protected int tamaño;

        DocumentoBase(String titulo, String fuente, int tamaño) {
            this.titulo = titulo; this.fuente = fuente; this.tamaño = tamaño;
        }

        void setTitulo(String t) { this.titulo = t; }

        @Override public DocumentoBase clone() {
            try { return (DocumentoBase) super.clone(); }
            catch (CloneNotSupportedException e) { throw new AssertionError(); }
        }

        @Override public String toString() {
            return getClass().getSimpleName() + "{titulo=" + titulo + ", fuente=" + fuente + ", tamaño=" + tamaño + "}";
        }
    }

    static class DocumentoFactura  extends DocumentoBase { DocumentoFactura()  { super("Factura",  "Arial",           11); } }
    static class DocumentoContrato extends DocumentoBase { DocumentoContrato() { super("Contrato", "Times New Roman", 12); } }

    static class RegistroPrototipos {
        private final Map<String, DocumentoBase> prototipos = new HashMap<>();

        void registrar(String nombre, DocumentoBase proto) { prototipos.put(nombre, proto); }

        DocumentoBase crear(String nombre) {
            DocumentoBase proto = prototipos.get(nombre);
            if (proto == null) throw new IllegalArgumentException("Prototipo no encontrado: " + nombre);
            return proto.clone();
        }
    }

    public static void main(String[] args) {
        RegistroPrototipos registro = new RegistroPrototipos();
        registro.registrar("factura",  new DocumentoFactura());
        registro.registrar("contrato", new DocumentoContrato());

        DocumentoBase f1 = registro.crear("factura");
        DocumentoBase f2 = registro.crear("factura");
        f1.setTitulo("Factura #001");
        f2.setTitulo("Factura #002");

        System.out.println(f1);
        System.out.println(f2);
        System.out.println("Distintas instancias: " + (f1 != f2));

        DocumentoBase contrato = registro.crear("contrato");
        contrato.setTitulo("Contrato XYZ");
        System.out.println(contrato);
    }
}
