package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import loadingdocks.Block.Type;

/**
 * Environment
 * @author Rui Henriques
 */
public class Board {

	/** The environment */

	public static int nX = 20, nY = 20;
	private static Block[][] board;
	private static Entity[][] objects;
	private static List<Agent> vehicles;
	private static List<User> users;
	
//	private static double wallPercentage = 0.5;
	public static final int nVehicles = 6;
	public static final int nUsers = 15;
	
	private static Core core;
	
	/****************************
	 ***** A: SETTING BOARD *****
	 ****************************/


	public Board(){
		this.initialize();
		
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
				if( (i<nX/4 && j<nY/2 && i>0 && j>0) || (i>nX/4 && i<nX/2 && j>0 && j<nY/4)
						|| (i>nX/4 && i<nX/2 && j<nY*3/4 && j>nY/4) || (i<nX/3 && i>nX/6 && j>nY/2 && j<nY-1)
						|| (i<nX/6 && i>0 && j>nY/2 && j<nY*2/3) || (i<nX/6 && i>0 && j<nY-2 && j>nY*2/3) 
						|| (i<nX/6 && i>0 && j==nY-1) || ( i>nX/3 && i<nX/2 && j<nY-2 && j>nY*3/4)
						|| (i<nX*4/5 && i>nX/3 && j==nY-1) || (i<nX*3/4 && i>nX/2 && j>nY*3/4)
						|| (i<nX*3/5 && i>nX/2 && j>nY/3 && j<nY*3/4) || (i>nX*3/5 && i<nX*4/5 && j>nY/2 && j<nY*3/4)
						|| (i>nX*3/5 && i<nX*5/7 && j>nY/3 && j<nY*3/4) || (i>nX/2 && i<nX*6/7 && j>nY/8 && j<nY/3 )
						|| (i>nX/2 && i<nX*2/3 && j<nY/8 ) || (i>nX*4/6 && i<nX*6/7  && j<nY/8 )
						|| ( i>nX*6/7 && j<nY/3 ) || ( i>nX*7/10 && i<nX-1 && j>nY/3 && j<nY/2)
						|| ( i>nX*8/10 && i<nX-1 && j>nY/3 && j<nY-1)  || ( i<nX*5/6 && i>nX/2 && j<nY-2 && j>nY*3/4))
					board[i][j] = new Block(Type.building, Color.gray);
			}

		}
		
		
		/** Add Users */
		while(users.size()<nUsers) {
			double rXd = Math.random()*nX;
			double rYd = Math.random()*nY;
			int rX = (int) rXd;
			int rY = (int) rYd;
			
			double dXd = Math.random()*nX;
			double dYd = Math.random()*nY;
			int dX = (int) dXd;
			int dY = (int) dYd;
			if(board[rX][rY].color!=Color.gray ) {
					//&& closeToStreet(rX,rY)){
				users.add(new User(new Point(rX,rY), new Point(dX,dY), Color.RED));
			}
		}
		
		/** C: create agents */
		vehicles = new ArrayList<Agent>();
		while(vehicles.size()<nVehicles) {
			double rXd = Math.random()*nX;
			double rYd = Math.random()*nY;
			int rX = (int) rXd;
			int rY = (int) rYd;
			MobType type = MobType.values()[new Random().nextInt(MobType.values().length)];

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
			if(board[rX][rY].color!=Color.gray || (type.equals(MobType.B) && closeToStreet(rX,rY))) {
				vehicles.add(new Agent(new Point(rX,rY), yor,type,maxUsers,this));

			}
		}
		objects = new Entity[nX][nY];
		for(User user : users) objects[user.point.x][user.point.y]=user;
		for(Agent agent : vehicles) objects[agent.point.x][agent.point.y]=agent;
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
		//for(Agent a : vehicles) a.move();
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
		    public Node(Point point, Node parent) {
		    	this.point = point;
		    	this.parent = parent;
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
		} 
		
	public static Node shortestPath(Point src, Point dest) { 
	    boolean[][] visited = new boolean[nX][nY]; 
	    visited[src.x][src.y] = true; 
	    Queue<Node> q = new LinkedList<Node>(); 
	    q.add(new Node(src,null)); //enqueue source cell 
	    
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
    	        if(x==dest.x && y==dest.y) return new Node(dest,curr); 
	            if(board[x][y].color!=Color.gray  && !visited[x][y]){ 
	                visited[x][y] = true; 
	    	        q.add(new Node(new Point(x,y), curr)); 
	            } 
	        }
	    }
	    return null; //destination not reached
	}
	
	public Queue<Node> shortestPath(Point src, List<Point> pickups, List<Point> destinations) {
		if(pickups.size()==0)
			return null;
		int[] order = shortestPathOrder(new Point(0,0),pickups,destinations,null,0); 
		Queue<Node> completePath = new LinkedList<Node>(); 
		completePath.add(shortestPath(src,pickups.get(0)));
		for(int i=0; i<order.length;i++) {
			Node innerPath = shortestPath(pickups.get(i),destinations.get(i));
			completePath.add(innerPath);
			if(i <order.length-1) {
				Node outerPath = shortestPath(destinations.get(i),pickups.get(i+1));
				completePath.add(outerPath);
			}
		}
		return completePath;
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
