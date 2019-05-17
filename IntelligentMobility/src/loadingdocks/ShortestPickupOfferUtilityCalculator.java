package loadingdocks;
import java.awt.*;
import java.util.List;

public class ShortestPickupOfferUtilityCalculator extends OfferUtilityCalculator {
    private Point myPosistion;

    public ShortestPickupOfferUtilityCalculator(List<Agent> offerList, Board referenceToBoard, Point myPos) {
        super(offerList,referenceToBoard);
        myPosistion = myPos;
    }

    @Override
    public Agent calculateMaxUtility() {
        offers.sort((Agent offeringAgent1, Agent offeringAgent2) -> {

            return board.pathLength(Board.shortestPath(offeringAgent1.point, myPosistion)) <
                    board.pathLength(Board.shortestPath(offeringAgent2.point, myPosistion)) ? -1 : 1;
        });
        return offers.get(0);
    }
}
