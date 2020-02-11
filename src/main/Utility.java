/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/**
 *
 * @author t.fotakis
 */
public final class Utility {
    
    private static javax.swing.JTextArea outArea = null;
    private static javax.swing.JTextArea errArea = null;
    private static Highlighter.HighlightPainter errorPainter = null;
    private static SimpleDateFormat formatter = null;
    
    private Utility() { }
    
    public static <T extends myBaseObject> T get_by_name(ArrayList<T> list_of_objects, String search_name, Boolean case_sensitive) {
        if (case_sensitive) {
            for (T obj : list_of_objects) {
                if (search_name.equals(obj.name)) {
                    return obj;
                }
            }
        }
        else {
            String name = search_name.toUpperCase();
            for (T obj : list_of_objects) {
                if (name.equals(obj.name.toUpperCase())) {
                    return obj;
                }
            }
        }
        return null;
    }
    
    public static <T extends myBaseObject> String toMyString(ArrayList<T> thelist) {
        String res = "[";
        for (Iterator<T> it = thelist.iterator(); it.hasNext();) {
            T obj = it.next();
            res+="\n" + obj.toJSONString();
            if (it.hasNext()) {
                res+=", ";
            }
        }
        res += "\n]";
        
        return res;
    }
    
    public static void setOut(javax.swing.JTextArea area) {
        outArea = area;
        errorPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
    }
    
    public static void setErr(javax.swing.JTextArea area) {
        errArea = area;
        formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }
    
    public static final class out {
    
        public static void println(final Object s) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    outArea.append(s.toString()+"\n");
                }
            });
        }
    
        public static void print(final Object s) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    outArea.append(s.toString());
                }
            });
        }
    }
    
    public static final class err {
    
        public static void println(final Object s) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Integer i = outArea.getText().length();
                    outArea.append(s.toString()+"\n");
                    try {
                        outArea.getHighlighter().addHighlight(i, i+s.toString().length(), errorPainter);
                    } catch (BadLocationException ex) {
                        Utility.err.printStackTrace(ex);
                    }
                }
            });
        }
        
        public static void printStackTrace(final Exception ex) {

            final StringWriter stringWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(stringWriter));            

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    errArea.append(formatter.format(new Date()) + "\n" + stringWriter.toString()+"\n");
                }
            });
        }
    }
    
}
