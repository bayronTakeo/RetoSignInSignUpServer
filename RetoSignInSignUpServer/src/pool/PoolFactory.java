/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pool;

/**
 * Esta clase crea un pool singletone
 *
 * @author Bayron
 */
public class PoolFactory {

    private static Pool pool;

    public static Pool getPool() {
        if (pool == null) {
            pool = new Pool();
        }
        return pool;
    }
}
