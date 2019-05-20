package loadingdocks;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;

public class TimeStressedOfferUtilityCalculator extends OfferUtilityCalculator {

    Point pickup;
    Point target;
    public TimeStressedOfferUtilityCalculator(List<Agent> offerList, Board referenceToBoard, Point myPickup, Point myTarget) {
        super(offerList,referenceToBoard);
        pickup = myPickup;
        target = myTarget;
    }

    private float calculateLengthUntilDelivered(Board.Node route) {
        boolean[] isAfterTarget = new boolean[1];
        isAfterTarget[0] = false;
        List<Board.Node> untilDropoff = new ArrayList<Board.Node>();
        route.visit((Board.Node n) -> {
            if(n.getPoint() == target)
                isAfterTarget[0] = true;
            if(!isAfterTarget[0]) {
                untilDropoff.add(n);
            }
        });
        float dist = 0.0f;
        for(int i = 0; i < untilDropoff.size() - 1; i++) {
            dist += untilDropoff.get(i).getPoint().distance(
                    untilDropoff.get(i+1).getPoint()
            );
        }
        return dist;
    }

    @Override
    public Agent calculateMaxUtility() {
        //choose offer with as less current users  as possible (i want to be alone in the taxi!)
        offers.sort((Agent offeringAgent1, Agent offeringAgent2) -> {
            Board.Node route1 = offeringAgent1.potentialNewRoute(pickup, target);
            Board.Node route2 = offeringAgent2.potentialNewRoute(pickup, target);
            return calculateLengthUntilDelivered(route1) < calculateLengthUntilDelivered(route2) ? -1 : 1;
        });
        return offers.get(0);
    }
}
