package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import loadingdocks.Block.Type;

/**
 * Environment
 * @author Kevin Corrales
 */
public class Board {

	/** The environment */

	public static int nX = 20, nY = 20;
	private static Block[][] board;
	private static Entity[][] objects;
	private static List<Agent> vehicles;
	private static List<User> users;
	
//	private static double wallPercentage = 0.5;
	public static final int nVehicles = 1;
	public static final int nUsers = 8;
	
	private static Core core;
	
	
	/****************************
	 ***** A: SETTING BOARD *****
	 ****************************/


	public Board(){
		this.initialize();
		
		System.out.println(shortestPath(new Point(5,9),new Point(0,6)));
//		List<Point> pickups = new ArrayList<Point>();
//		List<Point> destinations = new ArrayList<Point>();
//		pickups.add(new Point(10,10));
//		destinations.add(new Point(10,19));
//		pickups.add(new Point(5,5));
//		destinations.add(new Point(5,8));
//		int [] order = shortestPathOrder(new Point(0,0),pickups,destinations,null,0);
//		Queue<Node> path = shortestPath(new Point(0,0),pickups,destinations);
//		for(int o : order)
//			System.out.println(o);
//		for(Node p : path ) {
//			System.out.println(p);
//		}
		
	}


	
	public void initialize() {

		/** A: create board */
		board = new Block[nX][nY];
		for(int i=0; i<nX; i++)
			for(int j=0; j<nY; j++)
				board[i][j] = new Block(Block.Type.free, Color.lightGray);


		users = new ArrayList<User>();
		
		/* Random walls
		for(int i = 0 ; i< nX*nY*wallPercentage; i++) {
			double rX = Math.random()*nX;
			double rY = Math.random()*nY;
			
			board[(int)rX][(int)rY] = new Block(Type.building, Color.gray);
		}
		*/
		
		/**Create Map **/
		for(int i = 0;i<nX;i++) {
			for(int j = 0; j<nY;j++) {
				if( (i==0 || j==0 || i==nX-1 || j==nY-1
						|| i<nX/4 && j<nY/2 && i>nX/11 && j>nY/11) || (i>nX/4 && i<nX/2 && j>0 && j<nY/4)
						|| (i>nX/4 && i<nX/2 && j<nY*3/4 && j>nY/4) || (i<nX/3 && i>nX/6 && j>nY/2 && j<nY-1)
						|| (i<nX/6 && i>0 && j>nY/2 && j<nY*2/3) || (i<nX/6 && i>0 && j<nY-2 && j>nY*2/3) 
						|| (i<nX/6 && i>0 && j==nY-1) || ( i>nX/3 && i<nX/2 && j<nY-2 && j>nY*3/4)
						|| (i<nX*4/5 && i>nX/3 && j==nY-1) || (i<nX*3/4 && i>nX/2 && j>nY*3/4)
						|| (i<nX*3/5 && i>nX/2 && j>nY/3 && j<nY*3/4) || (i>nX*3/5 && i<nX*4/5 && j>nY/2 && j<nY*3/4)
						|| (i>nX*3/5 && i<nX*5/7 && j>nY/3 && j<nY*3/4) || (i>nX/2 && i<nX*6/7 && j>nY/8 && j<nY/3 )
						|| (i>nX/2 && i<nX*2/3 && j<nY/8 ) || (i>nX*4/6 && i<nX*6/7  && j<nY/8 )
						|| ( i>nX*6/7 && j<nY/3 ) || ( i>nX*7/10 && i<nX-1 && j>nY/3 && j<nY/2)
						|| ( i>nX*8/10 && i<nX-2 && j>nY/3 && j<nY-2)  
						|| ( i<nX*5/6 && i>nX/2 && j<nY-2 && j>nY*3/4))
					board[i][j] = new Block(Type.building, Color.gray);
			}

		}
		
		
		/** Add Users */
		while(users.size()<nUsers) {

			Point startP = getRandomStreetCell(MobType.DEFAULT);
			Point targetP = getRandomStreetCell(MobType.DEFAULT);

			users.add(new User(startP, targetP, Color.RED));
		}
		
		/** C: create agents */
		vehicles = new ArrayList<Agent>();
		while(vehicles.size()<nVehicles) {

			MobType type = MobType.A; //MobType.values()[new Random().nextInt(MobType.values().length)];

			Color yor = Color.pink;
			int maxUsers = 1;
			switch(type) {
			case A:
				yor = Color.yellow;
				maxUsers=4;
				break;
			case B:
				yor = Color.blue;
				break;
				
			}

			Point p = getRandomStreetCell(type);
			vehicles.add(new Agent(p, yor,type,maxUsers,this));

		}
		objects = new Entity[nX][nY];
		for(User user : users) objects[user.point.x][user.point.y]=user;
		for(Agent agent : vehicles) objects[agent.point.x][agent.point.y]=agent;
	}

