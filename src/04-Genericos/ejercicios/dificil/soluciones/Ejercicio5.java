import java.util.function.Function;

public class Ejercicio5 {

    // Either<L,R>: Left = error, Right = éxito (convención funcional)
    static abstract class Either<L, R> {

        static <L, R> Either<L, R> left(L value)  { return new Left<>(value); }
        static <L, R> Either<L, R> right(R value) { return new Right<>(value); }

        abstract boolean isLeft();
        abstract boolean isRight();
        abstract L getLeft();
        abstract R getRight();
        abstract <N> Either<L, N> map(Function<R, N> fn);

        static class Left<L, R> extends Either<L, R> {
            private final L value;
            Left(L value) { this.value = value; }

            @Override public boolean isLeft()  { return true; }
            @Override public boolean isRight() { return false; }
            @Override public L getLeft()       { return value; }
            @Override public R getRight()      { throw new UnsupportedOperationException("Es Left"); }
            @Override public <N> Either<L, N> map(Function<R, N> fn) { return Either.left(value); }
            @Override public String toString() { return "Left(" + value + ")"; }
        }

        static class Right<L, R> extends Either<L, R> {
            private final R value;
            Right(R value) { this.value = value; }

            @Override public boolean isLeft()  { return false; }
            @Override public boolean isRight() { return true; }
            @Override public L getLeft()       { throw new UnsupportedOperationException("Es Right"); }
            @Override public R getRight()      { return value; }
            @Override public <N> Either<L, N> map(Function<R, N> fn) { return Either.right(fn.apply(value)); }
            @Override public String toString() { return "Right(" + value + ")"; }
        }
    }

    static Either<String, Double> dividir(double a, double b) {
        if (b == 0) return Either.left("División por cero");
        return Either.right(a / b);
    }

    public static void main(String[] args) {

        Either<String, Double> r1 = dividir(10, 2);
        Either<String, Double> r2 = dividir(10, 0);

        System.out.println(r1); // Right(5.0)
        System.out.println(r2); // Left(División por cero)

        // map solo actúa si es Right
        Either<String, String> r3 = r1.map(d -> String.format("%.1f", d));
        Either<String, String> r4 = r2.map(d -> String.format("%.1f", d));

        System.out.println(r3); // Right(5.0)
        System.out.println(r4); // Left(División por cero) — sin cambio
    }
}
