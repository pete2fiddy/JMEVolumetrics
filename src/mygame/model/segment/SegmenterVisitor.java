package mygame.model.segment;

public interface SegmenterVisitor<D> {
    public D visit(PaintbrushSegmenter brushSegmenter);
    public D visit(FloodfillSegmenter fillSegmenter);
    public D visit(SelectionSimilarityConstrainedPaintbrushSegmenter constrainedPaintSegmenter);
}
