/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * <p>All rights reserved.
 *
 * <p>This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Apr 24, 2008
 */
package edu.uci.ics.jung.visualization.picking;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;

/**
 * A <code>NetworkElementAccessor</code> that finds the closest element to the pick point, and
 * returns it if it is within the element's shape. This is best suited to elements with convex
 * shapes that do not overlap. It differs from <code>ShapePickSupport</code> in that it only checks
 * the closest element to see whether it contains the pick point. Possible unexpected odd behaviors:
 *
 * <ul>
 *   <li>If the elements overlap, this mechanism may pick another element than the one that's "on
 *       top" (rendered last) if the pick point is closer to the center of an obscured vertex.
 *   <li>If element shapes are not convex, then this mechanism may return <code>null</code> even if
 *       the pick point is inside some element's shape, if the pick point is closer to the center of
 *       another element.
 * </ul>
 *
 * Users who want to avoid either of these should use <code>ShapePickSupport</code> instead, which
 * is slower but more flexible. If neither of the above conditions (overlapping elements or
 * non-convex shapes) is true, then <code>ShapePickSupport</code> and this class should have the
 * same behavior.
 */
public class ClosestShapePickSupport<N, E> implements NetworkElementAccessor<N, E> {

  protected VisualizationServer<N, E> vv;
  protected float pickSize;

  /**
   * Creates a <code>ShapePickSupport</code> for the <code>vv</code> VisualizationServer, with the
   * specified pick footprint. The <code>VisualizationServer</code> is used to fetch the current
   * <code>Layout</code>.
   *
   * @param vv source of the current <code>Layout</code>.
   * @param pickSize the layoutSize of the pick footprint for line edges
   */
  public ClosestShapePickSupport(VisualizationServer<N, E> vv, float pickSize) {
    this.vv = vv;
    this.pickSize = pickSize;
  }

  /**
   * Create a <code>ShapePickSupport</code> with the <code>vv</code> VisualizationServer and default
   * pick footprint. The footprint defaults to 2.
   *
   * @param vv source of the current <code>Layout</code>.
   */
  public ClosestShapePickSupport(VisualizationServer<N, E> vv) {
    this.vv = vv;
  }

  @Override
  public E getEdge(LayoutModel<N, Point2D> layoutModel, double x, double y) {
    return null;
  }

  @Override
  public N getNode(LayoutModel<N, Point2D> layoutModel, double x, double y) {
    VisualizationModel<N, E, Point2D> visualizationModel = vv.getModel();
    //    LayoutModel<N, Point2D> layoutModel = visualizationModel.getLayoutModel();
    // first, find the closest vertex to (x,y)
    double minDistance = Double.MAX_VALUE;
    N closest = null;
    while (true) {
      try {
        for (N v : visualizationModel.getNetwork().nodes()) {
          Point2D p = layoutModel.apply(v);
          double dx = p.getX() - x;
          double dy = p.getY() - y;
          double dist = dx * dx + dy * dy;
          if (dist < minDistance) {
            minDistance = dist;
            closest = v;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    // now check to see whether (x,y) is in the shape for this vertex.

    // get the vertex shape
    Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(closest);
    // get the vertex location
    Point2D p = layoutModel.apply(closest);
    // transform the vertex location to screen coords
    p =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, (Point2D) p.clone());

    double ox = x - p.getX();
    double oy = y - p.getY();

    if (shape.contains(ox, oy)) {
      return closest;
    } else {
      return null;
    }
  }

  @Override
  public Collection<N> getNodes(LayoutModel<N, Point2D> layoutModel, Shape rectangle) {
    // FIXME: RadiusPickSupport and ShapePickSupport are not using the same mechanism!
    // talk to Tom and make sure I understand which should be used.
    // in particular, there are some transformations that the latter uses; the latter is also
    // doing a couple of kinds of filtering.  (well, only one--just predicate-based.)
    // looks to me like the VV could (should) be doing this filtering.  (maybe.)
    //
    return null;
  }
}
