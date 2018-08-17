package mygame.model.data.load;

import com.jme3.math.Vector3f;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import mygame.util.FileUtil;

/*
    Assumes the file is of the format of:
    
    pointx pointy pointz
    pointx pointy pointz
    ...
    */
public class SimpleCloudLoader {
    private static final String COMPONENT_SEGMENT_TOKEN = " ";
    
    public static Vector3f[] load(String path) throws FileNotFoundException, IOException {
        BufferedReader reader = FileUtil.loadToReader(path);
        List<Vector3f> vecs = new LinkedList<Vector3f>();
        String line = null;
        while((line = reader.readLine()) != null) {
            vecs.add(parseVec(line));
        }
        return vecs.toArray(new Vector3f[vecs.size()]);
    }
    
    private static Vector3f parseVec(String vec) {
        Vector3f out = new Vector3f();
        int start = 0;
        int end = vec.indexOf(COMPONENT_SEGMENT_TOKEN, start);
        out.x = Float.parseFloat(vec.substring(start, end));
        start = end+1;
        end = vec.indexOf(COMPONENT_SEGMENT_TOKEN, start);
        out.y = Float.parseFloat(vec.substring(start, end));
        start = end+1;
        out.z = Float.parseFloat(vec.substring(start));
        return out;
    }
    
}
