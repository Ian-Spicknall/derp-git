package HGT1;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Utils {

	public static void saveToFile(double[][] m, String filename, boolean append) {
		int j, z;
		String s;
		try { 
	        BufferedWriter out = new BufferedWriter(new FileWriter(filename, append));
	        
	        for(j=0;j<m.length;j++) {
	        	for(z=0;z<m[j].length;z++) {
	        		s = m[j][z]+ " ";
	        		out.write(s);
	        	}
	        	out.write("\n");
	        }
	        out.close();	        
	    } catch (Exception e) {
	    	System.err.println("Error writing in file");
	    	e.printStackTrace();
	    	System.exit(-1);
	    }
		
	}
	public static void saveStringToFile(String[][] m, String filename, boolean append) {
		int j, z;
		String s;
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(filename, append));
	        
	        for(j=0;j<m.length;j++) {
	        	for(z=0;z<m[j].length;z++) {
	        		s = m[j][z]+ " ";
	        		out.write(s);
	        	}
	        	out.write("\n");
	        }
	        out.close();	        
	    } catch (Exception e) {
	    	System.err.println("Error writing in file");
	    	e.printStackTrace();
	    	System.exit(-1);
	    }
		
	}
	
	public static int[][] buildNeigh(int size) {
		int[] g = null;
		int i, x, y;
		int[][] res = new int[size][4];
		int L = (int)Math.sqrt(size);
		if (L*L!=size) {
			// the size is not a square
			System.err.println("Size is not square, invalid size");
			System.exit(-1);	
		}
		else {
			for(i=0;i<size;i++) {
				g = pos2grid(i,L);
				x = g[0]-1;
				y = g[1]-1;
				if ((x<0) || (y<0)) res[i][0] = i;
				
				else res[i][0] = Utils.grid2pos(x,y,L);
				x = g[0]-1;
				y = g[1]+1;
				if ((x<0) || (y>L-1)) res[i][1] = i;
				else res[i][1] = Utils.grid2pos(x,y,L);
				x = g[0]+1;
				y = g[1]-1;
				if ((x>L-1) || (y<0)) res[i][2] = i;
				else res[i][2] = Utils.grid2pos(x,y,L);
				x = g[0]+1;
				y = g[1]+1;
				if ((x>L-1) || (y>L-1)) res[i][3] = i;
				else res[i][3] = Utils.grid2pos(x,y,L);
			}								
		}
				
		return res;
		
	}
	
	public static int[] pos2grid(int pos, int L) {		
		int[] res = new int[2];		
		res[0] = (int)pos/L;
		res[1] = pos % L;		
		return res;
	}
	
	public static int grid2pos(int x, int y, int L) {
		return (x*L)+y;		
	}
	
	public static int buildShedNeigh(int shed, int size) {
		//1) determine how many loci to go out
		//2) based on current position return all neighborhood spots to contaminate--done in another method
		int radius = 0;
		if (size / shed <1){
			radius = 1;
		while(  ((radius * size))/ ((shed)) < 1){
			radius +=2;
		}
		radius -=1;
		radius /=2;
		}
		System.out.println(radius); 
		return radius;
		
	}
	
	
	public static double buildShedProb(int radius, int shed, int size){
		double p;
		if (radius >0){
		p =(Math.pow(shed, 2)) / (Math.pow(radius * size, 2) ) ;
		
		}
		else {
			p = 1;
		}
		System.out.println(p);
		return p;
	}
	
	
	
	
	
	
}
