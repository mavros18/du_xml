/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_xml;

import du_objects.connection_obj;
import du_objects.node_obj;
import du_objects.task_obj;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import main.Utility;

/**
 *
 * @author t.fotakis
 */
public final class du_xml_outp_files {
    
/*
    concon : select distinct from con

    the_connections_out                 ->      ( concon + concon_estimate ) && not self

    connection_details.js          	->	mytasks.con + mytasks.con_estimate
    tasks.csv                           ->      mytasks
    connections.csv                     ->      con + con_estimate
    connections_unique.csv              ->      concon + concon_estimate

*/
 
    private static boolean DISPLAY_NO_COUNT = false;
    private static boolean DISPLAY_SELF = false;
    private static boolean CREATE_JSON = false;
    
    private du_xml_outp_files() { }
    
    public static void set_flags(boolean count,boolean self,boolean json) {
        DISPLAY_NO_COUNT = count;
        DISPLAY_SELF = self;
        CREATE_JSON = json;
    }

    private static boolean contain_con(ArrayList<connection_obj> myconnections,connection_obj newconnection) {

        for (connection_obj c : myconnections) {
            if ( newconnection.name.equals(c.name) && newconnection.node.equals(c.node) ) {
                return true;
            }
        }
        return false;
    }

    public static void run(ArrayList<node_obj> final_data, String out_path) throws FileNotFoundException, UnsupportedEncodingException {
        
        
        String tmp_string, tmp_str2;

        //find 0 execution tasks
        for (node_obj it : final_data) {
            for (int j = 0; j < it.mytasks.size(); j++) {
                if (it.mytasks.get(j).execs == 0) {
                    if (DISPLAY_NO_COUNT) { 
                        Utility.out.println(it.name + "!" + it.mytasks.get(j).name + " has 0 executions");
                    }
                }

            }
        }
        
        //Check for multiple MU with same name
        LinkedHashMap<String, String[]> mu_m = new LinkedHashMap<>();

        for (node_obj it : final_data) {

            for (int k = 0; k < it.mus.size(); k++) {
                if (mu_m.get(it.mus.get(k).name) == null) {
                    mu_m.put(it.mus.get(k).name, new String[]{it.mus.get(k).node, it.name});

                } else if (!mu_m.get(it.mus.get(k).name)[0].toUpperCase().equals(it.mus.get(k).node.toUpperCase())) {
                    Utility.out.println("MU OVERLOAD : " + it.mus.get(k).name + " : " + mu_m.get(it.mus.get(k).name)[0] + " (" + mu_m.get(it.mus.get(k).name)[1] + ")" + " vs " + it.mus.get(k).node + " (" + it.name + ")");
                }

            }
        }
        
        //unique connections
        for (node_obj it : final_data) {
            for (int k = 0; k < it.mytasks.size(); k++) {
                for (int l = 0; l < it.mytasks.get(k).con.size(); l++) {
                    if (!contain_con(it.mytasks.get(k).concon,it.mytasks.get(k).con.get(l))) {
                        it.mytasks.get(k).concon.add(it.mytasks.get(k).con.get(l));
                    }
                }
            }
        }
        
        //unique estimated connections
        for (node_obj it : final_data) {
            for (int k = 0; k < it.mytasks.size(); k++) {
                for (int l = 0; l < it.mytasks.get(k).con_estimate.size(); l++) {
                    if (!contain_con(it.mytasks.get(k).concon_estimate,it.mytasks.get(k).con_estimate.get(l))) {
                        it.mytasks.get(k).concon_estimate.add(it.mytasks.get(k).con_estimate.get(l));
                    }
                }
            }
        }
        
        //+-----------------------------------------------------------------------------------------------------------------------------------------------------+

        //+------------------------------------------------------------------------------------------------------------------------------------------------------------+

        PrintWriter file9 = new PrintWriter(out_path + File.separator + "the_connections_out.txt", "UTF-8");

        file9.write("");

        for (node_obj it : final_data) {
            for (int k = 0; k < it.mytasks.size(); k++) {
                if ((it.mytasks.get(k).concon_estimate.size() > 0) || (it.mytasks.get(k).concon.size() > 0)) {
                    tmp_string = it.name + "!" + it.mytasks.get(k).name;
                    
                    for (int l = 0; l < it.mytasks.get(k).concon.size(); l++) {
                        tmp_str2 = it.mytasks.get(k).concon.get(l).node + "!" + it.mytasks.get(k).concon.get(l).name;

                        if (!tmp_str2.equals(tmp_string)) {
                            file9.append(tmp_string + "\t" + tmp_str2 + "\n");
                        } else {
                            if (DISPLAY_SELF) { Utility.out.println("omitted self connection " + tmp_string + " : " + tmp_str2); }
                        }
                    }

                    for (int l = 0; l < it.mytasks.get(k).concon_estimate.size(); l++) {
                        if ( (!contain_con(it.mytasks.get(k).concon,it.mytasks.get(k).concon_estimate.get(l))) ) {
                            tmp_str2 = it.mytasks.get(k).concon_estimate.get(l).node + "!" + it.mytasks.get(k).concon_estimate.get(l).name;

                            if (!tmp_str2.equals(tmp_string)) {
                                file9.append(tmp_string + "\t" + tmp_str2 + "\n");
                            } else {
                                if (DISPLAY_SELF) { Utility.out.println("omitted self connection " + tmp_string + " : " + tmp_str2); }
                            }
                        }
                    }
                }
            }
        }
        
        file9.close();

        //+------------------------------------------------------------------------------------------------------------------------------------------------------------+

        
//        file9 = new PrintWriter(out_path + File.separator + "tasks.csv", "UTF-8");
//
//        file9.write("node;name;session;uproc;num_of_uprocs;type;is_active;executions;connections\n");
//
//        for (node_obj it : final_data) {
//            for ( task_obj tt : it.mytasks) {
//                file9.append(it.name + ";" + tt.name + ";"+tt.session+";"+tt.uproc+";"+tt.number_of_uprocs+";"+tt.type+";"+tt.is_active+";"+tt.execs+";"+((tt.concon_estimate.size() > 0) || (tt.concon.size() > 0))+"\n");
//            }
//        }
//        
//        file9.close();
        
        //+------------------------------------------------------------------------------------------------------------------------------------------------------------+
        
        file9 = new PrintWriter(out_path + File.separator + "tasks.csv", "UTF-8");

        file9.write("node;name;session;uproc;num_of_uprocs;type;is_active;executions;connections;rules;launches\n");

        for (node_obj it : final_data) {
            for ( task_obj tt : it.mytasks) {
                file9.append(it.name + ";" + tt.name + ";"+tt.session+";"+tt.uproc+";"+tt.number_of_uprocs+";"+tt.type+";"+tt.is_active+";"+tt.execs+";"+((tt.concon_estimate.size() > 0) || (tt.concon.size() > 0))+";"+tt.rule+";"+tt.launch+"\n");
            }
        }
        
        file9.close();
        
        //+------------------------------------------------------------------------------------------------------------------------------------------------------------+
        
        file9 = new PrintWriter(out_path + File.separator + "connections.csv", "UTF-8");

        file9.write("source;destination;executions_source;executions_destination;is_estimation;with_self;origin\n");

        for (node_obj it : final_data) {
            for ( task_obj tt : it.mytasks) {
                for (connection_obj cc : tt.con) { 
                    file9.append(it.name + "!" + tt.name+ ";"+cc.node+"!"+cc.name +";"+tt.execs + ";"+cc.execs + ";"+"FALSE;"+(cc.node.equals(it.name) && cc.name.equals(tt.name))+";"+cc.origin+"\n");
                }
                for (connection_obj cc : tt.con_estimate) { 
                    file9.append(it.name + "!" + tt.name+ ";"+cc.node+"!"+cc.name +";"+tt.execs + ";"+cc.execs + ";"+"TRUE;"+(cc.node.equals(it.name) && cc.name.equals(tt.name))+";"+cc.origin+"\n");
                }
            }
        }
        
        file9.close();
        
        //+------------------------------------------------------------------------------------------------------------------------------------------------------------+
        
        file9 = new PrintWriter(out_path + File.separator + "connections_unique.csv ", "UTF-8");

        file9.write("source;destination;executions_source;executions_destination;is_estimation;with_self\n");

        for (node_obj it : final_data) {
            for ( task_obj tt : it.mytasks) {
                for (connection_obj cc : tt.concon) { 
                    file9.append(it.name + "!" + tt.name+ ";"+cc.node+"!"+cc.name +";"+tt.execs + ";"+cc.execs + ";"+"FALSE;"+(cc.node.equals(it.name) && cc.name.equals(tt.name))+"\n");
                }
                for (connection_obj cc : tt.concon_estimate) {
                    if (!contain_con(tt.concon,cc)) {
                        file9.append(it.name + "!" + tt.name+ ";"+cc.node+"!"+cc.name +";"+tt.execs + ";"+cc.execs + ";"+"TRUE;"+(cc.node.equals(it.name) && cc.name.equals(tt.name))+"\n");
                    }
                }
            }
        }
        
        file9.close();
        
        //+------------------------------------------------------------------------------------------------------------------------------------------------------------+
        
        file9 = new PrintWriter(out_path + File.separator + "connection_details.js", "UTF-8");

        file9.write("var du_connections=[");

        int fff = 0;

        for (node_obj it : final_data) {
            for (int k = 0; k < it.mytasks.size(); k++) {
                
                tmp_string = it.name + "!" + it.mytasks.get(k).name;

                for (int l = 0; l < it.mytasks.get(k).con.size(); l++) {
                    tmp_str2 = it.mytasks.get(k).con.get(l).node + "!" + it.mytasks.get(k).con.get(l).name;

                    if (fff == 0) {
                        fff = 1;
                        file9.append("\n{source:\"" + tmp_string + "\",destination:\"" + tmp_str2 + "\",description:\"" + it.mytasks.get(k).con.get(l).origin + "\",exec_s:" + it.mytasks.get(k).execs + ",exec_d:" + it.mytasks.get(k).con.get(l).execs + "}");
                    } else {
                        file9.append(",\n{source:\"" + tmp_string + "\",destination:\"" + tmp_str2 + "\",description:\"" + it.mytasks.get(k).con.get(l).origin + "\",exec_s:" + it.mytasks.get(k).execs + ",exec_d:" + it.mytasks.get(k).con.get(l).execs + "}");
                    }
                }

                for (int l = 0; l < it.mytasks.get(k).con_estimate.size(); l++) {
                    tmp_str2 = it.mytasks.get(k).con_estimate.get(l).node + "!" + it.mytasks.get(k).con_estimate.get(l).name;
                    
                    if (fff == 0) {
                        fff = 1;
                        file9.append("\n{source:\"" + tmp_string + "\",destination:\"" + tmp_str2 + "\",description:\"" + it.mytasks.get(k).con_estimate.get(l).origin + "\",exec_s:" + it.mytasks.get(k).execs + ",exec_d:" + it.mytasks.get(k).con_estimate.get(l).execs + "}");
                    } else {
                        file9.append(",\n{source:\"" + tmp_string + "\",destination:\"" + tmp_str2 + "\",description:\"" + it.mytasks.get(k).con_estimate.get(l).origin + "\",exec_s:" + it.mytasks.get(k).execs + ",exec_d:" + it.mytasks.get(k).con_estimate.get(l).execs + "}");
                    }
                }

            }
        }

        file9.print("];");
        
        file9.close();
        
        try (InputStream is = du_xml_outp_files.class.getClassLoader().getResourceAsStream("resources/connections.html")) {
            Files.copy(is, Paths.get(out_path + File.separator + "connections.html"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Utility.err.println(ex);
            Utility.err.printStackTrace(ex);
        }
        
        //+------------------------------------------------------------------------------------------------------------------------------------------------------------+

        if (CREATE_JSON) {
            
            try {
                Files.createDirectories(Paths.get(out_path + File.separator + "exports"));
                
                for (node_obj n : final_data) {
                    file9 = new PrintWriter(out_path + File.separator + "exports" + File.separator + n.name + "_out.json", "UTF-8");
                    file9.write("");
                    
                    file9.append("{\n\"uprocs\":\n"+Utility.toMyString(n.uprocs)+",\n");
                    file9.append("\"sessions\":\n"+Utility.toMyString(n.sessions)+",\n");
                    file9.append("\"tasks\":\n"+Utility.toMyString(n.tasks)+",\n");
                    file9.append("\"mus\":\n"+Utility.toMyString(n.mus)+",\n");
                    file9.append("\"resources\":\n"+Utility.toMyString(n.resources)+"\n}\n");
                    
                    file9.close();
                }
                
                
            } catch (IOException ex) {
                Utility.err.println(ex);
                Utility.err.printStackTrace(ex);
            }
        }
    }

}
