/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du_xml;

import du_objects.task_obj;
import du_objects.ses_tree_obj;
import du_objects.node_obj;
import du_objects.session_obj;
import du_objects.uproc_obj;
import du_objects.resource_obj;
import du_objects.mu_obj;
import du_objects.dep_obj;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import main.Utility;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author t.fotakis
 */
public final class du_xml_parser {

    private static ArrayList<ses_tree_obj> mytmp;
    
    private du_xml_parser() { }
    
    
    private static Element getDirectChild(Element parent, String name) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && name.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }
    
    private static void rec_tree(Element myroot, String parentmu, String myparent, int myparentID, String con_type) {

        if (myroot == null) {
            Utility.out.println("NULL DETECTED");
        }

        Element tempE;
        String mymu, myname;
        tempE = getDirectChild(myroot, "data");

        myname = tempE.getElementsByTagName("uprocName").item(0).getTextContent();

        if ("MU".equals(tempE.getElementsByTagName("type").item(0).getTextContent())) {
            mymu = tempE.getElementsByTagName("muName").item(0).getTextContent();
        } else {
            mymu = parentmu;
        }

        mytmp.add(new ses_tree_obj(myname, tempE.getElementsByTagName("type").item(0).getTextContent(), mymu, myparent, myparentID, false, con_type));

        int mid = mytmp.size() - 1;

        tempE = getDirectChild(myroot, "childOk");
        if (tempE != null) {
            rec_tree(tempE, mymu, myname, mid, "childOk");
        }

        tempE = getDirectChild(myroot, "childKo");
        if (tempE != null) {
            rec_tree(tempE, mymu, myname, mid, "childKo");
        }

        tempE = getDirectChild(myroot, "nextSibling");
        if (tempE != null) {
            rec_tree(tempE, parentmu, myparent, myparentID, "nextSibling");
        }

    }
        
    private static boolean check_LOI(String line) {
	return ( (line.toUpperCase().contains("UXEXE")) || (line.toUpperCase().contains("UNI_DIR_EXEC")) );
    }
    
    public static ArrayList<node_obj> run(String path,String company) throws Exception {

        ArrayList<node_obj> du = new ArrayList<>();
        
        File dir = new File(path);
        File[] listOfFiles = dir.listFiles();

        LinkedHashMap<String, String> task_vars;
        ArrayList<session_obj> ss;
        ArrayList<mu_obj> mm;
        ArrayList<task_obj> tt;
        ArrayList<resource_obj> rr;
        ArrayList<uproc_obj> uu;
        String nodename, tmp, task_session, task_uproc, task_is_template, task_type, task_mu, task_active, new_l,tt_queue,tt_optional,t_name;
        ArrayList<String> task_launch;
        ArrayList<String> tt_rule;
        uproc_obj utmp;
        dep_obj cur_dep;
        
        Pattern p = Pattern.compile(company);

        for (File listOfFile : listOfFiles) {
            if ((listOfFile.isFile()) && (listOfFile.getName().endsWith(".xml"))) {

                ss = new ArrayList<>();
                mm = new ArrayList<>();
                tt = new ArrayList<>();
                uu = new ArrayList<>();
                rr = new ArrayList<>();
                //nodename = listOfFile.getName().replace("_X_Full_Export.xml", "").replace(company+"_", "");

                try {
                    Utility.out.println("Start parsing file \"" + listOfFile.getName() + "\" ...");
                    
                    Matcher m1 = p.matcher(listOfFile.getName());
                    
                    if (!m1.find()) {
                        Utility.out.println("No match for nodename!");
                    }
                    else {
                    nodename =  m1.group(1);
                    

                    Utility.out.println("Nodename : " + nodename);
                    

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(listOfFile);
                    doc.getDocumentElement().normalize();

                    Utility.out.println("Finished parsing!\n");

                    Node nPar, nNode, nNode2, tempNode;
                    NodeList nList, nList2, nList3;
                    Element myElem, eElement, eElement2, tempElem, tempElem2;

                    nPar = doc.getElementsByTagName("mus").item(0);
                    myElem = (Element) nPar;
                    nList = myElem.getElementsByTagName("com.orsyp.ExportMu");
                    for (int temp = 0; temp < nList.getLength(); temp++) {
                        nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            eElement = (Element) nNode;
                            eElement2 = getDirectChild(eElement, "mu");
                            eElement2 = getDirectChild(eElement2, "identifier");
                            mm.add(new mu_obj(eElement2.getElementsByTagName("name").item(0).getTextContent(), eElement.getElementsByTagName("nodeName").item(0).getTextContent()));
                        }
                    }

                    nPar = doc.getElementsByTagName("com.orsyp.ExportObjects").item(0);
                    myElem = getDirectChild((Element) nPar, "resources");
                    nList = myElem.getElementsByTagName("Resource");
                    for (int temp = 0; temp < nList.getLength(); temp++) {
                        nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            eElement = (Element) nNode;
                            eElement2 = getDirectChild(eElement, "identifier");
                            rr.add(new resource_obj(eElement2.getElementsByTagName("name").item(0).getTextContent(), eElement.getElementsByTagName("nature").item(0).getTextContent()));
                        }
                    }

                    nPar = doc.getElementsByTagName("sessions").item(0);
                    myElem = (Element) nPar;
                    nList = myElem.getElementsByTagName("Session");
                    for (int temp = 0; temp < nList.getLength(); temp++) {
                        nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            eElement = (Element) nNode;
                            eElement2 = getDirectChild(eElement, "identifier");
                            tmp = eElement2.getElementsByTagName("name").item(0).getTextContent();
                            eElement2 = getDirectChild(eElement, "tree");
                            mytmp = new ArrayList<>();
                            tempElem = getDirectChild(eElement2, "root");
                            rec_tree(tempElem, "", "", -1, "");
                            eElement2 = getDirectChild(eElement, "label");

                            ss.add(new session_obj(tmp, mytmp, eElement2.getTextContent()));
                        }
                    }
                    mytmp = null;

                    nPar = doc.getElementsByTagName("tasks").item(0);
                    myElem = (Element) nPar;
                    nList = myElem.getElementsByTagName("Task");
                    for (int temp = 0; temp < nList.getLength(); temp++) {
                        nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            eElement = (Element) nNode;
                            eElement2 = getDirectChild(eElement, "identifier");

                            if (eElement2.getElementsByTagName("name").getLength() == 0) {
                                t_name = "";
                            } else {
                                t_name = eElement2.getElementsByTagName("name").item(0).getTextContent();
                            }
                            task_session = eElement2.getElementsByTagName("sessionName").item(0).getTextContent();
                            task_uproc = eElement2.getElementsByTagName("uprocName").item(0).getTextContent();
                            task_is_template = eElement.getElementsByTagName("template").item(0).getTextContent();
                            task_type = getDirectChild(eElement, "specificData").getAttribute("class");
                            task_mu = eElement2.getElementsByTagName("muName").item(0).getTextContent();
                            task_active = getDirectChild(eElement, "active").getTextContent();
                            tt_queue = getDirectChild(eElement, "queue").getTextContent();
                            task_vars = new LinkedHashMap<>();
                            
                            tt_optional = "false";
                            tt_rule = new ArrayList<>();

                            task_launch = new ArrayList<>();

                            if ("TaskPlanified".equals(task_type)) {
                                
                                tempElem = getDirectChild(eElement, "specificData");
                                
                                tt_optional = getDirectChild(tempElem,"optional").getTextContent();

                                nList2 = tempElem.getElementsByTagName("com.orsyp.api.task.TaskImplicitData");
                                for (int j = 0; j < nList2.getLength(); j++) {
                                    nNode2 = nList2.item(j);
                                    tempElem2 = getDirectChild((Element) nNode2,"identifier");
                                    tt_rule.add(getDirectChild(tempElem2,"name").getTextContent());
                                }

                                if ( ("false".equals(task_is_template)) ) { //  && ("true".equals(task_active))
                                    
                                    Element patterns = getDirectChild(tempElem, "launchHourPatterns"); // DU 6
                                    Element relaunch = getDirectChild(tempElem, "autoRelaunch"); // DU 6
                                    nList2 = eElement.getElementsByTagName("launchTime"); // embeddedLaunchTime DU 6 || simpleLaunches om.orsyp.api.task.SimpleLaunch DU 5
                                    nList3 = eElement.getElementsByTagName("launchTimes"); // multipleLaunch DU 5
                                    
                                    if (patterns != null) {
                                        nList2 = patterns.getElementsByTagName("startTime");
                                        for (int j = 0; j < nList2.getLength(); j++) {
                                            nNode2 = nList2.item(j);
                                            if (!task_launch.contains(nNode2.getTextContent().replace(" ","0"))) {
                                                task_launch.add(nNode2.getTextContent().replace(" ","0"));
                                            }
                                        }
                                    }
                                    else if (relaunch != null) {
                                        tempElem2 = getDirectChild(relaunch, "from");
                                        String tth,ttm;
                                        tth = getDirectChild(tempElem2, "hour").getTextContent();
                                        if (tth.length() == 1) {
                                            tth = "0"+tth;
                                        }
                                        ttm = getDirectChild(tempElem2, "minute").getTextContent();
                                        if (ttm.length() == 1) {
                                            ttm = "0"+ttm;
                                        }
                                        task_launch.add(tth + ttm + "00");
                                    }
                                    else if (nList2.getLength() > 0) {
                                        for (int j = 0; j < nList2.getLength(); j++) {
                                            nNode2 = nList2.item(j);
                                            if (!task_launch.contains(nNode2.getTextContent())) {
                                                task_launch.add(nNode2.getTextContent());
                                            }
                                        }
                                    }
                                    else if (nList3.getLength() > 0) {
                                        nList2 = ((Element) eElement.getElementsByTagName("launchTimes").item(0)).getElementsByTagName("string");
                                        for (int j = 0; j < nList2.getLength(); j++) {
                                            nNode2 = nList2.item(j);
                                            new_l = nNode2.getTextContent() + "00";

                                            if (!task_launch.contains(new_l)) {
                                                task_launch.add(new_l);
                                            }
                                        }
                                    }
                                    else {
                                        //utility.out.println("No launches : "+t_name);
                                    }

                                }
                            } else if ("TaskProvoked".equals(task_type)) {
                                if ( ("false".equals(task_is_template)) ) {
                                    tempElem = getDirectChild(eElement, "specificData");
                                    tempElem2 = getDirectChild(tempElem, "startLaunchTime");
                                    if (!"999999".equals(tempElem2.getTextContent())) {
                                        task_launch.add(tempElem2.getTextContent());
                                    }
                                }
                            } else if (!"TaskCyclic".equals(task_type)) {
                                Utility.out.println(nodename + " [" + task_session + "," + task_uproc + "] : [" + task_type + "] : INVALID_TYPE");
                            }
                            
                            myElem = getDirectChild(eElement,"variables");
                            if (myElem != null){
				nList2 = myElem.getElementsByTagName("com.orsyp.api.VariableText");
				for (int j=0;j<nList2.getLength();j++){
					nNode2 = nList2.item(j);
					eElement2 = (Element) nNode2;
					if (eElement2.getElementsByTagName("name").getLength()>0){
						tempElem=getDirectChild(eElement2,"value");
						task_vars.put(eElement2.getElementsByTagName("name").item(0).getTextContent(),tempElem.getTextContent());
					}
				}
				
				nList2 = myElem.getElementsByTagName("com.orsyp.api.VariableNumeric");
				for (int j=0;j<nList2.getLength();j++){
					nNode2 = nList2.item(j);
					eElement2 = (Element) nNode2;
					if (eElement2.getElementsByTagName("name").getLength()>0){
						tempElem=getDirectChild(eElement2,"value");
						task_vars.put(eElement2.getElementsByTagName("name").item(0).getTextContent(),tempElem.getTextContent());
					}
                                }
                            }
                            
                            
                            
                            tt.add(new task_obj(task_session, task_uproc, task_launch, task_active, task_mu, task_is_template, task_type, eElement.getElementsByTagName("isUprocHeader").item(0).getTextContent(),task_vars,tt_queue,tt_rule,tt_optional,t_name));
                        }
                    }

                    nPar = doc.getElementsByTagName("uprocs").item(0);
                    myElem = (Element) nPar;
                    nList = myElem.getElementsByTagName("com.orsyp.ExportUproc");
                    for (int temp = 0; temp < nList.getLength(); temp++) {

                        nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            utmp = new uproc_obj();

                            eElement = (Element) nNode;
                            eElement = getDirectChild(eElement, "uproc");

                            //name
                            eElement2 = getDirectChild(eElement, "identifier");
                            tmp = eElement2.getElementsByTagName("name").item(0).getTextContent();
                            utmp.name = tmp;

                            //class
                            eElement2 = getDirectChild(eElement, "uprocClass");
                            utmp.myclass = eElement2.getTextContent();

                            //incompatibilities
                            myElem = getDirectChild(eElement, "incompatibilities");
                            if (myElem != null) {

                                nList2 = myElem.getElementsByTagName("com.orsyp.api.uproc.Incompatibility");
                                for (int j = 0; j < nList2.getLength(); j++) {
                                    nNode2 = nList2.item(j);
                                    eElement2 = (Element) nNode2;
                                    if (eElement2.getElementsByTagName("uprocClass").getLength() > 0) {
                                        utmp.incompatibilities.add(eElement2.getElementsByTagName("uprocClass").item(0).getTextContent());
                                    }
                                }

                            }

                            //successors
                            myElem = getDirectChild(eElement, "successors");
                            if (myElem != null) {

                                nList2 = myElem.getElementsByTagName("Successor");
                                for (int j = 0; j < nList2.getLength(); j++) {
                                    nNode2 = nList2.item(j);
                                    eElement2 = (Element) nNode2;
                                    if (eElement2.getElementsByTagName("name").getLength() > 0) {
                                        utmp.successors.add(eElement2.getElementsByTagName("name").item(0).getTextContent());
                                    }
                                }

                            }

                            //resources
                            myElem = getDirectChild(eElement, "resources");
                            if (myElem != null) {

                                nList2 = myElem.getElementsByTagName("com.orsyp.api.uproc.ResourceCondition");
                                for (int j = 0; j < nList2.getLength(); j++) {
                                    nNode2 = nList2.item(j);
                                    eElement2 = (Element) nNode2;
                                    if (eElement2.getElementsByTagName("resource").getLength() > 0) {
                                        tempElem = getDirectChild(eElement2, "type");
                                        utmp.resources.add(new resource_obj(eElement2.getElementsByTagName("resource").item(0).getTextContent(), tempElem.getTextContent()));
                                    }
                                }

                            }

                            //variables
                            myElem = getDirectChild(eElement, "variables");
                            if (myElem != null) {

                                nList2 = myElem.getElementsByTagName("com.orsyp.api.VariableText");
                                for (int j = 0; j < nList2.getLength(); j++) {
                                    nNode2 = nList2.item(j);
                                    eElement2 = (Element) nNode2;
                                    if (eElement2.getElementsByTagName("name").getLength() > 0) {
                                        tempElem = getDirectChild(eElement2, "value");
                                        utmp.variables.put(eElement2.getElementsByTagName("name").item(0).getTextContent(), tempElem.getTextContent());
                                    }
                                }

                                nList2 = myElem.getElementsByTagName("com.orsyp.api.VariableNumeric");
                                for (int j = 0; j < nList2.getLength(); j++) {
                                    nNode2 = nList2.item(j);
                                    eElement2 = (Element) nNode2;
                                    if (eElement2.getElementsByTagName("name").getLength() > 0) {
                                        tempElem = getDirectChild(eElement2, "value");
                                        utmp.variables.put(eElement2.getElementsByTagName("name").item(0).getTextContent(), tempElem.getTextContent());
                                    }
                                }

                            }

                            //nonSimultaneities
                            nList2 = eElement.getElementsByTagName("com.orsyp.api.uproc.NonSimultaneityCondition");
                            for (int j = 0; j < nList2.getLength(); j++) {
                                nNode2 = nList2.item(j);
                                eElement2 = (Element) nNode2;
                                if (eElement2.getElementsByTagName("uproc").getLength() > 0) {

                                    cur_dep = new dep_obj();

                                    assert eElement2.getElementsByTagName("uproc").getLength() == 1;

                                    cur_dep.uproc = eElement2.getElementsByTagName("uproc").item(0).getTextContent();

                                    assert eElement2.getElementsByTagName("sessionControl").getLength() == 1;
                                    tempNode = eElement2.getElementsByTagName("sessionControl").item(0);
                                    tempElem = (Element) tempNode;

                                    if ("SPECIFIC_SESSION".equals(tempElem.getElementsByTagName("type").item(0).getTextContent())) {
                                        cur_dep.session = tempElem.getElementsByTagName("session").item(0).getTextContent();
                                    } else {
                                        cur_dep.session = tempElem.getElementsByTagName("type").item(0).getTextContent();
                                    }

                                    assert eElement2.getElementsByTagName("muControl").getLength() == 1;
                                    tempNode = eElement2.getElementsByTagName("muControl").item(0);
                                    tempElem = (Element) tempNode;

                                    if ("SPECIFIC_MU".equals(tempElem.getElementsByTagName("type").item(0).getTextContent())) {
                                        cur_dep.mu = tempElem.getElementsByTagName("mu").item(0).getTextContent();
                                    } else {
                                        cur_dep.mu = tempElem.getElementsByTagName("type").item(0).getTextContent();
                                    }

                                    utmp.notsim.add(cur_dep);

                                }
                            }

                            //dependencies
                            nList2 = eElement.getElementsByTagName("com.orsyp.api.uproc.DependencyCondition");
                            for (int j = 0; j < nList2.getLength(); j++) {
                                nNode2 = nList2.item(j);
                                eElement2 = (Element) nNode2;
                                if (eElement2.getElementsByTagName("uproc").getLength() > 0) {
                                    cur_dep = new dep_obj();

                                    assert eElement2.getElementsByTagName("uproc").getLength() == 1;

                                    cur_dep.uproc = eElement2.getElementsByTagName("uproc").item(0).getTextContent();

                                    assert eElement2.getElementsByTagName("sessionControl").getLength() == 1;
                                    tempNode = eElement2.getElementsByTagName("sessionControl").item(0);
                                    tempElem = (Element) tempNode;

                                    if ("SPECIFIC_SESSION".equals(tempElem.getElementsByTagName("type").item(0).getTextContent())) {
                                        cur_dep.session = tempElem.getElementsByTagName("session").item(0).getTextContent();
                                    } else {
                                        cur_dep.session = tempElem.getElementsByTagName("type").item(0).getTextContent();
                                    }

                                    assert eElement2.getElementsByTagName("muControl").getLength() == 1;
                                    tempNode = eElement2.getElementsByTagName("muControl").item(0);
                                    tempElem = (Element) tempNode;

                                    if (tempElem.getElementsByTagName("type").item(0) != null) {
                                        if ("SPECIFIC_MU".equals(tempElem.getElementsByTagName("type").item(0).getTextContent())) {
                                            cur_dep.mu = tempElem.getElementsByTagName("mu").item(0).getTextContent();
                                        } else {
                                            cur_dep.mu = tempElem.getElementsByTagName("type").item(0).getTextContent();
                                        }
                                    } else if (tempElem.getElementsByTagName("mu").item(0) != null) {
                                        cur_dep.mu = tempElem.getElementsByTagName("mu").item(0).getTextContent();
                                        Utility.out.println("Warning: " + tmp + " : missing <type> on <muControl> on <com.orsyp.api.uproc.DependencyCondition> : Resolved to " + tempElem.getElementsByTagName("mu").item(0).getTextContent() + "");
                                    } else {
                                        cur_dep.mu = "!ERROR_MISSING!";
                                        Utility.out.println("ERROR: " + tmp + " : missing <type> on <muControl> on <com.orsyp.api.uproc.DependencyCondition>");
                                        //println tempElem.getTextContent()
                                    }

                                    utmp.dependencies.add(cur_dep);
                                }
                            }

                            //internalScript
                            myElem = getDirectChild(eElement, "type");
                            if ("CL_INT".equals(myElem.getTextContent())) {

                                myElem = getDirectChild(eElement, "internalScript");
                                if (myElem != null) {

                                    eElement2 = getDirectChild(myElem, "lines");
                                    nList2 = eElement2.getElementsByTagName("string");

                                    for (int j = 0; j < nList2.getLength(); j++) {
                                        nNode2 = nList2.item(j);
                                        
                                        //if (check_LOI(nNode2.getTextContent())){
                                            //utmp.LOI.put((j+1),nNode2.getTextContent().replace("\\\"", "\"").replace("\"", "\\\""));		
					//}
                                        
                                        utmp.LOI.put((j + 1), nNode2.getTextContent());
                                        utmp.IS.put((j + 1), nNode2.getTextContent().replace("\\\"", "\"").replace("\"", "\\\""));
                                    }
                                } else {
                                    Utility.out.println(tmp + " : TYPE CL_INT doesn't have internal script");
                                }
                            }

                            uu.add(utmp);
                        }
                    }

                    du.add(new node_obj(nodename, ss, mm, tt, uu, rr));
                    }

                } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
                    Utility.err.println(e);
                    Utility.err.println(listOfFile);
                    throw e;
                }

            }
        }
        
        return du;
    }
    
}
