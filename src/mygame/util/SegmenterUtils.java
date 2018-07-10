package mygame.util;

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
}
