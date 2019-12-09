package groenbaek.bank.jpa.model;

import com.google.common.base.Strings;
import com.google.common.math.DoubleMath;
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
import java.time.Instant;

import static groenbaek.bank.jpa.model.Account.PENNY_TOLERANCE;

@Entity
@Getter
@ToString(exclude = "account")
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalEntry {

    public static JournalEntry create(Account account, String text, double amount) {
        if (Strings.isNullOrEmpty(text)) {
            throw new IllegalArgumentException("text is required");
        }
        return new JournalEntry()
                .setAccount(account)
                .setText(text)
                .setAmount(amount)
                .setEntryTime(Instant.now());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "acc_id", nullable = false)
    private Account account;
    @Column(nullable = false)
    private Instant entryTime;
    @Column(nullable = false)
    private String text;
    @Column(nullable = false)
    private double amount;
}
