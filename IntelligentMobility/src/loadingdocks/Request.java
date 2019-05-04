package loadingdocks;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Wieler on 30.04.2019.
 */
public class Request {

    static int id_count = 0;
    int ID;
    int userID;
    int matchedAgentID=-1;
    Point initPosition;
    Point targetPosition;
    List<Agent> offers;
    public boolean MATCHED = false;


    public Request(int userid, Point init, Point target){
        ID = id_count++;
        userID = userid;
        initPosition = init;
        targetPosition = target;
        offers = new ArrayList<Agent>();
    }

    public void appendOffer(Agent agent){
        offers.add(agent);
    }

    public void match(int agent_id){
        MATCHED = true;
        offers.clear();
        matchedAgentID = agent_id;
    }

}
