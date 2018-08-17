package mygame.model.data.save;

import com.jme3.math.Vector3f;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/*
Saves a set of vectors in a format readable by SimpleCloudLoader
*/
public class SimpleCloudSaver {
    
    public static void save(Vector3f[] vecs, String path) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(path);
        for(Vector3f vec : vecs) {
            writeVec(writer, vec);
        }
        writer.close();
    }
    
    private static void writeVec(PrintWriter writer, Vector3f vec) {
        writer.println(Float.toString(vec.x) + " " + Float.toString(vec.y) + " " + Float.toString(vec.z));
    }
}
