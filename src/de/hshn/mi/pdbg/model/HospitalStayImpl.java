package de.hshn.mi.pdbg.model;

import de.hshn.mi.pdbg.basicservice.BasicDBService;
import de.hshn.mi.pdbg.basicservice.HospitalStay;
import de.hshn.mi.pdbg.basicservice.Patient;
import de.hshn.mi.pdbg.basicservice.Ward;
import de.hshn.mi.pdbg.basicservice.jdbc.AbstractPersistentJDBCObject;
import de.hshn.mi.pdbg.exception.FetchException;
import de.hshn.mi.pdbg.exception.StoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class HospitalStayImpl extends AbstractPersistentJDBCObject implements HospitalStay  {

    private Date admissionDate;
    private Date dischargeDate;
    private Ward ward;
    private Patient patient;

    public HospitalStayImpl(BasicDBService service, long idHospitalStay, Date admissionDate, Date dischargeDate, Patient patient, Ward ward) {
        super(service, idHospitalStay);
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.patient=patient;
        this.ward=ward;
    }


    @Override
    public Date getAdmissionDate() {
        return admissionDate;
    }

    /**
     * @param admissionDate
     *            An admission date is only valid if it is a date before the
     *            discharge date. It must not be {@code null}.
     * @throws AssertionError
     *             Thrown if a given parameter value is invalid.
     */
    @Override
    public void setAdmissionDate(Date admissionDate) {
        if(dischargeDate!=null)
            assert(admissionDate!=null && admissionDate.before(dischargeDate));
        assert(admissionDate!=null);
        this.admissionDate=admissionDate;
    }

    @Override
    public Date getDischargeDate() {
        return dischargeDate;
    }

    /**
     * @param dischargeDate
     *            A discharge date is only valid if it is a date after the
     *            admission date or {@code null}.
     * @throws AssertionError
     *             Thrown if a given parameter value is invalid.
     */
    @Override
    public void setDischargeDate(Date dischargeDate) {
        if(dischargeDate!=null)
            assert(dischargeDate.after(admissionDate) || dischargeDate==null);
        this.dischargeDate=dischargeDate;
    }

    /**
     * @return An associated {@link Ward} object.
     * @exception FetchException
     *                Thrown if an error occurred while fetching a Ward object.
     */
    @Override
    public Ward getWard() throws FetchException{
        return ward;
    }

    @Override
    public void setWard(Ward ward) {
        assert(ward!=null);
        this.ward=ward;
    }

    @Override
    public Patient getPatient() throws FetchException {
        return patient;
    }

    @Override
    public long store(Connection connection) throws SQLException {
       
            if(!this.isPersistent()) {
                try {
                    long idHospitalStay=INVALID_OBJECT_ID;
                    PreparedStatement pst = connection.prepareStatement("select nextval('hospitalstay_seq')");
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        idHospitalStay=rs.getLong(1);
                    }
                    String sqlInsert = "insert into hospitalstay (idHospitalStay, admissionDate, dischargeDate,patientIdPerson,idWard) values (?,?,?,?,?)";
                    PreparedStatement ps = connection.prepareStatement(sqlInsert);
                    ps.setLong(1, idHospitalStay);

                    java.sql.Date admissionDateSQL = new java.sql.Date(this.getAdmissionDate().getTime());
                    java.sql.Date dischargeDateSQl = new java.sql.Date(this.getDischargeDate().getTime());

                    ps.setDate(2, admissionDateSQL);
                    ps.setDate(3, dischargeDateSQl);

                    ps.setLong(4, this.getPatient().getObjectID());
                    ps.setLong(5, this.getWard().getObjectID());
                    ps.executeUpdate();
                    this.setObjectID(idHospitalStay);


                    return this.getObjectID();

                } catch (SQLException e) {
                    throw new StoreException(e);
                }
            }

            else{
                try{
                    PreparedStatement ps = connection.prepareStatement("UPDATE hospitalstay SET admissiondate=?, dischargedate=?,patientIdPerson=?,idWard=? WHERE idhospitalstay=?");

                    java.sql.Date admissionDateSQL = new java.sql.Date(this.getAdmissionDate().getTime());
                    java.sql.Date dischargeDateSQl = new java.sql.Date(this.getDischargeDate().getTime());

                    ps.setDate(1, admissionDateSQL);
                    ps.setDate(2, dischargeDateSQl);
                    ps.setLong(3, this.getPatient().getObjectID());
                    ps.setLong(4, this.getWard().getObjectID());
                    ps.setLong(5, this.getObjectID());
                    ps.executeUpdate();
                    return this.getObjectID();


                } catch (SQLException e) {
                    throw new StoreException(e);
                }
            }


    }
}
