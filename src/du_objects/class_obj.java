/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_objects;

import main.myBaseObject;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author t.fotakis
 */
public class class_obj extends myBaseObject {
    
    public ArrayList<String> members;

    public class_obj(String myclass, String name)  {
        this.name = myclass;
        this.members = new ArrayList<>();
        this.members.add(name);
    }
    
    @Override
    public String toString() {
        String ret;
        
        ret ="[class:"+this.name+", members:[";
        for (Iterator<String> it = this.members.iterator(); it.hasNext();) {
            String con1 = it.next();
            ret+=con1;
            if (it.hasNext()) {
                ret+=", ";
            }
        }
        ret+="]]";
        return ret;
    }
}
