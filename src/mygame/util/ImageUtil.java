package mygame.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtil {
    
    public static BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }
    
    /*
    assumes the red channel is the grayscale channel (since they should all be equal anyway)
    */
    public static double[][] toArrGrayscale(BufferedImage img) {
        double[][] out = new double[img.getHeight()][img.getWidth()];
        for(int i = 0; i < out.length; i++) {
            for(int j = 0; j < out[i].length; j++) {
                Color c = new Color(img.getRGB(j, i));
                out[i][j] = c.getRed();
            }
        }
        return out;
    }
    
    public static double[][][] toRGBArr(BufferedImage img) {
        double[][][] out = new double[img.getHeight()][img.getWidth()][3];
        for(int x = 0; x < img.getWidth(); x++) {
            for(int y = 0; y < img.getHeight(); y++) {
                Color c = new Color(img.getRGB(x,y));
                out[y][x][0] = c.getRed();
                out[y][x][1] = c.getGreen();
                out[y][x][2] = c.getBlue();
            }
        }
        return out;
    }
    
    /*
    https://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
    */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
