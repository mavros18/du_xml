/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author t.fotakis
 */
public class myBaseObject {
    
    public String name;
    
    public String toJSONString() {
        return "\"name\": \""+ this.name+"\"";
    }

}
