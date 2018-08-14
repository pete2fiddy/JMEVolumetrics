package mygame.model.data.search.priorityqueue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import mygame.util.ArrayUtil;

//See: https://www.cs.cmu.edu/~adamchik/15-121/lectures/Binary%20Heaps/heaps.html
public class ArrayBinaryMinHeap<E> implements MyUpdatablePriorityQueue<E> {
    private E[] heap;
    private double[] priorities;
    private int heapEnd = 0;
    private Map<E, Integer> indMap = new HashMap<E, Integer>();
    
    public ArrayBinaryMinHeap(int size) {
        this.heap = (E[])new Object[size];
        this.priorities = new double[size];
        for(int i = 0; i < priorities.length; i++) {
            this.priorities[i] = Double.MAX_VALUE;
        }
    }
    
    @Override
    public void add(E elem, double priority) {
        heap[heapEnd] = elem;
        priorities[heapEnd] = priority;
        indMap.put(elem, heapEnd);
        bubbleUp(heapEnd);
        heapEnd++;
    }
    
    @Override
    public E pop() {
        E out = heap[0];
        heap[0] = null;
        priorities[0] = Double.MAX_VALUE;
        indMap.remove(out);
        swap(0, heapEnd-- - 1);
        bubbleDown(0);
        
        return out;
    }

    @Override
    public void updatePriority(E elem, double priority) {
        int indOf = indexOf(elem);
        double oldPriority = priorities[indOf];
        priorities[indOf] = priority;
        if(priorityHigher(priority, oldPriority)) {
            bubbleUp(indOf);
        } else {
            bubbleDown(indOf);
        }
        
    }
    
    @Override
    public int size() {
        return heapEnd;
    }
    
    private void bubbleUp(int bubbleInd) {
        int bubbleIndParent = parentOf(bubbleInd);
        while(bubbleIndParent >= 0 && priorityHigher(priorities[bubbleInd], priorities[bubbleIndParent])) {
            //while bubbleInd is higher priority than its parent
            swap(bubbleIndParent, bubbleInd);
            bubbleInd = bubbleIndParent;
            bubbleIndParent = parentOf(bubbleInd);
        }
        assert(assertShape()): "shape: " + Arrays.toString(heap);
    }
    
    private void bubbleDown(int bubbleInd) {
        if(isLeaf(bubbleInd)) return;
        int leftChild = leftChildOf(bubbleInd);
        if(leftChild >= heapEnd) return;
        int rightChild = rightChildOf(bubbleInd);
        while(priorityLower(priorities[bubbleInd], priorities[leftChild]) ||
                priorityLower(priorities[bubbleInd], priorities[rightChild])) {
            int newBubbleInd = -1;
            //while bubbleInd is lower priority than either of its children
            if(priorityLower(priorities[bubbleInd], priorities[leftChild]) && 
               priorityLower(priorities[bubbleInd], priorities[rightChild])) {
                newBubbleInd = (priorityHigher(priorities[leftChild], priorities[rightChild]))? leftChild : rightChild;
            } else if(priorityLower(priorities[bubbleInd], priorities[leftChild])) {
                newBubbleInd = leftChild;
            } else {
                newBubbleInd = rightChild;
            }
            swap(bubbleInd, newBubbleInd);
            bubbleInd = newBubbleInd;
            leftChild = leftChildOf(bubbleInd);
            if(leftChild >= heapEnd) return;//checks if left child is over, since if it is, 
            //then both children are, and no more bubbling can be done
            rightChild = rightChildOf(bubbleInd);
        }
        assert(assertShape()): "shape: " + Arrays.toString(heap);
    }
    
    protected boolean priorityHigher(double ind1Val, double ind2Val) {
        return ind1Val < ind2Val;
    }
    
    //can't just return inverse of priority higher, because can cause shape invariant to not be correct in cases of tied priority
    protected boolean priorityLower(double ind1Val, double ind2Val) {
        return ind1Val > ind2Val;//!priorityHigher(ind1Val, ind2Val);
    }
    
    private void swap(int ind1, int ind2) {
        indMap.put(heap[ind1], ind2);
        indMap.put(heap[ind2], ind1);
        ArrayUtil.swap(heap, ind1, ind2);
        double ind1Priority = priorities[ind1];
        priorities[ind1] = priorities[ind2];
        priorities[ind2] = ind1Priority;
    }
    
    private int parentOf(int childInd) {
        return (childInd-1)/2;
    }
    
    private int leftChildOf(int parentInd) {
        return 2*parentInd + 1;
    }
    
    private int rightChildOf(int parentInd) {
        return 2*parentInd + 2;
    }
    
    private boolean isLeaf(int ind) {
        return leftChildOf(ind) >= heapEnd;
    }
    
    private int indexOf(E elem) {
        return indMap.get(elem);
    }
    
    private boolean assertShape() {
        for(int i = 0; i < heapEnd; i++) {
            if(heap[i] == null) return false;
        }
        return true;
    }
}
