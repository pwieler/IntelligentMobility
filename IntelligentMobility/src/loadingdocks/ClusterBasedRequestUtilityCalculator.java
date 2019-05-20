package loadingdocks;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class ClusterBasedRequestUtilityCalculator extends RequestUtilityCalculator {
    private  List<Integer> clusters;

    public ClusterBasedRequestUtilityCalculator(List<Request> requestList, Board referenceToBoard, List<User> currentAgentsUsers) {
        super(requestList,referenceToBoard);
        clusters = new ArrayList<Integer>();
        for(User u : currentAgentsUsers)
            clusters.add(u.cluster);
    }

    @Override
    public Request calculateMaxUtility() {

        requests.sort((Request r1, Request r2) -> {
            return clusters.contains(r1.user.cluster) ? -1 : 0;
        });
        return requests.get(0);
    }
}