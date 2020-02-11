/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_xml;

import du_objects.connection_obj;
import du_objects.node_obj;
import du_objects.task_obj;
import graph.GraphTab;
import java.io.IOException;
import java.util.ArrayList;
import main.Utility;

/**
 *
 * @author t.fotakis
 */
public final class du_xml_graph {
    
    private du_xml_graph() { }
    
    public static void run(ArrayList<node_obj> final_data,String path, GraphTab g) throws IOException {

        ArrayList<String> nodes = new ArrayList<>();

        ArrayList<String[]> connections = new ArrayList<>();

        String myname;

        for (node_obj it : final_data) {
            for ( task_obj tt : it.mytasks) {
                myname = it.name+"!"+tt.name;
                for (connection_obj cc : tt.concon) {
                    if (!myname.equals(cc.node+"!"+cc.name)) {
                        if (is_not_contained(connections,myname,cc.node+"!"+cc.name)) {
                            connections.add(new String[]{myname,cc.node+"!"+cc.name});
                        }
                    }
                }
                for (connection_obj cc : tt.concon_estimate) {
                    // this can add a connectoin already added from concon
                    if (!myname.equals(cc.node+"!"+cc.name)) {
                        if (is_not_contained(connections,myname,cc.node+"!"+cc.name)) {
                            connections.add(new String[]{myname,cc.node+"!"+cc.name});
                        }
                    }
                }
            }
        }
        
        // remove target-only tasks from free group
        for (String[] ss : connections) {
            if (!nodes.contains(ss[1])) {
                nodes.add(ss[1]);
            }
            if (!nodes.contains(ss[0])) {
                nodes.add(ss[0]);
            }
        }
        
        Utility.out.println("Total tasks (graph-nodes) in graph : "+nodes.size());
        
        for (String s : nodes) {
            g.addNode(s);
        }
        
        for (String[] ss : connections) {
            g.addEdge(ss[0]+" :: "+ss[1], ss[0], ss[1]);
        }
        
        g.write();

        //graph.display();
    }

    private static boolean is_not_contained(ArrayList<String[]> connections, String node1, String node2) {
        for (String[] ss : connections) {
            if (ss[0].equals(node2) && ss[1].equals(node1)) {
                return false;
            }
            if (ss[1].equals(node2) && ss[0].equals(node1)) {
                return false;
            }
        }
        
        return true;
    }
    
}
