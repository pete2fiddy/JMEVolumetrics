/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 * @author Owner
 */
public class ArrayUtil {
    
    public static void fill2d(int[][] arr, int val) {
        for(int i = 0; i < arr.length; i++) {
            for(int j = 0; j < arr[i].length; j++){
                arr[i][j] = val;
            }
        }
    }
    
    public static boolean inBounds(int[][] arr, int... indices) {
        return indices[0] >= 0 && indices[0] < arr.length && indices[1] >= 0 && indices[1] < arr[indices[0]].length;
    }
    
    public static <D> void swap(D[] arr, int ind1, int ind2) {
        D ind1Temp = arr[ind1];
        arr[ind1] = arr[ind2];
        arr[ind2] = ind1Temp;
    }
}
