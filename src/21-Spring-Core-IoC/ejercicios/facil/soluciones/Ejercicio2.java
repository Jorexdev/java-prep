public class Ejercicio2 {

    interface Repositorio {
        void guardar(String dato);
    }

    static class RepositorioMemoria implements Repositorio {
        @Override
        public void guardar(String dato) {
            System.out.println("[Memoria] Guardado en lista: " + dato);
        }
    }

    static class RepositorioFichero implements Repositorio {
        @Override
        public void guardar(String dato) {
            System.out.println("[Fichero] Escrito en disco: " + dato);
        }
    }

    static class Servicio {
        // DI por setter: la dependencia puede cambiar en runtime
        // En Spring: @Autowired sobre el setter
        private Repositorio repositorio;

        // Setter que Spring llamaría automáticamente: // @Autowired
        void setRepositorio(Repositorio repositorio) {
            System.out.println("  -> Setter llamado, impl: " + repositorio.getClass().getSimpleName());
            this.repositorio = repositorio;
        }

        void procesar(String dato) {
            if (repositorio == null) {
                throw new IllegalStateException("Repositorio no inyectado");
            }
            repositorio.guardar(dato);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== DI por setter — cambio de implementación en runtime ===\n");

        Servicio servicio = new Servicio();

        System.out.println("1. Inyectando RepositorioMemoria:");
        servicio.setRepositorio(new RepositorioMemoria());
        servicio.procesar("evento-A");

        System.out.println("\n2. Cambiando a RepositorioFichero sin recrear Servicio:");
        servicio.setRepositorio(new RepositorioFichero());
        servicio.procesar("evento-B");

        System.out.println("\nObservación: el mismo objeto Servicio usó dos implementaciones distintas.");
    }
}
