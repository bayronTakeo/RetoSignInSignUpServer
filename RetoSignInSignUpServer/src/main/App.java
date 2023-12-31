/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import DataTransferObjects.MessageEnum;
import controller.ServerCloseThread;
import controller.Worker;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * La clase principal de la aplicación que gestiona la conexión con los
 * clientes.
 *
 * @author Bayron
 */
public class App {

    private ServerSocket scktServer;
    private Socket scktClient;
    Worker worker;
    ServerCloseThread serverClose;
    private final ResourceBundle bundle = ResourceBundle.getBundle("pool.config");
    private final Integer MAX_CONNECTIONS = Integer.parseInt(bundle.getString("MAX_CONNECTIONS"));
    private static Integer connections = 0;
    private static final Logger LOGGER = Logger.getLogger("Application");
    private DataTransferObjects.Package pack;

    /**
     * . Inicia un servidor y maneja las conexiones entrantes.
     *
     */
    public App() {
        try {
            scktServer = new ServerSocket(Integer.parseInt(bundle.getString("PORT")));
            serverClose = new ServerCloseThread(scktServer);
            serverClose.start();
            while (true) {

                scktClient = scktServer.accept();
                if (connections < MAX_CONNECTIONS) {
                    worker = new Worker(scktClient);
                    worker.start();
                } else {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(scktClient.getOutputStream());
                        pack.setMessage(MessageEnum.AN_MAXCONNECTION);
                        oos.writeObject(pack);
                        oos.close();
                        scktClient.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
    }

    public static synchronized void removeConnection() {
        connections--;
    }

    public static synchronized void addConnection() {
        connections++;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new App();
    }

}
