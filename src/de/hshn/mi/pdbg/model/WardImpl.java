package de.hshn.mi.pdbg.model;

import de.hshn.mi.pdbg.PersistentObject;
import de.hshn.mi.pdbg.basicservice.BasicDBService;
import de.hshn.mi.pdbg.basicservice.Ward;
import de.hshn.mi.pdbg.basicservice.jdbc.AbstractPersistentJDBCObject;
import de.hshn.mi.pdbg.exception.StoreException;


import java.sql.*;

public class WardImpl extends AbstractPersistentJDBCObject implements Ward {

    private String name;
    private int numberOfBeds;

    public WardImpl(BasicDBService service, long idWard, String name, int numberOfBeds) {
        super(service, idWard);
        this.name = name;
        this.numberOfBeds = numberOfBeds;
    }

    @Override
    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    @Override
    public void setNumberOfBeds(int numberOfBeds) {
        assert (numberOfBeds>0);
        this.numberOfBeds=numberOfBeds;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        assert (name!=null && name!="");
        this.name=name;
    }

    /**
     * Override this method in order to store an instance of this class using
     * JDBC. <p/> This method should be invoked via your implementation of
     * {//@link BasicDBService#store(PersistentObject)}.
     *
     * @param connection
     *            The JDBC connection to be used for storing (should be open an
     *            usable).
     * @return The persistent object ID of this (stored) object.
     * @throws SQLException
     *             In case of any SQL/JDBC problems.
     */
    @Override
    public long store(Connection connection) throws SQLException {

        if(!this.isPersistent()) {
            try {
                long idWard=INVALID_OBJECT_ID;
                PreparedStatement pst = connection.prepareStatement("select nextval('ward_seq')");
                ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        idWard=rs.getLong(1);
                    }

                    String sqlInsert = "insert into ward (idward,wardname,bednr) values (?,?,?)";
                    PreparedStatement pst2 = connection.prepareStatement(sqlInsert);
                    pst2.setLong(1, idWard);
                    pst2.setString(2, this.getName());
                    pst2.setInt(3, this.getNumberOfBeds());
                    pst2.executeUpdate();
                    this.setObjectID(idWard);

                return this.getObjectID();
            } catch (SQLException e) {
                throw new StoreException(e);
            }
        }

        else{
            try{
                PreparedStatement ps2 = connection.prepareStatement("UPDATE ward SET wardname=?, bednr=? WHERE idward=?");
                ps2.setString(1, this.getName());
                ps2.setInt(2, this.getNumberOfBeds());
                ps2.setLong(3, this.getObjectID());
                ps2.executeUpdate();

                return this.getObjectID();


            } catch (SQLException e) {
                throw new StoreException(e);
            }
        }


    }

    @Override
    public String toString() {
        return "WardImpl{" +
                ", name='" + name + '\'' +
                ", NumberOfBeds=" + numberOfBeds +
                '}';
    }
}
