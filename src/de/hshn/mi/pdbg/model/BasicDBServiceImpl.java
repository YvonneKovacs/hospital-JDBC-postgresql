package de.hshn.mi.pdbg.model;

import de.hshn.mi.pdbg.PersistentObject;
import de.hshn.mi.pdbg.basicservice.*;
import de.hshn.mi.pdbg.basicservice.jdbc.AbstractPersistentJDBCObject;
import de.hshn.mi.pdbg.exception.FetchException;
import de.hshn.mi.pdbg.exception.StoreException;


import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BasicDBServiceImpl implements BasicDBService {

    private Connection connection;

    public BasicDBServiceImpl() throws SQLException {

        connection = getConnection();
    }

    public Connection getConnection() throws SQLException {
        if(connection==null)
            connection= DriverManager.getConnection("jdbc:postgresql://localhost:5432/HOSPITAL", "postgres", "guido");
        return connection;
    }

    public static void releaseConnection(Connection conn) throws SQLException {
        conn.close();
    }

    @Override
    public Patient createPatient(String lastname, String firstname) {
        Patient patient=null;
        assert (lastname!=null && lastname!="" && firstname!=null && firstname!="");
        patient = new PatientImpl(this, PatientImpl.INVALID_OBJECT_ID, firstname, lastname,null,null,null);
        return patient;
    }


    @Override
    public Ward createWard(String name, int numberOfBeds) {
        Ward ward = null;
        assert (name != null && name != "");
        assert (numberOfBeds >= 0);
        ward = new WardImpl(this, WardImpl.INVALID_OBJECT_ID, name, numberOfBeds);
        return ward;
    }


    @Override
    public HospitalStay createHospitalStay(Patient patient, Ward ward, Date admissionDate) {
        HospitalStay hospitalStay=null;
        assert(patient!=null && ward!=null && admissionDate!=null);
        hospitalStay = new HospitalStayImpl(this,HospitalStayImpl.INVALID_OBJECT_ID,admissionDate,null, patient, ward);
        return hospitalStay;
    }


    @Override
    public void removeHospitalStay(long hospitalStayID) {
        assert(hospitalStayID>0 && hospitalStayID!=HospitalStay.INVALID_OBJECT_ID);
        try
        {
            connection=getConnection();
            PreparedStatement ps = connection.prepareStatement("delete from hospitalstay where idhospitalstay = ?");
            ps.setLong(1, hospitalStayID);
            ps.executeUpdate();


        } catch(SQLException e)
        {
            throw new StoreException(e);
        }

    }


    @Override
    public List<Patient> getPatients(String lastname, String firstname, Date startDate, Date endDate) {

        try {
            Connection conn = getConnection();

            PreparedStatement selectedPS = null;
            PreparedStatement ps0=conn.prepareStatement("select * from person pe, patient pa where pe.idPerson=pa.idPerson");
            PreparedStatement ps1 = conn.prepareStatement("select idperson, firstname, dateofbirth from person where lastname=?");
            PreparedStatement ps2 = conn.prepareStatement("select * from person where firstname=?");
            PreparedStatement ps3 = conn.prepareStatement("select * from person where dateofbirth>=? and dateofbirth<=?");
            PreparedStatement ps4 = conn.prepareStatement("select * from person where dateofbirth>=?");
            PreparedStatement ps5 = conn.prepareStatement("select * from person where dateofbirth<=?");
            ResultSet rs=null;
            List<Patient> patientList = new ArrayList<>();

            // prepare the query
            if (lastname != null) {
                ps1.setString(1, lastname);
                selectedPS = ps1;

            }
            else    if(firstname!=null) {
                ps2.setString(1, firstname);
                selectedPS = ps2;
            }
            else if(startDate!=null){
                ps3.setDate(1, new java.sql.Date(startDate.getTime()));
                selectedPS = ps3;
            }
            else if(endDate!=null){
                ps4.setDate(1,  new java.sql.Date(endDate.getTime()));
                selectedPS = ps4;
            }
            else {
                selectedPS=ps0;
            }
            // get the data
            rs = selectedPS.executeQuery();
            while (rs.next()) {

                long idPerson=rs.getLong("idPerson");
                patientList.add(new PatientImpl(
                        this,
                        idPerson,
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getDate("dateOfBirth"),
                        rs.getString("healthInsurance"),
                        rs.getString("insuranceNumber")
                ));
            }
                return patientList;


    }catch (SQLException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public Patient getPatient(long patientID) {

        assert(patientID>0 && patientID!=PatientImpl.INVALID_OBJECT_ID);
        Patient patient=null;
        try
        {
            connection=getConnection();
            PreparedStatement ps = connection.prepareStatement("select * from person pe, patient pa where pe.idperson = pa.idperson and pe.idperson=?");
            ps.setLong(1,patientID);
            ResultSet rs = ps.executeQuery();


            if(rs.next())
            {

                String firstname=rs.getString("firstname");
                String lastname=rs.getString("lastname");
                Date dateofbith=rs.getDate("dateofbirth");

                    patient = new PatientImpl(
                            this,
                            patientID,
                            firstname,
                            lastname,
                            dateofbith,
                            rs.getString("healthinsurance"),
                            rs.getString("insurancenumber")
                    );

            }

            return patient;
        }
        catch(SQLException e)
        {
            throw new FetchException(e);
        }

    }

    @Override
    public List<Ward> getWards() {
        try
        {
            connection=getConnection();
            PreparedStatement ps = connection.prepareStatement("select * from ward");
            ResultSet rs = ps.executeQuery();
            List<Ward> wardList = new ArrayList<>();

            while(rs.next())
            {
                wardList.add(new WardImpl(
                        this,
                        rs.getInt("idward"),
                        rs.getString("wardname"),
                        rs.getInt("bedNr")));
            }

            return wardList;
        }
        catch(SQLException e)
        {
            throw new FetchException(e);
        }
    }

    @Override
    public Ward getWard(long wardID) {

        assert (wardID>0 && wardID!=PersistentObject.INVALID_OBJECT_ID);

        Ward ward = null;
        try
        {
            connection=getConnection();
            PreparedStatement ps = connection.prepareStatement("select * from ward where idward = ?");
            ps.setLong(1, wardID);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                ward = new WardImpl(
                        this,
                        wardID,
                        rs.getString("wardname"),
                        rs.getInt("bednr")
                        );

            }

            return ward;
        }
        catch(SQLException e)
        {
            throw new FetchException(e);
        }


    }

    @Override
    public List<HospitalStay> getHospitalStays(long patientID) {
        assert(patientID>0 && patientID!=HospitalStay.INVALID_OBJECT_ID);
        try {
            connection=getConnection();
            PreparedStatement ps = connection.prepareStatement("select * from HospitalStay where patientIdPerson=?");
            ps.setLong(1, patientID);
            ResultSet rs = ps.executeQuery();
            List<HospitalStay> hospitalStays=new ArrayList<>();
            while(rs.next())
            {

                long wardID = rs.getLong("idward");
                Ward ward= getWard(wardID);
                Patient patient=getPatient(patientID);
                hospitalStays.add(new HospitalStayImpl(this,
                        rs.getLong("idHospitalstay"),
                        rs.getDate("admissiondate"),
                        rs.getDate("dischargedate"),
                        patient,
                        ward
                        ));
            }

            return hospitalStays;
        }
        catch(SQLException e)
        {
            throw new FetchException(e);
        }


    }


    @Override
    public List<HospitalStay> getHospitalStays(long patientID, Date startDate, Date endDate) {
        assert(patientID>0 && patientID!=PersistentObject.INVALID_OBJECT_ID);
        assert(startDate.before(endDate));
        List<HospitalStay> hospitalStayList = new ArrayList<>();
        try {
            Connection conn = getConnection();

            if (startDate != null) {

                    PreparedStatement ps1 = conn.prepareStatement("select * from patient where admissiondate>=?");
                    ps1.setDate(1, new java.sql.Date(startDate.getTime()));
                    ResultSet rs = ps1.executeQuery();
                    while (rs.next()) {
                        Patient patient = getPatient(patientID);
                        long wardID = rs.getLong("idward");
                        Ward ward = getWard(wardID);
                        hospitalStayList.add(new HospitalStayImpl(
                                this,
                                rs.getLong("idhospitalstay"),
                                rs.getDate("admissiondate"),
                                rs.getDate("dischargedate"),
                                patient,
                                ward
                        ));
                    }
                }
            else  if(endDate!=null) {

                    PreparedStatement ps2 = conn.prepareStatement("select * from patient where dischargedate<=?");
                    ps2.setDate(1, new java.sql.Date(endDate.getTime()));
                    ResultSet rs = ps2.executeQuery();
                    while (rs.next()) {
                        Patient patient = getPatient(patientID);
                        long wardID = rs.getLong("idward");
                        Ward ward = getWard(wardID);
                        hospitalStayList.add(new HospitalStayImpl(
                                this,
                                rs.getLong("idhospitalstay"),
                                rs.getDate("admissiondate"),
                                rs.getDate("dischargedate"),
                                patient,
                                ward
                        ));
                    }
                }}
         catch(SQLException e)
         {
             throw new FetchException(e);
         }
        catch(StoreException e)
        {
            throw new StoreException();
        }
        return hospitalStayList;
    }


    @Override
    public long store(PersistentObject object) {

        assert(object != null);

        try {
            connection=getConnection();
                if(object instanceof AbstractPersistentJDBCObject)
                    return ((AbstractPersistentJDBCObject) object).store(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        throw new StoreException();
    }

    @Override
    public void close() {
        try {
            releaseConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
