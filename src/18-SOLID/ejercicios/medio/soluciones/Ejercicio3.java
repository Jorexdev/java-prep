public class Ejercicio3 {

    interface Imprimible   { void imprimir(String doc); }
    interface Escaneable   { String escanear(); }
    interface Faxeable     { void fax(String destino, String doc); }
    interface Fotocopiable { void fotocopiar(String doc); }

    static class ImpresoraBasica implements Imprimible {
        @Override public void imprimir(String doc) {
            System.out.println("Imprimiendo: " + doc);
        }
    }

    static class ImpresoraMultifuncion implements Imprimible, Escaneable, Faxeable, Fotocopiable {
        private final ImpresoraBasica impresora = new ImpresoraBasica();

        @Override public void imprimir(String doc)            { impresora.imprimir(doc); }
        @Override public String escanear()                    { System.out.println("Escaneando..."); return "doc_escaneado.pdf"; }
        @Override public void fax(String dest, String doc)   { System.out.println("Fax a " + dest + ": " + doc); }
        @Override public void fotocopiar(String doc)          { System.out.println("Fotocopiando: " + doc); }
    }

    public static void main(String[] args) {
        ImpresoraBasica basica = new ImpresoraBasica();
        basica.imprimir("CV.pdf");

        ImpresoraMultifuncion multi = new ImpresoraMultifuncion();
        multi.imprimir("Contrato.pdf");
        System.out.println("Escaneado: " + multi.escanear());
        multi.fax("123456789", "Presupuesto.pdf");
        multi.fotocopiar("DNI.pdf");
    }
}
