package groenbaek.bank.jpa;

import com.google.common.math.DoubleMath;
import groenbaek.bank.jpa.model.Account;
import groenbaek.bank.jpa.model.AccountNumber;
import groenbaek.bank.jpa.model.Customer;
import groenbaek.bank.jpa.model.JournalEntry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static groenbaek.bank.jpa.model.Account.PENNY_TOLERANCE;

/**
 * provides a service layer that communicates with the database
 */
public class BankService {

    private final EntityManagerFactory emf;

    public BankService() {
        this.emf = EntityManagerFactoryProvider.getEmf();
    }

    public Customer createCustomer(String name, String cpr) {
        Customer customer = Customer.create(name, cpr);
        persist(customer);
        return customer;
    }

    public Customer findCustomerByCpr(String cpr) {
        EntityManager em = emf.createEntityManager();
        try {
            return findCustomerNoTransaction(cpr, em);
        } finally {
            em.close();
        }
    }

    public Account createAccount(String cpr) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Customer customer = findCustomerNoTransaction(cpr, em);
            if (customer == null) {
                throw new IllegalArgumentException("No customer with cpr " + cpr);
            }
            AccountNumber accountNumber = new AccountNumber();
            em.persist(accountNumber);
            Account account = Account.create(customer, accountNumber);
            em.persist(account);
            em.getTransaction().commit();
            return account;
        } finally {
            em.close();
        }
    }

    public void renameCustomer(String cpr, String newName) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Customer customer = findCustomerNoTransaction(cpr, em);
            customer.setName(newName);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void deposit(String cpr, String accountNumber, String text, double amount) {

        if (DoubleMath.fuzzyCompare(amount, 0, PENNY_TOLERANCE) < 0) {
            throw new IllegalArgumentException("Amount cant be negative");
        }
        addJournalEntry(cpr, accountNumber, text, amount);
    }

    public void withdraw(String cpr, String accountNumber, String text, double amount) {

        if (DoubleMath.fuzzyCompare(amount, 0, PENNY_TOLERANCE) < 0) {
            throw new IllegalArgumentException("Amount cant be negative");
        }
        addJournalEntry(cpr, accountNumber, text, -amount);
    }

    public double balance(String cpr, String accountNumber) {
        EntityManager em = emf.createEntityManager();
        try {
            Double balance = em.createQuery(
                    "select sum (j.amount) from JournalEntry j where j.account.accountNumber = :accountNumber and j.account.customer.cpr = :cpr", Double.class)
                    .setParameter("accountNumber", accountNumber).setParameter("cpr", cpr)
                    .getSingleResult();
            if (balance == null) {
                return 0;
            }
            return balance;
        } finally {
            em.close();
        }
    }

    public List<JournalEntry> getEntries(String cpr, String accountNumber) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<JournalEntry> entries = em.createQuery(
                    "select j from JournalEntry j where j.account.accountNumber = :accountNumber and j.account.customer.cpr = :cpr " +
                            "order by j.entryTime", JournalEntry.class)
                    .setParameter("accountNumber", accountNumber).setParameter("cpr", cpr)
                    .getResultList();
            em.getTransaction().commit();
            return entries;
        } finally {
            em.close();
        }
    }

    public Map<Customer, List<JournalEntry>> findPotentialMoneyLaundryCustomers(Instant since) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<JournalEntry> entries = em.createQuery(
                    "select j from JournalEntry j where j.entryTime >= :since and j.amount > 10000 order by j.entryTime", JournalEntry.class)
                    .setParameter("since", since)
                    .getResultList();
            em.getTransaction().commit();
            return entries.stream()
                    .collect(Collectors.groupingBy(journalEntry -> journalEntry.getAccount().getCustomer()));
        } finally {
            em.close();
        }
    }

    private Customer findCustomerNoTransaction(String cpr, EntityManager em) {
        return em.createQuery("select c from Customer c where c.cpr = :cpr", Customer.class)
                .setParameter("cpr", cpr).getSingleResult();
    }

    private void persist(Object customer) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(customer);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private void addJournalEntry(String cpr, String accountNumber, String text, double amount) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Account account = em.createQuery("select a from Account a where a.accountNumber = :accountNumber and a.customer.cpr = :cpr", Account.class)
                    .setParameter("accountNumber", accountNumber).setParameter("cpr", cpr)
                    .getSingleResult();
            em.persist(JournalEntry.create(account, text, amount));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
