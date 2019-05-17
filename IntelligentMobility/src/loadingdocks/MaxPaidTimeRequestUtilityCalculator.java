package loadingdocks;

import java.util.List;

public class MaxPaidTimeRequestUtilityCalculator extends RequestUtilityCalculator {


    public MaxPaidTimeRequestUtilityCalculator(List<Request> requestList, Board referenceToBoard) {
        super(requestList,referenceToBoard);
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
