package defunct;

import java.util.Set;

/*
Generally going to be used by implementers whose job it is to spit out a subset of currently selected points, but could be used for other purposes
*/
public interface SegmentContainer {
    public Set<Integer> getSegmentedIds();
}
