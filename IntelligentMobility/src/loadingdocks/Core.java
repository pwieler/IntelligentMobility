package loadingdocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pascal on 30.04.2019.
 */
public class Core {
    // Implement subscription pattern

    Map<Integer, Agent> agents = new HashMap<Integer, Agent>();
    Map<Integer, User> users = new HashMap<Integer, User>();
    Map<Integer, Request> requests = new HashMap<Integer, Request>();

    public Core(){

    }

    public void registerToCore(Agent a){
        agents.put(a.ID,a);
    }

    public void registerToCore(User u){
        users.put(u.ID,u);
    }

    public void appendRequest(Request r){
        requests.put(r.ID, r);
    }
}
