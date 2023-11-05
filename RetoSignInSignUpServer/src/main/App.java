/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import controller.ServerCloseThread;
import controller.Worker;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
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
    
    
    public App() {
        try {
            scktServer = new ServerSocket(Integer.parseInt(bundle.getString("PORT")));
            serverClose = new ServerCloseThread(scktServer);
            serverClose.start();
            while (true) {
                  LOGGER.info("Prueba");
                scktClient = scktServer.accept();
                if (connections < MAX_CONNECTIONS) {
                    worker = new Worker(scktClient, false);
                } else {
                    worker = new Worker(scktClient, true);
                }
                worker.start();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage());
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
