package mygame.model.data.search.priorityqueue;

import java.util.Iterator;

public interface MyPriorityQueue<E> {
    
    public void add(E elem, double key);
    public E pop();
    public int size();
}
