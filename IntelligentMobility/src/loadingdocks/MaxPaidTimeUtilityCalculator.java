package loadingdocks;

import java.util.List;

public class MaxPaidTimeUtilityCalculator extends UtilityCalculator {

    private List<Request> requests;
    private Board board;
    public MaxPaidTimeUtilityCalculator(List<Request> requestList, Board referenceToBoard) {
        board  = referenceToBoard;
        requests = requestList;
    }

    @Override
    Request calculateMaxUtilityRequest() {
        float maxLength = 0.0f;
        float currentLength;
        Request maxPaidTime = null;
        for (Request request : requests) {
            currentLength = board.pathLength(board.shortestPath(request.initPosition, request.targetPosition));
            if (currentLength > maxLength) {
                maxLength = currentLength;
                maxPaidTime = request;
            }
        }
        return maxPaidTime;
    }
}
