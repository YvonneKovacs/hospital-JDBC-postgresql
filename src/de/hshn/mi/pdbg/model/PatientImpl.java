package de.hshn.mi.pdbg.model;

import de.hshn.mi.pdbg.basicservice.BasicDBService;
import de.hshn.mi.pdbg.basicservice.HospitalStay;
import de.hshn.mi.pdbg.basicservice.Patient;
import de.hshn.mi.pdbg.exception.FetchException;
import de.hshn.mi.pdbg.exception.StoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PatientImpl extends PersonImpl implements Patient {

    private String healthInsurance;
    private String insuranceNumber;
    private Set<HospitalStay> hospitalStays;

    public PatientImpl(BasicDBService service, long idPerson, String firstname, String lastname, Date dateOfBirth, String healthInsurance, String insuranceNumber) {
        super(service, idPerson, firstname, lastname, dateOfBirth);
        this.healthInsurance = healthInsurance;
        this.insuranceNumber = insuranceNumber;
    }

    @Override
    public void setHealthInsurance(String healthInsurance) {
        this.healthInsurance=healthInsurance;
    }

    @Override
    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber=insuranceNumber;
    }

    @Override
    public String getHealthInsurance() {
        return healthInsurance;
    }

    @Override
    public String getInsuranceNumber() {
        return insuranceNumber;
    }


    @Override
    public Set<HospitalStay> getHospitalStays() throws FetchException{
        Set<HospitalStay> hospitalStays=new HashSet<>();
        if(isPersistent()) {
            return new HashSet<HospitalStay>(getBasicDBService().getHospitalStays(this.getObjectID()));
        }
        else return hospitalStays;
    }

    @Override
    public long store(Connection connection) throws SQLException {

        if(!this.isPersistent()) {
            try {

                    long idPatient=INVALID_OBJECT_ID;
                    PreparedStatement pst = connection.prepareStatement("select nextval('person_seq')");
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        idPatient=rs.getLong(1);
                    }

                    String sqlInsert = "insert into Person (idperson,firstname,lastname,dateofbirth) values (?,?,?,?)";
                    PreparedStatement pst2 = connection.prepareStatement(sqlInsert);
                    pst2.setLong(1, idPatient);
                    pst2.setString(2, this.getFirstname());
                    pst2.setString(3, this.getLastname());

                    if(this.getDateOfBirth()!=null) {
                        java.sql.Date date = new java.sql.Date(this.getDateOfBirth().getTime());
                        pst2.setDate(4, date);
                    }
                    else pst2.setDate(4,null);

                    pst2.executeUpdate();

                    //how to insert in patient healthinsurance

                    String sqlInsertPatient = "insert into patient (idperson,healthinsurance,insurancenumber) values (?,?,?)";
                    PreparedStatement pst3 = connection.prepareStatement(sqlInsertPatient);
                    pst3.setLong(1, idPatient);

                    pst3.setString(2, (this).getHealthInsurance());
                    pst3.setString(3, (this).getInsuranceNumber());
                    pst3.executeUpdate();
                    this.setObjectID(idPatient);


                return this.getObjectID();

            } catch (SQLException e) {
                throw new StoreException(e);
            }
        }

        else{
            try{
                PreparedStatement ps2 = connection.prepareStatement("UPDATE person SET firstname=?, lastname=?, dateofbirth=? WHERE idperson=?");
                ps2.setString(1, this.getFirstname());
                ps2.setString(2, this.getLastname());

                if(this.getDateOfBirth()!=null) {
                    java.sql.Date date = new java.sql.Date(this.getDateOfBirth().getTime());
                    ps2.setDate(3, date);
                }
                else ps2.setDate(3,null);

                ps2.setLong(4, this.getObjectID());
                ps2.executeUpdate();


                PreparedStatement ps3 = connection.prepareStatement("UPDATE patient SET healthinsurance=?, insurancenumber=? WHERE idperson=?");
                // System.out.println(((PatientImpl)this).getHealthInsurance());
                ps3.setString(1, (this).getHealthInsurance());
                ps3.setString(2, (this).getInsuranceNumber());
                ps3.setLong(3, this.getObjectID());
                ps3.executeUpdate();



                return this.getObjectID();


            } catch (SQLException e) {
                throw new StoreException(e);
            }
        }

    }
}


