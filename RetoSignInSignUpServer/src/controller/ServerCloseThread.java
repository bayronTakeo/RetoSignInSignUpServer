/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import pool.Pool;

/**
 *
 * @author Bayron
 */
public class ServerCloseThread extends Thread {

    private final ServerSocket serverSocket;
    private static final Logger LOGGER = Logger.getLogger("ServerCloseThread.class");

    /**
     * @param serverSocket Server socket a ser cerrado.
     */
    public ServerCloseThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Este metodo se mantiene en un bucle hasta que se escribe la palabra kill,
     * que mata el servidor.
     */
    @Override
    public void run() {
        try {
            while (true) {
                LOGGER.info("Write 'kill' to close the server.");
                Scanner sc = new Scanner(System.in);
                String s = sc.next();
                if (s.equalsIgnoreCase("kill")) {
                    break;
                }
            }
            Pool.closeAllConnections();
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerCloseThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
