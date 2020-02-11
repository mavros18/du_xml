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
public class mu_obj extends myBaseObject {

    public String node;
    
    public mu_obj(String name, String node) {
        this.name = name;
        this.node = node;
    }
    
    @Override
    public String toString() {
        return "[mu:"+this.name+", node:"+this.node+"]";
    }
    
    @Override
    public String toJSONString() {
        return "{\"mu\":\""+this.name+"\", \"node\":\""+this.node+"\"}";
    }
}
