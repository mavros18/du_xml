/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_objects;

import main.myBaseObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author t.fotakis
 */
public class uproc_obj extends myBaseObject {
    
    public ArrayList<dep_obj> notsim;
    public ArrayList<dep_obj> dependencies;
    public LinkedHashMap<Integer, String> IS;
    public LinkedHashMap<Integer, String> LOI;
    public String myclass;
    public ArrayList<String> incompatibilities;
    public ArrayList<String> successors;
    public ArrayList<resource_obj> resources;
    public LinkedHashMap<String, String> variables;
    public ArrayList<dep_obj> c_class;
    public ArrayList<dep_obj> c_resources;
    public ArrayList<uxordre_obj> uxordre;
    public ArrayList<uxordre_obj> uxordre_estimate;
    public ArrayList<connection_obj> origin_tasks;
    public Integer execs;
    
    public uproc_obj() {
        this.name = "";
        this.notsim = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.IS = new LinkedHashMap<>();
        this.myclass = "";
        this.incompatibilities = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.resources = new ArrayList<>();
        this.variables = new LinkedHashMap<>();
        this.c_class = new ArrayList<>();
        this.c_resources = new ArrayList<>();
        this.LOI = new LinkedHashMap<>();
        this.uxordre = new ArrayList<>();
        this.uxordre_estimate = new ArrayList<>();
        this.execs = null;
        this.origin_tasks = new ArrayList<>();
    }
    
    @Override
    public String toString() {
        String ret="[name:"+this.name+", class:"+this.myclass;
        ret+=", incompatibilities:[";
        for (Iterator<String> it = this.incompatibilities.iterator(); it.hasNext();) {
            String con1 = it.next();
            ret+="[class:"+con1+"]";
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], successors:[";
        for (Iterator<String> it = this.successors.iterator(); it.hasNext();) {
            String con1 = it.next();
            ret+="[uproc:"+con1+"]";
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], resources:[";
        for (Iterator<resource_obj> it = this.resources.iterator(); it.hasNext();) {
            resource_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], notsim:[";
        for (Iterator<dep_obj> it = this.notsim.iterator(); it.hasNext();) {
            dep_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], dependencies:[";
        for (Iterator<dep_obj> it = this.dependencies.iterator(); it.hasNext();) {
            dep_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], LOI:, IS:, variables:[";
        Set<String> itk = this.variables.keySet();
        
        for (Iterator iter = itk.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            ret+="[variable:"+key+", value:"+this.variables.get(key)+"]";
            if (iter.hasNext()) {
                ret+=", ";
            }
        }        
        ret+="], c_class:[";
        for (Iterator<dep_obj> it = this.c_class.iterator(); it.hasNext();) {
            dep_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
               ret+=", ";
            }
        }
        ret+="], c_resources:[";
        for (Iterator<dep_obj> it = this.c_resources.iterator(); it.hasNext();) {
            dep_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], uxordre:[";
        for (Iterator<uxordre_obj> it = this.uxordre.iterator(); it.hasNext();) {
            uxordre_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], uxordre_estimate:[";
        for (Iterator<uxordre_obj> it = this.uxordre_estimate.iterator(); it.hasNext();) {
            uxordre_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], origin_tasks:[";
        for (Iterator<connection_obj> it = this.origin_tasks.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], execs:"+this.execs + "]";
        return ret;

    }
    
    @Override
    public String toJSONString() {
        String ret="{\"name\":\""+this.name+"\", \"class\":\""+this.myclass+"\"";
        ret+=", \"incompatibilities\":[";
        for (Iterator<String> it = this.incompatibilities.iterator(); it.hasNext();) {
            String con1 = it.next();
            ret+="{\"class\":\""+con1+"\"}";
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"successors\":[";
        for (Iterator<String> it = this.successors.iterator(); it.hasNext();) {
            String con1 = it.next();
            ret+="{\"uproc\":\""+con1+"\"}";
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"resources\":[";
        for (Iterator<resource_obj> it = this.resources.iterator(); it.hasNext();) {
            resource_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"notsim\":[";
        for (Iterator<dep_obj> it = this.notsim.iterator(); it.hasNext();) {
            dep_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"dependencies\":[";
        for (Iterator<dep_obj> it = this.dependencies.iterator(); it.hasNext();) {
            dep_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"LOI\":\"\", \"IS\":\"\", \"variables\":[";
        Set<String> itk = this.variables.keySet();
        
        for (Iterator iter = itk.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            ret+="{\"variable\":\""+key+"\", \"value\":\""+this.variables.get(key)+"\"}";
            if (iter.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"c_class\":[";
        for (Iterator<dep_obj> it = this.c_class.iterator(); it.hasNext();) {
            dep_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
               ret+=", ";
            }
        }
        ret+="], \"c_resources\":[";
        for (Iterator<dep_obj> it = this.c_resources.iterator(); it.hasNext();) {
            dep_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"uxordre\":[";
        for (Iterator<uxordre_obj> it = this.uxordre.iterator(); it.hasNext();) {
            uxordre_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"uxordre_estimate\":[";
        for (Iterator<uxordre_obj> it = this.uxordre_estimate.iterator(); it.hasNext();) {
            uxordre_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"origin_tasks\":[";
        for (Iterator<connection_obj> it = this.origin_tasks.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"execs\":"+this.execs + "}";
        return ret;

    }
}
