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
import pool.PoolFactory;

/**
 * Esta clase es la implementación del modelo que realiza las acciones con la
 * base de datos.
 *
 * @author Bayron
 */
public class Dao implements Model {

    private Connection con;

    private PreparedStatement stmt;
    private static Pool pool;
    final String INSERTRESPARTNER = "INSERT INTO res_partner(create_date, name, create_uid, write_uid, street, phone, active) VALUES(?,?,2,2,?,?,'true');";
    final String INSERTRESUSER = "INSERT INTO res_users(company_id, partner_id, create_date, login, password, create_uid, write_date, notification_type) VALUES(1,?,?,?,?,2,?,'email');";
    final String INSERTRESCOMP = "INSERT INTO res_company_users_rel (cid, user_id) VALUES (1,?);";
    final String INSERTRESGROUP = "INSERT INTO res_groups_users_rel (gid, uid) VALUES (16,?),(26,?),(28,?),(31,?);";
    final String SELECTRESPARTNERID = "SELECT MAX(id) AS id FROM res_partner;";
    final String SELECUSERID = "SELECT MAX(id) AS id FROM res_users;";
    final String SELECTEMAIL = "SELECT login FROM res_users WHERE login = ? GROUP BY login;";
    final String LOGIN = "SELECT * FROM res_users WHERE login = ? and password = ?";

    private static final Logger LOGGER = Logger.getLogger("Dao.class");

    /**
     *
     * @throws ConnectionErrorException Excepcion lanzada hacia arriba
     */
    public void getConnection() throws ConnectionErrorException {
        pool = PoolFactory.getPool();
        con = pool.getConnection();
    }

    /**
     * Método para hacer el inicio de sesión de un cliente
     *
     * @param user el usuario que debe comprobarse si existe
     * @return el usuario si encuentra uno
     * @throws InvalidUserException el usuario especificado no existe
     * @throws ConnectionErrorException se produjo un error de conexión al
     * intentar para conectarse a la base de datos
     * @throws TimeOutException no puede conectarse a la base de datos
     * @throws MaxConnectionException el número máximo de conexión fue superado
     */
    @Override
    public User doSignIn(User user) throws InvalidUserException, ConnectionErrorException, TimeOutException, MaxConnectionException {
        try {
            getConnection();
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

    /**
     * Metodo para registrar un nuevo usuario.
     *
     * @param user el usuario que recibe
     * @throws UserExistException execepcion lanzada hacia arriba
     * @throws ConnectionErrorException xecepcion lanzada hacia arriba
     * @throws TimeOutException execepcion lanzada hacia arriba
     * @throws MaxConnectionException execepcion lanzada hacia arriba
     */
    @Override
    public void doSignUp(User user) throws UserExistException, ConnectionErrorException, TimeOutException, MaxConnectionException {

        try {
            getConnection();

            //con.setAutoCommit(false);
            stmt = con.prepareStatement(SELECTEMAIL);
            stmt.setString(1, user.getEmail());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                throw new UserExistException();
            }

            stmt = con.prepareStatement(INSERTRESPARTNER);
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getDirection());
            stmt.setInt(4, user.getPhoneNumber());
            stmt.executeUpdate();

            stmt = con.prepareStatement(SELECTRESPARTNERID);

            ResultSet rs2 = stmt.executeQuery();

            if (rs2.next()) {
                stmt = con.prepareStatement(INSERTRESUSER);
                stmt.setInt(1, rs2.getInt("id"));
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getPassword());
                stmt.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
                stmt.executeUpdate();
            }
            int id = getId();

            stmt = con.prepareStatement(INSERTRESCOMP);
            stmt.setInt(1, id);
            stmt.executeUpdate();

            stmt = con.prepareStatement(INSERTRESGROUP);
            stmt.setInt(1, id);
            stmt.setInt(2, id);
            stmt.setInt(3, id);
            stmt.setInt(4, id);
            stmt.executeUpdate();

            //con.commit();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            // try {
            // En caso de error, hacer un rollback para deshacer los cambios
            //   if (con != null) {
            //     con.rollback();
            //}
            // } catch (SQLException e1) {
            //   LOGGER.log(Level.SEVERE, e1.getMessage());
            //}
            throw new ConnectionErrorException("Connection error with the database. Try again later.");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new UserExistException();
        } finally {
            closeConnection();
        }
    }

    /**
     * Este metodo cierra la conexion y la libera si no es null.
     */
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

    /**
     * Este metodo recoge el ultimo id de la base de datos para las dos ultimas
     * consultas.
     *
     * @return devuelve un id
     */
    public int getId() {
        int id = 0;
        try {
            con = pool.getConnection();
            stmt = con.prepareStatement(SELECUSERID);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
            }
            LOGGER.info("Valor" + String.valueOf(id));
        } catch (ConnectionErrorException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
}
