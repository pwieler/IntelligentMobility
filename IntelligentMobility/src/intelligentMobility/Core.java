package intelligentMobility;

import java.util.*;

import org.jfree.ui.RefineryUtilities;

/**
 * Created by Pascal on 30.04.2019.
 */
public class Core {
    // Implement subscription pattern

    // changed this

    static Map<Integer, Agent> agents = new HashMap<Integer, Agent>();
    static Map<Integer, User> users = new HashMap<Integer, User>();
    static List<Request> requests = new LinkedList<Request>();
    static Board board;
    static boolean resetLock = false;
    static int time_steps = 0;

    // Chart
    static XYChart chart;
    static XYChart timeChart;
    static ArrayList<Double> xValues = new ArrayList<Double>();
    static ArrayList<Double> yValues = new ArrayList<Double>();
    static ArrayList<Double> yTimes = new ArrayList<Double>();
    static int average_steps = 10;
    static int steps_so_far = 0;



    public static void initialize(Board b) {
    	xValues = new ArrayList<Double>();
    	yValues = new ArrayList<Double>();
    	yTimes = new ArrayList<Double>();
        chart = new XYChart();
        timeChart = new XYChart();
        board = b;
        cluster();
    }

    public static void reset(){
    	resetLock = true;
    	time_steps = 0;
        agents.clear();
        users.clear();
        requests.clear();
        board.reset();
        cluster();
        resetLock=false;
    }

    public static void registerToCore(Agent a){
        agents.put(a.ID,a);
    }

    public static void registerToCore(User u){
        users.put(u.ID,u);
        
    }

    public static void appendRequest(Request r){
        requests.add(r);
    }

    public static void broadcastRequests(){

        for(Agent a:new ArrayList<Agent>(agents.values())){
            a.receiveRequests(requests);
        }

    }

    private static void broadcastOffers() {


        Iterator<Request> iter = requests.iterator();
        while(iter.hasNext()){
            Request rn = iter.next();
            if(rn.user.processOffers()){
                iter.remove();
            }
        }

//        for(Request r:requests){
//            // Process the offers of the user belonging to the current request
//            boolean requestSolved = users.get(r.userID).processOffers();
//
//            if(requestSolved)
//                requests.remove(r);
//
//        }
    }


    /***********************************
     ***** C: ELICIT AGENT ACTIONS *****
     ***********************************/

    private static RunThread runThread;

    static double strategyId = 5;
    static double totalRunDistance = 0;
    static double totalRuns = 0;
    
    public static void step(){

        board.step();
        broadcastRequests();
        broadcastOffers();
        
        
        // Time-steps update
        int notDeliveredUsers = 0;
        for(User uu:users.values()){
            if(uu.state != User.USER_STATE.DELIVERED){
                notDeliveredUsers++;
            }
        }

        // Evaluation handle
        if(notDeliveredUsers>0 && time_steps < 1100){
            // Continue as long as not all the users are delivered!
            time_steps++;
        }else{
            // Now all users are delivered
            if(time_steps < 1000){
                // Store the information and count how many data points we have collected for one system setup
//                System.out.println(time_steps);
//                chart.addRun(agents); // <--- graph-code: here information of one run is stored to graph
                for(Agent agent : agents.values()) {
                	totalRunDistance+= agent.getTotalDistance();
                }
                totalRuns++;
                steps_so_far++;
            }

            if(steps_so_far<average_steps){
                // We have not collected enough datapoints yet --> reset and get another datapoint!
                reset();
            }else{

                // One system state is captured --> we have enough data points for one system setup (EvaluationSetup)

                // now the average has to be made over all these data points
                // <-- graph-code: here calculate average of all the runs stored with addRun()

            	yValues.add(totalRunDistance/totalRuns);
            	yTimes.add((double)time_steps);
                // a new EvaluationSetup has to be configured
            	xValues.add(strategyId);
            	totalRunDistance = 0;
                totalRuns = 0;
            	strategyId+=5;
                EvaluationSetup.nextSetup();

                if(EvaluationSetup.evaluationMode != EvaluationSetup.EvaluationMode.Default){
                    // And we have to start over collecting data points for the new setup:
                    steps_so_far = 0;
                    reset();
                }else{
                    // We have gone through all the setups and collected data points for all setups!

                    // Now its time to build the graphs!

                    // <-- graph-code: here build graphs

                    System.out.println("Showing graph...");
                    chart.addSeries(xValues,yValues,"Total Distance Traveled");
                    chart.showLocalGraph("Number of Users","Total Distance Traveled");
                    timeChart.addSeries(xValues,yTimes,"Total Time to Deliver Completely");
                   timeChart.showLocalGraph("Number of Users","Total Time to Deliver Completely");

                    // And stop the system!
                    stop();
                }
            }
        }
    }




    // Threading stuff

    public static class RunThread extends Thread {

        int time;

        public RunThread(int time){
            this.time = time*time;
        }

        public void run() {
            while(true){
            	if(!resetLock) {
            		step();

                // step
                // board.update()


	                try {
	                    sleep(time);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
            	}
            }
        }
    }

    public static void run(int time) {
        runThread = new RunThread(time);
        runThread.start();
    }

    public static void stop() {
        runThread.interrupt();
        //XYChart chart = new XYChart(agents);
        
        runThread.stop();
    }
    
    public static void cluster() {
    	KMedoids cluster = new KMedoids();
		cluster.cluster(users,3);
		users = cluster.userDefineCluster(users);
    }
}
