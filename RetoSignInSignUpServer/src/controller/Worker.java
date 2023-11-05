/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import DataTransferObjects.MessageEnum;
import DataTransferObjects.Model;
import DataTransferObjects.User;
import DataTransferObjects.Package;
import exceptions.ConnectionErrorException;
import exceptions.InvalidUserException;
import exceptions.MaxConnectionException;
import exceptions.TimeOutException;
import exceptions.UserExistException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.App;
import model.DaoFactory;

/**
 *
 * @author Bayron
 */
public class Worker extends Thread {

    private Package pack;
    private final Socket skt;
    private User user;
    private final Boolean exc;
    private static final Logger LOGGER = Logger.getLogger("Worker.class");

    /**
     * @param skt Socket to get the streams.
     * @param exc Boolean to handle the MaxConnectionExceededException.
     */
    public Worker(Socket skt, Boolean exc) {
        this.skt = skt;
        this.exc = exc;
        pack = new Package();
    }

    /**
     * This method manages the requests and answers through streams.
     */
    @Override
    public void run() {
        try {
            App.addConnection();
            if (exc) {
                throw new MaxConnectionException();
            }
            ObjectInputStream ois = new ObjectInputStream(skt.getInputStream());
            Model model = DaoFactory.getModel();
            pack = (Package) ois.readObject();
            pack = processMessage(pack);
        } catch (IOException | ClassNotFoundException | TimeOutException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        } catch (InvalidUserException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            pack.setMessage(MessageEnum.AN_INVALIDUSER);
        } catch (MaxConnectionException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            pack.setMessage(MessageEnum.AN_MAXCONNECTION);
        } catch (UserExistException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            pack.setMessage(MessageEnum.AN_USEREXIST);
        } catch (ConnectionErrorException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            pack.setMessage(MessageEnum.AN_CONNECTIONERROR);
        } finally {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(skt.getOutputStream());
                oos.writeObject(pack);
                oos.close();
                skt.close();
                App.removeConnection();
            } catch (IOException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Package processMessage(Package pack) throws InvalidUserException, ConnectionErrorException, TimeOutException, MaxConnectionException, UserExistException {

        Model model = DaoFactory.getModel();
        User user;

        if (pack.getMessage().equals(MessageEnum.RE_SIGNIN)) {
            user = model.doSignIn(pack.getUser());
            pack.setUser(user);
        } else if (pack.getMessage().equals(MessageEnum.RE_SIGNUP)) {
            model.doSignUp(pack.getUser());
        }

        pack.setMessage(MessageEnum.AN_OK);

        return pack; // Devolver el objeto Package después de procesarlo
    }
}
