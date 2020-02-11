/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.Viewer;

/**
 *
 * @author t.fotakis
 */
public class GraphTab {
    
    protected static final String STYLE1 = 
            "node {\n" +
            "	size: 5px;\n" +
            "	fill-color: #1a0dab;\n" +
            "	text-mode: hidden;\n" +
            "	z-index: 0;\n" +
            "}\n" +
            "\n" +
            "edge {\n" +
            "	shape: line;\n" +
            "	fill-color: #222;\n" +
            "}";
    
    protected static final String STYLE2 = 
            "node {\n" +
            "	size: 5px;\n" +
            "	fill-color: #1a0dab;\n" +
            "	text-mode: normal;\n" +
            "	z-index: 0;\n" +
            "}\n" +
            "\n" +
            "edge {\n" +
            "	shape: line;\n" +
            "	fill-color: #222;\n" +
            "}";
    
    private final Graph graph;
    private final Camera camera;
    private final Viewer viewer;
    private String path;
    private String name;
    private Boolean enabled;
    
    public GraphTab(String name, javax.swing.JPanel panel) {
        
        this.graph = new SingleGraph(name);
        
        //-----------------------------
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");
        this.graph.addAttribute("ui.stylesheet", STYLE1);
        //-----------------------------
        
        this.viewer = new Viewer(this.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        ViewPanel viewP = this.viewer.addDefaultView(false);
        this.camera = viewP.getCamera();
        
        panel.setLayout(new BorderLayout());
        panel.add(viewP, BorderLayout.CENTER);
        this.viewer.enableAutoLayout();
        this.enabled = true;
        
        MouseListener[] mouseListeners = viewP.getMouseListeners();
        for (MouseListener ml : mouseListeners) {
            viewP.removeMouseListener(ml);
        }
        MouseMotionListener[] mouseMListeners = viewP.getMouseMotionListeners();
        for (MouseMotionListener ml : mouseMListeners) {
            viewP.removeMouseMotionListener(ml);
        }
        
        
        GraphMouseWheelEvent wheel = new GraphMouseWheelEvent(this.camera);
        GraphDragEvent drag = new GraphDragEvent(this.camera,this.graph,this.viewer.getView("defaultView"));
        
        viewP.addMouseWheelListener(wheel);
        viewP.addMouseListener(drag);
        viewP.addMouseMotionListener(drag);
        
    }
    
    public void default_graph_options(boolean reset) {
        if (reset) {
            this.camera.setViewPercent(1.0);
            this.camera.setViewCenter(0.0,0.0,0.0);
            this.graph.clear();
            this.camera.resetView();
        }
        
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");
        this.graph.addAttribute("ui.stylesheet", STYLE1);
    }
    
    public void save_graph_coordinates() throws FileNotFoundException, UnsupportedEncodingException {
        
        GraphicGraph gg = this.viewer.getGraphicGraph();
        //Iterator it = gg.getEachNode().iterator();
        Iterator it = gg.getNodeIterator();
        GraphicNode gn;
        
        PrintWriter file = new PrintWriter(this.path + File.separator + this.name + ".coordinates.txt", "UTF-8");

        file.write("");
        
        while (it.hasNext()) {
            gn = (GraphicNode) it.next();
            file.append(gn + "\t" + gn.getX() + "\t" + gn.getY()+"\n");
        }
        
        //(VALUE - REAL_MIN)*(TARGET_MAX-TARGET_MIN)/(REAL_MAX-REAL_MIN)+TARGET_MIN
        
        file.close();
            
    }

    public void read(String filename) throws GraphParseException, ElementNotFoundException, IOException {
        this.graph.read(filename);
        
        this.viewer.enableAutoLayout();
        this.enabled = true;
        
        File f = new File(filename);
        this.name = f.getName();
        this.path = f.getParent();
        
        for (Node node : this.graph.getEachNode()) {
            node.addAttribute("ui.label", node.toString());
        }
    }

    public void addNode(String nodename) {
        Node n;
        n = this.graph.addNode(nodename);
        n.addAttribute("ui.label", nodename);
    }

    public void addEdge(String id, String node1, String node2) {
        this.graph.addEdge(id, node1, node2);
    }

    public void write() throws IOException {
        this.graph.write(this.path + File.separator + this.name);
    }

    public void setPath(String path, String name) {
        this.path = path;
        this.name = name;
        this.viewer.enableAutoLayout();
        this.enabled = true;
    }

    public void start_stop() {
        if (this.enabled) {
            this.viewer.disableAutoLayout();
        }
        else {
            this.viewer.enableAutoLayout();
        }
        this.enabled = !this.enabled;
    }
    
}
