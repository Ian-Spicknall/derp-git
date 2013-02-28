package HGT1;

import java.util.Random;
import java.util.Date;

public class Simulation {
 //still to do:
	//file location
	//any model specific parameters to be set???
	//number of trials to be run.
	
	/**
	 * @param args
	 */
	
	public void run(MersenneTwisterFast random, int num_replicas, String[] args) {
		int i, j, z;
		Parameters par = new Parameters();
		//System.out.println(par.SIZE +" " + par.MAX_TIME);
		if (args != null){
			//if we want to initialzie parameters from the command line, we'll do that here.
			//par.air_dieout =				(Double.parseDouble(args[2]));
		}
		 //par.shedRad = Utils.buildShedNeigh(par.shed_neigh, par.rasterLength);
		
		Model m = null;		
		double [][] simpleStats = null;
		double[][] avg_stats = null;
		double[][] stats = null;
		String[][] header = null;
		String fileLocation =" ";//"/nobackup/ispickna/camra/data/"/////////////////////
/* "/afs/umich.edu/user/i/s/ispickna/Public/data/" */;
		
		//first arg is used to ID the output file
		String paramsText =   fileLocation +  (Integer.toString( Integer.parseInt(args[0])) + ".txt");
			
		//header for output file--need data column names
		String headerst = "treated untreated";
		

		
		for(i=0;i<num_replicas;i++) {
			
			m = new Model(par,random);
			m.runGillespie();
			//stats = m.getStats();
			simpleStats = m.simpleStats;
			header = m.header;
			//System.out.println("Ellapsed time: " + m.time_ellapsed + ", Num events: " + m.num_events);
									
			//if (i==0) avg_stats = new double[m.n_stat][m.s_stat];
		
		/*	for(j=0;j<m.n_stat;j++) {
				for(z=0;z<m.s_stat;z++) {
					avg_stats[j][z]+=stats[j][z];
				}					
			}*/
			if 	(i==0) {
				//double[][]newHeader = header[][];
				Utils.saveStringToFile (  header , paramsText    , false);
				Utils.saveToFile(simpleStats,paramsText,true);
			}
			else Utils.saveToFile(simpleStats,paramsText,true);	
		}
		
	/*	for(j=0;j<m.n_stat;j++) {
			for(z=0;z<m.s_stat;z++) {
				avg_stats[j][z]=avg_stats[j][z]/(double)num_replicas;
			}					
		}
		Utils.saveToFile(avg_stats,"results_avg.txt",false);
		*/
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Simulation sim = new Simulation();
		// for efficiency we use the MersenneTwisterFast instead of the java.util.Random
		MersenneTwisterFast r  = new MersenneTwisterFast(  System.currentTimeMillis());		
		int numRep = 1;
		if (args != null){
			numRep = Integer.parseInt(args[1]);
			//System.out.println(numRep);
		}
		//sim.run(r,1);		
		sim.run(r,numRep, args);
		

	}

}
