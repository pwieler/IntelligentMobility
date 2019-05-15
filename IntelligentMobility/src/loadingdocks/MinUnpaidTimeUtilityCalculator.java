package loadingdocks;

import java.util.List;
import java.awt.Point;

public class MinUnpaidTimeUtilityCalculator extends UtilityCalculator{

    private List<Request> requests;
    private Board board;
    private Point myPos;
    public MinUnpaidTimeUtilityCalculator(List<Request> requestList, Board referenceToBoard, Point myPosition) {
        board  = referenceToBoard;
        requests = requestList;
        myPos = myPosition;
    }

    @Override
    Request calculateMaxUtilityRequest() {
        //if agent is zero confirmed users accept request based on the following
        //minimize "unpaid" time: sort to minimum pickup distance
        Request minDistToPickup = null;
        float minDist = Float.MAX_VALUE;

        for (Request request : requests) {
            float currentDist = board.pathLength(board.shortestPath(myPos, request.initPosition));
            if (currentDist < minDist) {
                minDistToPickup = request;
                minDist = currentDist;
            }

        }
        return minDistToPickup;
    }
}
