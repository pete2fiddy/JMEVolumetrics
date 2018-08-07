package mygame.model.segment;

import java.util.Set;
import mygame.util.ArgumentContainer;

public interface Segmenter<SegmenterArgType extends ArgumentContainer> {
    public Set<Integer> segment(SegmenterArgType args);
    
    public <D> D accept(SegmenterVisitor<D> visitor);
}
