import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import java.util.Vector;
import java.awt.Robot;
import java.io.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

//----------------------------------------------------------------------------

public class WorkShopBuddy extends Applet implements KeyListener
{
	ActivePlanPanel panel;
	ControlPanel controls;
	
	public void init()
	{
		setLayout(new BorderLayout());
		panel = new ActivePlanPanel();
		controls = new ControlPanel(panel);
		add("Center", panel);
		add("South",controls);
		addKeyListener(this);
	}

	public void keyTyped(KeyEvent evt) {


	}  // end keyTyped()
   
   
	public void keyPressed(KeyEvent evt) {
		int key = evt.getKeyCode();  // keyboard code for the key that was pressed
		System.out.println(evt.getKeyText( evt.getKeyCode()) );
		if (key == KeyEvent.VK_LEFT) {
			System.out.println(evt.getKeyText( evt.getKeyCode()) );
		}
		else if (key == KeyEvent.VK_RIGHT) {
			System.out.println(evt.getKeyText( evt.getKeyCode()) );
		}
		else if (key == KeyEvent.VK_UP) {
			System.out.println(evt.getKeyText( evt.getKeyCode()) );
		}
		else if (key == KeyEvent.VK_DOWN) {
			System.out.println(evt.getKeyText( evt.getKeyCode()) );
		}

	}  // end keyPressed()


	public void keyReleased(KeyEvent evt) {
      // empty method, required by the KeyListener Interface
	}

	public void destroy()
	{
		remove(panel); remove(controls);
	}

	public static void main(String args[])
	{
		Frame f = new Frame("WorkShopBuddy");
		WorkShopBuddy WorkShopBuddy = new WorkShopBuddy();
		WorkShopBuddy.init();
		WorkShopBuddy.start();
		f.add("Center", WorkShopBuddy);
		f.setSize(600, 400);
		f.show();
	}
}

//----------------------------------------------------------------------------

class ActivePlanPanel extends Panel implements MouseListener,MouseMotionListener 
{
	public static final int LINES = 0;
	public CNCLineCollection lines = new CNCLineCollection();
	public CNCLineCollection templines = new CNCLineCollection();
	
	int x1,y1,x2,y2; // starting from
	int x,y; // mouse loc
	int proposedx=0, proposedy = 0;
	boolean atEndPoint = false;
	boolean dragging = false;
	boolean moving = false;
	boolean dirty = true;
	boolean selecting =false;
	public boolean mousemoveflag =false;
	int mousemovex=0;
	int mousemovey=0;
	int snapsize = 1;
	Robot robot;

	public ActivePlanPanel()
	{
		setFocusTraversalKeysEnabled(false);
		setBackground(Color.white);
		addMouseMotionListener(this);
		addMouseListener(this);
		
		mdriver.setCoordinates(0,0,0);
	}




	public void mouseDragged(MouseEvent e)
	{
		if (mousemoveflag)
		{
			int xdif = mousemovex - e.getX();
			int ydif = mousemovey - e.getY();
			if (xdif > 0) xdif = 1;
			if (ydif > 0) ydif = -1;
			if (xdif < 0) xdif = -1;
			if (ydif < 0) ydif = 1;
			mdriver.moveRelative(xdif,ydif,0);

			
		}
		else
		{
		x2 = e.getX();
		y2 = e.getY();
		this.mouseMoved(e);
		repaint();
		}
	}

	public void mouseMoved(MouseEvent e)
	{

		atEndPoint=false;
		for (int i=0; i  < lines.size(); i++)
		{
			CNCLine p = (CNCLine)lines.elementAt(i);
			if ((Math.abs(e.getX()-p.x)  <=(snapsize/2)) && (Math.abs(e.getY()-p.y)<5))
			{
				proposedx = p.x;	proposedy = p.y;
				atEndPoint = true;
				repaint();
			} else if ((Math.abs(e.getX()-p.x1)  <=(snapsize/2)) && (Math.abs(e.getY()-p.y1)<=(snapsize/2)))
			{
				proposedx = p.x1;	proposedy = p.y1;
				atEndPoint = true;
				repaint();
			}
		}
		
		x = e.getX();	y = e.getY();
		
		if ((!atEndPoint)&&(proposedx  >0)&&(proposedy  >0))
		{
			proposedx =0;	proposedy = 0;
			repaint();
		}
	}

