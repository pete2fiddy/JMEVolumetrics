package mygame;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import mygame.util.RandomUtil;

public class Test {
    
    protected static Vector3f[] generateCubesVec3f(int nPointsPerCube, Vector3f[] centers, float[] radiuses) {
        Vector3f[] out = new Vector3f[nPointsPerCube*centers.length];
        for(int cubeNum = 0; cubeNum < centers.length; cubeNum++) {
            Vector3f[] cube = generateCubeVec3f(nPointsPerCube, centers[cubeNum], radiuses[cubeNum]);
            for(int i = 0; i < cube.length; i++) {
                out[cubeNum*nPointsPerCube + i] = cube[i];
            }
        }
        return out;
    }
    
    protected static Vector3f[] generateCubeVec3f(int nPoints, Vector3f center, float radius) {
        float halfRadius = radius;///2f;
        Matrix3f cubeBasises = new Matrix3f(halfRadius, 0f, 0f,
        0f, halfRadius, 0f,
        0f, 0f, halfRadius);
        Vector3f[] points = new Vector3f[nPoints];
        
        for(int i = 0; i < nPoints; i++) {
            
            float[][] mixMatArr = new float[3][3];
            int fixedSide = (int)(Math.random()*3);
            for(int j = 0; j < mixMatArr.length; j++){
                if(j == fixedSide) {
                    mixMatArr[j][j] = (float)((Math.random() < 0.5)? -1:1);
                } else {
                    mixMatArr[j][j] = (float)(2*(Math.random()-.5));
                }
            }
            Matrix3f mixMat = new Matrix3f();
            mixMat.set(mixMatArr);
            mixMat = mixMat.mult(cubeBasises);
            Vector3f point = center;
            for(int j = 0; j < 3; j++) {
                point = point.add(mixMat.getRow(j));
            }
            points[i] = point;
        }
        return points;
    }
    
    protected static Vector3f[] generateSpheresVec3f(int nPointsPerSphere, Vector3f[] centers, float[] radiuses) {
        Vector3f[] out = new Vector3f[nPointsPerSphere*centers.length];
        for(int sphereNum = 0; sphereNum < centers.length; sphereNum++) {
            Vector3f[] points = generateSphereVec3f(nPointsPerSphere, centers[sphereNum], radiuses[sphereNum]);
            for(int i = 0; i < nPointsPerSphere; i++){
                out[sphereNum*nPointsPerSphere + i] = points[i];
            }
        }
        return out;
    }
    
    protected static Vector3f[] generateFilledCubeVec3f(int nPoints, Vector3f center, float r) {
        Vector3f[] out = new Vector3f[nPoints];
        for(int i = 0; i < out.length; i++) {
            out[i] = new Vector3f((float)(2*(Math.random()-.5)*r), (float)(2*(Math.random()-.5)*r), (float)(2*(Math.random()-.5)*r));
            out[i] = out[i].add(center);
        }
        return out;
    }
    
    protected static Vector3f[] generateFilledSphereVec3f(int nPoints, Vector3f center, float r) {
        Vector3f[] out = new Vector3f[nPoints];
        for(int i = 0; i < nPoints; i++) {
            out[i] = new Vector3f();
            float z = (float)(2*(Math.random()-.5));
            float rHeight = (float)Math.sqrt((1 - z*z));
            float theta = (float)(2*Math.PI*Math.random());
            out[i].setX((float)(rHeight * Math.sin(theta)));
            out[i].setY((float)(rHeight * Math.cos(theta)));
            out[i].setZ(z);
            out[i] = out[i].mult((float)(Math.random()*r));
            out[i] = out[i].add(center);
        }
        return out;
    }
    
    protected static Vector3f[] generateFilledHyperboloidVec3f(int nPoints, Vector3f center, float r) {
        Vector3f[] out = new Vector3f[nPoints];
        for(int i = 0; i < out.length; i++) {
            float iterR = (float)(Math.random() * r);
            double theta1 = Math.random()*Math.PI*2;
            double theta2 = Math.random()*Math.PI*2;
            out[i] = new Vector3f((float)(Math.cos(theta1)), (float)(Math.sin(theta1)), (float)(Math.cos(theta2)));
            out[i] = out[i].normalize();
            out[i] = out[i].mult(iterR);
            out[i] = out[i].add(center);
        }
        return out;
    }
    
    protected static Vector3f[] generateSphereVec3f(int nPoints, Vector3f center, float r) {
        Vector3f[] out = new Vector3f[nPoints];
        for(int i = 0; i < nPoints; i++) {
            out[i] = new Vector3f();
            float z = (float)(2*r*(Math.random()-.5));
            float rHeight = (float)Math.sqrt((r*r - z*z));
            float theta = (float)(2*Math.PI*Math.random());
            out[i].setX((float)(rHeight * Math.sin(theta)));
            out[i].setY((float)(rHeight * Math.cos(theta)));
            out[i].setZ(z);
            out[i] = out[i].add(center);
        }
        return out;
    }
    
    protected static Vector3f[] generateRandomShapes(int shapePoints, int nSpheres, int nCubes) {
        double[][] range = {{-10,10},{0,0}, {-10,10}};
        double[] radiusBounds = {.5, 2};
        
        Vector3f[] out = new Vector3f[shapePoints*(nSpheres+nCubes)];
        int outInd = 0;
        for(int i = 0; i < nSpheres; i++) {
            Vector3f[] spherePoints = Test.generateSphereVec3f(shapePoints, 
                    new Vector3f((float)RandomUtil.generateInRange(range[0]),
                    (float)RandomUtil.generateInRange(range[1]),
                    (float)RandomUtil.generateInRange(range[2])), 
                    (float)RandomUtil.generateInRange(radiusBounds));
            for(int j = 0; j < spherePoints.length; j++) {
                out[outInd++] = spherePoints[j];
            }
        }
        for(int i = 0; i < nCubes; i++) {
            Vector3f[] cubePoints = Test.generateCubeVec3f(shapePoints,
                    new Vector3f((float)RandomUtil.generateInRange(range[0]),
                    (float)RandomUtil.generateInRange(range[1]),
                    (float)RandomUtil.generateInRange(range[2])), 
                    (float)RandomUtil.generateInRange(radiusBounds));
            for(int j = 0; j < cubePoints.length; j++) {
                out[outInd++] = cubePoints[j];
            }
        }
        return out;
    }
}
