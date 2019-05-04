package loadingdocks;

import java.util.*;

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


    public static void initialize(Board b) {
        board = b;
    }

    public static void reset(){
        agents.clear();
        users.clear();
        requests.clear();
        board.reset();
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
            if(users.get(rn.userID).processOffers()){
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
    }




    // Threading stuff

    public static class RunThread extends Thread {

        int time;

        public RunThread(int time){
            this.time = time*time;
        }

        public void run() {
            while(true){
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

    public static void run(int time) {
        runThread = new RunThread(time);
        runThread.start();
    }

    public static void stop() {
        runThread.interrupt();
        runThread.stop();
    }
}
