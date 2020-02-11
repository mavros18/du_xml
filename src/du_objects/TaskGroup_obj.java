/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_objects;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author t.fotakis
 */
public class TaskGroup_obj {
    public String name;
    public ArrayList<String> tasks;
    public ArrayList<String> nodes;
    public int members;
    
    private static int COUNT = 0;

    public static Comparator<TaskGroup_obj> getCompTasks() {   
        Comparator<TaskGroup_obj> comp = new Comparator<TaskGroup_obj>(){
            @Override
            public int compare(TaskGroup_obj o1, TaskGroup_obj o2) {
                return (o1.members - o2.members);
            }
        };
        return comp;
    }  
    
    public TaskGroup_obj(String name, ArrayList<String> tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    public TaskGroup_obj() {
        if (COUNT < 10) {
            this.name = "super_0"+COUNT;
        }
        else {
            this.name = "super_"+COUNT;
        }
        this.tasks = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.members = 0;
        
        COUNT++;
    }
}
