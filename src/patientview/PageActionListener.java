/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import java.io.File;
import java.util.Scanner;
import java.io.*;
import static patientview.PatientJFrame.HEART_RATE;
import static patientview.PatientJFrame.RESPIRATORY_RATE;
import static patientview.PatientJFrame.SPO2;
import static patientview.PatientJFrame.SYSTOLIC;
import static patientview.PatientJFrame.TEMPERATURE;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 *
 * @author s1571182
 */
public class PageActionListener implements ActionListener {
    
    private Patient pat;
    private javax.swing.JLabel heartRate;
    private javax.swing.JLabel jLabel_systemTime;
    private javax.swing.JLabel oxygenSat;
    private javax.swing.JLabel respRate;
    private javax.swing.JLabel systolic;
    private javax.swing.JLabel temp;
    private javax.swing.JLabel pSews;
    private int timerSeconds;
    private Scanner scanner;
    private String[] lineData;
    private File f;
    private int offset;
    
    public PageActionListener(Patient pat, javax.swing.JLabel hRate, javax.swing.JLabel sysTime, 
            javax.swing.JLabel o2, javax.swing.JLabel systolic, javax.swing.JLabel temp, 
            javax.swing.JLabel rRate, javax.swing.JLabel pSews, int offset) {
        this.pat = pat;
        this.heartRate = hRate;
        this.jLabel_systemTime = sysTime;
        this.oxygenSat = o2;
        this.respRate = rRate;
        this.systolic = systolic;
        this.temp = temp;
        this.timerSeconds = 0;
        this.pSews = pSews;
        this.offset = offset;
        
        File folder = new File("/afs/inf.ed.ac.uk/user/s15/s1571182/NetBeansProjects/Coursework2/data");
        File[] listOfFiles = folder.listFiles();
        this.scanner = null;
        Pattern p = Pattern.compile(this.pat.getFirstName() + ".*csv");
        this.f = null;
        for(File file : listOfFiles) {
            if(p.matcher(file.getName()).matches()) {
                this.f = file;
            } 
        }
        
        try {
            this.scanner = new Scanner(this.f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PageActionListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.lineData = scanner.nextLine().split(",");
    }
    public void actionPerformed(ActionEvent e) {
        
        

        // do it every 1 second
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        jLabel_systemTime.setText(sdf.format(cal.getTime()));
        // do it every 5 seconds
        if (timerSeconds % 5 == 0) {
            //generate random value
            
            while("Timestamp".equals(lineData[0]) || lineData[0] == null) {
                this.lineData = this.scanner.nextLine().split(",");
            }
            
            int br = Integer.parseInt(lineData[1]);
            int spo2 = Integer.parseInt(lineData[2]);
            float temp = Float.parseFloat(lineData[3]);
            int systolic = Integer.parseInt(lineData[4]);
            int hr = Integer.parseInt(lineData[5]);

            displayData(br,spo2,temp,systolic,hr);
            
            
            if(this.scanner.hasNextLine()) {
                this.lineData = this.scanner.nextLine().split(",");
            } else {
                try {
                    this.scanner = new Scanner(this.f);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PageActionListener.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.lineData = this.scanner.nextLine().split(",");
            }
        }
        timerSeconds++;
    }
    
    public void displayData(int rRate, int o2sat, float t, int sys, int hRate) {
        respRate.setText(Integer.toString(rRate) + " breaths/min");
        oxygenSat.setText(Integer.toString(o2sat) + " %");
        temp.setText(String.format("%.3g%n", t) + " \u00b0 C");
        systolic.setText(Integer.toString(sys) + " mmHg");
        heartRate.setText(Integer.toString(hRate) + " bpm");
        
        //calculate psews
        int pSewsNum = calcPSews(rRate, o2sat, t, sys, hRate);
        this.pSews.setText(Integer.toString(pSewsNum));
        
        if(pSewsNum < 2) {
            this.pSews.setForeground(new Color(0, 204, 0));
        } else if(pSewsNum >= 4) {
            this.pSews.setForeground(Color.red);
        } else {
            this.pSews.setForeground(new Color(0xffbf00));
        }
    }
    
    public int calcPSews(int rRate, int o2sat, float t, int sys, int hRate) {
        int sum = this.offset;
        
        if(rRate <= 8 || rRate >= 36) {
            sum += 3;
        } else if(rRate <= 30 && rRate >= 21) {
            sum += 1;
        } else if(rRate <= 35 && rRate >= 31) {
            sum += 2;
        }
        
        if(o2sat < 85) {
            sum += 3;
        } else if(o2sat >= 85 && o2sat <= 89) {
            sum += 2;
        } else if(o2sat >= 90 && o2sat <= 92) {
            sum += 1;
        }
        
        if(t < 34.0) {
            sum += 3;
        } else if((t >= 34.0 && t <= 34.9) || t >= 38.5) {
            sum += 2;
        } else if((t >= 35.0 && t <= 35.9) || (t >= 38.0 && t <= 38.4)) {
            sum += 1;
        }
        
        if(sys <= 69) {
            sum += 3;
        } else if((sys >= 70 && sys <= 79) || sys >= 200) {
            sum += 2;
        } else if((sys >= 80 && sys <= 99) || (sys >= 200)) {
            sum += 1;
        }
        
        if(hRate <= 29 || hRate >= 130) {
            sum += 3;
        } else if((hRate >= 30 && hRate <= 39) || (hRate >= 110 && hRate <= 129)) {
            sum += 2;
        } else if((hRate >= 40 && hRate <= 49) || (hRate >= 100 && hRate <= 109)) {
            sum += 1;
        }
        
        return sum;
    }
    
}
