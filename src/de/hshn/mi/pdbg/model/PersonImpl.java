package de.hshn.mi.pdbg.model;

import de.hshn.mi.pdbg.basicservice.BasicDBService;
import de.hshn.mi.pdbg.basicservice.Patient;
import de.hshn.mi.pdbg.basicservice.Person;
import de.hshn.mi.pdbg.basicservice.jdbc.AbstractPersistentJDBCObject;
import de.hshn.mi.pdbg.exception.StoreException;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public abstract class PersonImpl extends AbstractPersistentJDBCObject implements Person{

    private String firstname;
    private String lastname;
    private Date dateOfBirth;

    public PersonImpl(BasicDBService service, long idPerson, String firstname, String lastname, Date dateOfBirth) {
        super(service, idPerson);
        this.firstname = firstname;
        this.lastname = lastname;
        this.dateOfBirth = dateOfBirth;
    }


    @Override
    public String getLastname() {return lastname;
    }

    @Override
    public void setLastname(String lastname) {
        assert(lastname!=null && lastname!="");
        this.lastname=lastname;

    }

    @Override
    public String getFirstname() {
        return firstname;
    }

    @Override
    public void setFirstname(String firstname) {
        assert(firstname!=null && firstname!="");
        this.firstname=firstname;
    }

    @Override
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth
     *            A <code>dateOfBirth</code> is only valid if it is {@code null} or a date before the current date.
     * @throws AssertionError
     *             Thrown if a given parameter value is invalid.
     */
    @Override
    public void setDateOfBirth(Date dateOfBirth) {

        Date current = new Date();
        assert(dateOfBirth==null || dateOfBirth.before(current));
        this.dateOfBirth=dateOfBirth;
    }


}
