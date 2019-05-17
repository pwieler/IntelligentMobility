package loadingdocks;

import java.util.List;
import java.awt.Point;

public class MinUnpaidTimeRequestUtilityCalculator extends RequestUtilityCalculator {


    private Point myPos;
    public MinUnpaidTimeRequestUtilityCalculator(List<Request> requestList, Board referenceToBoard, Point myPosition) {
       super(requestList,referenceToBoard);
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
