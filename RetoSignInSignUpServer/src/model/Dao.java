/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import DataTransferObjects.Model;
import DataTransferObjects.User;
import exceptions.ConnectionErrorException;
import exceptions.MaxConnectionException;
import exceptions.TimeOutException;
import exceptions.UserExistException;

/**
 *
 * @author Bayron
 */
public class Dao implements Model {

    @Override
    public User doSignIn(User user) throws ConnectionErrorException, TimeOutException, MaxConnectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doSignUp(User user) throws UserExistException, ConnectionErrorException, TimeOutException, MaxConnectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
