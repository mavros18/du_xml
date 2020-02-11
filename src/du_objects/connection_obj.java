/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_objects;

import main.myBaseObject;

/**
 *
 * @author t.fotakis
 */
public class connection_obj extends myBaseObject {
    
    public String uproc;
    public String session;
    public String node;
    public Integer execs;
    public String origin;
    public String mu;

    public connection_obj(String node, String session, String uproc, Integer execs, String origin, String name) {
        this.node = node;
        this.session = session;
        this.uproc = uproc;
        this.execs = execs;
        this.origin = origin;
        this.name = name;
    }
    
    public connection_obj(String node, String name, String session, String uproc, String mu) {
        this.node = node;
        this.session = session;
        this.uproc = uproc;
        this.execs = 0;
        this.mu = mu;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "[node:"+this.node+", name:"+this.name+", session:"+this.session+", uproc:"+this.uproc+", mu:"+this.mu+", execs:"+this.execs+", origin:"+this.origin +"]";
    }
    
    @Override
    public String toJSONString() {
        return "{\"node\":\""+this.node+"\", \"name\":\""+this.name+"\", \"session\":\""+this.session+"\", \"uproc\":\""+this.uproc+"\", \"mu\":\""+this.mu+"\", \"execs\":"+this.execs+", \"origin\":\""+this.origin +"\"}";
    }
    
    
}
