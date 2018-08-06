package mygame.ui.action;

import mygame.graph.Graph;
import mygame.ml.Segmenter;
import mygame.pointcloud.InteractivePointCloudController;

public class UISegmenterAction implements UIControlAction {
    private Segmenter segmenter;
    private Graph graph;
    private InteractivePointCloudController controlCloud;
    //add some nice way to change the radius of paintbrush using a slider -- have slider logic in separate class and have it talk to the active action
    //in a "nice" way
    public UISegmenterAction(InteractivePointCloudController controlCloud, Segmenter segmenter, Graph graph) {
        this.controlCloud = controlCloud;
        this.segmenter = segmenter;
        this.graph = graph;
    }
    
    @Override
    public void performAction() {
        controlCloud.setSegmenter(segmenter);
        controlCloud.setSimGraph(graph);
    }
}
