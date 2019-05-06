package loadingdocks;

import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.ui.ApplicationFrame; 
import org.jfree.ui.RefineryUtilities; 
import org.jfree.chart.plot.XYPlot; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.data.xy.XYSeriesCollection; 
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class XYChart extends ApplicationFrame {
	private static final long serialVersionUID = 9136101228200499887L;
	private final static String title = "Run Results";
	private final String xLabel = "Time";
	private final String yLabel = "Distance";

	public XYChart( Map<Integer, Agent> agents ) {
      super(title);
      JFreeChart xylineChart = ChartFactory.createXYLineChart(
         title ,
         xLabel ,
         yLabel ,
         createDataset( agents) ,
         PlotOrientation.VERTICAL ,
         true , true , false);
         
      ChartPanel chartPanel = new ChartPanel( xylineChart );
      chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
      final XYPlot plot = xylineChart.getXYPlot( );
      
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
      renderer.setSeriesStroke( 0 , new BasicStroke( 1.0f ) );
      plot.setRenderer( renderer ); 
      setContentPane( chartPanel ); 
   }
   
   private XYDataset createDataset(Map<Integer, Agent> agents ) {      
      
      final XYSeries iexplorer = new XYSeries( "Agents" );   
      for(int i = 0;i<agents.size();i++);
    //	  iexplorer.add( 0, agents.get(0).getTotalDistance() );          
      
      final XYSeriesCollection dataset = new XYSeriesCollection( );                 
      dataset.addSeries( iexplorer );
      return dataset;
   }

   public static void main( String[ ] args ) {
	   Map<Integer, Agent> agents = new HashMap<Integer, Agent>();
	   XYChart chart = new XYChart(agents);
      chart.pack( );          
      RefineryUtilities.centerFrameOnScreen( chart );          
      chart.setVisible( true ); 
   }
}