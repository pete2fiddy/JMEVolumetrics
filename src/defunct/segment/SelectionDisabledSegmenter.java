package defunct.segment;

import defunct.Segmenter;
import java.util.HashSet;
import java.util.Set;
import mygame.model.graph.Graph;

//a segmenter whose purpose is to be the active segmenter when selection is disabled
public class SelectionDisabledSegmenter implements Segmenter {

    @Override
    public Set<Integer> getSegmentedIds(Graph simGraph) {
        return new HashSet<Integer>();
    }
}
