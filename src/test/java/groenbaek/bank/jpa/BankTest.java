package groenbaek.bank.jpa;

import groenbaek.bank.jpa.model.Account;
import groenbaek.bank.jpa.model.Customer;
import groenbaek.bank.jpa.model.JournalEntry;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static groenbaek.bank.jpa.model.Account.PENNY_TOLERANCE;
import static org.junit.Assert.*;

public class BankTest {

    /**
     * remove all data between tests so we don't need to use different cpr/account numbers
     */
    @Before
    public void cleanDB() {
        EntityManagerFactory emf = EntityManagerFactoryProvider.getEmf();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("delete from AccountNumber").executeUpdate();
            em.createQuery("delete from JournalEntry").executeUpdate();
            em.createQuery("delete from Account").executeUpdate();
            em.createQuery("delete from Customer").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Test
    public void testCustomer() {

        BankService bankService = new BankService();
        bankService.createCustomer("Klaus", "0123456789");
        Customer found = bankService.findCustomerByCpr("0123456789");
        assertEquals("Klaus", found.getName());

        bankService.renameCustomer(found.getCpr(), "Peter");
        found = bankService.findCustomerByCpr("0123456789");
        assertEquals("Peter", found.getName());
    }

    @Test
    public void testAccount() {
        BankService bankService = new BankService();
        Customer customer = bankService.createCustomer("Klaus", "0123456789");
        Account account = bankService.createAccount(customer.getCpr());
        assertNotNull(account.getAccountNumber());
        assertEquals(customer.getId(), account.getCustomer().getId());
    }

    @Test
    public void depositAndWithdrawals() {
        BankService bankService = new BankService();
        Customer customer = bankService.createCustomer("Klaus", "0123456789");
        Account account = bankService.createAccount(customer.getCpr());

        double balance = bankService.balance(customer.getCpr(), account.getAccountNumber());
        assertEquals(0, balance, PENNY_TOLERANCE);

        bankService.deposit(customer.getCpr(), account.getAccountNumber(), "first deposit", 500.0);
        bankService.deposit(customer.getCpr(), account.getAccountNumber(), "second deposit", 400.0);

        balance = bankService.balance(customer.getCpr(), account.getAccountNumber());
        assertEquals(900, balance, PENNY_TOLERANCE);
        bankService.withdraw(customer.getCpr(), account.getAccountNumber(), "fisrt withdrawl", 300);

        balance = bankService.balance(customer.getCpr(), account.getAccountNumber());
        assertEquals(600, balance, PENNY_TOLERANCE);

        List<JournalEntry> entries = bankService.getEntries(customer.getCpr(), account.getAccountNumber());
        Assertions.assertThat(entries).hasSize(3);
        assertEquals(500.0, entries.get(0).getAmount(), PENNY_TOLERANCE);
        assertEquals(400.0, entries.get(1).getAmount(), PENNY_TOLERANCE);
        assertEquals(-300.0, entries.get(2).getAmount(), PENNY_TOLERANCE);
    }

    @Test
    public void testMoneyLaundering() throws InterruptedException {

        Instant start = Instant.now();
        // computers are so fast that we need to insert a 1ms sleep to ensure that we can measure that time passes
        Thread.sleep(1);

        BankService bankService = new BankService();
        Customer klaus = bankService.createCustomer("Klaus", "0123456789");
        Account klausAccount = bankService.createAccount(klaus.getCpr());

        Customer peter = bankService.createCustomer("Peter", "1234567890");
        Account peterAccount = bankService.createAccount(peter.getCpr());


        bankService.deposit(klaus.getCpr(), klausAccount.getAccountNumber(), "under 10K", 500.0);
        bankService.deposit(klaus.getCpr(), klausAccount.getAccountNumber(), "exactly 10K", 10000.0);
        bankService.deposit(klaus.getCpr(), klausAccount.getAccountNumber(), "over 10K", 10000.01);
        bankService.withdraw(klaus.getCpr(), klausAccount.getAccountNumber(), "withdraw", 10000.01);

        bankService.deposit(peter.getCpr(), peterAccount.getAccountNumber(), "under 10K", 500.0);

        Map<Customer, List<JournalEntry>> map = bankService.findPotentialMoneyLaundryCustomers(start);
        Assertions.assertThat(map).hasSize(1);
        List<JournalEntry> entries = map.get(klaus);
        Assertions.assertThat(entries).hasSize(1);
        JournalEntry foundEntry = entries.get(0);
        assertEquals("over 10K", foundEntry.getText());
        assertTrue(foundEntry.getEntryTime().isAfter(start));
        assertEquals(10000.01, foundEntry.getAmount(), PENNY_TOLERANCE);
    }
}
