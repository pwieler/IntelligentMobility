package intelligentMobility;

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
	private final String xLabel = "Agent";
	private final String yLabel = "Distance";
	private Map<Integer, Agent> allAgents;

	public XYChart( ) {
      super(title);
      allAgents = new HashMap<Integer, Agent>();
   }
	
	public void showGraph() {
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
		         title ,
		         xLabel ,
		         yLabel ,
		         createDataset( allAgents) ,
		         PlotOrientation.VERTICAL ,
		         true , true , false);
		         
		      ChartPanel chartPanel = new ChartPanel( xylineChart );
		      chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		      final XYPlot plot = xylineChart.getXYPlot( );
		      
		      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
		      renderer.setSeriesStroke( 0 , new BasicStroke( 1.0f ) );
		      plot.setRenderer( renderer ); 
		      setContentPane( chartPanel ); 
		      
		      pack( );          
		      RefineryUtilities.centerFrameOnScreen( this );          
		      setVisible( true ); 
	}
   
   private XYDataset createDataset( Map<Integer, Agent> agents) {      
      
      final XYSeries agentsSeries  = new XYSeries( "Agents" );   
      for(int i = 0;i<agents.size();i++)
      	agentsSeries.add( i,(agents.get(i).getTotalDistance()/agents.get(i).getTotalRuns()) );          
      
      final XYSeriesCollection dataset = new XYSeriesCollection( );                 
      dataset.addSeries( agentsSeries );
      return dataset;
   }

   public void addRun(Map<Integer, Agent> agents) {
	   for(int i=0;i<allAgents.size();i++) {
		   for(int j=0;j<agents.size();j++) {
			   if(allAgents.get(i).equals(agents.get(j))) {
				   allAgents.get(i).addTotalDistance(agents.get(j).getTotalDistance());
				   allAgents.get(i).incrementRun();
			   }
		   }
	   }
   }
   
   public static void main( String[ ] args ) {
	   Map<Integer, Agent> run1 = new HashMap<Integer, Agent>();
	   Map<Integer, Agent> run2 = new HashMap<Integer, Agent>();
	   
	   XYChart chart = new XYChart();
	   chart.addRun(run1);
	   chart.addRun(run2);
	   chart.showGraph();
   }

}