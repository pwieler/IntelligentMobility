package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import loadingdocks.Block.Type;

/**
 * Agent behavior
 * @author Rui Henriques
 */
public class Agent extends Entity {

	static int id_count = 0;
	int ID;

	public int direction = 90;

	public int usersDelivered, usersToPickUp;
	private MobType type;
	private Board referenceToBoard;
	public Agent(Point point, Color color,MobType pType, int countUsers, Board boardReference){
		super(point, color);
		ID = id_count++;
		Core.registerToCore(this);
		type = pType;
		usersDelivered = 0;
		usersToPickUp = countUsers;
		this.referenceToBoard = boardReference;
	}


	// Core communication
	public void receiveRequests(List<Request> requestList) {

		//if agent is idle (state remains unimplemented), accept request based on the following
		//minimize "unpaid" time: sort to minimum pickup distance
		Request minDistToPickup = null;
		float minDist = Float.MAX_VALUE;
		//OR maximize guaranteed paid time
		float maxLength = 0.0f;
		float currentLength;
		Request maxPaidTime = null;
		for (Request request : requestList) {
			float currentDist = (float)this.point.distance(request.initPosition);
			if (currentDist < minDist) {
				minDistToPickup = request;
				minDist = currentDist;
			}
			currentLength = (float) request.initPosition.distance(request.targetPosition);
			if(currentLength > maxLength) {
				maxLength = currentLength;
				maxPaidTime = request;
			}
		}
		//TODO accept them both?
		minDistToPickup.appendOffer(this.ID	);
		maxPaidTime.appendOffer(this.ID);

		//assuming the agent already has an accepted request and thinks about accepting another one:
		Request oldRequest = new Request(Integer.MAX_VALUE,new Point(),new Point()); //TODO assume this request is the current one

		//"protect" old request: compare old route to the one including the new user
		//compare old route "firstPickup,firstDrop" with new route "firstPickup,secondPickup,firstDrop,secondDrop"
		//TODO for simplicity assume this new route is the shortest possible one (not e.g. "secondPickup,firstPickup,firstDrop,secondDrop")
		//float myPosToFirstPickupDist = pathLength(shortestPath(point,oldRequest.initPosition));
		float firstPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(oldRequest.initPosition,oldRequest.targetPosition));
		Request bestFittingRequest = null;
		float minIncludingNewRequest = Float.MAX_VALUE;
		for (Request newRequest : requestList) {
			float firstPickupToSecondPickupDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(oldRequest.initPosition,newRequest.initPosition));
			float secondPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(newRequest.initPosition,oldRequest.targetPosition));
			float includingNewPickup = firstPickupToSecondPickupDist + secondPickupToFirstDropDist;
			if( includingNewPickup < minIncludingNewRequest) { // found a more fitting route combining the two requests
				bestFittingRequest = newRequest;
				minIncludingNewRequest = includingNewPickup;
			}
		}
		float aThreshold = 100.0f; //TODO define threshold somewhere else
		//found the best fitting new request. is the first user not annoyed ?
		if( minIncludingNewRequest - firstPickupToFirstDropDist <  aThreshold) {
			bestFittingRequest.appendOffer(this.ID); //TODO
		}

		//do not protect old request: just find the best combination of 2 (or n?),
		//with best combination meaning maximum occupancy
		Request first = null;
		Request second = null;
		float distanceSharing = 0.0f;
		for(Request request1 : requestList) {
			for(Request request2 : requestList) {
				//TODO still assume that (first pickup, second pickup, first drop, second drop) is optimal
				float secondPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(request2.initPosition,request1.targetPosition));
				if(secondPickupToFirstDropDist > distanceSharing) {
					first = request1;
					second = request2;
					distanceSharing = secondPickupToFirstDropDist;
				}
			}
		}
		//have found two requests with maximum shared distance.
		// TODO but, is this a feasible ride ? probably no...
		first.appendOffer(this.ID);
		second.appendOffer(this.ID);

	}



}
