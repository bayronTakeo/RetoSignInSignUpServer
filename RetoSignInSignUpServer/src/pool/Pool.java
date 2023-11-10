/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pool;

import exceptions.ConnectionErrorException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase gestiona las conexiones con la base de datos.
 *
 * @author Bayron
 */
public class Pool {

    private final ResourceBundle bundle = ResourceBundle.getBundle("pool.config");
    private final String url = bundle.getString("URL");
    private final String user = bundle.getString("USER");
    private final String password = bundle.getString("PASS");
    private final int maxConnections = Integer.parseInt(bundle.getString("MAX_CONNECTIONS"));
    private static Stack<Connection> usedConnections = new Stack<>();
    private static Stack<Connection> releasedConnections = new Stack<>();
    Connection connection = null;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("Pool.class");

    /**
     * Este método devuelve una conexión con la base de datos. Si no quedan
     * conexiones libres y no se ha alcanzado el máximo crea una nueva.
     *
     * @return la conexión con la base de datos.
     * @throws ConnectionErrorException Esta excepción se produce si las
     * solicitudes de la base de datos exceden.
     */
    public synchronized Connection getConnection() throws ConnectionErrorException {
        if ((usedConnections.size() + releasedConnections.size()) > maxConnections) {
            throw new ConnectionErrorException("Maximum number of requests reached. Try it again later.");
        } else if (releasedConnections.empty()) {
            connection = createConnection();
            usedConnections.push(connection);
        } else {
            connection = releasedConnections.pop();
            usedConnections.push(connection);
        }
        return connection;
    }

    /**
     * Este método libera una conexión.
     *
     * @param connection la conexión que se debe liberar.
     * @return un booleano que comprueba si el método salió bien.
     */
    public boolean releaseConnection(Connection connection) {
        boolean work = false;
        releasedConnections.push(connection);
        if (!releasedConnections.isEmpty()) {
            work = true;
        }
        return work;
    }

    /**
     * Este metodo crea una nueva conexion
     *
     * @return la nueva conexion
     * @throws exceptions.ConnectionErrorException
     */
    public Connection createConnection() throws ConnectionErrorException {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            return connection;
        } catch (SQLException ex) {
            LOGGER.info(ex.getMessage());
            throw new ConnectionErrorException("Connection error with the database. Try again later");
        }
    }

    /**
     * Este metodo cierra todas las conexiones.
     *
     * @return the new connection.
     */
    public static boolean closeAllConnections() {
        boolean closeConnectionOrNot = false;
        for (int i = 0; i < releasedConnections.size(); i++) {
            try {
                releasedConnections.get(i).close();
            } catch (SQLException ex) {
                LOGGER.info(ex.getMessage());
            }
        }
        for (int i = 0; i < usedConnections.size(); i++) {
            try {
                usedConnections.get(i).close();
            } catch (SQLException ex) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (releasedConnections.isEmpty() && usedConnections.isEmpty()) {
            closeConnectionOrNot = true;
        }
        return closeConnectionOrNot;
    }
}
