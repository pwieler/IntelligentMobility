package loadingdocks;

import java.awt.*;

/**
 * Created by Pascal Wieler on 30.04.2019.
 */
public class Request {

    static int id_count = 0;
    int ID;
    int userID;
    Point initPosition;
    Point targetPosition;

    public Request(int userid, Point init, Point target){
        ID = id_count++;
        userID = userid;
        initPosition = init;
        targetPosition = target;
    }
}
