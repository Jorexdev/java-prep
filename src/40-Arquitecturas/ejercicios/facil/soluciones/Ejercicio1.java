import java.math.BigDecimal;
import java.util.Objects;

public class Ejercicio1 {

    static final class Money {
        private final BigDecimal amount;
        private final String currency;

        Money(BigDecimal amount, String currency) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("amount must be >= 0");
            }
            this.amount = amount;
            this.currency = currency;
        }

        Money add(Money other) {
            if (!this.currency.equals(other.currency)) {
                throw new IllegalArgumentException(
                    "Cannot add " + this.currency + " and " + other.currency);
            }
            return new Money(this.amount.add(other.amount), this.currency);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Money m)) return false;
            return amount.compareTo(m.amount) == 0 && currency.equals(m.currency);
        }

        @Override
        public int hashCode() {
            return Objects.hash(amount.stripTrailingZeros(), currency);
        }

        @Override
        public String toString() {
            return amount + " " + currency;
        }
    }

    public static void main(String[] args) {
        Money a = new Money(new BigDecimal("10.00"), "EUR");
        Money b = new Money(new BigDecimal("10.00"), "EUR");
        Money c = new Money(new BigDecimal("5.50"), "EUR");

        System.out.println("a equals b (mismo valor): " + a.equals(b));
        System.out.println("a equals c (distinto valor): " + a.equals(c));

        Money suma = a.add(c);
        System.out.println("a + c = " + suma);

        try {
            Money usd = new Money(new BigDecimal("10.00"), "USD");
            a.add(usd);
        } catch (IllegalArgumentException e) {
            System.out.println("Monedas distintas: " + e.getMessage());
        }

        try {
            new Money(new BigDecimal("-1"), "EUR");
        } catch (IllegalArgumentException e) {
            System.out.println("Amount negativo: " + e.getMessage());
        }
    }
}