	public void mousePressed(MouseEvent e)
	{
		if (mousemoveflag)
		{

			mousemovex = e.getX();
			mousemovey = e.getY();
			
		} else
		{
		if (atEndPoint)
		{
			moveMouse(proposedx,proposedy);
			x = proposedx;	y = proposedy;
		} else
		{
			x = e.getX();	y = e.getY();
		}
		dirty = true;
		e.consume();
		if ((moving) && (templines.size()>0))
		{
			x1 = x;	y1 = y;
			dragging = true;
			repaint();
		} else if ((moving) && (templines.size()<=0))
		{
			CNCLineCollection temp = lines;
			lines = new CNCLineCollection();
			templines = temp;
			x1 = x;	y1 = y;
			dragging = true;
			repaint();
		} else	if (atEndPoint)
		{
			for (int i=0; i  < lines.size(); i++)
			{
				CNCLine p = (CNCLine)lines.elementAt(i);
				if ((x == p.x) && (y == p.y))
				{
					lines.remove(i);
					x1 = p.x1;	y1 = p.y1;
					dragging = true;
					i= lines.size();
				} else if  ((x == p.x1) && (y == p.y1))
				{
					lines.remove(i);
					x1 = p.x;	y1 = p.y;
					i= lines.size();
					dragging = true;
				}
			}
		} else
		{
			x1 = x; y1 = y;
			dragging = true;
		}
		}
	}

	public void mouseReleased(MouseEvent e)
	{
  		e.consume();

		if (atEndPoint)
		{
			moveMouse(proposedx,proposedy);
			x = proposedx;	y = proposedy;
		} else
		{
			x = e.getX();	y = e.getY();
		}
		dirty = true;
  		if (selecting)
  		{
     			templines = lines.getSubset(x1,y1,x,y);
     			x1 = x;	y1 = y;
     			moving = true;
     			selecting = false;
  		} else if (moving)
  		{ } else
  		{
    			int _x2 = (x/snapsize) * snapsize;
    			int _y2 = (y/snapsize) * snapsize;
    			int _x1 = (x1/snapsize) * snapsize;
    			int _y1 = (y1/snapsize) * snapsize;
    			lines.addElement(new CNCLine(_x1,_y1, _x2, _y2));
    			lines.optimize();
  		}
  		dragging = false;
  		repaint();
	}

	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mouseClicked(MouseEvent e) { }
	
	public void moveMouse(int x,int y)
	{
		try
		{
			robot = new Robot();
			Point p = this.getLocationOnScreen();
			robot.mouseMove(p.x+x,p.y+y);
		} catch (Exception e1) { }
	}
	
	public void CNCMove(int x, int y, int z)
	{
		mdriver.moveRelative(x,y,z);
	}
	public void CNCdraw()
	{
		mdriver.setCoordinates(0,0,0);
		System.out.println("here" + lines.size());
		for (int i=0; i  < lines.size(); i++)
		{
			System.out.println("here1");
			CNCLine p = (CNCLine)lines.elementAt(i);
			int dtop = Math.abs(Math.abs(mdriver.absx)-Math.abs(p.x)) + Math.abs(Math.abs(mdriver.absy)-Math.abs(p.y));
			int dbot = Math.abs(Math.abs(mdriver.absx)-Math.abs(p.x1)) + Math.abs(Math.abs(mdriver.absy)-Math.abs(p.y1));
			int gx=p.x;	int gy=p.y;	int gx1=p.x1;	int gy1=p.y1;
			if (dbot  < dtop)
			{
				gx=p.x1;	gy=p.y1;	gx1=p.x;	gy1=p.y;
			}
			System.out.println("line : x=" + gx +  " y=" +gy + " x1="+gx1+" y1= "+ gy1);
			if ((mdriver.absx != gx) || (mdriver.absy != -gy))
			{
				System.out.println("moving to : x=" + gx +  " y=" +gy);
				mdriver.moveAbsolute(mdriver.absx,mdriver.absy,4); //move up
				mdriver.moveAbsolute(gx,-gy,4); //to location
				mdriver.moveAbsolute(gx,-gy,-1); // move down;
			}
			System.out.println("drawing   : x=" + gx1 +  " y=" +gy1);
			mdriver.moveAbsolute(gx1,-gy1,-1); //cut
		}
		System.out.println("going home: x=" + 0 +  " y=" + 0);
		mdriver.moveAbsolute(mdriver.absx,mdriver.absy,4); // move up
		mdriver.moveAbsolute(0,0,4);  // move home
		mdriver.moveAbsolute(0,0,0);  // move back
		System.out.println("done.");  
	}

