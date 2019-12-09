package groenbaek.bank.jpa;

import lombok.Synchronized;

import javax.annotation.concurrent.GuardedBy;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Standard static singleton.
 * Hibernate does not cache the EMF, so we have too, otherwise hibernate will try to create tables the second time we create the EMF
 */
public class EntityManagerFactoryProvider {

    @GuardedBy("$LOCK")
    private static EntityManagerFactory emf;

    @Synchronized
    public static EntityManagerFactory getEmf() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("PU");
        }
        return emf;
    }


}
