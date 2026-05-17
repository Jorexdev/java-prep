// MAL - Esta clase tiene tres razones para cambiar:
// si cambia el formato del reporte, si cambia cómo se imprime, o si cambia cómo se guarda.
class ReportGod {
    public String generate()        { return "Contenido del reporte"; }
    public void print(String r)     { System.out.println("Imprimiendo: " + r); }
    public void saveToFile(String r){ System.out.println("Guardando en fichero: " + r); }
}

// BIEN - Cada clase tiene una sola responsabilidad.
// Si cambia el formato, solo tocas Report.
// Si cambia cómo se imprime, solo tocas ReportPrinter.
// Si cambia dónde se guarda, solo tocas ReportRepository.
class Report {
    public String generate() { return "Contenido del reporte"; }
}

class ReportPrinter {
    public void print(String report) {
        System.out.println("Imprimiendo: " + report);
    }
}

class ReportRepository {
    public void save(String report) {
        System.out.println("Guardando en fichero: " + report);
    }
}

public class SingleResponsibility {
    public static void main(String[] args) {
        Report report = new Report();
        String contenido = report.generate();

        new ReportPrinter().print(contenido);
        new ReportRepository().save(contenido);
    }
}
