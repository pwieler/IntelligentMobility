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
    List<Integer> offers;
    public boolean MATCHED = false;


    public Request(int userid, Point init, Point target){
        ID = id_count++;
        userID = userid;
        initPosition = init;
        targetPosition = target;
        offers = new ArrayList<Integer>();
    }

    public void appendOffer(int agent_id){
        offers.add(agent_id);
    }

    public void match(int agent_id){
        MATCHED = true;
        offers.clear();
        matchedAgentID = agent_id;
    }

}
