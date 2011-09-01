package evoker;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;

/**
 * Implements lasso select.  USE ONLY WITH PLOT DIAGRAMS.
 */
public class Lasso {

    /** Representing the enclosed area*/
    private Polygon poly;
    /** To later hold all ChartEntitys within the polygon after they've been 
     * calculated once, to not have to search them again. */
    private EntityCollection ec = null;

    public Lasso() {
        poly = new Polygon();
    }

    /**
     * 
     * @param entityCollection containing all ChartEntity-Objects to be searched through 
     * @return EntityCollection containing all ChartEntity-Object within the borders of the selection
     */
    public EntityCollection getContainedEntitys(EntityCollection entityCollection) {
        if (ec == null) {
            ec = new StandardEntityCollection();
            Collection entities = entityCollection.getEntities();
            for (int i = 0; i < entities.size(); i++) {
                ChartEntity entity = entityCollection.getEntity(i);
                if (entity.getToolTipText() != null && "poly".equals(entity.getShapeType())) {  // get sure (?) we only get data-points
                    Point p = getScreenCoordinatesOfEntity(entity);
                    if (poly.contains(p)) {
                        ec.add(entity);
                    }
                }
            }
        }
        return ec;
    }
    
    /**
     * Returns the previously calculated entities.  If 
     * <code>getContainedEntitys(EntityCollection entityCollection)</code> 
     * hasn't been called beforehand, it'll return <code>null</code>
     * 
     * @return EntityCollection
     */
    public EntityCollection getContainedEntitys() {
        return ec;
    }
    
    /**
     * Reads out all diagram-coordinates of the points.
     * 
     * @param entityCollection containing all ChartEntity-Objects to be searched through 
     * @return ArrayList containing 
     */
    public ArrayList<EvokerPoint2D> getContainedPoints(EntityCollection entityCollection) {
        ArrayList<EvokerPoint2D> al_ret = new ArrayList<EvokerPoint2D>();
        getContainedEntitys(entityCollection);
        Collection entities = ec.getEntities();
        for (int i = 0; i < entities.size(); i++) {
            ChartEntity entity = ec.getEntity(i);
            al_ret.add(getCoordinatesOfEntity(entity));
        }
        return al_ret;
    }
    
    /**
     * Reads out all diagram-coordinates of the points.
     * 
     * @param entityCollection containing all ChartEntity-Objects to be searched through 
     * @return ArrayList containing 
     */
    public HashMap<EvokerPoint2D, String> getContainedPointsInd(EntityCollection entityCollection) {
        HashMap<EvokerPoint2D, String> hm_ret = new HashMap<EvokerPoint2D, String>();
        getContainedEntitys(entityCollection);
        Collection entities = ec.getEntities();
        for (int i = 0; i < entities.size(); i++) {
            ChartEntity entity = ec.getEntity(i);
            hm_ret.put(getCoordinatesOfEntity(entity), getIndOfEntity(entity));
        }
        return hm_ret;
    }

    /**
     * Returns the (screen-relative) coordinates of an entity
     * @param entity object
     * @return Point
     */
    public Point getScreenCoordinatesOfEntity(ChartEntity e) {
        String shapeCoords = e.getShapeCoords();
        String[] shapeCoords_array = shapeCoords.split(",");

        // I decided that these points are most like the center of the circle-area
        return new Point(
                Integer.parseInt(shapeCoords_array[2]),
                Integer.parseInt(shapeCoords_array[1]));
    }

    /**
     * Returns the (Diagram-relative) coordinates of a point
     * @param entity-object
     * @return Point (containing the coordinates)
     */
    public EvokerPoint2D getCoordinatesOfEntity(ChartEntity e) {
        String tooltip = e.getToolTipText();
        if (tooltip == null) {
            return null;
        }
        return new EvokerPoint2D(
                Double.parseDouble(
                tooltip.substring(
                tooltip.indexOf('(') + 1, tooltip.indexOf(','))),
                Double.parseDouble(
                tooltip.substring(
                tooltip.indexOf(',') + 1,
                tooltip.indexOf(')'))));
    }

    /**
     * Returns ind of a ChartEntity
     * @param entity object
     * @return the ind
     */
    public String getIndOfEntity(ChartEntity e) {
        String tooltip = e.getToolTipText();
        return tooltip.substring(0, tooltip.indexOf("(") - 1);
    }

    /**
     * Adds a point to the polygon
     * @param x coordinate
     * @param y coordinate
     */
    public void addPoint(int x, int y) {
        poly.addPoint(x, y);
    }
    
    /**
     * Returns the number of Points the Polygon is made of
     * @return 
     */
    public int getNumberOfEdges(){
        return poly.npoints;
    }
}
