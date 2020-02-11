/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_xml;

import du_objects.connection_obj;
import du_objects.node_obj;
import du_objects.task_obj;
import du_objects.TaskGroup_obj;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import main.Utility;

/**
 *
 * @author t.fotakis
 */
public final class du_xml_group {

    private static ArrayList<String> cur_stack;
    
    private static ArrayList<String[]> connections;
    
    private du_xml_group() { }
    
    private static void expand_stack() {
        boolean cont0,cont1;
        String[] ss;
        int i = 0;
        int start_size = connections.size();
        
        while ( i<connections.size()) {
            ss = connections.get(i);
            cont0 = cur_stack.contains(ss[0]);
            cont1 = cur_stack.contains(ss[1]);
            
            if ( cont0 || cont1 ) {
                if ( !cont1 ) {
                    cur_stack.add(ss[1]);
                }
                else if ( !cont0 ) {
                    cur_stack.add(ss[0]);
                }
                connections.remove(i);
            }
            else {
                i++;
            }
        }
        
        if (start_size != connections.size()) {
            expand_stack();
        }
    }
    
    private static boolean is_compatible(TaskGroup_obj simple, TaskGroup_obj sup) {
        
        if (simple.nodes.size() != sup.nodes.size()) {
            return false;
        }
        
        for (String node : simple.nodes) {
            if (!sup.nodes.contains(node)) {
                return false;
            }
        }
        
        return true;
        
    }
    
    public static void run(ArrayList<node_obj> final_data, String out_path) throws FileNotFoundException, UnsupportedEncodingException {
        
        ArrayList<TaskGroup_obj> groups = new ArrayList<>();

        ArrayList<String> group_free = new ArrayList<>();

        connections = new ArrayList<>();
        
        String myname;
        String[] con;
        
        boolean solo;
        
        int t_t = 0;
        
        for (node_obj it : final_data) {
            for ( task_obj tt : it.mytasks) {
                myname = it.name.toUpperCase()+"!"+tt.name;
                solo = true;
                for (connection_obj cc : tt.concon) {
                    if (!myname.equals(cc.node.toUpperCase()+"!"+cc.name)) { connections.add(new String[]{myname,cc.node.toUpperCase()+"!"+cc.name}); solo = false; }
                    
                }
                for (connection_obj cc : tt.concon_estimate) {
                    // this can add a connectoin already added from concon
                    if (!myname.equals(cc.node.toUpperCase()+"!"+cc.name)) { connections.add(new String[]{myname,cc.node.toUpperCase()+"!"+cc.name}); solo = false; }
                }
                if (solo) {
                    group_free.add(myname);
                }
            }
            t_t += it.mytasks.size();
        }
        
        // remove target-only tasks from free group
        for (String[] ss : connections) {
            if (group_free.contains(ss[1])) {
                group_free.remove(ss[1]);
            }
        }
        
        //----------------------------------------------------------------------
        int i = 0;
        
        while (!connections.isEmpty()) {
            cur_stack = new ArrayList<>();
 
            con = connections.get(0);
            cur_stack.add(con[0]);
            cur_stack.add(con[1]);
            connections.remove(0);
        
            expand_stack();

            i++;
            groups.add(new TaskGroup_obj("group_"+i,cur_stack));        
        }
        
        //----------------------------------------------------------------------      

        PrintWriter file9 = new PrintWriter(out_path + File.separator + "groups.csv ", "UTF-8");

        file9.write("Name;Group\n");
          
        int t_c = 0;
        
        for (TaskGroup_obj it : groups) {
            //utility.out.println(it.name+ " has " + it.tasks.size()+ " members");
            for ( String s : it.tasks) {
                file9.append(s+";"+it.name+"\n");
            }
            t_c += it.tasks.size();
        }
        
        //utility.out.println("FREE has " + group_free.size()+ " members");
        for ( String s : group_free) {
            file9.append(s+";FREE\n");
        }
        
        if (t_c + group_free.size() == t_t) {
            Utility.out.println("Total tasks     : " + t_t);
            Utility.out.println("Tasks in groups : " + t_c);
            Utility.out.println("Free tasks      : " + group_free.size());
            Utility.out.println("Total groups    : " + groups.size() + " + 1 (FREE)\n\n");
        }
        else {
            Utility.out.println("ERROR!! Wrong grouping");
        }
        file9.close();
        
        //----------------------------------------------------------------------
       
        file9 = new PrintWriter(out_path + File.separator + "groups_merged.csv ", "UTF-8");
        
        file9.write("Name;Size;Group\n");
        
        ArrayList<TaskGroup_obj> super_groups = new ArrayList<>();
        
        for (TaskGroup_obj it : groups) {
            it.nodes = new ArrayList<>();
            it.members = it.tasks.size();
            for ( String s : it.tasks) {
                if ( !it.nodes.contains(s.split("!")[0]) ) {
                    it.nodes.add(s.split("!")[0]);
                }
            }
        }
        
        
        Collections.sort(groups, TaskGroup_obj.getCompTasks());
        
        int limit_for_merge = t_c/10;
        
        TaskGroup_obj g;
        
        for (TaskGroup_obj it : groups) {
            solo = true;
            
            if (it.tasks.size() < limit_for_merge) {
                for (TaskGroup_obj sup : super_groups) {
                    if (is_compatible(it,sup)) {
                        sup.tasks.add(it.name);
                        sup.members += it.tasks.size();
                        solo = false;
                        break;
                    }
                }
            }
        
            if (solo) {
                g = new TaskGroup_obj();
                g.tasks.add(it.name);
                g.members = it.tasks.size();
                for (String node : it.nodes) {
                    g.nodes.add(node);
                }
                super_groups.add(g);
            }
        }
        
        Collections.sort(super_groups, TaskGroup_obj.getCompTasks());
        
        for (TaskGroup_obj it : super_groups) {
            Utility.out.println(it.name + "\t" + it.members + "\t" + it.nodes);
            for (String group : it.tasks) {
                file9.append(group+";"+group_size(group,groups)+";"+it.name+"\n");
            }
        }
        
        file9.close();
        
        cur_stack = null;
        connections = null;
    }

    private static int group_size(String group,ArrayList<TaskGroup_obj> groups) {
        for (TaskGroup_obj g : groups) {
            if (g.name.equals(group)) {
                return g.members;
            }
        }
        return -1;
    }
    
}
