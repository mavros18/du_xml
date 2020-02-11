/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/*
 * TO-DO
 *   FEATURES
 *      MU Dependency
 *      DU v5 : tasks with same session+uproc but dif MU !!!!
 *      investigate null mu on connections -> (probably from constructor without mu)
 *      investigate unable to group old romtelcol
            nodename appears as both upper and lowercase - applied hotfix to xml_group - does this affect proc? nah...
 *      (Optional) uxlst
 *   OPTIMISATION
 *      Move uxordre parsing in parser and remove IS from uproc object
 *          (will cause overhead by processing uprocs which are out of scope)
 *          create option to skip IS+variable parsing in parser (uxordre, uxordre estimate, uxhld/uxrls RES)
*/

//tabbedPane.addTab("<html><b>Tab #" + ntabs + "</b></html>", new JLabel("Tab #" + ntabs));

import du_objects.node_obj;
import du_xml.du_xml_graph;
import du_xml.du_xml_group;
import du_xml.du_xml_outp_files;
import du_xml.du_xml_parser;
import du_xml.du_xml_proc;
import graph.GraphTab;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author t.fotakis
 */
public class DU_Analysis_UI extends javax.swing.JFrame {
    
    //COSMOT
    //TMNLDU
    private String DU_COMPANY = "[^_]+_(.*?)_X_Full_Export";   
    private String OUTP_PATH = "C:\\Users\\t.fotakis\\Desktop\\out";
    private String DU_EXPORTS = "C:\\Users\\t.fotakis\\Desktop\\exports";
    private String DU_EXECS = "C:\\Users\\t.fotakis\\Desktop\\TEST_ENV\\new_int_exec.properties"; 

    private boolean DISPLAY_NO_COUNT = false;
    private boolean DISPLAY_OPTIONAL = false;
    private boolean DISPLAY_SELF = false;
    private boolean CASE_SENSITIVE = false;
    private boolean WORKING = false;
    private boolean LOADING = false;
    private boolean CREATE_JSON = false;
    
    private GraphTab xml_graph;
    private GraphTab load_graph;
    
    /**
     * Creates new form DU_Analysis_UI
     */
    public DU_Analysis_UI() {
        initComponents();
        jTextField1.setText(DU_EXPORTS);
        jTextField2.setText(OUTP_PATH);
        jTextField3.setText(DU_EXECS);
        jTextField4.setText(DU_COMPANY);
        jTextField5.setText(OUTP_PATH + "\\graph.dgs");
        
        DefaultCaret caret = (DefaultCaret)jTextArea1.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        caret = (DefaultCaret)jTextArea1.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        Utility.setOut(jTextArea1);
        Utility.setErr(jTextArea2);

        try {
            xml_graph = new GraphTab("Output",jPanel6);
            load_graph = new GraphTab("Loaded",jPanel4);
        
        } catch (Exception ex) {
            Utility.err.printStackTrace(ex);
        }
    }

    private void save_output_to_file() {
        
        try {
            
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {                 
                }
            });
            
