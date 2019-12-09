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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Getter
@ToString(exclude = {"customer", "entries"})
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    public static final double PENNY_TOLERANCE = 0.005;

    public static Account create(Customer customer, AccountNumber accountNumber) {
        return new Account()
                .setCustomer(customer)
                .setAccountNumber( String.format("%09d", accountNumber.getId()));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "cust_id", nullable = false)
    @ManyToOne
    private Customer customer;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @OneToMany
    private List<JournalEntry> entries;



}
