package intelligentMobility;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;



public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	static JTextField speed;
	static JPanel boardPanel;
	static JButton run, reset, step;
	Board board;
	private int nX, nY;
	

	public class Cell extends JPanel {

		private static final long serialVersionUID = 1L;
		
		//public List<Entity> entities = new ArrayList<Entity>();
		public Entity[] entities = new Entity[board.nUsers+board.nVehicles];
		
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
	            for(Entity entity : entities) {
	            	if(entity!=null) {
			            g.setColor(entity.color);
			            if(entity instanceof User) {
			            	g.fillRect(8, 8, 13, 13);
				            g.setColor(Color.white);
			            	g.drawRect(8, 8, 13, 13);
			            } else {
			        		switch(((Agent)entity).direction) {
				    			case 0:  g.fillPolygon(new int[]{10, 20, 30}, new int[]{25, 0, 25}, 3); break;
				    			case 90: g.fillPolygon(new int[]{0, 25, 0}, new int[]{10, 20, 30}, 3); break;
				    			case 180:g.fillPolygon(new int[]{0, 20, 10}, new int[]{0, 0, 25}, 3); break;
				    			default: g.fillPolygon(new int[]{0, 25, 25}, new int[]{10, 20, 0}, 3); 
				    		}
			            }
	            	}
	            }
        }
	}

	public GUI() {
		setTitle("IntelligentMobility");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		board = new Board();
		Core.initialize(board);

		board.associateGUI(this);

		nX = board.nX;
		nY = board.nY;

		setSize(40*nY, 50*nX);
		add(createButtonPanel());


		
		boardPanel = new JPanel();
		boardPanel.setSize(new Dimension(30*nY,30*nX));
		boardPanel.setLocation(new Point(20,60));
		

		boardPanel.setLayout(new GridLayout(nX,nY));
		for(int i=0; i<nX; i++)
			for(int j=0; j<nY; j++)
				boardPanel.add(new Cell());
		
		displayBoard();
		board.displayObjects();
		update();
		add(boardPanel);
	}

	public void displayBoard() {
		for(int i=0; i<nX; i++){
			for(int j=0; j<nY; j++){
				int row=nY-j-1, col=i;
				Block block = board.getBlock(new Point(i,j));
				JPanel p = ((JPanel)boardPanel.getComponent(row*nX+col));
				p.setBackground(block.color);
				p.setBorder(BorderFactory.createLineBorder(Color.white));
			}
		}
	}
	
	public void removeObject(Entity object) {
		int row=nY-object.point.y-1, col=object.point.x;
		Cell p = (Cell)boardPanel.getComponent(row*nX+col);
		p.setBorder(BorderFactory.createLineBorder(Color.white));	
		for(int i = 0; i<p.entities.length;i++) {
			if(p.entities[i]!=null) {
				if(p.entities[i].equals(object))
					p.entities[i]=null;
			}
		}
	}
	
	public void displayObject(Entity object) {
		int row=nY-object.point.y-1, col=object.point.x;
		Cell p = (Cell)boardPanel.getComponent(row*nX+col);
		p.setBorder(BorderFactory.createLineBorder(Color.white));	
		for(int i = 0; i<p.entities.length;i++) {
			if(p.entities[i]==null) {
				p.entities[i]=object;
				break;
			}
		}
	}

	public void update() {
		boardPanel.invalidate();
	}

	private Component createButtonPanel() {
		JPanel panel = new JPanel();
		panel.setSize(new Dimension(600,50));
		panel.setLocation(new Point(0,0));
		
		step = new JButton("Step");
		panel.add(step);
		step.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(run.getText().equals("Run")) Core.step();
				else Core.stop();
			}
		});
		reset = new JButton("Reset");
		panel.add(reset);
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Core.reset();
			}
		});
		run = new JButton("Run");
		panel.add(run);
		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(run.getText().equals("Run")){
					int time = -1;
					try {
						time = Integer.valueOf(speed.getText());
					} catch(Exception e){
						JTextPane output = new JTextPane();
						output.setText("Please insert an integer value to set the time per step\nValue inserted = "+speed.getText());
						JOptionPane.showMessageDialog(null, output, "Error", JOptionPane.PLAIN_MESSAGE);
					}
					if(time>0){
						Core.run(time);
	 					run.setText("Stop");						
					}
 				} else {
					Core.stop();
 					run.setText("Run");
 				}
			}
		});
		speed = new JTextField("1");
		speed.setMargin(new Insets(5,5,5,5));
		panel.add(speed);
		
		return panel;
	}
}
