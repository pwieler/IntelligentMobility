package loadingdocks;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

public class Utils {

	public static void main(String[] args) {
	    int[][] mat = new int[][]{ 
	        { 1, 0, 1, 1, 1, 1, 0, 1, 1, 1 }, 
	        { 1, 0, 1, 0, 1, 1, 1, 0, 1, 1 }, 
	        { 1, 1, 1, 0, 1, 1, 0, 1, 0, 1 }, 
	        { 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 }, 
	        { 1, 1, 1, 0, 1, 1, 1, 0, 1, 0 }, 
	        { 1, 0, 1, 1, 1, 1, 0, 1, 0, 0 }, 
	        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, 
	        { 1, 0, 1, 1, 1, 1, 0, 1, 1, 1 }, 
	        { 1, 1, 0, 0, 0, 0, 1, 0, 0, 1 } 
	    }; 
	    Point source = new Point(0, 0); 
	    Point dest = new Point(3, 4); 
	    //int dist = BFS(mat, source, dest); 
	    //if (dist != Integer.MAX_VALUE) System.out.println("Shortest Path is " + dist); 
	    //else System.out.println("Shortest Path doesn't exist"); 
	}
	
}