	Image offScrImg;
	public void paint(Graphics g)
	{
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setStroke( new BasicStroke(snapsize));
 		if ( offScrImg == null )
 		{
  			offScrImg = null;
  			offScrImg =  createImage( getSize().width, getSize().height );
 		}
  		if (dirty)
  		{
			Graphics og = offScrImg.getGraphics();
			Graphics2D og2 = (Graphics2D)og;
			og2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			og2.setStroke( new BasicStroke(snapsize));
			og.setColor(getBackground());
			og.fillRect(0, 0, getSize().width, getSize().height);
			og.setColor(getForeground());
			for (int i=0; i  < lines.size(); i++)
			{
				CNCLine p = (CNCLine)lines.elementAt(i);
				og2.setColor((Color)getForeground());
				og2.draw(new Line2D.Double(p.x, p.y, p.x1, p.y1));
			}
			dirty = false;
			og.dispose();
		}
  		g.drawImage(offScrImg, 0, 0, this);
  		if (selecting)
  		{
   			g.drawRect(x1,y1,x2-x1,y2-y1);
  		} else if ((dragging) && (!moving))
  		{
			g2.setColor(getForeground());
			int _x2 = (x2/snapsize) * snapsize;
			int _y2 = (y2/snapsize) * snapsize;
			int _x1 = (x1/snapsize) * snapsize;
			int _y1 = (y1/snapsize) * snapsize;
			g2.draw(new Line2D.Double(_x1,_y1,_x2,_y2));
  		}
		if (templines.size()>0)
  		{
			templines.transpose((x-x1),(y-y1),snapsize);
			x1 = x;	y1 = y;
			for (int i=0; i  < templines.size(); i++)
			{
				CNCLine p = (CNCLine)templines.elementAt(i);
				g2.setColor(Color.green);
				g2.draw(new Line2D.Double(p.x, p.y, p.x1, p.y1));
			}
  		}
  		if (atEndPoint)
  		{
      			g.setColor(Color.red);
      			g.fillRect(proposedx-5,proposedy-5,10,10);
  		}
	}
}
//----------------------------------------------------------------------------

class ControlPanel extends Panel implements ItemListener,ActionListener,KeyListener
{
	ActivePlanPanel target;
	Checkbox moveCheckbox,select;
	Button save,load, U,D,L,R,F,B,snapsize,mousemove;
	TextField filename;
	Panel controlkeys;

	public ControlPanel(ActivePlanPanel target)
	{
		this.target = target;
		setLayout(new FlowLayout());
		setBackground(Color.lightGray);
		setSize(600,400);
		target.setForeground(Color.red);
		CheckboxGroup group = new CheckboxGroup();
		Checkbox b;
		Button b1;
		
		add(filename = new TextField ("x.gif"));
		
		add(save = new Button("save"));
		save.addActionListener(this);
		
		add(load = new Button("load"));
		load.addActionListener(this);
		add(b1= new Button("CNC"));
		b1.addActionListener(this);
		add(snapsize = new Button ("1mm"));
		snapsize.addActionListener(this);
		add(select= new Checkbox("Select", null, false));
		select.addItemListener(this);
		add(moveCheckbox= new Checkbox("Move", null, false));
		moveCheckbox.addItemListener(this);
		
		controlkeys = new Panel();
		controlkeys.setLayout(new GridLayout());
		controlkeys.setSize(300, 300);
		add(controlkeys,"Right");
		
		controlkeys.add("North",F = new Button("F"));
		F.addActionListener(this);
		controlkeys.add("South",B = new Button("B"));
		B.addActionListener(this);
		controlkeys.add("Left",L = new Button("L"));
		L.addActionListener(this);
		controlkeys.add("Right",R = new Button("R"));
		R.addActionListener(this);
		
		controlkeys.add("Right", U = new Button("U"));
		U.addActionListener(this);
		controlkeys.add("Right", D = new Button("D"));
		D.addActionListener(this);
		add("Left", mousemove = new Button("MM"));
		mousemove.addActionListener(this);
		
		
		target.setForeground(Color.black);
		addKeyListener( this );
	}
	