            try (PrintWriter out_file = new PrintWriter(OUTP_PATH + File.separator + "out.txt", "UTF-8")) {
                out_file.write(jTextArea1.getText());
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                Utility.err.println(ex);
                Utility.err.printStackTrace(ex);
            }
        } catch (InterruptedException | InvocationTargetException ex) {
            Utility.err.println(ex);
            Utility.err.printStackTrace(ex);
        }
    }
    
    private void work() {
        
        //System.out.println(SwingUtilities.isEventDispatchThread());
        
        Date startDate = new Date();
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        du_xml_proc.set_flags(DISPLAY_NO_COUNT, DISPLAY_OPTIONAL, CASE_SENSITIVE);
        
        du_xml_outp_files.set_flags(DISPLAY_NO_COUNT, DISPLAY_SELF, CREATE_JSON);

        try {
            
            Utility.out.println(formatter.format(new Date()) + " +--------------------- START ---------------------+\n");
            
            ArrayList<node_obj> du = du_xml_parser.run(DU_EXPORTS, DU_COMPANY);
            Utility.out.println("\n" + formatter.format(new Date()) + " +--------------------- FINISHED PARSING DU XML ---------------------+\n");

            du_xml_proc.run(du, DU_EXECS);
            Utility.out.println("\n" + formatter.format(new Date()) + " +--------------------- FINISHED PROCESSING DU ---------------------+\n");

            du_xml_outp_files.run(du, OUTP_PATH);
            Utility.out.println("\n" + formatter.format(new Date()) + " +--------------------- FINISHED FILE OUTPUT  ---------------------+\n");

            du_xml_group.run(du, OUTP_PATH);
            Utility.out.println("\n" + formatter.format(new Date()) + " +--------------------- FINISHED GROUPING  ---------------------+\n");
            
            du_xml_graph.run(du, OUTP_PATH, xml_graph);
            Utility.out.println("\n" + formatter.format(new Date()) + " +--------------------- FINISHED GRAPH  ---------------------+\n");
        }
        catch (FileNotFoundException | UnsupportedEncodingException e) {
            Utility.out.println("+----------------------------------------------+\n");
            Utility.err.println(e);
            Utility.err.printStackTrace(e);
            Utility.out.println("\n+----------------------------------------------+\n");
        }
        catch (Exception ex) {
            Utility.out.println("+----------------------------------------------+\n");
            Utility.err.println(ex);
            Utility.err.printStackTrace(ex);
            Utility.out.println("\n+----------------------------------------------+\n");
        }
        
        Date endDate   = new Date();

        long duration  = endDate.getTime() - startDate.getTime();
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        
        Utility.out.println("\n"+diffInSeconds/60 + ":"+ String.format("%02d", diffInSeconds%60) + "\t("+formatter.format(startDate) + " - " + formatter.format(endDate) + ")");
        
        save_output_to_file();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DU_XML");
        setMinimumSize(new java.awt.Dimension(970, 600));

        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setName(""); // NOI18N

        jPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel1.setPreferredSize(new java.awt.Dimension(978, 503));

        jLabel1.setText("XML directory :");

        jTextField1.setText(".");

        jLabel3.setText("Output directory :");

        jTextField2.setText(".");

        jLabel4.setText("Executions :");

        jButton1.setText("Start");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jLabel2.setText("Name Regex :");

        jCheckBox1.setText("Optonal Task warnings");

        jCheckBox2.setText("No available count warnings");

        jCheckBox3.setText("Omitted connection messages");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setMinimumSize(new java.awt.Dimension(200, 200));
        jScrollPane1.setViewportView(jTextArea1);

        jCheckBox4.setText("Case Sensitive");

        jCheckBox5.setText("Create json");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jButton1))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jCheckBox1)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox2)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox3)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox4)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox5)))
                        .addGap(0, 10, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(33, 33, 33)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField4)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox5))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jButton1.getAccessibleContext().setAccessibleName("");
        jTextField4.getAccessibleContext().setAccessibleName("");

        jTabbedPane1.addTab("Analysis", jPanel1);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 458, Short.MAX_VALUE)
        );

        jButton3.setText("Save");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        jButton5.setText("Start/Stop");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton3)
                .addGap(18, 18, 18)
                .addComponent(jButton5)
                .addContainerGap(796, Short.MAX_VALUE))
            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Graph preview", jPanel2);

        jLabel5.setText("Input dgs :");

        jButton2.setText("Load");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 458, Short.MAX_VALUE)
        );

        jButton4.setText("Save");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });

        jButton6.setText("Start/Stop");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton6MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(36, 36, 36)
                .addComponent(jButton4)
                .addGap(34, 34, 34)
                .addComponent(jButton6)
                .addContainerGap(87, Short.MAX_VALUE))
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2)
                    .addComponent(jButton4)
                    .addComponent(jButton6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Load Graph", jPanel3);

        jTextArea2.setEditable(false);
        jTextArea2.setColumns(20);
        jTextArea2.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jTextArea2.setForeground(new java.awt.Color(255, 0, 0));
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Trace", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        
        if (WORKING || (!SwingUtilities.isLeftMouseButton(evt))) {
            return;
        }
        
        WORKING = true;
        DISPLAY_NO_COUNT = jCheckBox1.isSelected();
        DISPLAY_OPTIONAL = jCheckBox2.isSelected();
        DISPLAY_SELF = jCheckBox3.isSelected();
        CASE_SENSITIVE = jCheckBox4.isSelected();
        CREATE_JSON = jCheckBox5.isSelected();
        
        DU_EXPORTS = jTextField1.getText();
        OUTP_PATH = jTextField2.getText();
        DU_EXECS = jTextField3.getText();
        DU_COMPANY = jTextField4.getText();
        
        jTextArea1.getHighlighter().removeAllHighlights();
        jTextArea1.setText("");
        
        Thread workThread = new Thread() {
            @Override
            public void run() {
                try {
                    Pattern p = Pattern.compile(DU_COMPANY);
                    xml_graph.default_graph_options(true);
                    xml_graph.setPath(OUTP_PATH,"graph.dgs");
                    work();
                }
                catch (PatternSyntaxException ex) {
                    Utility.out.println("+----------------------------------------------+");
                    Utility.err.println("PatternSyntaxException : " + ex.getMessage());
                    Utility.out.println("+----------------------------------------------+\n");
                }
                catch (Exception ex) {
                    Utility.out.println("+----------------------------------------------+\n");
                    Utility.err.println(ex);
                    Utility.err.printStackTrace(ex);
                    Utility.out.println("\n+----------------------------------------------+\n");
                }
                finally {
                    WORKING = false;
                    System.gc();
                }
            }

            
        };
        workThread.start();
        
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        
        if (LOADING || (!SwingUtilities.isLeftMouseButton(evt))) {
            return;
        }
        
        LOADING = true;
        
        final String filename = jTextField5.getText();
        
        if ("".equals(filename)) {
            LOADING = false;
            return;
        }
        
        //System.out.println("Loading...");
        
        load_graph.default_graph_options(true);
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    load_graph.read(filename);
                } catch (Exception ex) {
                    Utility.err.printStackTrace(ex);
                }
                finally {
                    LOADING = false;
                }
            }
        });
        
        
        
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        if (!SwingUtilities.isLeftMouseButton(evt)) {
            return;
        }
        
        try {
            xml_graph.save_graph_coordinates();
        } catch (Exception ex) {
            Utility.err.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        if (!SwingUtilities.isLeftMouseButton(evt)) {
            return;
        }
        
        try {
            load_graph.save_graph_coordinates();
        } catch (Exception ex) {
            Utility.err.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
        if (!SwingUtilities.isLeftMouseButton(evt)) {
            return;
        }
        
        xml_graph.start_stop();
    }//GEN-LAST:event_jButton5MouseClicked

    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
        if (!SwingUtilities.isLeftMouseButton(evt)) {
            return;
        }
        
        load_graph.start_stop();
    }//GEN-LAST:event_jButton6MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DU_Analysis_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DU_Analysis_UI().setVisible(true);
            }
        });
  
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables

}
