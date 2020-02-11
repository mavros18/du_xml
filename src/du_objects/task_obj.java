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

/**
 *
 * @author t.fotakis
 */
public class task_obj extends myBaseObject {
    
    public String session;
    public String uproc;
    public ArrayList<String> launch;
    public String is_active;
    public String mu;
    public String template;
    public String type;
    public String isUprocHeader;
    public String queue;
    public ArrayList<String> rule;
    public String optional;
    public ArrayList<connection_obj> con_estimate;
    public ArrayList<connection_obj> con;
    public Integer execs;
    public Integer number_of_uprocs;
    public ArrayList<connection_obj> concon;
    public ArrayList<connection_obj> concon_estimate;
    public LinkedHashMap<String,String> variables;
    
    public task_obj(String task_session, String task_uproc, ArrayList<String> task_launch, String task_active, String task_mu, String task_is_template, String task_type, String textContent,LinkedHashMap<String,String> vars,String queue,ArrayList<String> rule,String optional,String name) {
        this.session = task_session;
        this.uproc = task_uproc;
        this.launch = task_launch;
        this.is_active = task_active;
        this.mu = task_mu;
        this.template = task_is_template;
        this.type = task_type;
        this.isUprocHeader = textContent;
        this.con_estimate = new ArrayList<>();
        this.con = new ArrayList<>();
        this.concon = new ArrayList<>();
        this.concon_estimate = new ArrayList<>();
        this.variables = vars;
        this.queue = queue;
        this.rule = rule;
        this.optional = optional;
        
        if (name.equals("")) {
            if ("".equals(task_session)) {
                this.name = "u!" + task_uproc+"!"+task_mu;
            }
            else {
                this.name = "s!" + task_session+"!"+task_mu;
            }
        }
        else {
            this.name = name;
        }

        this.execs = null;
        this.number_of_uprocs = null;
    }
    
    @Override
    public String toString() {
        
        String ret="[name:"+this.name+", session:"+this.session+", uproc:"+this.uproc+", mu:"+this.mu+", active:"+this.is_active+", queue:"+ this.queue +", template:"+this.template+", type:"+this.type+ ", optional:"+this.optional+", rule:"+this.rule +", isUprocHeader:"+this.isUprocHeader +", execs:"+this.execs + ", number_of_uprocs:" + this.number_of_uprocs;
        //con_estimate:[], con:[], concon:[], concon_estimate:[]
        ret+=", con_estimate:[";
        for (Iterator<connection_obj> it = this.con_estimate.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], con:[";
        for (Iterator<connection_obj> it = this.con.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], concon:[";
        for (Iterator<connection_obj> it = this.concon.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], concon_estimate:[";
        for (Iterator<connection_obj> it = this.concon_estimate.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="]]";
        
        return ret;
    }

    public String toMyString() {
        String ret="[session:"+this.session+", uproc:"+this.uproc+", mu:"+this.mu+", queue:"+this.queue+", template:"+this.template+", type:"+this.type+ ", isUprocHeader:"+this.isUprocHeader +", execs:"+this.execs + ", number_of_uprocs:" + this.number_of_uprocs;
        //con_estimate:[], con:[], concon:[], concon_estimate:[]
        ret+=", con_estimate:[";
        for (Iterator<connection_obj> it = this.con_estimate.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], con:[";
        for (Iterator<connection_obj> it = this.con.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="]]";
        
        return ret;
    }
    
    @Override
    public String toJSONString() {
        
        String ret="{\"name\":\""+this.name+"\", \"session\":\""+this.session+"\", \"uproc\":\""+this.uproc+"\", \"mu\":\""+this.mu+"\", \"active\":\""+this.is_active+"\", \"queue\":\""+ this.queue +"\", \"template\":\""+this.template+"\", \"type\":\""+this.type+ "\", \"optional\":\""+this.optional+"\", \"rule\":\""+this.rule +"\", \"isUprocHeader\":\""+this.isUprocHeader +"\", \"execs\":"+this.execs + ", \"number_of_uprocs\":" + this.number_of_uprocs;
        //con_estimate:[], con:[], concon:[], concon_estimate:[]
        ret+=", \"con_estimate\":[";
        for (Iterator<connection_obj> it = this.con_estimate.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"con\":[";
        for (Iterator<connection_obj> it = this.con.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"concon\":[";
        for (Iterator<connection_obj> it = this.concon.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="], \"concon_estimate\":[";
        for (Iterator<connection_obj> it = this.concon_estimate.iterator(); it.hasNext();) {
            connection_obj con1 = it.next();
            ret+=con1.toJSONString();
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="]}";
        
        return ret;
    }
    
}
