public class Ejercicio4 {

    static class SistemaAntiguo {
        String getDataXML() {
            return "<data><item id=\"1\"><name>Laptop</name><price>999</price></item></data>";
        }
    }

    interface DataProvider {
        String getData();
    }

    static class AdaptadorXMLaJSON implements DataProvider {
        private final SistemaAntiguo antiguo;

        AdaptadorXMLaJSON(SistemaAntiguo antiguo) { this.antiguo = antiguo; }

        @Override public String getData() {
            String xml = antiguo.getDataXML();
            // Conversión simulada (no un parser real)
            String id    = xml.replaceAll(".*id=\"(\\d+)\".*", "$1");
            String name  = xml.replaceAll(".*<name>(.*?)</name>.*", "$1");
            String price = xml.replaceAll(".*<price>(.*?)</price>.*", "$1");
            return String.format("{\"id\":%s,\"name\":\"%s\",\"price\":%s}", id, name, price);
        }
    }

    static void mostrarDatos(DataProvider provider) {
        System.out.println("JSON: " + provider.getData());
    }

    public static void main(String[] args) {
        SistemaAntiguo legacy = new SistemaAntiguo();
        DataProvider adaptado = new AdaptadorXMLaJSON(legacy);
        mostrarDatos(adaptado);
    }
}
