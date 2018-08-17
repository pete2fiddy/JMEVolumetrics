package mygame.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FileUtil {
    public static BufferedReader loadToReader(String path) throws FileNotFoundException {
        return new BufferedReader(new FileReader(path));
    }
}
