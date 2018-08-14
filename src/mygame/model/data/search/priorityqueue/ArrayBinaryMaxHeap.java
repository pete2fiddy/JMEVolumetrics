package mygame.model.data.search.priorityqueue;

public class ArrayBinaryMaxHeap extends ArrayBinaryMinHeap {
    
    public ArrayBinaryMaxHeap(int size) {
        super(size);
    }
    
    protected boolean priorityHigher(double ind1Val, double ind2Val) {return super.priorityLower(ind1Val, ind2Val);}
    protected boolean priorityLower(double ind1Val, double ind2Val) {return super.priorityHigher(ind1Val, ind2Val);}
}
