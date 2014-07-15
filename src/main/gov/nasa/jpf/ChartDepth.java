//package gov.nasa.jpf;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Scanner;
//
//import javax.swing.JFrame;
//
//import java.awt.BorderLayout;
//import java.awt.EventQueue;
//import java.awt.FlowLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileFilter;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//
//import javax.swing.AbstractAction;
//import javax.swing.DefaultComboBoxModel;
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import javax.swing.JPanel;
//
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.DateAxis;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.data.xy.XYDataset;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//
//public class ChartDepth extends JFrame {
//
//		Scanner in = new Scanner(System.in);	 
//		int i;
//		int count;
//	  	double[][] times = new double[1][10];
//	  	double[][] depth = new double[1][10];
//	
//	
//	  	private static final long serialVersionUID = 1L;
//	
//	  	private static final String title = "Fault times over depth";
//	  	private ChartPanel chartPanel = createChart();
//	 
//	public ChartDepth() {
//        JFrame f = new JFrame(title);
//        f.setTitle(title);
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.getContentPane().setLayout(new BorderLayout(0, 5));
//        f.getContentPane().add(chartPanel, BorderLayout.CENTER);
//        chartPanel.setMouseWheelEnabled(true);
//        chartPanel.setHorizontalAxisTrace(true);
//        chartPanel.setVerticalAxisTrace(true);
//
//        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        
//        JButton btnRefreshBFSTest = new JButton("Delete BFS Results");
//        panel.add(btnRefreshBFSTest);
//        btnRefreshBFSTest.addActionListener(new ActionListener() {
//        		 
//        public void actionPerformed(ActionEvent e) {
//                    //Execute when button is pressed
//                    System.out.println("Results deleted, please rerun JPF to see a change");
//                    
//                    File directory = new File(System.getProperty("user.home")  + "/TestTime/");  
//                    
//                    File[] toBeDeleted = directory.listFiles(new FileFilter() {  
//                    public boolean accept(File theFile) {  
//                    	if (theFile.isFile()) {  
//                    		return theFile.getName().startsWith("BFSDepth");  
//                    	}  
//                    return false;  
//                    }  
//                    });  
//                       
//                    System.out.println(Arrays.toString(toBeDeleted));  
//                    for(File deletableFile:toBeDeleted){  
//                    deletableFile.delete();
//                 } 
//                }
//            });   
//        
//        JButton btnRefreshDFSTest = new JButton("Delete DFS Results");
//        panel.add(btnRefreshDFSTest);
//        btnRefreshDFSTest.addActionListener(new ActionListener() {
//        		 
//        public void actionPerformed(ActionEvent e) {
//                    //Execute when button is pressed
//                    System.out.println("Results deleted, please rerun JPF to see a change");
//                    
//                    File directory = new File(System.getProperty("user.home")  + "/TestTime/");  
//                    
//                    File[] toBeDeleted = directory.listFiles(new FileFilter() {  
//                    public boolean accept(File theFile) {  
//                    	if (theFile.isFile()) {  
//                    		return theFile.getName().startsWith("DFSDepth");  
//                    	}  
//                    return false;  
//                    }  
//                    });  
//                       
//                    System.out.println(Arrays.toString(toBeDeleted));  
//                    for(File deletableFile:toBeDeleted){  
//                    deletableFile.delete();  
//                 } 
//                }
//            });   
//        
//        panel.add(createTrace());
//        panel.add(createTime());
//        panel.add(createZoom());
//        f.getContentPane().add(panel, BorderLayout.SOUTH);
//        f.pack();
//        f.setLocationRelativeTo(null);
//        f.setVisible(true);
//    }
//	
//	private ChartPanel createChart() {
//        XYDataset errorData = createDataset();
//        JFreeChart chart = ChartFactory.createXYLineChart(
//            title, "Depth", "Time to all faults (seconds)", errorData);
//        XYPlot plot = chart.getXYPlot();
//        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
//        renderer.setBaseShapesVisible(true);
//       //NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        return new ChartPanel(chart);
//    }
//	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private JComboBox createTrace() {
//        final JComboBox trace = new JComboBox();
//        final String[] traceCmds = {"Enable Trace", "Disable Trace"};
//        trace.setModel(new DefaultComboBoxModel(traceCmds));
//        trace.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (traceCmds[0].equals(trace.getSelectedItem())) {
//                    chartPanel.setHorizontalAxisTrace(true);
//                    chartPanel.setVerticalAxisTrace(true);
//                    chartPanel.repaint();
//                } else {
//                    chartPanel.setHorizontalAxisTrace(false);
//                    chartPanel.setVerticalAxisTrace(false);
//                    chartPanel.repaint();
//                }
//            }
//        });
//        return trace;
//    }
//	
//	@SuppressWarnings("serial")
//	private JButton createZoom() {
//        final JButton auto = new JButton(new AbstractAction("Auto Zoom") {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                chartPanel.restoreAutoBounds();
//            }
//        });
//        return auto;
//    }
//
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//	private JComboBox createTime() {
//        final JComboBox time = new JComboBox();
//        final String[] timeCmds = {"Horizontal Times", "Vertical Times"};
//        time.setModel(new DefaultComboBoxModel(timeCmds));
//        time.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFreeChart chart = chartPanel.getChart();
//                XYPlot plot = (XYPlot) chart.getPlot();
//                DateAxis domain = (DateAxis) plot.getDomainAxis();
//                if (timeCmds[0].equals(time.getSelectedItem())) {
//                    domain.setVerticalTickLabels(false);
//                } else {
//                    domain.setVerticalTickLabels(true);
//                }
//            }
//        });
//        return time;
//    }
//	  
//    //Add more searches to plot/points are added here
//	   @SuppressWarnings("resource")
//	private XYDataset createDataset() {
//		   
//		   final XYSeries bfs = new XYSeries("BFS");
//		   
//		   File f = null;
//		   File d = null;
//		   Scanner scan = null;
//		   Scanner scan2 = null;
//		   try{
//		      f = new File(System.getProperty("user.home")+ "/TestTime/"+ "BFSfinal_Depth" +".txt");
//		      d = new File(System.getProperty("user.home")+ "/TestTime/"+ "BFSfinal_result" +".txt");
//		      scan = new Scanner(f);
//		      scan2 = new Scanner(d);
//		   }
//		   catch(Exception e){
//		      System.exit(0);
//		   }
//		   
//		   ArrayList<Integer> x = new ArrayList<Integer>();
//		   ArrayList<Integer> xz = new ArrayList<Integer>();
//		   x.add(scan.nextInt());
//		   xz.add(scan2.nextInt());
//		   count=1;
//		   times[i][count-1] = xz.get(count-1);
//		   depth[i][count-1] = x.get(count-1);
//		   bfs.add(depth[i][count-1], times[i][count-1]*.001);
//		   
//		   while(scan.hasNextInt()){
//			   x.add(scan.nextInt());
//			   xz.add(scan2.nextInt());
//			   depth[i][count] = x.get(count);
//			   times[i][count] = xz.get(count) + times[i][count-1];
//			   bfs.add(depth[i][count], times[i][count]*.001);
//			   count++;
//		   }
//		   
//		   final XYSeries dfs = new XYSeries("DFS");
//		   try{
//			      f = new File(System.getProperty("user.home")+ "/TestTime/"+ "DFSfinal_Depth" +".txt");
//			      d = new File(System.getProperty("user.home")+ "/TestTime/"+ "DFSfinal_result" +".txt");
//			      scan = new Scanner(f);
//			      scan2 = new Scanner(d);
//			   }
//			   catch(Exception e){
//			      System.exit(0);
//			   }
//			   
//			   ArrayList<Integer> y = new ArrayList<Integer>();
//			   ArrayList<Integer> yz = new ArrayList<Integer>();
//			   y.add(scan.nextInt());
//			   yz.add(scan2.nextInt());
//			   count=1;
//			   times[i][count-1] = yz.get(count-1);
//			   depth[i][count-1] = y.get(count-1);
//			   dfs.add(depth[i][count-1], times[i][count-1]*.001);
//			   
//			   while(scan.hasNextInt()){
//				   y.add(scan.nextInt());
//				   yz.add(scan2.nextInt());
//				   depth[i][count] = y.get(count);
//				   times[i][count] = yz.get(count) + times[i][count-1];
//				   dfs.add(depth[i][count], times[i][count]*.001);
//				   count++;
//			   }
//		   
//		   
//	     
//	      final XYSeriesCollection dataset = new XYSeriesCollection();
//	      dataset.addSeries(bfs);
//	      dataset.addSeries(dfs);
//	      return dataset;
//	     
//	  }
//
//	   public static void main(String[] args) {
//	        EventQueue.invokeLater(new Runnable() {
//
//	            @Override
//	            public void run() {
//	                ChartDepth cpd = new ChartDepth();
//	            }
//	        });
//	    }
//}
