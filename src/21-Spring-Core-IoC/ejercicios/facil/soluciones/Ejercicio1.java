import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    interface Repositorio {
        void guardar(String dato);
    }

    static class RepositorioMemoria implements Repositorio {
        private final List<String> almacen = new ArrayList<>();

        @Override
        public void guardar(String dato) {
            almacen.add(dato);
            System.out.println("[Memoria] Guardado: " + dato + " | Total: " + almacen.size());
        }
    }

    static class RepositorioFichero implements Repositorio {
        @Override
        public void guardar(String dato) {
            System.out.println("[Fichero] Escribiendo en disco: " + dato);
        }
    }

    static class Servicio {
        private final Repositorio repositorio;

        // DI por constructor: la dependencia se declara explícitamente
        // En Spring: @Autowired en constructor (o implícito si hay uno solo)
        Servicio(Repositorio repositorio) {
            this.repositorio = repositorio;
            System.out.println("Servicio creado con: " + repositorio.getClass().getSimpleName());
        }

        void procesar(String dato) {
            System.out.println("Procesando dato: " + dato);
            repositorio.guardar(dato);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== DI por constructor ===\n");

        // El "contenedor" somos nosotros — creamos las dependencias manualmente
        Repositorio memoria = new RepositorioMemoria();
        Servicio servicioMemoria = new Servicio(memoria);
        servicioMemoria.procesar("pedido-001");
        servicioMemoria.procesar("pedido-002");

        System.out.println();

        Repositorio fichero = new RepositorioFichero();
        Servicio servicioFichero = new Servicio(fichero);
        servicioFichero.procesar("pedido-003");
    }
}
