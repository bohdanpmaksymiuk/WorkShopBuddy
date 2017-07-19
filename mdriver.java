 import java.io.*;
public class mdriver
{
	static int absx = 0;
	static int absy = 0;
	static int absz = 0;
	
    	public static void main (String[] args)
   	{
		int a=0;

		setCoordinates(0,0,0);
		//moveAbsolute(new Integer(args[0]),new Integer(args[1]),new Integer(args[2]));
		while (a != 113)
		{
			try {
				
				a = System.in.read();

			}
			catch  (Exception e)	{	}
			if (a == 65) 	{	moveRelative(0,1,0);  }
			if (a == 66) 	{	moveRelative(0,-1,0); }
			if (a == 68) 	{	moveRelative(-1,0,0); }
			if (a == 67) 	{	moveRelative(1,0,0);  }
			if (a == 53) 	{	moveRelative(0,0,1);  }
			if (a == 54) 	{	moveRelative(0,0,-1); }
		
			if (a == 56) 	{	moveRelative(16,0,-5);
						moveRelative(-16,0,5); 
					}
			if (a == 57) 	{	moveRelative(5,0,0);
						moveRelative(0,10,0);
						moveRelative(5,0,0);
						moveRelative(0,-10,0);
						 
					}			
			//System.out.println(a);	
			
		}
   	}
	public static void setCoordinates(int x,int y, int z)
	{
		absx=x;
		absy=y;
		absz=z;
	}
	public static void moveRelative(int x,int y, int z)
	{
		
		Move(x,y,-z);
		absx=absx+x;
		absy=absy+y;
		absz=absz+z;
		//System.out.println("x=" + absx + " y=" + absy + " z=" + absz);
	}
	public static void moveAbsolute(int x,int y, int z)
	{
		moveRelative(x-absx,y-absy,z-absz);
	}
	
    	static private native void Movedmm(int x,int y,int z);
	static private native void Move(int x,int y,int z);
	static { System.load(System.getProperty("user.dir")+"/libmdriver.so");}
    }

