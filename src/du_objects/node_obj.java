/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_objects;

import main.myBaseObject;
import java.util.ArrayList;

/**
 *
 * @author t.fotakis
 */
public class node_obj extends myBaseObject {
    
    public ArrayList<uproc_obj> uprocs;
    public ArrayList<session_obj> sessions;
    public ArrayList<task_obj> tasks;
    public ArrayList<mu_obj> mus;
    public ArrayList<resource_obj> resources;
    public ArrayList<uproc_obj> myuprocs;
    public ArrayList<session_obj> mysessions;
    public ArrayList<task_obj> mytasks;

    public node_obj(String nodename, ArrayList<session_obj> ss, ArrayList<mu_obj> mm, ArrayList<task_obj> tt, ArrayList<uproc_obj> uu,ArrayList<resource_obj> rr) {
        this.name = nodename;
        this.uprocs = uu;
        this.mus = mm;
        this.sessions = ss;
        this.tasks = tt;
        this.resources = rr;
        this.myuprocs = new ArrayList<>();
        this.mysessions = new ArrayList<>();
        this.mytasks = new ArrayList<>();
    }
    
}