	public void keyPressed( KeyEvent e ) {
		

	}
	public void keyReleased( KeyEvent e ) {
		System.out.println(e.getKeyText( e.getKeyCode()) );
	}
	public void keyTyped( KeyEvent e ) {
		System.out.println(e.getKeyText( e.getKeyCode()) );
	}

	public void paint(Graphics g)
	{
		Rectangle r = getBounds();
		g.setColor(Color.lightGray);
		g.draw3DRect(0, 0, r.width, r.height, false);
		int n = getComponentCount();
		for(int i=0; i<n; i++)
		{
			Component comp = getComponent(i);
			if (comp instanceof Checkbox)
			{
				Point loc = comp.getLocation();
				Dimension d = comp.getSize();
				g.setColor(comp.getForeground());
				g.drawRect(loc.x-1, loc.y-1, d.width+1, d.height+1);
			}
		}
	}
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == save)
		{
			target.lines.saveToFile(filename.getText());
		} else	if (e.getSource() == load)
		{
			if ((filename.getText()).indexOf(".gif")>-1)
			{
				target.templines.readFromJPG(filename.getText(),target.snapsize);
			} else
			{
				target.templines.readFromSVGFile(filename.getText());
			}
			target.x = 0;	target.y = 0;	target.x1 = 0;	target.y1 = 0;
			target.dirty=true;
			moveCheckbox.setState(true);
			target.moving = moveCheckbox.getState();
			target.repaint();
		} else if (e.getSource() == snapsize)
		{
			if (snapsize.getLabel()=="1mm")
			{
				snapsize.setLabel("3mm");
				target.snapsize = 3;
			} else if (snapsize.getLabel()=="3mm")
			{
				snapsize.setLabel("5mm");
				target.snapsize = 5;
			} else if (snapsize.getLabel()=="5mm")
			{
				snapsize.setLabel("10mm");
				target.snapsize = 10;
			} else
			{
				snapsize.setLabel("1mm");
				target.snapsize = 1;
			}
			target.dirty=true;
			target.repaint();
		}
		else if (e.getSource() == F)   { target.CNCMove(0,1,0); }
		else if (e.getSource() == B)   { target.CNCMove(0,-1,0); }
		else if (e.getSource() == L)   { target.CNCMove(-1,0,0); }
		else if (e.getSource() == R)   { target.CNCMove(1,0,0); }
		else if (e.getSource() == U)   { target.CNCMove(0,0,1); }
		else if (e.getSource() == D)   { target.CNCMove(0,0,-1); }
		else if (e.getSource() == mousemove)   { target.mousemoveflag = !target.mousemoveflag; }
		else{ target.CNCdraw();	}
	}
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getSource() == moveCheckbox)
		{
			if (moveCheckbox.getState())
			{
				target.templines = new CNCLineCollection();
				target.moving = moveCheckbox.getState();
				target.dirty = true;
				repaint();
			} else
			{
				while (target.templines.size()  >0)
				{
					CNCLine p = (CNCLine)target.templines.elementAt(0);
					target.lines.addElement(p);
					target.templines.remove(0);
				}
				target.x1 = 0;	target.x = 0;	target.y1 = 0;	target.y = 0;
				target.moving = moveCheckbox.getState();
				select.setState(false);
				target.dirty = true;
				target.repaint();
			}
		} else
		{
			if (e.getSource() == select)
			{
				target.selecting = select.getState();
				moveCheckbox.setState(true);
			}
		}
	}
}
//----------------------------------------------------------------------------
class CNCLineCollection
{
	Vector lines = new Vector();
	public int size()
	{
		return(lines.size());
	}
	public CNCLine elementAt(int i)
	{
		return((CNCLine) lines.elementAt(i));
	}
	public void remove(int i)
	{
		lines.remove(i);
	}
	public void  addElement(CNCLine line)
	{
		lines.addElement(line);
	}
	public CNCLineCollection getSubset(int x1,int y1,int x2,int y2)
	{
		CNCLineCollection sub = new CNCLineCollection();
		
		for (int i=0; i  < size(); i++)
		{
			CNCLine p = elementAt(i);
			int sx,sy,ex,ey;
			if (p.x < p.x1) { sx = p.x; ex=p.x1; } else {sx = p.x1;ex=p.x; }
			if (p.y < p.y1) { sy = p.y; ey=p.y1; } else {sy = p.y1;ey=p.y; }		
			if ((sx >= x1)&&(ex <=x2)&&(sy>=y1)&&(ey <=y2))
			{
				sub.addElement(p);
				remove(i);
				i = i -1;
			}
		}
		return sub;
	}
	
