package com.passwordmanager.services;

import com.passwordmanager.api.Password;
import com.passwordmanager.infrastructure.Dao;
import com.passwordmanager.security.AES;
import com.passwordmanager.util.NonExistingPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The Ledger class of Password Manager. I have chosen to separate the functionality between Ledger and the
 * PasswordServiceImpl class in order to get cleaner and more readable code.
 *
 * In Ledger the DAO operations take place, and in the PasswordServiceImpl the methods in this class is called and
 * the HTTP part is handled there.
 */
public class Ledger {

    // Logger for Ledger class
    private static final Logger LOG = LoggerFactory.getLogger(Ledger.class);

    // DAO(Data Access Object) object
    private final Dao dao;

    // Constructor
    public Ledger(Dao dao){
        this.dao = dao;
    }

    /**
     * addPassword() method of type UUID. Takes a Password object as parameter.
     * Generates a random UUID for the password and also gets the current date.
     *
     * Gets the key and the value of the password, and then use AES to encrypt those values.
     * Calls insert() on dao instance to store the encrypted password with the UUID and date in the database.
     *
     */
    UUID addPassword(Password password) throws SQLException {
        UUID uuid = UUID.randomUUID();
        LocalDate now = LocalDate.now();

        String key = AES.encrypt(password.getKey(), "karim");
        String value = AES.encrypt(password.getValue(), "karim");
        dao.insert(uuid.toString(), now, key, value);

        LOG.info("Created password with id {}. Fields: key {}, value {}, date {} ", uuid, password.getKey(), password.getValue(), now);
        return uuid;
    }

    /**
     * deletePassword() void method. Takes a String representing an ID as a parameter.
     *
     * Calls deleteOnId() with the ID as a parameter on the dao instance to delete the password from the database.
     *
     */
    void deletePassword(String id) throws SQLException {

        dao.deleteOnId(id);

        LOG.info("Deleted password with id {}. ", id);
    }

    /**
     * getPassword() method of type Password. Takes a String representing an ID as a parameter.
     *
     * Calls selectOnId() with the ID as a parameter on the dao instance to fetch the password from the database.
     * Stores the password in a Password List. Checks that the list is not empty, and then returns the first password
     * in that list.
     *
     */
    Password getPassword(String id) throws SQLException {
        LOG.info("Getting password with id {}", id);
        try {
            List<Password> passwords = dao.selectOnId(id);

            if (passwords.isEmpty()) {
                LOG.info("No password with id {}", id);
                throw new NonExistingPasswordException("Password with id " + id + " does not exist");
            }

            return passwords.get(0);
        } catch (SQLException e) {
            LOG.warn("Got SQL exception when fetching passwords with id {}", id);
            throw e;
        }
    }

    /**
     * getAllPasswords() method of type List<Password>.
     *
     * Calls selectAll() on the dao instance to fetch all the passwords from the database table.
     * Stores all the passwords in a Password List. Checks that the list is not empty, and then returns the list that
     * contains all the passwords.
     *
     */
    List<Password> getAllPasswords() throws SQLException {
        LOG.info("Getting all the passwords");
        try {
            List<Password> passwords = dao.selectAll();

            if (passwords.isEmpty()) {
                LOG.info("No passwords exists.");
                throw new NonExistingPasswordException("Password List is empty.");
            }

            return passwords;
        } catch (SQLException e) {
            LOG.warn("Got SQL exception when fetching passwords");
            throw e;
        }
    }

}
