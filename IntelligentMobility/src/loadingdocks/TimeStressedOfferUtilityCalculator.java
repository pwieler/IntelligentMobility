package loadingdocks;
import java.util.List;

public class TimeStressedOfferUtilityCalculator extends OfferUtilityCalculator {

    public TimeStressedOfferUtilityCalculator(List<Agent> offerList, Board referenceToBoard) {
        super(offerList,referenceToBoard);
    }

    @Override
    public Agent calculateMaxUtility() {
        //choose offer with as less current users  as possible (i want to be alone in the taxi!)
        offers.sort((Agent offeringAgent1, Agent offeringAgent2) -> {
            //TODO
            return 1;
        });
        throw new UnsupportedOperationException("not yet implemented");
        //return offers.get(0);
    }
}
