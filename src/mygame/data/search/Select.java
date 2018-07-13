/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.data.search;

import java.util.Comparator;

/**
 *
 * @author Owner
 */
public class Select {

    //returns the nth smallest element in arr
    public static <DataType> int quickSelect(DataType[] arr, Comparator<DataType> comparator, int n) {
        return quickSelect(arr, comparator, 0, arr.length, n);
    }

    private static <DataType> int quickSelect(DataType[] arr, Comparator<DataType> comparator, int left, int right, int n) {
        while (left + 1 < right) {
            int k = partition(arr, comparator, left, right);
            if (n < k) {
                right = k;
            } else {
                left = k;
            }
        }
        return left;
    }

    /**
     * Partition array a into a[l..k) and a[k..r), where l<k<r, and all elements
     * in a[l..k) are less than or equal to all elements in a[k..r). Requires:
     * 0≤l, r≤a.length, and r-l≥2.
     */
    private static <DataType> int partition(DataType[] arr, Comparator<DataType> comparator, int left, int right) {
        DataType p = arr[left]; // better: swap a[l] with random element first
        int i = left, j = right;
        do {
            j--;
        } while (comparator.compare(arr[j], p) == 1);
        while (i < j) {
            swapElems(arr, i, j);
            do {
                i++;
            } while (comparator.compare(arr[i], p) == -1);//arr[i] < p);
            do {
                j--;
            } while (comparator.compare(arr[j], p) == 1);//(arr[j] > p);
        }
        return j + 1;
    }

    private static <DataType> void swapElems(DataType[] arr, int swap1, int swap2) {
        DataType temp = arr[swap1];
        arr[swap1] = arr[swap2];
        arr[swap2] = temp;
    }
}
