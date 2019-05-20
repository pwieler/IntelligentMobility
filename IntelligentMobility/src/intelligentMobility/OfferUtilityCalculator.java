package intelligentMobility;

import java.util.List;

public class OfferUtilityCalculator {
    protected List<Agent> offers;
    protected Board board;
    public OfferUtilityCalculator(List<Agent> offerList, Board referenceToBoard) {
        board  = referenceToBoard;
        offers = offerList;
    }
    Agent calculateMaxUtility() { return null;}
}
