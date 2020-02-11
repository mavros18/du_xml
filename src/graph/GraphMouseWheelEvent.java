/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import org.graphstream.ui.view.Camera;

/**
 *
 * @author t.fotakis
 */
public class GraphMouseWheelEvent implements MouseWheelListener {
    
    private static final int SCROLL_MULTIPLIER = 3;
    
    private final Camera camera;
    
    public GraphMouseWheelEvent(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Double notches = 0.0 + e.getWheelRotation()*SCROLL_MULTIPLIER;
        notches = camera.getViewPercent() + notches/100;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            if (notches > 0) {
                camera.setViewPercent(notches);
            }
        }
    } 
}