	public void optimize()
	{
		for (int i=0; i  < size(); i++)
		{
			CNCLine p = elementAt(i);
			double slope = (int)((((float)p.y1-p.y)/((float)p.x1-p.x))*10);
			for (int j=0; j < size(); j++)
			{
				if (j != i)
				{
					CNCLine p1 = elementAt(j);
					double slope1 = (int)((((float)p1.y1-p1.y)/((float)p1.x1-p1.x))*10);
					if ((slope1 == slope) && ((p.x==p1.x1)&&(p.y==p1.y1)))
					{
						p.x = p1.x;	p.y = p1.y;
						remove(j);
						j = j -1;
					}
				}
			}
		}
		double totaldistance = 0;
		double totallength = 0;
		int swaps = 0;
		for (int i=0; i  < size(); i++)
		{
			CNCLine p = elementAt(i);
			double length =  Math.sqrt(((p.x-p.x1)*(p.x-p.x1)) + ((p.y-p.y1)*(p.y-p.y1)));
			int closestline=-1;
			double closestdistance=100000000;
			for (int j=i+1; j < size(); j++)
			{
				CNCLine p1 = elementAt(j);
				double length1 =  Math.sqrt(((p1.x-p1.x1)*(p1.x-p1.x1)) + ((p1.y-p1.y1)*(p1.y-p1.y1)));
				double d = Math.sqrt(((p1.x-p.x)*(p1.x-p.x)) + ((p1.y-p.y)*(p1.y-p.y))); // b b
				double d1 = Math.sqrt(((p1.x1-p.x)*(p1.x1-p.x)) + ((p1.y1-p.y)*(p1.y1-p.y))); // b e
				double d2 = Math.sqrt(((p1.x-p.x1)*(p1.x-p.x1)) + ((p1.y-p.y1)*(p1.y-p.y1))); // e b
				double d3 = Math.sqrt(((p1.x1-p.x1)*(p1.x1-p.x1)) + ((p1.y1-p.y1)*(p1.y1-p.y1))); // e e
				double distance =0;
				distance = (d < d1)?d:d1;
				distance = (distance < d2)?distance:d2;
				distance = (distance < d3)?distance:d3;
				distance = distance; //+ length1;
				if (closestdistance>= distance)
				{
					closestline=j;
					closestdistance=distance;
				}
			}
			if (closestline > -1)
			{
				CNCLine temp = (CNCLine) lines.elementAt(closestline);
				lines.setElementAt(elementAt(i+1),closestline);
				lines.setElementAt(temp,i+1);
				totaldistance = totaldistance + closestdistance;
				swaps = swaps +1;
				//System.out.println("swap "+(i+1)+" "+ closestline);
			}
			totallength = totallength + length;
		}
		System.out.println("Lines:" + size() + " total distance="+(int)totaldistance+" cut length="+(int)totallength+" %Cutting="+(int)((totallength*100)/(totallength+totaldistance))+" ESTTIME="+(int)((totallength+totaldistance)/4)/60);
	} 
	
