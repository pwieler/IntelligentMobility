package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import loadingdocks.Agent.Action;
import loadingdocks.Block.Type;

/**
 * Environment
 * @author Rui Henriques
 */
public class Board {

	/** The environment */

	public static int nX = 30, nY = 20;
	private static Block[][] board;
	private static Entity[][] objects;
	private static List<Agent> robots;
	private static List<User> users;
	private static Core core;
	
	
	/****************************
	 ***** A: SETTING BOARD *****
	 ****************************/
	
	public static void initialize() {

		core = new Core();

		
		/** A: create board */
		board = new Block[nX][nY];
		for(int i=0; i<nX; i++) 
			for(int j=0; j<nY; j++) 
				board[i][j] = new Block(Block.Type.free, Color.lightGray);


		Color[] colors = new Color[] {Color.red, Color.blue, Color.green, Color.yellow};
		users = new ArrayList<User>();

		int nUsers = 10;

		for(int i=0;i<nUsers; i++) {
			int x = (int)(Math.random() * nX);
			int y = (int)(Math.random() * nY);
			board[x][y] = new Block(Block.Type.pickup, Color.gray);

			Point initial_position = new Point(x,y);

			while(board[x][y].type != Type.free){
				x = (int)(Math.random() * nX);
				y = (int)(Math.random() * nY);
			}

			Point target_position = new Point(x,y);

			users.add(new User(core, initial_position, target_position, colors[i%4]));
			board[x][y] = new Block(Type.target_location, colors[i%4]);

		}

		
		/** C: create agents */
		int nrobots = 10;
		robots = new ArrayList<Agent>();
		for(int j=0; j<nrobots; j++) robots.add(new Agent(core, new Point(0,j), Color.pink, nUsers));
		
		objects = new Entity[nX][nY];
		for(User user : users) objects[user.point.x][user.point.y]= user;
		for(Agent agent : robots) objects[agent.point.x][agent.point.y]=agent;
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

	public static void sendMessage(Point point, Block.Type type, Color color, boolean free) {
		for(Agent a : robots) a.receiveMessage(point, type, color, free);
	}

	public static void sendMessage(Action action, Point pt) {
		for(Agent a : robots) a.receiveMessage(action, pt);		
	}

	public static void step() {
		removeObjects();
		for(Agent a : robots) a.agentDecision();
		displayObjects();
		GUI.update();
	}

	public static void stop() {
		runThread.interrupt();
		runThread.stop();
	}

	public static void displayObjects(){
		for(Agent agent : robots) GUI.displayObject(agent);
		for(User user : users) GUI.displayObject(user);
	}
	
	public static void removeObjects(){
		for(Agent agent : robots) GUI.removeObject(agent);
		for(User user : users) GUI.removeObject(user);
	}
	
	public static void associateGUI(GUI graphicalInterface) {
		GUI = graphicalInterface;
	}
}
