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
public class session_obj extends myBaseObject {
    
    public ArrayList<ses_tree_obj> tree;
    public String label;

    public session_obj(String tmp, ArrayList<ses_tree_obj> tree, String label) {
        this.name = tmp;
        this.tree = tree;
        this.label = label;
    }
    
    @Override
    public String toString() {
        String tmp;
        
        tmp = "[name:"+this.name+", tree:[";
        for (Iterator<ses_tree_obj> it = this.tree.iterator(); it.hasNext();) {
            ses_tree_obj con1 = it.next();
            tmp+=con1;
            if (it.hasNext()) {
                tmp+=", ";
            }
        }
        tmp += "]]";
        return tmp;
    }
    
    @Override
    public String toJSONString() {
        String tmp;
        
        tmp = "{\"name\":\""+this.name+"\", \"tree\":[";
        for (Iterator<ses_tree_obj> it = this.tree.iterator(); it.hasNext();) {
            ses_tree_obj con1 = it.next();
            tmp+=con1.toJSONString();
            if (it.hasNext()) {
                tmp+=", ";
            }
        }
        tmp += "]}";
        return tmp;
    }
    
}
