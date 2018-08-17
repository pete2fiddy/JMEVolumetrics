/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

/**
 *
 * @author Owner
 */
public class RandomUtil {
    public static double generateInRange(double... minMax) {
        return minMax[0]+Math.random()*(minMax[1]-minMax[0]);
    }
}
