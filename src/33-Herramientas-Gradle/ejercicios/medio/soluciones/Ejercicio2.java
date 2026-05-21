import java.util.ArrayList;
import java.util.List;

public class Ejercicio2 {

    record ProductFlavor(String name) {}
    record BuildType(String name) {}

    static class Dependency {
        final String coordinates;
        Dependency(String coords) { this.coordinates = coords; }
        @Override public String toString() { return coordinates; }
    }

    static class BuildVariant {
        final ProductFlavor flavor;
        final BuildType buildType;
        final List<Dependency> dependencies = new ArrayList<>();

        BuildVariant(ProductFlavor flavor, BuildType buildType) {
            this.flavor = flavor;
            this.buildType = buildType;
        }

        String name() { return flavor.name() + capitalize(buildType.name()); }

        void addDependency(String coords) { dependencies.add(new Dependency(coords)); }

        private String capitalize(String s) {
            return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }

        void print() {
            System.out.println("Variant: " + name());
            System.out.printf("  flavor: %-15s buildType: %s%n", flavor.name(), buildType.name());
            System.out.println("  dependencies:");
            dependencies.forEach(d -> System.out.println("    " + d));
            System.out.println();
        }
    }

    static class Android {
        private final List<ProductFlavor> flavors = new ArrayList<>();
        private final List<BuildType> buildTypes = new ArrayList<>();

        void addFlavor(ProductFlavor flavor) { flavors.add(flavor); }
        void addBuildType(BuildType buildType) { buildTypes.add(buildType); }

        // Genera todas las combinaciones flavor × buildType
        List<BuildVariant> generateVariants() {
            List<BuildVariant> variants = new ArrayList<>();
            for (ProductFlavor flavor : flavors) {
                for (BuildType buildType : buildTypes) {
                    variants.add(new BuildVariant(flavor, buildType));
                }
            }
            return variants;
        }
    }

    static class GradleProject {
        final Android android = new Android();
        // Dependencias compartidas (todas las variantes)
        final List<Dependency> commonDeps = new ArrayList<>();

        void commonDep(String coords) { commonDeps.add(new Dependency(coords)); }

        // Configura las dependencias específicas para cada variante
        void configureDependencies(List<BuildVariant> variants) {
            for (BuildVariant variant : variants) {
                // Todas las variantes tienen las dependencias comunes
                commonDeps.forEach(d -> variant.addDependency(d.coordinates));

                // Dependencias específicas por flavor
                if (variant.flavor.name().equals("free")) {
                    variant.addDependency("com.ads:ads-sdk:2.0");
                } else if (variant.flavor.name().equals("paid")) {
                    variant.addDependency("com.premium:premium-features:1.5");
                }

                // Dependencias específicas por buildType
                if (variant.buildType.name().equals("debug")) {
                    variant.addDependency("com.squareup.leakcanary:leakcanary:2.12");
                    variant.addDependency("com.facebook.stetho:stetho:1.6.0");
                } else if (variant.buildType.name().equals("release")) {
                    variant.addDependency("com.crashlytics:crashlytics:18.5.1");
                }
            }
        }
    }

    public static void main(String[] args) {
        GradleProject project = new GradleProject();

        // 2 product flavors
        project.android.addFlavor(new ProductFlavor("free"));
        project.android.addFlavor(new ProductFlavor("paid"));

        // 2 build types
        project.android.addBuildType(new BuildType("debug"));
        project.android.addBuildType(new BuildType("release"));

        // Dependencias comunes
        project.commonDep("org.springframework:spring-context:6.1.2");
        project.commonDep("org.slf4j:slf4j-api:2.0.9");

        List<BuildVariant> variants = project.android.generateVariants();
        project.configureDependencies(variants);

        System.out.println("=== Build Variants (" + variants.size() + ") ===");
        System.out.println("flavors: " + project.android.flavors.stream().map(ProductFlavor::name).toList());
        System.out.println("types:   " + project.android.buildTypes.stream().map(BuildType::name).toList());
        System.out.println();

        variants.forEach(BuildVariant::print);

        System.out.println("Variantes generadas: " +
                variants.stream().map(BuildVariant::name).toList());
    }
}
