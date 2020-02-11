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
public class uxordre_obj {
    
    public String uproc;
    public String session;
    public String node;
    public String name;

    public uxordre_obj(String node, String string, String ux_upr,String name) {
        
        this.uproc = ux_upr;
        this.session = string;
        this.node = node;
        this.name = name;
    }
    
    @Override
    public String toString(){
        return "[node:"+this.node+", name:"+this.name+", session:"+this.session+", uproc:"+this.uproc+"]";
    }
    
    public String toJSONString(){
        return "{\"node\":\""+this.node+"\", \"name\":\""+this.name+"\", \"session\":\""+this.session+"\", \"uproc\":\""+this.uproc+"\"}";
    }
}
