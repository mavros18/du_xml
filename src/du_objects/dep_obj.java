/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_objects;

/**
 *
 * @author t.fotakis
 */
public class dep_obj {
    
    public String uproc;
    public String session;
    public String mu;
    public String node;
    public String desc;

    public dep_obj() {
        this.uproc = "";
        this.session = "";
        this.mu = "";
        this.node = "";
        this.desc = "";
    }

    public dep_obj(String uproc, String any_session, String mu, String node, String classname) {
        this.uproc = uproc;
        this.session = any_session;
        this.mu = mu;
        this.node = node;
        this.desc = classname;
    }
    
    @Override
    public String toString() {
        if (this.desc.equals("")) {
           return "[uproc:"+this.uproc+", session:"+this.session+", mu:"+this.mu+", node:"+this.node+"]"; 
        }
        return "[uproc:"+this.uproc+", session:"+this.session+", mu:"+this.mu+", node:"+this.node+", "+this.desc+"]";
    }
    
    public String toJSONString() {
        if (this.desc.equals("")) {
           return "{\"uproc\":\""+this.uproc+"\", \"session\":\""+this.session+"\", \"mu\":\""+this.mu+"\", \"node\":\""+this.node+"\"}"; 
        }
        return "{\"uproc\":\""+this.uproc+"\", \"session\":\""+this.session+"\", \"mu\":\""+this.mu+"\", \"node\":\""+this.node+"\", \""+this.desc.split(":")[0]+"\":\""+this.desc.split(":")[1]+"\"}";
    }
    
}