	// TODO: snapsize
	public void transpose(int x,int y,int snapsize)
	{
		for (int i=0; i  < size(); i++)
		{
			CNCLine p = elementAt(i);
			//p.x = ((p.x +x)/snapsize) *snapsize;
			//p.y = ((p.y +y)/snapsize) *snapsize;
			//p.x1 = ((p.x1 + x)/snapsize) *snapsize;
			//p.y1 = ((p.y1 + y)/snapsize) *snapsize;
			p.x = p.x +x;
			p.y = p.y +y;
			p.x1 = p.x1+ x;
			p.y1 = p.y1 + y;
		}
		optimize();
	}
	public void saveToFile(String filename)
	{
		Writer output = null;
		try
		{
			output = new BufferedWriter( new FileWriter(filename) );
			output.write("<?xml version=\"1.0\" standalone=\"no\"?>");
			//output.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"  \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
			output.write("<svg width=\"50cm\" height=\"40cm\" viewBox=\"0 0 500 400\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
			for (int i=0; i  < size(); i++)
			{
				CNCLine p = elementAt(i);
				//  <line x1="100" y1="30" x2="30" y2="10" stroke-width="5"  />
				output.write("<line x1=\"" + p.x +  "\" y1= \""+p.y+"\" x2=\""+p.x1+"\" y2=\""+p.y1+"\" />\n");
			}
			output.write("</svg>");
			output.close();
		} catch (Exception e)
		{ }
	}
	
	public void readFromJPG(String filename,int dist)
	{
		int distance = 3;
		int maxx = 400;
		int maxy = 500;
		BufferedImage image = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
		try
		{
			File f = new File(filename);
			image = ImageIO.read(f);
		} catch(IOException e){   e.printStackTrace(); }
		//BufferedImage image1 = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.white);
		g.drawRect(0,0,image.getWidth()-1,image.getHeight()-1);
		g.drawRect(1,1,image.getWidth()-1,image.getHeight()-2);
		g.drawRect(2,2,image.getWidth()-1,image.getHeight()-3);
		g.drawRect(3,3,image.getWidth()-1,image.getHeight()-4);
		//Graphics2D g2 = (Graphics2D)g;
		int x = distance+1;
		int y = distance+1;
		boolean nextspot = true;
		/*while (nextspot)	
		{
			image.setRGB(x,y,0xFF0000); //mark it
			nextspot=false;
			for (x=1;x>image.getWidth()-1;x++)
			{
				for (y=1;y>image.getHeight()-1;y++)
				{
					try
					{
						levelw  = ((image.getRGB(x1,y)  & 0xFFFFFF) == 0x000000)?1:0;
					} catch (Exception e)
					{
						levelw = 0;
					}

				}
			}


	}*/
		System.out.println("writing output..");
		try
		{
			File fo = new File("out.jpg");
			ImageIO.write(image, "JPG", fo);
		} catch(IOException e){   e.printStackTrace(); }
		System.out.println("done.");

	}
	public void readFromFile(String filename)
	{
		BufferedReader input = null;
		try
		{
			input = new BufferedReader( new FileReader(filename) );
			String line = null;
		while (( line = input.readLine()) != null)
		{
			String[] coords = line.split(",");
			int _x  = Integer.parseInt(coords[0]);
			int _x1 = Integer.parseInt(coords[2]);
			int _y  = Integer.parseInt(coords[1]);
			int _y1 = Integer.parseInt(coords[3]);
			lines.addElement(new CNCLine(_x,_y,_x1,_y1));
		}
		} catch (Exception e)
		{
			System.out.println("error" + e);
		}
	}
	public void readFromSVGFile(String filename)
	{
		DocumentBuilderFactory  factory;
		DocumentBuilder parser;
		System.out.println("reading XML file");
		File f = new File(filename);
		try
		{
		System.out.println("making factory");
		factory = DocumentBuilderFactory.newInstance();
		System.out.println("making parser");
		parser = factory.newDocumentBuilder();
		System.out.println("parsing");
		Document document = parser.parse(f);
		System.out.println("parsed XML file");
		//  <line x1="100" y1="30" x2="30" y2="10" stroke-width="5"  />
		NodeList linenodes = document.getElementsByTagName("line");
		for(int i = 0; i  < linenodes.getLength(); i++)
		{
		Element linenode = (Element)linenodes.item(i);
		
		int _x  = Integer.parseInt(linenode.getAttribute("x1"));
		int _x1 = Integer.parseInt(linenode.getAttribute("x2"));
		int _y  = Integer.parseInt(linenode.getAttribute("y1"));
		int _y1 = Integer.parseInt(linenode.getAttribute("y2"));
		lines.addElement(new CNCLine(_x,_y,_x1,_y1));
		}
		//  <rect x="1" y="1" width="1198" height="398" fill="none" stroke="blue" stroke-width="2" />
		NodeList rectnodes = document.getElementsByTagName("rect");
		for(int i = 0; i  < rectnodes.getLength(); i++)
		{
		Element rectnode = (Element)rectnodes.item(i);
		
		int _x      = Integer.parseInt(rectnode.getAttribute("x"));
		int _y      = Integer.parseInt(rectnode.getAttribute("y"));
		int _width  = Integer.parseInt(rectnode.getAttribute("width"));
		int _height = Integer.parseInt(rectnode.getAttribute("height"));
		
		lines.addElement(new CNCLine(_x,_y,_x+_width,_y));
		lines.addElement(new CNCLine(_x+_width,_y,_x+_width,_y+_height));
		lines.addElement(new CNCLine(_x+_width,_y+_height,_x,_y+_height));
		lines.addElement(new CNCLine(_x,_y+_height,_x,_y));
		}
		//  <circle cx="60" cy="20" r="10" fill="red" stroke="blue" stroke-width="10"  />
		// circle ((x+x0)-x0)^2)+((y+y0)-y0)^2) = r^2
		NodeList circlenodes = document.getElementsByTagName("circle");
		for(int i = 0; i  < circlenodes.getLength(); i++)
		{
		Element circlenode = (Element)circlenodes.item(i);
		
		int _x      = Integer.parseInt(circlenode.getAttribute("cx"));
		int _y      = Integer.parseInt(circlenode.getAttribute("cy"));
		int _r  = Integer.parseInt(circlenode.getAttribute("r"));
		
		//((x+x0)-x0)^2)+((y+y0)-y0)^2) = r^2
		//y = y0 + sqrt(r^2-(x-x0)^2)
		float r2 = _r * _r;
		int x = 0;
		int y = 0;
		int x1 = -10;
		int y1 = 0;
		int x2,y2;
		for (x = -_r; x  <= _r; x=x+2) {
			y = (int) (Math.sqrt(r2 - x*x) + 0.5);
		
			x2 = _x + x;
			y2 = _y + y;
			if (x1 !=-10)
			{
			lines.addElement(new CNCLine(x1,y1,x2,y2));
			}
			x1 = x2;
			y1 = y2;
		}
		for (x = _r; x  >= -_r; x=x-2) {
			y = (int) (Math.sqrt(r2 - x*x) + 0.5);
			x2 = _x + x;
			y2 = _y - y;
		
			lines.addElement(new CNCLine(x1,y1,x2,y2));
			x1 = x2;
			y1 = y2;
		}
		
		//lines.addElement(new CNCLine(_x,_y,_x+_width,_y));
		//lines.addElement(new CNCLine(_x+_width,_y,_x+_width,_y+_height));
		//lines.addElement(new CNCLine(_x+_width,_y+_height,_x,_y+_height));
		//lines.addElement(new CNCLine(_x,_y+_height,_x,_y));
		}
		
		} catch (Exception e)
		{
		System.out.println("XML Parsing error");
		}
		factory = null;
		parser = null;
		optimize();
		System.out.println("done.");
		
	}
}
//----------------------------------------------------------------------------

class CNCLine
{
public int x,y,z;
public int x1,y1,z1;
public  CNCLine(int _x, int _y, int _x1, int _y1)
{
  x = _x;
  y = _y;
  z = 0;
  x1 = _x1;
  y1 = _y1;
  z1 = 0;
}
public  CNCLine(int _x, int _y, int _z, int _x1, int _y1, int _z1)
{
  x = _x;
  y = _y;
  z = _z;
  x1 = _x1;
  y1 = _y1;
  z1 = _z1;
}
}
//----------------------------------------------------------------------------