	public Point getRandomStreetCell(MobType type){

		while(true){
			int rX = (int)(Math.random()*nX);
			int rY = (int)(Math.random()*nY);

			if(board[rX][rY].color!=Color.gray ){//|| (type.equals(MobType.B) && closeToStreet(rX,rY))) {
				return new Point(rX,rY);
			}
		}

	}
	
	public static boolean closeToStreet(int rX,int rY) {
		for(int i=-1;i<2;i++) {
			for(int j=-1;j<2;j++) {
				if(rX>1 && rY>1 && rY<nY-1 && rX<nX-1)
				if(board[rX-i][rY-j].color!=Color.gray) {
					return true;
				}
			}
		}
		return false;

	}
	
	/****************************
	 ***** B: BOARD METHODS *****
	 ****************************/
	
	public static Entity getEntity(Point point) {
		return objects[point.x][point.y];
	}
	public static Block getBlock(Point point) {
		return board[point.x][point.y];
	}
	public static void updateEntityPosition(Point point, Point newpoint) {
		objects[newpoint.x][newpoint.y] = objects[point.x][point.y];
		objects[point.x][point.y] = null;
	}	
	public static void removeEntity(Point point) {
		objects[point.x][point.y] = null;
	}
	public static void insertEntity(Entity entity, Point point) {
		objects[point.x][point.y] = entity;
	}

	/***********************************
	 ***** C: ELICIT AGENT ACTIONS *****
	 ***********************************/

	private static GUI GUI;


	public void reset() {
		removeObjects();
		initialize();
		GUI.displayBoard();
		displayObjects();	
		GUI.update();
	}

	public static void step() {
		removeObjects();
		for(Agent a : vehicles) a.act();
		displayObjects();
		GUI.update();
	}




	public static void displayObjects(){
		for(Agent agent : vehicles) GUI.displayObject(agent);
		for(User user : users) GUI.displayObject(user);
	}
	
	public static void removeObjects(){
		for(Agent agent : vehicles) GUI.removeObject(agent);
		for(User user : users) GUI.removeObject(user);
	}
	
	public static void associateGUI(GUI graphicalInterface) {
		GUI = graphicalInterface;
	}
	
	//For queue used in BFS 
	public static class Node {
		Point point;
		Node parent; //cell's distance to source
		Node aggregated;

		boolean pickUp = false;
		boolean dropOff = false;

		public Node(Point point, Node parent) {
			this.point = point;
			this.parent = parent;
		}

		public Node(Point point, Node parent, boolean pickUp, boolean dropOff) {
			this.point = point;
			this.parent = parent;
			this.pickUp = pickUp;
			this.dropOff = dropOff;
		}
		public Point getPoint() {
			return this.point;
		}
		public void setAggregated(Node aggregated) {
			this.aggregated = aggregated;
		}
		public String toString() {
			return "("+point.x+","+point.y+")";
		}

		public void setPickUp(){
			pickUp = true;
		}

		public void setDropOff(){
			dropOff = true;
		}

	}
		
