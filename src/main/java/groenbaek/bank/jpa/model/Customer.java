package groenbaek.bank.jpa.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Entity
@Getter
@Setter
@ToString(exclude = "accounts")
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    private static Pattern cprPattern = Pattern.compile("\\d{10}");

    public static Customer create(String name, String cpr) {
        validateCPR(cpr);
        Customer customer = new Customer();
        customer.setName(name);
        customer.setCpr(cpr);
        return customer;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String cpr;

    @OneToMany(mappedBy = "customer")
    private List<Account> accounts;

    private static void validateCPR(String cpr) {
        if (!cprPattern.matcher(cpr).matches()) {
            throw new IllegalArgumentException(cpr + " is not a valid CPR number.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
