
package mygame.volumetrics.surfaceextraction;

import mygame.util.HashUtil;
import org.jblas.DoubleMatrix;

public class NetCoord {
    protected final int[] NET_COORDS;
    
    public NetCoord(int... cubeInds) {
        this.NET_COORDS = cubeInds;
    }
    
    public NetCoord add(int... coords) {
        assert(coords.length == NET_COORDS.length);
        int[] newCoords = new int[coords.length];
        for(int i = 0; i < newCoords.length; i++) {
            newCoords[i] = coords[i]+NET_COORDS[i];
        }
        return new NetCoord(newCoords);
    }
    
    @Override
    public int hashCode() {
        return HashUtil.simpleIntArrHashCode(NET_COORDS);
    }

    @Override
    public boolean equals(Object n2) {
        if(!(n2 instanceof NetCoord)) return false;
        NetCoord compareCoord = (NetCoord)n2;
        for(int i = 0; i < NET_COORDS.length; i++) {
            if(compareCoord.NET_COORDS[i] != NET_COORDS[i]) return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Net Coord: [" + Integer.toString(NET_COORDS[0]) + "," + Integer.toString(NET_COORDS[1]) + "," + Integer.toString(NET_COORDS[2]) + "]";
    }
}
