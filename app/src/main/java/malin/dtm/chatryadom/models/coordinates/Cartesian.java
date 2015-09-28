package malin.dtm.chatryadom.models.coordinates;

/**
 * Created by dmt on 14.09.2015.
 */
public class Cartesian extends Coordinate2D{
    public Cartesian(double x, double y) {
        super(x, y);
    }
    public double getX() {
        return super.getCoordinate1();
    }
    public double getY() {
        return super.getCoordinate2();
    }
}
