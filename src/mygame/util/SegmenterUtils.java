package mygame.util;

import com.jme3.math.Vector3f;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Owner
 */
public class SegmenterUtils {
 
    
    public static Set<Integer>[] convertIntoClusterSets(Map<Integer, Integer> idToClusterMap){
        int numClusters = Collections.max(idToClusterMap.values())+1;
        Set<Integer>[] clusterSets = new HashSet[numClusters];
        for(int i = 0; i < clusterSets.length; i++) {
            clusterSets[i] = new HashSet<Integer>();
        }
        for(Integer id : idToClusterMap.keySet()) {
            clusterSets[idToClusterMap.get(id)].add(id);
        }
        return clusterSets;
    }
    
    public static <D> Set<D>[] convertIntoClusterVectorSets(D[] X, Map<Integer, Integer> idToClusterMap, int nClusters) {
        HashSet<D>[] out = new HashSet[nClusters];
        for(int i = 0; i < out.length; i++) {
            out[i] = new HashSet<D>();
        }
        for(int i = 0; i < X.length; i++) {
            out[idToClusterMap.get(i)].add(X[i]);
        }
        return out;
    }
}
