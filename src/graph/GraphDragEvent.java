/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import main.Utility;
import org.graphstream.graph.Graph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.View;

/**
 *
 * @author t.fotakis
 */
public class GraphDragEvent extends MouseInputAdapter  {

    private final Camera camera;
    private final Graph graph;
    private final View view;
    private int x;
    private int y;
    private GraphicElement curElement;
    
    public GraphDragEvent(Camera camera,Graph graph,View view) {
        this.camera = camera;
        this.graph = graph;
        this.view = view;
        this.curElement = null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        
        curElement = view.findNodeOrSpriteAt(e.getX(), e.getY());
        if (curElement != null) {
            mouseButtonPressOnElement(curElement, e);
	}
    }
    
    protected void mouseButtonPressOnElement(GraphicElement element, MouseEvent event) {
	view.freezeElement(element, true);
	if (event.getButton() == 3) {
            element.addAttribute("ui.selected");
	}
        else {
            element.addAttribute("ui.clicked");
	}
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        x = 0;
        y = 0;
        if (curElement != null) {
            mouseButtonReleaseOffElement(curElement, e);
            curElement = null;
	}
    }
    
    protected void mouseButtonReleaseOffElement(GraphicElement element, MouseEvent event) {
	view.freezeElement(element, false);
	if (event.getButton() != 3) {
            element.removeAttribute("ui.clicked");
	}
    }
    
    @Override
    public void mouseClicked(MouseEvent e){
        if (SwingUtilities.isRightMouseButton(e)) {
            int fx = e.getX();
            int fy = e.getY();
            
            //Get mouse absolute location
            Point absolute = e.getLocationOnScreen();
            
            //Get current center location
            Point3 the_center = camera.getViewCenter();
            Point3 center_pxl = camera.transformGuToPx(the_center.x, the_center.y, the_center.z);
            
            //Set new center
            Point3 new_center = camera.transformPxToGu(fx, fy);
            camera.setViewCenter(new_center.x, new_center.y, new_center.z);
           
            //Find new center absolute location
            Double d;
                
            d = center_pxl.x;
            int new_x = absolute.x - fx + d.intValue();
            d = center_pxl.y;
            int new_y = absolute.y - fy + d.intValue();
            
            //Move mouse
            Robot robot;
            try {
                robot = new Robot();
                robot.mouseMove(new_x,new_y);
            } catch (AWTException ex) {
                //ex.printStackTrace(System.err);
                Utility.err.printStackTrace(ex);
            }
        }
        else if (e.getClickCount() == 2){
            String style = graph.getAttribute("ui.stylesheet");
            if (style.equals(GraphTab.STYLE1)) {
                graph.changeAttribute("ui.stylesheet", GraphTab.STYLE2);
            }
            else {
                graph.changeAttribute("ui.stylesheet", GraphTab.STYLE1);
            }
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        
        if (curElement != null) {
            elementMoving(curElement, e);
	}
        else if (SwingUtilities.isLeftMouseButton(e)) {
        
            int fx = e.getX();
            int fy = e.getY();
        
            //fx = Math.min(panel.getWidth(),fx);
            //fx = Math.max(0, fx);
        
            //fy = Math.min(panel.getHeight(),fy);
            //fy = Math.max(0, fy);
        
            Double ratio = camera.getMetrics().ratioPx2Gu;
            Point3 current_center = camera.getViewCenter();
        
            camera.setViewCenter(current_center.x + ( - fx + x)/ratio,current_center.y + ( - y + fy)/ratio,current_center.z);
        
            x = e.getX();
            y = e.getY();
        }
    }
    
    protected void elementMoving(GraphicElement element, MouseEvent event) {
        view.moveElementAtPx(element, event.getX(), event.getY());
    }

}
