/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patientview;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author s1571182
 */
public class WardActionListener implements ActionListener {
    
    private ArrayList<Patient> pats;
    private javax.swing.JLabel jLabel_systemTime;

    private int timerSeconds;
    private ArrayList<Scanner> scanners;
    private ArrayList<String[]> lineDatas;
    private ArrayList<File> files;
    private javax.swing.JLabel[] psews;
    private javax.swing.JPanel[] colors;
    private int[] offsets;
    private javax.swing.JLabel[] actions;
    
    public WardActionListener(ArrayList<Patient> pats, javax.swing.JLabel sysTime, 
            javax.swing.JLabel[] psews, javax.swing.JPanel[] colors, 
            int[] offsets, javax.swing.JLabel[] actions) {
        this.pats = pats;
        this.timerSeconds = 0;
        this.jLabel_systemTime = sysTime;
        this.psews = psews;
        this.colors = colors;
        this.offsets = offsets;
        this.actions = actions;
        
        File folder = new File("/afs/inf.ed.ac.uk/user/s15/s1571182/NetBeansProjects/Coursework2/data");
        File[] listOfFiles = folder.listFiles();
        
        scanners = new ArrayList<>();
        files = new ArrayList<>();
        lineDatas = new ArrayList<>();
        for(int i = 0; i < pats.size(); i++) {
            Scanner scanner = null;
            Pattern p = Pattern.compile(this.pats.get(i).getFirstName() + ".*csv");
            File f = null;
            for(File file : listOfFiles) {
                if(p.matcher(file.getName()).matches()) {
                    f = file;
                    files.add(f);
                } 
            }
        
            try {
                scanner = new Scanner(f);
                String[] lineData = scanner.nextLine().split(",");
                lineDatas.add(lineData);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PageActionListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            scanners.add(scanner);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        jLabel_systemTime.setText(sdf.format(cal.getTime()));
        // do it every 5 seconds
        if (timerSeconds % 5 == 0) {
            
            
            for(int i = 0; i < this.lineDatas.size(); i++) {
                
                String[] lineData = this.lineDatas.get(i);
                Scanner scanner = this.scanners.get(i);
                
                while("Timestamp".equals(lineData[0]) || lineData[0] == null) {
                    lineData = scanner.nextLine().split(",");
                }
                
                int br = Integer.parseInt(lineData[1]);
                int spo2 = Integer.parseInt(lineData[2]);
                float temp = Float.parseFloat(lineData[3]);
                int systolic = Integer.parseInt(lineData[4]);
                int hr = Integer.parseInt(lineData[5]);
                displayData(br,spo2,temp,systolic,hr, i);


                if(scanner.hasNextLine()) {
                    this.lineDatas.set(i, scanner.nextLine().split(","));   
                } else {
                    try {
                        this.scanners.set(i, new Scanner(this.files.get(i)));
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(PageActionListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    scanner = this.scanners.get(i);
                    this.lineDatas.set(i, scanner.nextLine().split(","));
                }
                this.scanners.set(i, scanner);
            }
         
        }
        timerSeconds++;
    }
    
    public void displayData(int rRate, int o2sat, float t, int sys, int hRate, int i) {
        
        //calculate psews
        int pSewsNum = calcPSews(rRate, o2sat, t, sys, hRate, i);
        if(pSewsNum < 2) {
            this.colors[i].setBackground(new Color(0, 204, 0));
        } else if(pSewsNum >= 4) {
            this.colors[i].setBackground(new Color(0xff4c4c));
        } else {
            this.colors[i].setBackground(new Color(0xffbf00));
        }
        
        if(pSewsNum >= 2 && pSewsNum <=3) {
            this.actions[i].setText("Continue routine observation");
        } else if(pSewsNum >= 4 && pSewsNum <= 5) {
            this.actions[i].setText("Involve nurse-in-charge immediately");
        } else if(pSewsNum >= 6) {
            this.actions[i].setText("Call registrar for immediate review");
        } else {
            this.actions[i].setText("");
        }
        
        psews[i].setText(Integer.toString(pSewsNum));
    }
    
    public int calcPSews(int rRate, int o2sat, float t, int sys, int hRate, int i) {
        int sum = this.offsets[i];
    
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
