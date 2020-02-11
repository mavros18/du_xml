/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_xml;

import main.Utility;
import du_objects.connection_obj;
import du_objects.uxordre_obj;
import du_objects.ses_tree_obj;
import du_objects.class_obj;
import du_objects.session_obj;
import du_objects.node_obj;
import du_objects.uproc_obj;
import du_objects.mu_obj;
import du_objects.dep_obj;
import du_objects.task_obj;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;


/**
 *
 * @author t.fotakis
 */
public final class du_xml_proc {

    private static boolean DISPLAY_NO_COUNT = false;
    
    private static boolean DISPLAY_OPTIONAL = false;
    
    private static boolean CASE_SENSITIVE = false;
    
    private static ArrayList<node_obj> du_out;
    
    private du_xml_proc() { }
    
    public static void set_flags(boolean count,boolean optional,boolean case_sens){
        DISPLAY_NO_COUNT = count;
        DISPLAY_OPTIONAL = optional;
        CASE_SENSITIVE = case_sens;
    }

    private static int countOccurrences(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    private static boolean is_not_contained(ArrayList<task_obj> task_ar, task_obj new_task) {       
        return (Utility.get_by_name(task_ar, new_task.name,CASE_SENSITIVE) == null);
    }

    private static boolean start_ses(ArrayList<session_obj> n_sessions, task_obj new_task, String node_name) {

        if ("".equals(new_task.session)) {
            return true;
        }

        for (session_obj ses : n_sessions) {
            if (new_task.session.equals(ses.name)) {
                if (new_task.uproc.equals(ses.tree.get(0).uproc)) {
                    if ((new_task.mu.equals(ses.tree.get(0).mu)) || ("".equals(ses.tree.get(0).mu))) {
                        return true;
                    } else {
                        int uproc_app = 0;
                        for (ses_tree_obj leaf : ses.tree) {
                            if (leaf.uproc.equals(new_task.uproc)) {
                                uproc_app++;
                            }
                        }
                        
                        if (uproc_app > 1) {
                            Utility.out.println(node_name + " : WARNING : Multiple header appearances : " + new_task);
                        }

                        return (uproc_app == 1);
                    }
                } else {
                    return false;
                }

            }
        }

        Utility.out.println(node_name + " : WARNING : Task with non-existing session : " + new_task);
        return false;
    }

    private static String[] get_context_tree(ArrayList<ses_tree_obj> my_tree, int my_index, node_obj def_node, ArrayList<node_obj> _parsed_data) {

        ses_tree_obj my_dad;
        node_obj parent_node;
        
        String[] estimation= new String[2];
        estimation[0] = ""; // Node
        estimation[1] = ""; // MU

        if (my_index == 0) {
            parent_node = def_node;
        } else {
            my_dad = my_tree.get(my_tree.get(my_index).pid);
            parent_node = Utility.get_by_name(_parsed_data, my_dad.node,CASE_SENSITIVE);

            if (parent_node == null) {
//			if (my_tree[my_tree[my_index]["pid"]].mu != "IW_GREECE" && my_tree[my_tree[my_index]["pid"]].mu != "IU_GREECE" && my_tree[my_tree[my_index]["pid"]].mu != "IU_INT" && my_tree[my_tree[my_index]["pid"]].mu != "IW_INT") {
                Utility.out.println("get_context_tree : ERROR :" + my_tree.get(my_index)+ " my parent " + my_dad + " is not in available nodes");
//			}
                return null;
            }
            estimation[0] = my_dad.node;
            estimation[1] = my_dad.mu;
        }

        if (!"".equals(my_tree.get(my_index).mu)) {
            mu_obj my_mu = Utility.get_by_name(parent_node.mus, my_tree.get(my_index).mu,CASE_SENSITIVE);

            if (my_mu == null) {
//			if (my_tree[my_index].mu != "IW_GREECE" && my_tree[my_index].mu != "IU_GREECE" && my_tree[my_index].mu != "IU_INT" && my_tree[my_index].mu != "IW_INT") {
                Utility.out.println("get_context_tree : ERROR :" + my_tree.get(my_index)+ " in " + parent_node.name + " could not find my mu " + my_tree.get(my_index).mu + " in node mus " + parent_node.mus);
//			}
                return null;
            }
            estimation[0] = my_mu.node;
            estimation[1] = my_mu.name;
        }

        if ((my_index == 0) && (!def_node.name.toUpperCase().equals(estimation[0].toUpperCase())) && (!"".equals(estimation[0]))) {
            Utility.out.println("get_context_tree : ERROR : task starts in " + def_node.name + " but node from mu is " + estimation[0] + "  " + my_tree);
        }

        return estimation;
    }

    private static ArrayList<class_obj> add_cl(ArrayList<class_obj> n_classes, uproc_obj new_uproc) {

        for (class_obj cl : n_classes) {
            if (cl.name.equals(new_uproc.myclass)) {
                cl.members.add(new_uproc.name);
                return n_classes;
            }
        }

        n_classes.add(new class_obj(new_uproc.myclass, new_uproc.name));
        return n_classes; //TODO remove this
    }

    private static ArrayList<connection_obj> find_task_of_dep(String session_name, String uproc_name, String node_name) {

        node_obj t_node;
        uproc_obj u;
        
        t_node = Utility.get_by_name(du_out,node_name,CASE_SENSITIVE);
        if (t_node == null) {
            return null;
        }
        
        u = Utility.get_by_name(t_node.myuprocs, uproc_name,CASE_SENSITIVE);
        if (u == null) {
            return null;
        }
        
        ArrayList<connection_obj> res = new ArrayList<>();
        
        for (connection_obj c : u.origin_tasks) {
            if (c.session.equals(session_name)) {
                // this connection is missing execs
                res.add(c);
            }
        }
        
        if (res.isEmpty()) {
            Utility.out.println("ERROR NO MATCH : "+u.origin_tasks);
            return null;
        }
        
        return res;

    }

    private static String dep_origin(uproc_obj con_uproc, dep_obj uproc_connection, String connection_type) {

        if ("notsim".equals(connection_type)) {
            return (con_uproc.name + " not_sim with " + uproc_connection.uproc);
        } else if ("dependencies".equals(connection_type)) {
            return (con_uproc.name + " dependency " + uproc_connection.uproc);
        } else if ("c_resources".equals(connection_type)) {
            return (con_uproc.name + " shares resource " + uproc_connection.desc.replace("resource:", "") + " with " + uproc_connection.uproc);
        } else if ("c_class".equals(connection_type)) {
            return (con_uproc.name + " is a member of class " + con_uproc.myclass + " and is incompatible with " + uproc_connection.uproc + " member of class " + uproc_connection.desc.replace("inc_class:", ""));
        }

        Utility.out.println("YOU SHOULD NEVER SEE THIS!!!!");
        return "";
    }
    
    private static Integer[] process_scripts() {
        String ux_upr, ux_ses, ux_node, ux_mu, ux_mytmp, orig_node,ux_tsk;
        String[] ux_line;
        task_obj cur_tsk;
        node_obj cur_node;
        
        boolean case_s = false;
        
        int ux_sum = 0;
        int ux_est = 0;
        
        for (node_obj it : du_out) {
            //uxordre
            for (uproc_obj cur_uproc : it.myuprocs) {
                for (int l = 1; l <= cur_uproc.IS.size(); l++) {
                    if ((cur_uproc.IS.get(l).toUpperCase().contains("UXORDRE")) && (!cur_uproc.IS.get(l).trim().startsWith("#")) && (!cur_uproc.IS.get(l).trim().startsWith("::")) && (!cur_uproc.IS.get(l).trim().startsWith("echo")) && (!cur_uproc.IS.get(l).trim().startsWith("REM"))) {
                        ux_upr = "";
                        ux_ses = "";
                        ux_node = "";
                        ux_mu = "";
                        ux_tsk = "";

                        ux_line = cur_uproc.IS.get(l).split("(?i)uxordre");
                        ux_line = ux_line[1].split(" ");
                        for (String ux_parm : ux_line) {

                            if (ux_parm.trim().toUpperCase().startsWith("UPR=")) {
                                ux_upr = ux_parm.split("=")[1].replace("\"", "").replace("\\", "");
                            } else if (ux_parm.trim().toUpperCase().startsWith("SES=")) {
                                ux_ses = ux_parm.split("=")[1].replace("\"", "").replace("\\", "");
                            } else if (ux_parm.trim().toUpperCase().startsWith("UG=")) {
                                ux_mu = ux_parm.split("=")[1].replace("\"", "").replace("\\", "");
                            } else if (ux_parm.trim().toUpperCase().startsWith("NODE=")) {
                                ux_node = ux_parm.split("=")[1].replace("\"", "").replace("\\", "");
                            } else if (ux_parm.trim().toUpperCase().startsWith("MU=")) {
                                ux_mu = ux_parm.split("=")[1].replace("\"", "").replace("\\", "");
                            } else if (ux_parm.trim().toUpperCase().startsWith("TSK=")) {
                                ux_tsk = ux_parm.split("=")[1].replace("\"", "").replace("\\", "");
                            }
                        }

                        ux_mytmp = ux_upr + ":" + ux_ses + ":" + ux_node + ":" + ux_mu + ":" + ux_tsk;
                        if ((!ux_mytmp.contains("$")) && (!ux_mytmp.contains("%"))) {

                            orig_node = null;

                            if ("".equals(ux_node)) {
                                cur_node = it;
                            } else {
                                cur_node = Utility.get_by_name(du_out, ux_node,case_s);
                                if (cur_node == null) {
                                    Utility.out.println("\tMissing node for uxordre estimate : " + ux_node + " current node : " + it.name + " current upr : " + cur_uproc.name);
                                }
                            }

                            if ((cur_node != null) && (!"".equals(ux_mu))) {
                                mu_obj orig_mu = Utility.get_by_name(cur_node.mus, ux_mu,case_s);
                                if (orig_mu != null) {
                                    orig_node = orig_mu.node;
                                }
                            }

                            if (orig_node == null) {
                                Utility.out.println("Missing MU for uxordre : " + ux_mu + " current node : " + it.name + " current upr : " + cur_uproc.name + " " + cur_uproc.IS.get(l));
                            } else if ("".equals(ux_tsk)) {
                                if ("".equals(ux_ses)) {
                                    cur_uproc.uxordre.add(new uxordre_obj(orig_node, "", ux_upr,ux_tsk));
                                } else {
                                    cur_uproc.uxordre.add(new uxordre_obj(orig_node, ux_ses, "",ux_tsk));
                                }
                            }
                            else {
                                cur_tsk = Utility.get_by_name(cur_node.mytasks, ux_tsk,case_s);
                                if ( cur_tsk != null) {
                                    cur_uproc.uxordre.add(new uxordre_obj(orig_node, cur_tsk.session, cur_tsk.uproc,ux_tsk));
                                }
                                else {
                                    Utility.out.println("ERROR MISSING UXORDRE TASK : " + cur_uproc.IS.get(l) + " | " + ux_tsk + "   from    " + it.name + " -> " + cur_uproc.name );
                                }
                            }
                        } else {
                            Utility.out.println(it.name + " " + cur_uproc.name + " | on line " + l + " | " + cur_uproc.IS.get(l));
                            ux_sum++;

                            boolean ux_upr_f = false;
                            boolean ux_ses_f = false;
                            boolean ux_node_f = false;
                            boolean ux_mu_f = false;
                            boolean ux_tsk_f = false;

                            boolean ux_var_f = false;

                            ArrayList<String> ux_upr_r = new ArrayList<>();
                            ArrayList<String> ux_ses_r = new ArrayList<>();
                            ArrayList<String> ux_node_r = new ArrayList<>();
                            ArrayList<String> ux_mu_r = new ArrayList<>();
                            ArrayList<String> ux_tsk_r = new ArrayList<>();

                            if ((countOccurrences(ux_upr, '$') > 1) || (countOccurrences(ux_upr, '%') > 2)) {
                                ux_var_f = true;
                            }
                            if ((countOccurrences(ux_ses, '$') > 1) || (countOccurrences(ux_ses, '%') > 2)) {
                                ux_var_f = true;
                            }
                            if ((countOccurrences(ux_node, '$') > 1) || (countOccurrences(ux_node, '%') > 2)) {
                                ux_var_f = true;
                            }
                            if ((countOccurrences(ux_mu, '$') > 1) || (countOccurrences(ux_mu, '%') > 2)) {
                                ux_var_f = true;
                            }
                            if ((countOccurrences(ux_tsk, '$') > 1) || (countOccurrences(ux_tsk, '%') > 2)) {
                                ux_tsk_f = true;
                            }

                            if ((ux_upr.contains("$")) || (ux_upr.contains("%"))) {
                                ux_upr_f = true;
                                ux_upr = ux_upr.replace("$", "").replace("%", "").replace("{", "").replace("}", "");
                            }
                            if ((ux_ses.contains("$")) || (ux_ses.contains("%"))) {
                                ux_ses_f = true;
                                ux_ses = ux_ses.replace("$", "").replace("%", "").replace("{", "").replace("}", "");
                            }
                            if ((ux_node.contains("$")) || (ux_node.contains("%"))) {
                                ux_node_f = true;
                                ux_node = ux_node.replace("$", "").replace("%", "").replace("{", "").replace("}", "");
                            }
                            if ((ux_mu.contains("$")) || (ux_mu.contains("%"))) {
                                ux_mu_f = true;
                                ux_mu = ux_mu.replace("$", "").replace("%", "").replace("{", "").replace("}", "");
                            }
                            if ((ux_tsk.contains("$")) || (ux_tsk.contains("%"))) {
                                ux_tsk_f = true;
                                ux_tsk = ux_tsk.replace("$", "").replace("%", "").replace("{", "").replace("}", "");
                            }

                            if (ux_upr_f) {
                                //println "SEARCH UPROC : " + ux_upr_f + " " + ux_upr
                                for (String key : cur_uproc.variables.keySet()) {
                                    if (key.equals(ux_upr)) {
                                        ux_upr_r.add(cur_uproc.variables.get(key));
                                    }
                                }
                            }
                            if (ux_ses_f) {
                                //println "SEARCH SES : " + ux_ses_f + " " + ux_ses
                                for (String key : cur_uproc.variables.keySet()) {
                                    if (key.equals(ux_ses)) {
                                        ux_ses_r.add(cur_uproc.variables.get(key));
                                    }
                                }
                            }
                            if (ux_node_f) {
                                //println "SEARCH NODE : " + ux_node_f + " " + ux_node
                                for (String key : cur_uproc.variables.keySet()) {
                                    if (key.equals(ux_node)) {
                                        ux_node_r.add(cur_uproc.variables.get(key));
                                    }
                                }
                            }
                            if (ux_mu_f) {
                                //println "SEARCH MU : " + ux_mu_f + " " + ux_mu
                                for (String key : cur_uproc.variables.keySet()) {
                                    if (key.equals(ux_mu)) {
                                        ux_mu_r.add(cur_uproc.variables.get(key));
                                    }
                                }
                            }
                            if (ux_tsk_f) {
                                //println "SEARCH TSK : " + ux_tsk_f + " " + ux_tsk
                                for (String key : cur_uproc.variables.keySet()) {
                                    if (key.equals(ux_tsk)) {
                                        ux_tsk_r.add(cur_uproc.variables.get(key));
                                    }
                                }
                            }

                            for (int n = 1; n <= cur_uproc.IS.size(); n++) {
                                if ((!cur_uproc.IS.get(n).trim().startsWith("#")) && (!cur_uproc.IS.get(n).trim().startsWith("::")) && (!cur_uproc.IS.get(n).trim().startsWith("echo"))) {
                                    if (ux_upr_f && (cur_uproc.IS.get(n).trim().startsWith(ux_upr + "=") || cur_uproc.IS.get(n).trim().contains("set " + ux_upr + "="))) {
                                        if ((!cur_uproc.IS.get(n).split(ux_upr + "=")[1].contains("`")) && (!cur_uproc.IS.get(n).split(ux_upr + "=")[1].contains("$")) && (!cur_uproc.IS.get(n).split(ux_upr + "=")[1].contains("%"))) {
                                            //println "UPROC : " + cur_uproc.IS.get(n)
                                            ux_upr_r.add(cur_uproc.IS.get(n).split(ux_upr + "=")[1].replace("\\", "").replace("\"", ""));
                                        } else {
                                            ux_var_f = true;
                                        }
                                    }
                                    if (ux_ses_f && (cur_uproc.IS.get(n).trim().startsWith(ux_ses + "=") || cur_uproc.IS.get(n).trim().contains("set " + ux_ses + "="))) {
                                        if ((!cur_uproc.IS.get(n).split(ux_ses + "=")[1].contains("`")) && (!cur_uproc.IS.get(n).split(ux_ses + "=")[1].contains("$")) && (!cur_uproc.IS.get(n).split(ux_ses + "=")[1].contains("%"))) {
                                            //println "SES : " + cur_uproc.IS.get(n)
                                            ux_ses_r.add(cur_uproc.IS.get(n).split(ux_ses + "=")[1].replace("\\", "").replace("\"", ""));
                                        } else {
                                            ux_var_f = true;
                                        }
                                    }
                                    if (ux_node_f && (cur_uproc.IS.get(n).trim().startsWith(ux_node + "=") || cur_uproc.IS.get(n).trim().contains("set " + ux_node + "="))) {
                                        if ((!cur_uproc.IS.get(n).split(ux_node + "=")[1].contains("`")) && (!cur_uproc.IS.get(n).split(ux_node + "=")[1].contains("$")) && (!cur_uproc.IS.get(n).split(ux_node + "=")[1].contains("%"))) {
                                            //println "NODE : " + cur_uproc.IS.get(n)
                                            ux_node_r.add(cur_uproc.IS.get(n).split(ux_node + "=")[1].replace("\\", "").replace("\"", ""));
                                        } else {
                                            ux_var_f = true;
                                        }
                                    }
                                    if (ux_mu_f && (cur_uproc.IS.get(n).trim().startsWith(ux_mu + "=") || cur_uproc.IS.get(n).trim().contains("set " + ux_mu + "="))) {
                                        if ((!cur_uproc.IS.get(n).split(ux_mu + "=")[1].contains("`")) && (!cur_uproc.IS.get(n).split(ux_mu + "=")[1].contains("$")) && (!cur_uproc.IS.get(n).split(ux_mu + "=")[1].contains("%"))) {
                                            //println "MU : " + cur_uproc.IS.get(n)
                                            ux_mu_r.add(cur_uproc.IS.get(n).split(ux_mu + "=")[1].replace("\\", "").replace("\"", ""));
                                        } else {
                                            ux_var_f = true;
                                        }
                                    }
                                    if (ux_tsk_f && (cur_uproc.IS.get(n).trim().startsWith(ux_tsk + "=") || cur_uproc.IS.get(n).trim().contains("set " + ux_tsk + "="))) {
                                        if ((!cur_uproc.IS.get(n).split(ux_tsk + "=")[1].contains("`")) && (!cur_uproc.IS.get(n).split(ux_tsk + "=")[1].contains("$")) && (!cur_uproc.IS.get(n).split(ux_tsk + "=")[1].contains("%"))) {
                                            //println "MU : " + cur_uproc.IS.get(n)
                                            ux_tsk_r.add(cur_uproc.IS.get(n).split(ux_tsk + "=")[1].replace("\\", "").replace("\"", ""));
                                        } else {
                                            ux_var_f = true;
                                        }
                                    }
                                }
                            }

                            if (ux_var_f) {
                                Utility.out.println("\tUXERR : VAR USED : " + cur_uproc.variables + cur_uproc.IS);
                            } else if ((ux_upr_f && ux_upr_r.isEmpty()) || (ux_ses_f && ux_ses_r.isEmpty()) || (ux_node_f && ux_node_r.isEmpty()) || (ux_mu_f && ux_mu_r.isEmpty()) || (ux_tsk_f && ux_tsk_r.isEmpty())) {
                                Utility.out.println("\tUXERR : SOMETHING WASN'T FOUND : " + cur_uproc.variables + cur_uproc.IS);
                                Utility.out.print("\t");

                                if (ux_upr_f) {
                                    Utility.out.print(" | FOUND UPROC : " + ux_upr_r);
                                }
                                if (ux_ses_f) {
                                    Utility.out.print(" | FOUND SES : " + ux_ses_r);
                                }
                                if (ux_node_f) {
                                    Utility.out.print(" | FOUND NODE : " + ux_node_r);
                                }
                                if (ux_mu_f) {
                                    Utility.out.print(" | FOUND MU : " + ux_mu_r);
                                }
                                if (ux_tsk_f) {
                                    Utility.out.print(" | FOUND TSK : " + ux_tsk_r);
                                }
                                Utility.out.print("\n");
                            } else if ((ux_upr_f && ux_upr_r.size() > 1) || (ux_ses_f && ux_ses_r.size() > 1) || (ux_node_f && ux_node_r.size() > 1) || (ux_mu_f && ux_mu_r.size() > 1) || (ux_tsk_f && ux_tsk_r.size() > 1)) {
                                Utility.out.println("\tWARNING : FOUND TOO MANY : " + cur_uproc.variables + cur_uproc.IS);
                                Utility.out.print("\t");

                                if (ux_upr_f) {
                                    Utility.out.print(" | FOUND UPROC : " + ux_upr_r);
                                }
                                if (ux_ses_f) {
                                    Utility.out.print(" | FOUND SES : " + ux_ses_r);
                                }
                                if (ux_node_f) {
                                    Utility.out.print(" | FOUND NODE : " + ux_node_r);
                                }
                                if (ux_mu_f) {
                                    Utility.out.print(" | FOUND MU : " + ux_mu_r);
                                }
                                if (ux_tsk_f) {
                                    Utility.out.print(" | FOUND TSK : " + ux_tsk_r);
                                }
                                Utility.out.print("\n");
                            } else {

                                if (ux_upr_f) {
                                    ux_upr = ux_upr_r.get(0);
                                }
                                if (ux_ses_f) {
                                    ux_ses = ux_ses_r.get(0);
                                }
                                if (ux_node_f) {
                                    ux_node = ux_node_r.get(0);
                                }
                                if (ux_mu_f) {
                                    ux_mu = ux_mu_r.get(0);
                                }
                                if (ux_tsk_f) {
                                    ux_tsk = ux_tsk_r.get(0);
                                }

                                orig_node = null;

                                if ("".equals(ux_node)) {
                                    cur_node = it;
                                } else {
                                    cur_node = Utility.get_by_name(du_out, ux_node,case_s);
                                    if (cur_node == null) {
                                        Utility.out.println("\tMissing node for uxordre estimate : " + ux_node + " current node : " + it.name + " current upr : " + cur_uproc.name);
                                    }
                                }

                                if ((cur_node != null) && (!"".equals(ux_mu))) {
                                    mu_obj orig_mu = Utility.get_by_name(cur_node.mus, ux_mu,case_s);
                                    if (orig_mu != null) {
                                        orig_node = orig_mu.node;
                                    }
                                }

                                if (orig_node == null) {
                                    Utility.out.println("\tMissing MU for uxordre estimate : " + ux_mu + " current node : " + it.name + " current upr : " + cur_uproc.name + " " + cur_uproc.IS.get(l));
                                } else if ("".equals(ux_tsk)){

                                    if ("".equals(ux_ses)) {
                                        Utility.out.println("\tESTIMATE : " + orig_node + " uproc " + ux_upr);
                                        cur_uproc.uxordre_estimate.add(new uxordre_obj(orig_node, "", ux_upr,ux_tsk));
                                    } else {
                                        Utility.out.println("\tESTIMATE : " + orig_node + " session " + ux_ses);
                                        cur_uproc.uxordre_estimate.add(new uxordre_obj(orig_node, ux_ses, "",ux_tsk));
                                    }
                                    ux_est++;
                                } else {
                                    cur_tsk = Utility.get_by_name(cur_node.mytasks, ux_tsk,case_s);
                                    if ( cur_tsk != null) {
                                        Utility.out.println("\tESTIMATE : " + orig_node + " tsk " + ux_tsk);
                                        cur_uproc.uxordre_estimate.add(new uxordre_obj(orig_node, cur_tsk.session, cur_tsk.uproc,ux_tsk));
                                    }
                                    else {
                                        Utility.out.println("ERROR MISSING uxordre estimate TASK : " + cur_uproc.IS.get(l) + " | " + ux_tsk + "   from    " + it.name + " -> " + cur_uproc.name );
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
        
        return new Integer[]{ux_sum,ux_est};
    }

    private static ArrayList<connection_obj> process_uproc(String my_node, task_obj my_task, uproc_obj my_uproc, String cur_mu) {

        ArrayList<connection_obj> result_con = new ArrayList<>();
        node_obj pu_cur_node;
        task_obj pu_cur_task;
        uproc_obj pu_cur_uproc;
        ArrayList<dep_obj> c_array;
        String c_con;
        String the_mu;
        
        

        String[] con_origins = new String[]{"notsim", "dependencies", "c_resources", "c_class"};

        for (int a = 0; a < con_origins.length; a++) {

            c_con = con_origins[a];
            switch (a) {
                case 0:
                    c_array = my_uproc.notsim;
                    break;
                case 1:
                    c_array = my_uproc.dependencies;
                    break;
                case 2:
                    c_array = my_uproc.c_resources;
                    break;
                case 3:
                    c_array = my_uproc.c_class;
                    break;
                default:
                    Utility.out.println("+++++ERROR++++++   :: " + my_uproc);
                    c_array = new ArrayList<>();
                    break;
            }

            for (int l = 0; l < c_array.size(); l++) {
                the_mu = c_array.get(l).mu;
                if (the_mu.equals("SAME_MU")) {
                    the_mu = cur_mu;
                }
                
                if ("SAME_SESSION".equals(c_array.get(l).session)) {
                    if (my_task.session.equals("") ) {
                        Utility.out.println("!!!!!!!!ERROR!!!!!! " + my_task);
                    }
                    else {
                        pu_cur_node = Utility.get_by_name(du_out, my_node,CASE_SENSITIVE);
                        for (int m = 0; m < pu_cur_node.mytasks.size(); m++) {
                            if ( my_task.session.equals(pu_cur_node.mytasks.get(m).session) ) {
                                result_con.add(new connection_obj(pu_cur_node.name, pu_cur_node.mytasks.get(m).session, pu_cur_node.mytasks.get(m).uproc, pu_cur_node.mytasks.get(m).execs, dep_origin(my_uproc, c_array.get(l), c_con),pu_cur_node.mytasks.get(m).name));
                            }
                        }
                    }
                } else if ("SAME_SESSION_AND_EXECUTION".equals(c_array.get(l).session)) {
                    result_con.add(new connection_obj(my_node, my_task.session, my_task.uproc, my_task.execs, dep_origin(my_uproc, c_array.get(l), c_con),my_task.name));
                } else if ("ANY_SESSION".equals(c_array.get(l).session)) {
                    //search in the noted mu/node (either current or other) for task with no session and this uproc
                    pu_cur_node = Utility.get_by_name(du_out, c_array.get(l).node,CASE_SENSITIVE);

                    for (int m = 0; m < pu_cur_node.mytasks.size(); m++) {
                        if ((pu_cur_node.mytasks.get(m).uproc.equals(c_array.get(l).uproc)) && ("".equals(pu_cur_node.mytasks.get(m).session))) {
                            result_con.add(new connection_obj(pu_cur_node.name, pu_cur_node.mytasks.get(m).session, pu_cur_node.mytasks.get(m).uproc, pu_cur_node.mytasks.get(m).execs, dep_origin(my_uproc, c_array.get(l), c_con),pu_cur_node.mytasks.get(m).name));
                            if (!"".equals(c_array.get(l).mu)) {
                                if (!pu_cur_node.mytasks.get(m).mu.equals(the_mu)) {
                                    Utility.out.println("MISSMATCH MU : TASK has "+ pu_cur_node.mytasks.get(m).mu +" condition has " +the_mu+ " ("+ c_array.get(l).mu + ")");
                                }
                            }
                        }
                    }
                    //search in all nodes for a task with a session that runs the uproc on the noted mu/node
                    pu_cur_uproc = Utility.get_by_name(pu_cur_node.myuprocs, c_array.get(l).uproc,CASE_SENSITIVE);
                    if (pu_cur_uproc != null) {
                        for (connection_obj co : pu_cur_uproc.origin_tasks) {
                            result_con.add(new connection_obj(co.node,co.session,co.uproc,co.execs,dep_origin(my_uproc, c_array.get(l), c_con),co.name));
                            if (!"".equals(c_array.get(l).mu)) {
                                if (!co.mu.equals(the_mu)) {
                                    Utility.out.println("MISSMATCH MU for : "+my_uproc.name+" with condition " +c_array.get(l) +" looking at "+pu_cur_uproc.name+" from task "+co+ "TASK has "+ co.mu +" condition has " +the_mu+ " ("+ c_array.get(l).mu + ")");
                                }
                            }
                        }
                    }
                } else { //SPECIFIC_SESSION
                    ArrayList<connection_obj> origin_dep = find_task_of_dep(c_array.get(l).session, c_array.get(l).uproc, c_array.get(l).node);

                    if (origin_dep == null) {
                        Utility.out.println(my_node + " ERROR: Could not find origin of dependency : uproc " + my_uproc.name + " with " + c_array.get(l)); //add to result_con for unknown connection
                    } else {
                        for (connection_obj co : origin_dep) {
                            result_con.add(new connection_obj(co.node,co.session,co.uproc,co.execs,dep_origin(my_uproc, c_array.get(l), c_con),co.name));
                        }
                    }
                }
            }
        }
        
        boolean no_match;

        for (uxordre_obj uu : my_uproc.uxordre) {
            //resolve to real task and check if it exists / get execs
            pu_cur_node = Utility.get_by_name(du_out,uu.node,CASE_SENSITIVE);
            if (pu_cur_node == null) {
                Utility.out.println("ERROR MISSING UXORDRE NODE!!!! " + uu.node);
            } else {
                no_match = true;
                if (!"".equals(uu.name)) {
                    pu_cur_task = Utility.get_by_name(pu_cur_node.mytasks,uu.name,CASE_SENSITIVE);
                    if (pu_cur_task != null) {
                        result_con.add(new connection_obj(uu.node, pu_cur_task.session, pu_cur_task.uproc, pu_cur_task.execs, my_uproc.name + " uxordre",pu_cur_task.name));
                        no_match = false;
                        //if (!pu_cur_task.type.equals("TaskProvoked")) {
                            //utility.out.println("UXORDRE : ERROR : Trigger on not provked task " + uu + "   from    " + my_node + " -> " + my_uproc.name);
                        //}
                    }
                }
                else {
                    for (task_obj tt : pu_cur_node.mytasks) {
                        if ((tt.session.equals(uu.session)) && (tt.uproc.equals(uu.uproc))) {
                            result_con.add(new connection_obj(uu.node, uu.session, uu.uproc, tt.execs, my_uproc.name + " uxordre",tt.name));
                            no_match = false;
                            //if (!tt.type.equals("TaskProvoked")) {
                                //utility.out.println("UXORDRE : ERROR : Trigger on not provked task " + uu + "   from    " + my_node + " -> " + my_uproc.name);
                            //}
                        } else if ( ("".equals(uu.uproc) && (tt.session.equals(uu.session)) && (!"".equals(uu.session)) )) {
                            result_con.add(new connection_obj(uu.node, uu.session, uu.uproc, tt.execs, my_uproc.name + " uxordre",tt.name));
                            no_match = false;
                            //if (!tt.type.equals("TaskProvoked")) {
                                //utility.out.println("UXORDRE : ERROR : Trigger on not provked task " + uu + "   from    " + my_node + " -> " + my_uproc.name);
                            //}
                        }
                    }
                }
                if (no_match) {
                    Utility.out.println("ERROR MISSING UXORDRE TASK : " + uu + "   from    " + my_node + " -> " + my_uproc.name);
                }
            }
        }

        return result_con;
    }

    private static ArrayList<connection_obj> process_estimates(String my_node, uproc_obj my_uproc) {

        ArrayList<connection_obj> result_con = new ArrayList<>();
        node_obj pu_cur_node;
        task_obj pu_cur_task;
        
        boolean no_match;

        for (uxordre_obj uu : my_uproc.uxordre_estimate) {
            //resolve to real task and check if it exists / get execs
            pu_cur_node = Utility.get_by_name(du_out,uu.node,CASE_SENSITIVE);
            if (pu_cur_node == null) {
                Utility.out.println("ERROR MISSING uxordre_estimate NODE!!!! " + uu.node);
            } else {
                no_match = true;
                if (!"".equals(uu.name)) {
                    pu_cur_task = Utility.get_by_name(pu_cur_node.mytasks,uu.name,CASE_SENSITIVE);
                    if (pu_cur_task != null) {
                        result_con.add(new connection_obj(uu.node, pu_cur_task.session, pu_cur_task.uproc, pu_cur_task.execs, my_uproc.name + " estimated uxordre",pu_cur_task.name));
                        no_match = false;
                    }
                }
                else {
                    for (task_obj tt : pu_cur_node.mytasks) {
                        if ((tt.session.equals(uu.session)) && (tt.uproc.equals(uu.uproc))) {
                            result_con.add(new connection_obj(uu.node, uu.session, uu.uproc, tt.execs, my_uproc.name + " estimated uxordre",tt.name));
                            no_match = false;
                        } else if ( ("".equals(uu.uproc) && (tt.session.equals(uu.session)) && (!"".equals(uu.session)) )) {
                            result_con.add(new connection_obj(uu.node, uu.session, uu.uproc, tt.execs, my_uproc.name + " estimated uxordre",tt.name));
                            no_match = false;
                        }
                    }
                }
                if (no_match) {
                    Utility.out.println("ERROR MISSING uxordre_estimate TASK : " + uu + "   from    " + my_node + " -> " + my_uproc.name);
                }
            }
        }

        return result_con;
    }

    private static void process_executions(String filename) {
        if ("".equals(filename)) {
            return;
        }
        
        node_obj cur_node;
        uproc_obj cur_uproc;
        
        Utility.out.println("\n--------------------------------------EXECS-----------------------------------------------");
        
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filename));
        } catch (IOException e) {
            Utility.err.println(e);
        }

        
        ArrayList<String> unknown_nodes = new ArrayList<>();
        String s_node, s_uproc, s_exec;

        //add number of executions to uprocs
        for (String key : properties.stringPropertyNames()) {
            s_node = key.split(">")[1];
            s_uproc = key.split(">")[0];
            s_exec = properties.getProperty(key);

            cur_node = Utility.get_by_name(du_out, s_node,CASE_SENSITIVE);
            if (cur_node != null) {
                cur_uproc = Utility.get_by_name(cur_node.myuprocs, s_uproc,CASE_SENSITIVE);
                if (cur_uproc != null) {
                    //utility.out.println("1 : " + s_uproc+" has "+ s_exec +" executions");
                    cur_uproc.execs = Integer.parseInt(s_exec);
                } else {
                    //utility.out.println("2 : " + s_uproc +" has "+ s_exec +" executions but isn't part of a task");
                }
                if (Utility.get_by_name(cur_node.uprocs, s_uproc,CASE_SENSITIVE) == null) {
                    Utility.out.println(s_node + " " + s_uproc + " doesn't exist in xml");
                }
            } else if (!unknown_nodes.contains(s_node)) {
                unknown_nodes.add(s_node);
            }

        }
        
        for (String un_node : unknown_nodes) {
            Utility.out.println("Unknown node : " + un_node);
        }
        
        Utility.out.println("\n-------------------------------------------------------------------------------------");
    }
            
    public static ArrayList<node_obj> run(ArrayList<node_obj> _parsed_data,String executions_file) {

        du_out = _parsed_data;
        
        int mysum = 0;
        int mysumsum = 0;
        int mysessum = 0;
        int ux_sum;
        int ux_est;

        uproc_obj cur_uproc;
        node_obj cur_node;
        session_obj cur_session;
        String cur_node_name;
        String[] estimation;


        /*
        //Change scheduled to optional where needed
        for (node_obj nn : _parsed_data) {
            for (task_obj tt : nn.tasks) {
                if (!tt.session.equals("") && tt.type.equals("TaskPlanified")) {
                    for (session_obj ss : nn.sessions) {
                        if (ss.name.equals(tt.session)) {
                            if (!ss.tree.get(0).uproc.equals(tt.uproc)) {
                                        tt.type = tt.type+"_Optional";
                            }
                        }
                    }
                }
            }
        }
        */
        
        for (node_obj it : _parsed_data) {

            //get scheduled tasks (uproc is first in session)
            for (int j = 0; j < it.tasks.size(); j++) {
                if ("TaskPlanified".equals(it.tasks.get(j).type) && "false".equals(it.tasks.get(j).template)) {
                    if (!start_ses(it.sessions, it.tasks.get(j), it.name)) {
                        if (it.tasks.get(j).optional.equals("false") && DISPLAY_OPTIONAL) {
                            Utility.out.println(it.name + " : WARNING : Scheduled task with non-header uproc (must be optional) : " + it.tasks.get(j));
                        }
                    }
                    if ((is_not_contained(it.mytasks, it.tasks.get(j))) && (start_ses(it.sessions, it.tasks.get(j), it.name)) && ("false".equals(it.tasks.get(j).template))) {
                        it.mytasks.add(it.tasks.get(j));
                    }
                }
            }
            //get provoked tasks (uproc is first in session)
            for (int j = 0; j < it.tasks.size(); j++) {
                if ("TaskProvoked".equals(it.tasks.get(j).type) && "false".equals(it.tasks.get(j).template)) {
                    if ((is_not_contained(it.mytasks, it.tasks.get(j))) && (start_ses(it.sessions, it.tasks.get(j), it.name)) && ("false".equals(it.tasks.get(j).template))) {
                        it.mytasks.add(it.tasks.get(j));
                    }
                }
            }

            mysum += it.tasks.size();
            mysumsum += it.mytasks.size();

            //add mu to uprocs in sessions from tasks and get said sessions (starting in this node with same context)
            for (int j = 0; j < it.mytasks.size(); j++) {
                if (!"".equals(it.mytasks.get(j).session)) {
                if (Utility.get_by_name(it.mysessions,it.mytasks.get(j).session,CASE_SENSITIVE) == null) {
                    cur_session = Utility.get_by_name(it.sessions,it.mytasks.get(j).session,CASE_SENSITIVE);
                    if (cur_session != null) {
                        if ("".equals(cur_session.tree.get(0).mu)) {
                            cur_session.tree.get(0).mu = it.mytasks.get(j).mu;
                        } else if (!cur_session.tree.get(0).mu.equals(it.mytasks.get(j).mu)) {
                            if ( Utility.get_by_name(it.mus, cur_session.tree.get(0).mu,CASE_SENSITIVE).node.toUpperCase().equals( Utility.get_by_name(it.mus, it.mytasks.get(j).mu,CASE_SENSITIVE).node.toUpperCase())) {
                                Utility.out.println(it.name + " : WARNING : Task dictates different mu than session : " + it.mytasks.get(j) + " : " + cur_session);
                            } else {
                                Utility.out.println(it.name + " : ERROR : Task dictates different mu than session : " + it.mytasks.get(j) + " : " + cur_session);
                            }
                        }
                        it.mysessions.add(cur_session);
                    }
                    else {
                        Utility.out.println("Missing session : " + it.mytasks.get(j).session + " current node : " + it.name + " task with session");
                    }
                }
                }
            }

            //get uprocs that are tasks
            for (int j = 0; j < it.mytasks.size(); j++) {
                if ("".equals(it.mytasks.get(j).session)) {
                    cur_uproc = Utility.get_by_name(it.uprocs, it.mytasks.get(j).uproc,CASE_SENSITIVE);
                    if (cur_uproc != null) {
                        if (Utility.get_by_name(it.myuprocs, it.mytasks.get(j).uproc,CASE_SENSITIVE) == null) {
                            it.myuprocs.add(cur_uproc);
                        }
                    }
                    else {
                        Utility.out.println("Missing uproc : " + it.mytasks.get(j).uproc + " current node : " + it.name + " task with uproc (1/3)");
                    }
                }
            }

            //resolve mu to nodename for uprocs in sessions
            for (int j = 0; j < it.mysessions.size(); j++) {
                for (int k = 0; k < it.mysessions.get(j).tree.size(); k++) {
                    estimation = get_context_tree(it.mysessions.get(j).tree, k, it, _parsed_data);
                    if (estimation != null) {
                        cur_node_name = estimation[0];
                        if (!"".equals(cur_node_name)) {
                            it.mysessions.get(j).tree.get(k).node = cur_node_name;
                            if (!"".equals(it.mysessions.get(j).tree.get(k).mu)) {
                                if (!estimation[1].equals(it.mysessions.get(j).tree.get(k).mu)) {
                                    Utility.out.println("Check MU estimation for "+ k + " in session " + it.mysessions.get(j));
                                }
                            }
                            else {
                                it.mysessions.get(j).tree.get(k).mu = estimation[1];
                            }
                        } else {
                            it.mysessions.get(j).tree.get(k).node = "";
                            Utility.out.println(it.name + " ERROR: MISSING MU : " + it.mysessions.get(j).tree.get(k).mu + " for " + it.mysessions.get(j).tree.get(k).uproc + " in " + it.mysessions.get(j).name);
                        }
                    }
                }
            }
            
            //            
            ArrayList<String> shared_tasks;
            
            //get uprocs from sessions
            for (int j = 0; j < it.mysessions.size(); j++) {
                shared_tasks = new ArrayList<>();
                for (task_obj tt : it.mytasks) {
                    if (tt.session.equals(it.mysessions.get(j).name)) {
                        shared_tasks.add(tt.name);
                    }
                }
                
                for (int k = 0; k < it.mysessions.get(j).tree.size(); k++) {
                    cur_node = Utility.get_by_name(_parsed_data, it.mysessions.get(j).tree.get(k).node,CASE_SENSITIVE);
                    if (cur_node != null) {
                        cur_uproc = Utility.get_by_name(cur_node.uprocs, it.mysessions.get(j).tree.get(k).uproc,CASE_SENSITIVE);
                        if (cur_uproc != null) {
                            for (String task_name : shared_tasks) {
                                cur_uproc.origin_tasks.add(new connection_obj(it.name,task_name,it.mysessions.get(j).name,it.mysessions.get(j).tree.get(0).uproc,it.mysessions.get(j).tree.get(k).mu));
                            }
                            if (Utility.get_by_name(cur_node.myuprocs, cur_uproc.name,CASE_SENSITIVE) == null) {
                                cur_node.myuprocs.add(cur_uproc);
                            }
                        }
                        else {
                            Utility.out.println("Missing uproc : " + it.mysessions.get(j).tree.get(k).uproc + " on " + cur_node.name + " current node : " + it.name + " current ses : " + it.mysessions.get(j).name + " (1/3)");
                        }
                    } else {
                        Utility.out.println(it.name + " ERROR: MISSING node : " + it.mysessions.get(j).tree.get(k).node + " for uproc : " + it.mysessions.get(j).tree.get(k).uproc + " (" + it.mysessions.get(j).tree.get(k).mu + ") from session : " + it.mysessions.get(j).name);
                    }
                }
            }

            mysessum += it.mysessions.size();

        }
        Utility.out.println("\n\ntotal : " + mysum + " tasks ( " + mysumsum + " ) " + mysessum + " sessions\n\n");

        for (node_obj it : _parsed_data) {
            //resolve mu to nodename for uproc dependencies and notsim
            for (int j = 0; j < it.myuprocs.size(); j++) {
                for (int k = 0; k < it.myuprocs.get(j).notsim.size(); k++) {
                    if ("SAME_MU".equals(it.myuprocs.get(j).notsim.get(k).mu)) {
                        it.myuprocs.get(j).notsim.get(k).node = it.name;
                    } else {
                        for (int l = 0; l < it.mus.size(); l++) {
                            if (it.mus.get(l).name.equals(it.myuprocs.get(j).notsim.get(k).mu)) {
                                it.myuprocs.get(j).notsim.get(k).node = it.mus.get(l).node;
                            }
                        }
                    }
                }
                for (int k = 0; k < it.myuprocs.get(j).dependencies.size(); k++) {
                    if ("SAME_MU".equals(it.myuprocs.get(j).dependencies.get(k).mu)) {
                        it.myuprocs.get(j).dependencies.get(k).node = it.name;
                    } else {
                        for (int l = 0; l < it.mus.size(); l++) {
                            if (it.mus.get(l).name.equals(it.myuprocs.get(j).dependencies.get(k).mu)) {
                                it.myuprocs.get(j).dependencies.get(k).node = it.mus.get(l).node;
                            }
                        }
                    }
                }
            }

            ArrayList<class_obj> cur_classes = new ArrayList<>();
            //resolve classes to conditions
            //first create list of members for each class
            for (int j = 0; j < it.myuprocs.size(); j++) {
                if (!"".equals(it.myuprocs.get(j).myclass)) {
                    cur_classes = add_cl(cur_classes, it.myuprocs.get(j));
                }
            }

            for (int j = 0; j < it.myuprocs.size(); j++) {
                for (int k = 0; k < it.myuprocs.get(j).incompatibilities.size(); k++) {
                    for (int l = 0; l < cur_classes.size(); l++) {
                        if (it.myuprocs.get(j).incompatibilities.get(k).equals(cur_classes.get(l).name)) {
                            for (int m = 0; m < cur_classes.get(l).members.size(); m++) {
                                it.myuprocs.get(j).c_class.add(new dep_obj(cur_classes.get(l).members.get(m), "ANY_SESSION", "", it.name, "inc_class:" + it.myuprocs.get(j).incompatibilities.get(k)));
                            }
                        }
                    }
                }
            }

            //find out which uprocs use logical resources
            for (int j = 0; j < it.myuprocs.size(); j++) {
                for (int k = 0; k < it.myuprocs.get(j).resources.size(); k++) {
                    if ("LOGICAL".equals(it.myuprocs.get(j).resources.get(k).type)) {
                        for (int m = 0; m < it.resources.size(); m++) {
                            if (it.resources.get(m).name.equals(it.myuprocs.get(j).resources.get(k).name)) {
                                if (!it.resources.get(m).used_by.contains(it.myuprocs.get(j).name)) {
                                    it.resources.get(m).used_by.add(it.myuprocs.get(j).name);
                                }
                            }
                        }
                    }
                }

                for (int k = 1; k <= it.myuprocs.get(j).IS.size(); k++) {
                    if (it.myuprocs.get(j).IS.get(k).toUpperCase().contains(" RES=")) {
                        for (int m = 0; m < it.resources.size(); m++) {
                            //if (it.myuprocs.get(j).IS.get(k).toUpperCase().contains(it.resources.get(m).name.toUpperCase())) {
                                //utility.out.println("LOIII : " + it.resources.get(m).name + " : " + it.myuprocs.get(j).IS.get(k));
                            //}

                            if (it.myuprocs.get(j).IS.get(k).replace("\"", "").replace("\\", "").toUpperCase().contains(" RES=" + it.resources.get(m).name.toUpperCase())) {
                                if ((!it.myuprocs.get(j).IS.get(k).trim().startsWith("#")) && (!it.myuprocs.get(j).IS.get(k).trim().startsWith("::")) && (!it.myuprocs.get(j).IS.get(k).trim().startsWith("REM ")) && (!it.myuprocs.get(j).IS.get(k).trim().startsWith("echo")) ) {
                                    if (!it.resources.get(m).used_by.contains(it.myuprocs.get(j).name)) {
                                        it.resources.get(m).used_by.add(it.myuprocs.get(j).name);
                                    }
                                    //utility.out.println("resource used in cl_int : " + it.resources.get(m).name + " : " + it.myuprocs.get(j).IS.get(k));
                                }
                            }
                        }
                    }
                }
            }
            
            //resolve to dep_obj like not sim
            for (int j = 0; j < it.resources.size(); j++) {
                for (int k = 0; k < it.resources.get(j).used_by.size(); k++) {
                    cur_uproc = Utility.get_by_name(it.myuprocs, it.resources.get(j).used_by.get(k),CASE_SENSITIVE);
                    for (int n = 0; n < it.resources.get(j).used_by.size(); n++) {
                        cur_uproc.c_resources.add(new dep_obj(it.resources.get(j).used_by.get(n), "ANY_SESSION", "", it.name, "resource:" + it.resources.get(j).name));
                    }
                }
            }
        }

        //+------------------------------------------------------------------------------------------------------------------------------+
        
        Integer[] p_scr;
        p_scr = process_scripts();
        
        ux_sum = p_scr[0];
        ux_est = p_scr[1];
        
        //+------------------------------------------------------------------------------------------------------------------------------+
   
        process_executions(executions_file);
        
        //+------------------------------------------------------------------------------------------------------------------------------+

        //sum uproc executions for tasks
        for (node_obj it : _parsed_data) {
            for (int j = 0; j < it.mytasks.size(); j++) {

                int exec_sums = 0;
                int uproc_sum = 0;

                if ("".equals(it.mytasks.get(j).session)) {
                    cur_uproc = Utility.get_by_name(it.myuprocs, it.mytasks.get(j).uproc,CASE_SENSITIVE);
                    if (cur_uproc == null) {
                        Utility.out.println("Missing uproc : " + it.mytasks.get(j).uproc + " current node : " + it.name + " task with uproc (2/3)");
                    } else {
                        if (cur_uproc.execs == null) {
                            if ( DISPLAY_NO_COUNT ) { Utility.out.println("No count on node " + it.name + " for " + cur_uproc.name); }
                        } else {
                            exec_sums += cur_uproc.execs;
                        }
                        uproc_sum++;
                    }
                } else {
                    cur_session = Utility.get_by_name(it.mysessions, it.mytasks.get(j).session,CASE_SENSITIVE);
                    for (int k = 0; k < cur_session.tree.size(); k++) {
                        if (cur_session.tree.get(k).node == null) {
                            Utility.out.println("Missing mu : " + cur_session.tree.get(k).mu + " current node : " + it.name + " current ses : " + cur_session.name + " current upr : " + cur_session.tree.get(k).uproc);
                        } else {
                            cur_node = Utility.get_by_name(_parsed_data, cur_session.tree.get(k).node,CASE_SENSITIVE);
                            if (cur_node == null) {
                                Utility.out.println("Missing node : " + cur_session.tree.get(k).node + " current node : " + it.name + " current ses : " + cur_session.name + " current upr : " + cur_session.tree.get(k).uproc);
                            } else {
                                cur_uproc = Utility.get_by_name(cur_node.myuprocs, cur_session.tree.get(k).uproc,CASE_SENSITIVE);
                                if (cur_uproc == null) {
                                    Utility.out.println("Missing uproc : " + cur_session.tree.get(k).uproc + " on " + cur_node.name + " current node : " + it.name + " current ses : " + cur_session.name + " (2/3)");
                                } else if (cur_uproc.execs == null) {
                                    if ( DISPLAY_NO_COUNT ) { Utility.out.println("No count on node " + cur_node.name + " for " + cur_uproc.name); }
                                } else {
                                    exec_sums += cur_uproc.execs;
                                }
                                uproc_sum++;
                            }
                        }
                    }
                }

                it.mytasks.get(j).execs = exec_sums;
                it.mytasks.get(j).number_of_uprocs = uproc_sum;
            }
        }
        
        Utility.out.println("\n-------------------------------------------------------------------------------------");
        
        //+------------------------------------------------------------------------------------------------------------------------------+

        //resolve uproc connections to task connections
        ArrayList<connection_obj> resulting_connections, est_connections;
        for (node_obj it : _parsed_data) {
            for (int j = 0; j < it.mytasks.size(); j++) {
                if ("".equals(it.mytasks.get(j).session)) {
                    cur_uproc = Utility.get_by_name(it.myuprocs, it.mytasks.get(j).uproc,CASE_SENSITIVE);
                    if (cur_uproc == null) {
                        Utility.out.println("Missing uproc : " + it.mytasks.get(j).uproc + " current node : " + it.name + " task with uproc (3/3)");
                    } else {
                        resulting_connections = process_uproc(it.name, it.mytasks.get(j), cur_uproc, it.mytasks.get(j).mu);
                        for (connection_obj newcon1 : resulting_connections) {
                            it.mytasks.get(j).con.add(newcon1);
                        }
                        est_connections = process_estimates(it.name, cur_uproc);
                        for (connection_obj newcon2 : est_connections) {
                            it.mytasks.get(j).con_estimate.add(newcon2);
                        }
                    }
                } else {
                    cur_session = Utility.get_by_name(it.mysessions, it.mytasks.get(j).session,CASE_SENSITIVE);
                    for (int k = 0; k < cur_session.tree.size(); k++) {
                        if (cur_session.tree.get(k).node == null) {
                            Utility.out.println("Missing mu : " + cur_session.tree.get(k).mu + " current node : " + it.name + " current ses : " + cur_session.name + " current upr : " + cur_session.tree.get(k).uproc);
                        } else {
                            cur_node = Utility.get_by_name(_parsed_data, cur_session.tree.get(k).node,CASE_SENSITIVE);
                            if (cur_node == null) {
                                Utility.out.println("Missing node : " + cur_session.tree.get(k).node + " current node : " + it.name + " current ses : " + cur_session.name + " current upr : " + cur_session.tree.get(k).uproc);
                            } else {
                                cur_uproc = Utility.get_by_name(cur_node.myuprocs, cur_session.tree.get(k).uproc,CASE_SENSITIVE);
                                if (cur_uproc == null) {
                                    Utility.out.println("Missing uproc : " + cur_session.tree.get(k).uproc + " on " + cur_node.name + " current node : " + it.name + " current ses : " + cur_session.name + " (3/3)");
                                } else {
                                    resulting_connections = process_uproc(it.name, it.mytasks.get(j), cur_uproc, cur_session.tree.get(k).mu);
                                    for (connection_obj newcon1 : resulting_connections) {
                                        it.mytasks.get(j).con.add(newcon1);
                                    }
                                    est_connections = process_estimates(it.name, cur_uproc);
                                    for (connection_obj newcon2 : est_connections) {
                                        it.mytasks.get(j).con_estimate.add(newcon2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //+------------------------------------------------------------------------------------------------------------------------------+
        //+------------------------------------------------------------------------------------------------------------------------------+

        Utility.out.println("\n");

        int tot_ts = 0;
        int tot_d_ts = 0;
        int tot_e_ts = 0;
        int tot_ss = 0;
        int tot_uu = 0;

        //all connections
        for (node_obj it : _parsed_data) {
            for (int k = 0; k < it.mytasks.size(); k++) {
                //utility.out.println(it.mytasks.get(k).session+"|"+it.mytasks.get(k).uproc + " : "+ it.mytasks.get(k).con.size());
                if (it.mytasks.get(k).con.size() > 0) {
                    tot_d_ts++;
                }
                else if (it.mytasks.get(k).con_estimate.size() > 0) {
                    tot_e_ts++;
                }
            }
            tot_ts += it.mytasks.size();
            tot_uu += it.myuprocs.size();
            tot_ss += it.mysessions.size();
        }
        
        Utility.out.println("\n\nTotal tasks:    " + tot_ts);
        Utility.out.println("Total sessions: " + tot_ss);
        Utility.out.println("Total uprocs:   " + tot_uu);
        Utility.out.println("\n\nTotal tasks which are a connection source (includes self-connection): " + tot_d_ts +" +"+tot_e_ts+ " (estimated)\nuxordre unchecked: " + (ux_sum - ux_est) + "/" + ux_sum);

        /*
        for (node_obj it : _parsed_data) {
          Utility.out.println("----------------------\n"+it.name);
          Utility.out.println("Tasks: " + it.tasks.size() + " -> "+ it.mytasks.size());
          for (task_obj t1 : it.mytasks) {
              Utility.out.println(t1);
          }
          Utility.out.println("Uprocs: " + it.uprocs.size() + " -> "+ it.myuprocs.size());
          for (uproc_obj u1 : it.myuprocs) {
              Utility.out.println(u1);
          }
        }
        */
        
        ArrayList<node_obj> res = du_out;
        
        du_out = null;
        
        return res;
    }
}
