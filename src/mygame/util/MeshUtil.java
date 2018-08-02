package mygame.util;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import mygame.pointcloud.LineCloud;
import mygame.volumetrics.Facet;
import mygame.volumetrics.IndexedVolume;
import mygame.volumetrics.Volume;

public class MeshUtil {
    /*
    See: https://wiki.jmonkeyengine.org/jme3/advanced/custom_meshes.html
    */
    
    /*
    assumes triangular facets, and that orientation wanted is as-is in the volume
    */
    public static Mesh createNoIndexMesh(Volume v) {
        Mesh mesh = new Mesh();
        Vector3f[] vertices = new Vector3f[VolumeUtil.numPoints(v)];
        int verticesInd = 0;
        for(int i = 0; i < v.numFacets(); i++) {
            Facet f = v.getFacet(i);
            assert(f.numPoints() == 3);//should throw error here
            for(int fPointNum = 0; fPointNum < f.numPoints(); fPointNum++) {
                vertices[verticesInd++] = JblasJMEConverter.toVector3f(f.getPointClones(fPointNum))[0];
            }
        }
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.updateBound();//can't remember if this is still supposed to be used.
        return mesh;
    }
    
    /*
    assumes triangular facets, and that orientation wanted is as-is in the volume
    */
    public static Mesh createIndexedMesh(IndexedVolume v) {
        Mesh mesh = new Mesh();
        Vector3f[] vertices = JblasJMEConverter.toVector3f(v.getPointSetClone());
        int[] indices = new int[VolumeUtil.numPoints(v)];
        int pointNum = 0;
        for(int facetNum = 0; facetNum < v.numFacets(); facetNum++) {
            int[] facetInds = v.getFacetInds(facetNum);
            assert(facetInds.length == 3);//should throw error here
            for(int i = 0; i < facetInds.length; i++) {
                indices[pointNum++] = facetInds[i];
            }
        }
        
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
        mesh.updateBound();//can't remember if this is still supposed to be used.
        return mesh;
    }
    
    public static LineCloud createLineCloudWireframe(AssetManager assetManager, Volume v) {
        int numPoints = VolumeUtil.numPoints(v);
        Vector3f[] volStarts = new Vector3f[numPoints];
        Vector3f[] volEnds = new Vector3f[numPoints];
        int pointNum = 0;
        for(int i = 0; i < v.numFacets(); i++) {
            Facet f = v.getFacet(i);
            for(int j = 0; j < f.numPoints(); j++) {
                volStarts[pointNum] = JblasJMEConverter.toVector3f(v.getFacet(i).getPointClones(j))[0];
                volEnds[pointNum++] = JblasJMEConverter.toVector3f(v.getFacet(i).getPointClones((j+1)%f.numPoints()))[0];
            }
        }
        return new LineCloud(assetManager, volStarts, volEnds);
    }
}
