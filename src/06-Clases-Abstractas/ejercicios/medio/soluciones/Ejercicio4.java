public class Ejercicio4 {
    abstract static class Conversor {
        static double celsiusAFahrenheit(double c) { return c * 9.0 / 5.0 + 32; }
        abstract double convertir(double valor);
    }
    static class ConversorCF extends Conversor {
        @Override double convertir(double c) { return c * 9.0 / 5.0 + 32; }
    }
    static class ConversorFC extends Conversor {
        @Override double convertir(double f) { return (f - 32) * 5.0 / 9.0; }
    }
    public static void main(String[] args) {
        System.out.println("Estático — 100°C = " + Conversor.celsiusAFahrenheit(100) + "°F");
        Conversor cf = new ConversorCF();
        Conversor fc = new ConversorFC();
        System.out.println("Polimórfico CF — 0°C   = " + cf.convertir(0)   + "°F");
        System.out.println("Polimórfico FC — 212°F = " + fc.convertir(212) + "°C");
    }
}
