package mygame.model.volumetrics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.jblas.DoubleMatrix;


/*
See: http://www.cs.cornell.edu/courses/cs5670/2017sp/lectures/lec13_stereo.pdf
*/
public class SimpleStereoReconstruction {
    
    public static DoubleMatrix extractPointCloud(double[][][] leftImg, double[][][] rightImg, int windowSize, double focalLength, double baseline, double disparityOffset, int maxDisparity) {
        //TODO: add smoothness cost
        int halfWindow = (windowSize-1)/2;
        double focalLengthTimesBaseline = focalLength*baseline;
        DoubleMatrix out = DoubleMatrix.zeros((leftImg.length-2*halfWindow)*(leftImg[0].length-2*halfWindow), 3);
        int pointNum = 0;
        for(int x = halfWindow; x < leftImg[0].length - halfWindow; x++) {
            for(int y = halfWindow; y < leftImg.length - halfWindow; y++) {
                double[] disparityAndCost = getDisparityAndCost(leftImg, rightImg, x, y, halfWindow, maxDisparity);
                double depth = focalLengthTimesBaseline/(disparityAndCost[0]+disparityOffset);
                double xPoint = ((double)x)*depth/focalLength;
                double yPoint = ((double)(leftImg.length-y))*depth/focalLength;
                out.putRow(pointNum++, new DoubleMatrix(new double[][]{{xPoint, yPoint, depth}}));
            }
        }
        return out;
    }
    
    private static double[] getDisparityAndCost(double[][][] leftImg, double[][][] rightImg, int leftX, int leftY, int halfWindow, int maxDisparity) {
        double bestCost = Double.MAX_VALUE;
        int bestInd = -1;
        /*
        for(int rightX = halfWindow; rightX < rightImg[leftY].length-halfWindow; rightX++){
            double windowCost = getWindowCost(leftImg, rightImg, leftX, leftY, rightX, leftY, halfWindow);
            if(windowCost < bestCost) {
                bestCost = windowCost;
                bestInd = rightX;
            }
        }
        */
        //if an image is taken on the right side, the same point will be farther to the left
        for(int rightX = leftX; rightX >= ((leftX >= maxDisparity)? leftX - maxDisparity + halfWindow:halfWindow); rightX--) {
            double windowCost = getWindowCost(leftImg, rightImg, leftX, leftY, rightX, leftY, halfWindow);
            if(windowCost < bestCost) {
                bestCost = windowCost;
                bestInd = rightX;
            }
        }
        return new double[]{Math.abs(bestInd - leftX), bestCost};
    }
    
    
    private static double getWindowCost(double[][][] leftImg, double[][][] rightImg, int leftX, int leftY, int rightX, int rightY, int halfWindow) {
        //attempt to replace with normalized cross corellation for better results
        
        double out = 0;
        for(int dx = -halfWindow; dx < halfWindow+1; dx++) {
            for(int dy = -halfWindow; dy < halfWindow+1; dy++) {
                int lx = leftX+dx;
                int ly = leftY+dy;
                int rx = rightX+dx;
                int ry = rightY+dy;
                for(int k = 0; k < leftImg[ly][lx].length; k++) {
                    double sub = leftImg[ly][lx][k]-rightImg[ry][rx][k];
                    out += sub*sub;
                }
            }
        }
        return out;
    }
    
    /*
    public static DoubleMatrix extractPointCloud(BufferedImage leftImg, BufferedImage rightImg, int windowSize, double focalLength, double baseline, double disparityOffset, double windowCostThreshold) {
        int halfWindow = (windowSize-1)/2;
        double focalLengthTimesBaseline = focalLength*baseline;
        DoubleMatrix out = DoubleMatrix.zeros((leftImg.getWidth()-2*halfWindow)*(leftImg.getHeight()-2*halfWindow), 3);
        int pointNum = 0;
        double costSum = 0;
        for(int x = halfWindow; x < leftImg.getWidth() - halfWindow; x++) {
            for(int y = halfWindow; y < leftImg.getHeight() - halfWindow; y++) {
                double[] disparityAndCost = getDisparityAndCost(leftImg, rightImg, x, y, halfWindow);
                costSum += disparityAndCost[1];
                if(disparityAndCost[1] < windowCostThreshold) {
                    double depth = focalLengthTimesBaseline/(disparityAndCost[0]+disparityOffset);
                    double xPoint = ((double)x)*depth/focalLength;
                    double yPoint = ((double)(leftImg.getHeight()-y))*depth/focalLength;
                    out.putRow(pointNum++, new DoubleMatrix(new double[][]{{xPoint, yPoint, depth}}));
                }
            }
        }
        System.out.println("average cost: " + (costSum/(double)out.rows));
        return out;
    }
    
    
    //in the order of [disparity, cost]
    private static double[] getDisparityAndCost(BufferedImage leftImg, BufferedImage rightImg, int leftX, int leftY, int halfWindow) {
        double bestCost = Double.MAX_VALUE;
        int bestInd = -1;
        for(int rightX = halfWindow; rightX < rightImg.getWidth()-halfWindow; rightX++){
            double windowCost = getWindowCost(leftImg, rightImg, leftX, leftY, rightX, leftY, halfWindow);
            if(windowCost < bestCost) {
                bestCost = windowCost;
                bestInd = rightX;
            }
        }
        return new double[]{Math.abs(bestInd - leftX), bestCost};
    }
    
    private static double getWindowCost(BufferedImage leftImg, BufferedImage rightImg, int leftX, int leftY, int rightX, int rightY, int halfWindow) {
        double out = 0;
        for(int dx = -halfWindow; dx < halfWindow+1; dx++) {
            for(int dy = -halfWindow; dy < halfWindow+1; dy++) {
                double[] sub = subtractColors(new Color(leftImg.getRGB(leftX+dx, leftY+dy)), new Color(rightImg.getRGB(rightX+dx, rightY+dy)));
                for(double d : sub) {
                    out += d*d;
                }
            }
        }
        return out;
    }
    
    private static double[] subtractColors(Color c1, Color c2) {
        return new double[] {c2.getRed()-c1.getRed(), c2.getGreen()-c1.getGreen(), c2.getBlue()-c1.getBlue()};
    }
    */
}
