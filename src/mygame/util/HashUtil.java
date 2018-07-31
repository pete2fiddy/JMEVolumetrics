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
public class HashUtil {
    public static int simpleIntArrHashCode(int... arr) {
        //hashcode implementation from here: https://stackoverflow.com/questions/9858376/hashcode-for-3d-integer-coordinates-with-high-spatial-coherence
        int hash = 23;
        for(int i : arr) {
            hash = hash*31 + i;
        }
        return hash;
    }
}
