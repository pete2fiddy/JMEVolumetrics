package mygame.model.data.search.priorityqueue;

public interface MyUpdatablePriorityQueue<E> extends MyPriorityQueue<E> {
    
    public void updatePriority(E elem, double key);
}
