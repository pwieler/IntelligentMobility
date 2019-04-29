package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import loadingdocks.Agent.Action;
import loadingdocks.Block.Shape;

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
	private static final int nVehicles = 6;
	private static final int nUsers = 15;
	
	/****************************
	 ***** A: SETTING BOARD *****
	 ****************************/
	
	public static void initialize() {
		
		/** A: create board */
		board = new Block[nX][nY];
		for(int i=0; i<nX; i++) 
			for(int j=0; j<nY; j++) 
				board[i][j] = new Block(Shape.free, Color.lightGray);
				
		/** B: create ramp, useres and shelves */
		users = new ArrayList<User>();
		
		/* Random walls
		for(int i = 0 ; i< nX*nY*wallPercentage; i++) {
			double rX = Math.random()*nX;
			double rY = Math.random()*nY;
			
			board[(int)rX][(int)rY] = new Block(Shape.building, Color.gray);
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
					board[i][j] = new Block(Shape.building, Color.gray);
			}
		}
		
		/** Add Users */
		while(users.size()<nUsers) {
			double rXd = Math.random()*nX;
			double rYd = Math.random()*nY;
			int rX = (int) rXd;
			int rY = (int) rYd;
			if(board[rX][rY].color==Color.gray && closeToStreet(rX,rY)){
				users.add(new User(new Point(rX,rY), Color.RED));
			}
		}
		
		/** C: create agents */
		vehicles = new ArrayList<Agent>();
		while(vehicles.size()<nVehicles) {
			double rXd = Math.random()*nX;
			double rYd = Math.random()*nY;
			int rX = (int) rXd;
			int rY = (int) rYd;
			Type type = Type.values()[new Random().nextInt(Type.values().length)];
			Color color = Color.pink;
			switch(type) {
			case A:
				color = Color.yellow;
				break;
			case B:
				color = Color.blue;
				break;
				
			}
			if(board[rX][rY].color!=Color.gray || (type.equals(Type.B) && closeToStreet(rX,rY))) {
				vehicles.add(new Agent(new Point(rX,rY), color,type));
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
	
	private static RunThread runThread;
	private static GUI GUI;

	public static class RunThread extends Thread {
		
		int time;
		
		public RunThread(int time){
			this.time = time*time;
		}
		
	    public void run() {
	    	while(true){
	    		step();
				try {
					sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    }
	}
	
	public static void run(int time) {
		Board.runThread = new RunThread(time);
		Board.runThread.start();
	}

	public static void reset() {
		removeObjects();
		initialize();
		GUI.displayBoard();
		displayObjects();	
		GUI.update();
	}

	public static void sendMessage(Point point, Shape shape, Color color, boolean free) {
		for(Agent a : vehicles) a.receiveMessage(point, shape, color, free);		
	}

	public static void sendMessage(Action action, Point pt) {
		for(Agent a : vehicles) a.receiveMessage(action, pt);		
	}

	public static void step() {
		removeObjects();
		for(Agent a : vehicles) a.agentDecision();
		displayObjects();
		GUI.update();
	}

	public static void stop() {
		runThread.interrupt();
		runThread.stop();
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
}
