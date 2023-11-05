/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import DataTransferObjects.Model;
import DataTransferObjects.User;
import exceptions.ConnectionErrorException;
import exceptions.InvalidUserException;
import exceptions.MaxConnectionException;
import exceptions.TimeOutException;
import exceptions.UserExistException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import pool.Pool;

/**
 *
 * @author Bayron
 */
public class Dao implements Model {

    private Connection con;
    Pool pool = new Pool();
    private PreparedStatement stmt;

    final String INSERTRESPARTNER = "INSERT INTO res_partner(create_date, name, create_uid, write_uid, street, phone, active) VALUES(?,?,2,2,?,?,'true');";
    final String INSERTRESUSER = "INSERT INTO res_users(company_id, partner_id, create_date, login, password, create_uid, write_date, notification_type) VALUES(1,?,?,?,?,2,?,'email');";
    final String INSERTRESCOMP = "INSERT INTO res_company_user_rel (cid, user_id) VALUES (1,?);";
    final String INSERTRESGROUP = "INSERT INTO res_groups_users_rel {gid, uid} VALUES (16,?),(26,?),(28,?),(31,?);";
    final String SELECTRESPARTNERID = "SELECT MAX(id) AS id FROM res_partner;";
    final String SELECUSERID = "SELECT MAX(id) AS id FROM res_users;";
    final String SELECTEMAIL = "SELECT login FROM res_users WHERE login = ? GROUP BY login;";
    final String LOGIN = "SELECT * FROM res_users WHERE login = ? and password = ? GROUP ;";

    private static final Logger LOGGER = Logger.getLogger("Dao.class");

    @Override
    public User doSignIn(User user) throws InvalidUserException, ConnectionErrorException, TimeOutException, MaxConnectionException {
        try {
            con = pool.getConnection();
            stmt = con.prepareStatement(LOGIN);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            ResultSet rs = stmt.executeQuery();
            User use = null;
            if (!rs.next()) {
                throw new InvalidUserException("Some data is wrong...");
            }
            use.setEmail(rs.getString("login"));

            return use;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new ConnectionErrorException("Connection error with the database. Try again later.");
        } finally {
            closeConnection();
        }
    }

    @Override
    public void doSignUp(User user) throws UserExistException, ConnectionErrorException, TimeOutException, MaxConnectionException {

        try {
            con = pool.getConnection();

            stmt = con.prepareStatement(SELECTEMAIL);
            stmt.setString(1, user.getEmail());

            if (stmt.executeUpdate() == 1) {
                throw new UserExistException("User already exist!");
            }
            stmt = con.prepareStatement(INSERTRESUSER);
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(6, user.getEmail());
            stmt.executeUpdate();
            stmt = con.prepareStatement(INSERTRESPARTNER);

            stmt.setDate(0, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(1, user.getName());
            stmt.setString(4, user.getDirection());
            stmt.setInt(5, user.getPhoneNumber());
            stmt.executeUpdate();

            stmt = con.prepareStatement(INSERTRESCOMP);
            stmt.setInt(1, getId());

            stmt = con.prepareStatement(INSERTRESGROUP);
            stmt.execute();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new UserExistException();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new ConnectionErrorException("Connection error with the database. Try again later.");
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                pool.releaseConnection(con);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
    }

    public int getId() {
        int id = 0;
        try {
            con = pool.getConnection();
            stmt = con.prepareStatement(SELECUSERID);

            ResultSet rs = stmt.executeQuery();
            id = rs.getInt("id");
        } catch (ConnectionErrorException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
}