	public static Node shortestPath(Point src, Point dest) {

		if(src==dest){
			return null;
		}

	    boolean[][] visited = new boolean[nX][nY]; 
	    visited[dest.x][dest.y] = true;
	    Queue<Node> q = new LinkedList<Node>(); 
	    q.add(new Node(dest,null)); //enqueue source cell
	    
		//access the 4 neighbours of a given cell 
		int row[] = {-1, 0, 0, 1}; 
		int col[] = {0, -1, 1, 0}; 
	     
	    while (!q.isEmpty()){//do a BFS 
	        Node curr = q.remove(); //dequeue the front cell and enqueue its adjacent cells
	        Point pt = curr.point; 
//			System.out.println(">"+pt);
	        for (int i = 0; i < 4; i++) { 
	        	int x=pt.x,y=pt.y;
	        	if(pt.x+ row[i]<nX && pt.x+ row[i]>0) {
	        		x = pt.x + row[i];
	        	}
	        	if(pt.y+ col[i]<nY && pt.y+ col[i]>0) {
	        		y = pt.y + col[i]; 
	        	}
    	        if(x==src.x && y==src.y) return new Node(src,curr);
	            if(board[x][y].color!=Color.gray  && !visited[x][y]){ 
	                visited[x][y] = true; 
	    	        q.add(new Node(new Point(x,y), curr)); 
	            } 
	        }
	    }
	    return null; //destination not reached
	}
	
	public Node shortestPath(Point src, List<Point> pickups, List<Point> destinations) {
		if(pickups.size()==0)
			return null;

		List<Integer> invalid_pickups = new LinkedList<Integer>();

		for(int i = 0;i<pickups.size();i++){
			if(pickups.get(i).x == -1){
				pickups.set(i,destinations.get(i));
				invalid_pickups.add(i);
			}
		}


		int[] order = shortestPathOrder(new Point(0,0),pickups,destinations,null,0); 
		List<Node> completePath = new LinkedList<Node>();
		completePath.add(shortestPath(src,pickups.get(0)));
		for(int i=0; i<order.length;i++) {
			Node innerPath = shortestPath(pickups.get(i),destinations.get(i));
			completePath.add(innerPath);
			if(i <order.length-1) {
				Node outerPath = shortestPath(destinations.get(i),pickups.get(i+1));
				completePath.add(outerPath);
			}
		}


		// Make List of paths to one path!
		Node start_node = completePath.get(0);
		Node tmp = start_node;

		int i = 0;
		while(i < completePath.size()-1){
			while(tmp.parent!=null){
				tmp = tmp.parent;
			}
			tmp.parent = completePath.get(i+1);
			if(tmp.parent==null){
				break;
			}
			tmp = tmp.parent;
			i++;
		}

		tmp = start_node;
		while(tmp!=null){

			if(pickups.contains(tmp.point)){
				tmp.setPickUp();
			}
			if(destinations.contains(tmp.point)){
				tmp.setDropOff();
			}

			System.out.println((tmp.point).toString()+" pickup: "+tmp.pickUp+" dropoff: "+tmp.dropOff);

			tmp = tmp.parent;
		}

		System.out.println();
		return start_node;
	}


	public int[] shortestPathOrder(Point src, List<Point> pickups, List<Point> destinations, int[] order, int currentIndex) {
		if(order==null) {
			order = new int[pickups.size()];
		}
		float min = Integer.MAX_VALUE;
		for(int i = 0;i<pickups.size();i++) {
			if(!containsIndex(order,i)) {
				Node node = shortestPath(src,pickups.get(i));
				float pathLength = pathLength(node);
				if( pathLength<=min) {
					min = pathLength;
					order[currentIndex]=i;
				}
			}
		}
		currentIndex++;
		if(currentIndex<order.length) {
			order = shortestPathOrder(destinations.get(order[currentIndex]),pickups,destinations,order,currentIndex);
		}
		return order;
	}
	
	private boolean containsIndex(int [] order,int i) {
		for(int index : order)
			if (index==i)
				return true;
		return false;
	}
	

	float pathLength(Node path) {
		float result = 0.0f;
		if(path!=null){
			while (path.parent != null) {
				result += (float)path.point.distance(path.parent.point);
				path = path.parent;
			}
		}
		return result;
	}

}
