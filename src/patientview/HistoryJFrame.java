package patientview;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
/**
 *
 * @author s1571182
 */
public class HistoryJFrame extends javax.swing.JFrame {
    
    
    private Patient pat;
    private int offset;
    private int timerSeconds;
    private Timer timer;
    private Scanner scanner;
    private File f;
    private String[] lineData;
    private XYSeries heart;
    private XYSeries o2;
    private XYSeries sys;
    private XYSeries br;
    private XYSeries temp;
    private XYSeriesCollection dataset;
    private int[] toggle;
    
    public HistoryJFrame(Patient pat, int offset) {
        initComponents();
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        //Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        //Move the window
        this.setLocation(x, y);
        this.getContentPane().setBackground(Color.WHITE);
        
        this.pat = pat;
        this.offset = offset;
        this.heart = new XYSeries("Heartbeat (bp)");
        this.o2 = new XYSeries("Oxygen Saturation (%)");
        this.sys = new XYSeries("Systolic (mmHg)");
        this.br = new XYSeries("Breathing Rate (breaths/min)");
        this.temp = new XYSeries("Temperature (celsius)");
        this.toggle = new int[] {1, 1, 1, 1, 1};
        this.dataset = new XYSeriesCollection();
        
        timerSeconds = 0;
        if(timer == null) {
            timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // do it every 1 second
                    Calendar cal = Calendar.getInstance();
                    cal.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                    jLabel_systemTime.setText(sdf.format(cal.getTime()));
                    timerSeconds++;
                }
            });
        }
        
        if(!timer.isRunning()) {
            timer.start();
        }
        
        
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
        displayChart();
        
        
        
        
        //setContentPane(chPanel);
    }
    
    public void displayChart() {
        createDataset();
        compileSet(this.toggle);
        chartHolder.validate();
        JFreeChart chart = createChart(this.dataset, "test", this.pat.getLastName() + ", " + this.pat.getFirstName() + ": " + "Vitals within the last hour");
        ChartPanel chPanel = new ChartPanel(chart);
        chartHolder.add(chPanel);
        chPanel.setMaximumSize(new Dimension(650, 200));
        chPanel.setVisible(true);
    }
    
    public JFreeChart createChart(XYDataset dataset, String unit, String title) {
        JFreeChart chart = ChartFactory.createXYLineChart(
            title,      // chart title
            "Time (s)",                      // x axis label
            unit,                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.gray);
        
        return chart;
    }
    
    public void createDataset() {
        
        while("Timestamp".equals(lineData[0]) || lineData[0] == null) {
            this.lineData = this.scanner.nextLine().split(",");
        }
        
        while(this.scanner.hasNextLine()) {
            
            int time = Integer.parseInt(lineData[0]);
            int br = Integer.parseInt(lineData[1]);
            int spo2 = Integer.parseInt(lineData[2]);
            float temp = Float.parseFloat(lineData[3]);
            int systolic = Integer.parseInt(lineData[4]);
            int hr = Integer.parseInt(lineData[5]);
            
            this.heart.add(time, hr);
            this.o2.add(time, spo2);
            this.sys.add(time, systolic);
            this.br.add(time, br);
            this.temp.add(time, temp);
            this.lineData = this.scanner.nextLine().split(",");
        }
        
        int time = Integer.parseInt(lineData[0]);
        int br = Integer.parseInt(lineData[1]);
        int spo2 = Integer.parseInt(lineData[2]);
        float temp = Float.parseFloat(lineData[3]);
        int systolic = Integer.parseInt(lineData[4]);
        int hr = Integer.parseInt(lineData[5]);

        this.heart.add(time, hr);
        this.o2.add(time, spo2);
        this.sys.add(time, systolic);
        this.br.add(time, br);
        this.temp.add(time, temp);
    }
    
    public void compileSet(int[] toggle) {
        this.dataset = new XYSeriesCollection();
        if(toggle[0] == 1) {
            this.dataset.addSeries(this.br);
        }
        
        if(toggle[1] == 1) {
            this.dataset.addSeries(this.heart);
        }
        
        if(toggle[2] == 1) {
            this.dataset.addSeries(this.o2);
        }
        
        if(toggle[3] == 1) {
            this.dataset.addSeries(this.sys);
        }
        
        if(toggle[4] == 1) {
            this.dataset.addSeries(this.temp);
        } 
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel_systemTime = new javax.swing.JLabel();
        chartHolder = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        temper = new javax.swing.JCheckBox();
        breathing = new javax.swing.JCheckBox();
        o2sat = new javax.swing.JCheckBox();
        systol = new javax.swing.JCheckBox();
        hRate = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(java.awt.Color.white);

        jButton1.setBackground(java.awt.Color.white);
        jButton1.setText("Exit");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(java.awt.Color.white);
        jButton2.setText("Back");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(34, 34, 34))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(37, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(35, 35, 35))
        );

        jLabel_systemTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_systemTime.setText("TIME");

        chartHolder.setBackground(java.awt.Color.white);
        chartHolder.setPreferredSize(new java.awt.Dimension(600, 700));
        chartHolder.setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/patientview/Redcross.png"))); // NOI18N

        jPanel2.setBackground(java.awt.Color.white);

        temper.setSelected(true);
        temper.setText("Temperature");
        temper.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                temperItemStateChanged(evt);
            }
        });

        breathing.setSelected(true);
        breathing.setText("Breathing Rate");
        breathing.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                breathingItemStateChanged(evt);
            }
        });

        o2sat.setSelected(true);
        o2sat.setText("Oxygen Saturation");
        o2sat.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                o2satItemStateChanged(evt);
            }
        });

        systol.setSelected(true);
        systol.setText("Systolic");
        systol.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                systolItemStateChanged(evt);
            }
        });

        hRate.setSelected(true);
        hRate.setText("Heart Rate");
        hRate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                hRateItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(breathing)
                .addGap(52, 52, 52)
                .addComponent(hRate)
                .addGap(50, 50, 50)
                .addComponent(o2sat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(systol)
                .addGap(53, 53, 53)
                .addComponent(temper)
                .addGap(44, 44, 44))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(temper)
                    .addComponent(breathing)
                    .addComponent(o2sat)
                    .addComponent(systol)
                    .addComponent(hRate))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chartHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel_systemTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_systemTime, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chartHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 503, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.dispose();
        try {
            new PatientJFrame(this.pat, this.offset).setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(PatientJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void temperItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_temperItemStateChanged
        System.out.println(evt.getStateChange());
        
        if(evt.getStateChange() == 1) {
            this.toggle[4] = 1;
        } else {
             this.toggle[4] = 0;
        }
        displayChart();
    }//GEN-LAST:event_temperItemStateChanged

    private void systolItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_systolItemStateChanged
        System.out.println(evt.getStateChange());
        
        if(evt.getStateChange() == 1) {
            this.toggle[3] = 1;
        } else {
             this.toggle[3] = 0;
        }
        displayChart();
    }//GEN-LAST:event_systolItemStateChanged

    private void o2satItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_o2satItemStateChanged
        System.out.println(evt.getStateChange());
        
        if(evt.getStateChange() == 1) {
            this.toggle[2] = 1;
        } else {
             this.toggle[2] = 0;
        }
        displayChart();
    }//GEN-LAST:event_o2satItemStateChanged

    private void hRateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hRateItemStateChanged
        System.out.println(evt.getStateChange());
        
        if(evt.getStateChange() == 1) {
            this.toggle[1] = 1;
        } else {
             this.toggle[1] = 0;
        }
        displayChart();
    }//GEN-LAST:event_hRateItemStateChanged

    private void breathingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_breathingItemStateChanged
        System.out.println(evt.getStateChange());
        
        if(evt.getStateChange() == 1) {
            this.toggle[0] = 1;
        } else {
             this.toggle[0] = 0;
        }
        displayChart();
    }//GEN-LAST:event_breathingItemStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HistoryJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HistoryJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HistoryJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HistoryJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                String [] data = {"1001","Alice","Bailey","F","1958-10-12"};
                Patient alice = new Patient(data, "W001");
                new HistoryJFrame(alice, 0).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox breathing;
    private javax.swing.JPanel chartHolder;
    private javax.swing.JCheckBox hRate;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel_systemTime;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JCheckBox o2sat;
    private javax.swing.JCheckBox systol;
    private javax.swing.JCheckBox temper;
    // End of variables declaration//GEN-END:variables
}
