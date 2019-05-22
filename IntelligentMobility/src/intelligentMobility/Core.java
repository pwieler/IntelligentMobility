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
    static int average_steps = 10;
    static int steps_so_far = 0;



    public static void initialize(Board b) {
        chart = new XYChart();
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

    public static void step(){

        board.step();
        broadcastRequests();
        broadcastOffers();


        // Time-steps
        int notDeliveredUsers = 0;
        for(User uu:users.values()){
            if(uu.state != User.USER_STATE.DELIVERED){
                notDeliveredUsers++;
            }
        }

        if(notDeliveredUsers>0 && time_steps < 1100){
            System.out.println(time_steps);
            time_steps++;
        }else{
            if(time_steps < 1000){
                chart.addRun(agents);
                steps_so_far++;
            }

            if(steps_so_far<average_steps){
                reset();
            }else{
            	System.out.println("Showing graph...");
                chart.showGraph("Average Steps","Agents");
                stop();
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
