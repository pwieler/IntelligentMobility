package loadingdocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Pascal on 30.04.2019.
 */
public class Core {
    // Implement subscription pattern

    // changed this

    static Map<Integer, Agent> agents = new HashMap<Integer, Agent>();
    static Map<Integer, User> users = new HashMap<Integer, User>();
    static Map<Integer, Request> requests = new HashMap<Integer, Request>();
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
        requests.put(r.ID, r);
    }

    public static void broadcastRequests(){
        List<Request> r_list = new ArrayList<Request>(requests.values());

        for(Agent a:new ArrayList<Agent>(agents.values())){
            a.receiveRequests(r_list);
        }
    }


    /***********************************
     ***** C: ELICIT AGENT ACTIONS *****
     ***********************************/

    private static RunThread runThread;

    private static void step(){
        broadcastRequests();
    }


    // Threading stuff

    public static class RunThread extends Thread {

        int time;

        public RunThread(int time){
            this.time = time*time;
        }

        public void run() {
            while(true){
                board.step();
                step();
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
