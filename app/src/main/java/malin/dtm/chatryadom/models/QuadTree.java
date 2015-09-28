package malin.dtm.chatryadom.models;

import java.util.ArrayList;
import java.util.List;

import malin.dtm.chatryadom.models.coordinates.Cartesian;
import malin.dtm.chatryadom.models.coordinates.Spherical;
import malin.dtm.chatryadom.utils.CommonUtil;

/**
 * Created by dmt on 14.09.2015.
 */
public class QuadTree {
    private int depth;

    private Spherical latLon;
    private Cartesian xy;
    private String quad;

    public QuadTree(double lat, double lon, int depth) {
        this.latLon = new Spherical(lat, lon);
        this.xy = latLon.toCartesian();
        this.depth = depth;
        this.quad = "";
    }

    public String getPointIndex() {
        return getIndex();
    }

    public String getQuadIndex(int accuracy) {
        return getIndex().substring(0, accuracy);
    }

    public Spherical getLatLon() {
        return latLon;
    }

    public Cartesian getXy() {
        return xy;
    }

    private String getIndex() {
        if(quad != null && !quad.isEmpty())
            return quad;
        else {
            long roundX = Math.round(xy.getX());
            long roundY = Math.round(xy.getY());
            return encode(roundX, roundY);
        }
    }

    public String encode(long x, long y) {
        List<Integer> arr = new ArrayList<>();
        for(int i = depth; i > 0; i--) {
            int pow = 1 << (i - 1);
            int cell = 0;
            if ((x&pow) != 0) cell += 1;
            if ((y&pow) != 0) cell += 2;
            arr.add(cell);
        }
        return CommonUtil.joinArray((ArrayList) arr);
    }
}
