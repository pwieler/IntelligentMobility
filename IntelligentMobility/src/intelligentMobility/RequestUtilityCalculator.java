package intelligentMobility;

import java.util.List;

public class RequestUtilityCalculator {

    protected List<Request> requests;
    protected Board board;
    public RequestUtilityCalculator(List<Request> requestList, Board referenceToBoard) {
        board  = referenceToBoard;
        requests = requestList;
    }
    Request calculateMaxUtility() {
        return null;
    }
}
