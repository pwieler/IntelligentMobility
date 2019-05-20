package intelligentMobility;
import java.util.List;

public class MostPassengersOfferUtilityCalculator extends OfferUtilityCalculator {

    public MostPassengersOfferUtilityCalculator(List<Agent> offerList, Board referenceToBoard) {
        super(offerList,referenceToBoard);
    }

    @Override
    public Agent calculateMaxUtility() {
        //choose offer with as less current users  as possible (i want to be alone in the taxi!)
        offers.sort((Agent offeringAgent1, Agent offeringAgent2) -> {
            return offeringAgent1.confirmed_users.size() >
                    offeringAgent2.confirmed_users.size() ? -1 : 1;
        });
        return offers.get(0);
    }
}
