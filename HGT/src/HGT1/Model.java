package HGT1;

/**
 * HGT model
 * Ian Spicknall
 * Last update: 3/01/13
 */
import java.util.Date;
import java.util.Random;
import java.util.Formatter;

public class Model {
	/**
	 * parameters of the model
	 */
	public Parameters param;

	/**
	 * MTF is an efficient random number generator
	 */
	public MersenneTwisterFast random;


	public static int S  = 1;
	public static int Iw = 2;
	public static int Iy = 3;
	public static int Iz = 4;
	public static int R  = 5;
	
	public static int U = 10;
	public static int T = 20;
	
	public static int EXP = 100;
	public static int UEX = 200;
	public static int TRA = 300;
	
	public static int W = 1000;
	public static int Y = 2000;
	public static int Z = 3000;
	
	
	public static int everIw = 11;
	public static int everIz = 12;
	
	
	/**
	 * frequency of agents in a number of states (see just above)
	 */
	int[] vAGENTS = new int[Z+TRA+T+R+1];

	/**
	 * array of event types. need final total number of events below
	 */
	double[] vEVENT = new double[20];


	/**
	 * array of agent (host) objects each host has all agent attributes and
	 * functionalites--see agent.java
	 */
	Agent[] vpAG = null;

	/**
	 * the infectious seed number initialized to -1 before it is who the seed is
	 */
	int theInfAgent = -1;

	/**
	 * remembers the TYPE of the last event
	 */
	int type_last_event;

	/**
	 * remembers the TIME of the last event
	 */
	double time_last_event = 0.0;

	/**
	 * mStats stores the model statistics for the current run
	 */
	double[][] mStats = null;

	/**
	 * another stats file for the current run
	 */
	double[][] simpleStats = null;

	/**
	 * header file has all the output stat names put at the top of each output
	 * file
	 */
	String[][] header = null;

	/**
	 * used in stat recordkeeping
	 */
	int last_stat = 0;

	/**
	 * used in stat recordkeeping
	 */
	int n_stat = 0;

	/**
	 * used in stat recordkeeping
	 */
	int s_stat = 0;

	/**
	 * stores the actual REAL time that elapsed during the simulation used for
	 * optimization
	 */
	long time_ellapsed;

	/**
	 * records the total number of events from a simulation
	 */
	int num_events = 0;

	// simple model statistics
	/**
	 * stat: number of all infections that result
	 */
	int numInf = 0;

	/**
	 * 
	 * @param param
	 * @param random
	 *            initializes all host agents starts stat recordkeeping, etc.
	 */
	public Model(Parameters param,
			MersenneTwisterFast random) {
		// initialize the model for a single run
		int i, j;

		this.param = param;

		this.random = random;

		vpAG = new Agent[param.N];

		n_stat = 3 + (int) (param.MAX_TIME / (double) param.t_log_time);
		s_stat = 12;

		//mStats = new double[n_stat][s_stat];
		simpleStats = new double[1][7];
		header = new String[1][7];

//initial conditions for the system states
		for (int ii=1;ii< vAGENTS.length;ii++){
			vAGENTS[ii]=0;
		}

		for (i = 0; i < param.N; i++) {
			vpAG[i] = new Agent();
			// do the seeding first
			if (i < param.initInf) {
				vpAG[i].infection_state = Iw;
				vAGENTS[everIw]++;
			} 
			else if (i < param.initInf+param.initInfZ) {
				vpAG[i].infection_state = Iz;
				vAGENTS[everIz]++;
			} 
			else if (i>= param.initInf+param.initInfZ){
				vpAG[i].infection_state = S;
			}

			vpAG[i].initial_state = vpAG[i].infection_state;
			//initialize the initial treatment status for all agents
			if (random.nextDouble()< (param.treat_start/ (param.treat_start + param.treat_stop)))  {
				vpAG[i].treated = T;
			}
			else {//UNTREATED
				//System.out.println("untreated init");
				vpAG[i].treated = U;
			}

			//initialize the initial transient status for all agents
			vpAG[i].exposed = UEX;
			vpAG[i].comm_state = Z;
			vpAG[i].system_state= vpAG[i].infection_state + vpAG[i].treated + vpAG[i].exposed+vpAG[i].comm_state;
			

			vAGENTS[  status(i)   ]++;
		}


	}
	/**
	 * determines the next event (by category number)
	 * 
	 * @return
	 */
	public int calculateNextEvent() {
		double tt = vEVENT[vEVENT.length - 1] * random.nextDouble();
		//System.out.println (tt);
		
		int pos = -1;
		for (int i = 0; i < vEVENT.length; i++) {
			// System.out.println (tt);
			// System.out.println (vAGENTS[LAB_I]);
			if (vEVENT[i] >= tt) {
				pos = i;
				break;
			}
		}
		return pos;
	}

public int status (int ag){
	int state = vpAG[ag].exposed + vpAG[ag].infection_state + vpAG[ag].treated +vpAG[ag].comm_state;
	return state;
}
	


	/**
	 * executes each event (based on event category number)
	 * 
	 * @param event
	 * @param time
	 * @return
	 */
	public boolean actionEvent(int event, double time) {
		//System.out.println(vAGENTS[S]);
		boolean change = false;
		double timeDelta = time - time_last_event;
		// LIST OF EVENTS
		//case 0 --start treatment
		//case 1 --stop treatment
		
		//case 2--recovery from infection
		//case 3-- recovery from infection while treated
		//default case--infection
		int ag;

		switch (event) {

		case 0: //start treatment--pick untreated agent, update agent and lists
			//System.out.println("start treatment");
			ag = random.nextInt(param.N);
			while (vpAG[ag].treated==T) { 	
				ag = random.nextInt(param.N);
				}
			
			vAGENTS[status(ag)]--;
			vpAG[ag].treated = T;
			vAGENTS[status(ag)]++;	
			
			change=true;
			break;
		case 1:
			//System.out.println("stop treatment");
			ag = random.nextInt(param.N);
			while (vpAG[ag].treated==U) { 
				ag = random.nextInt(param.N);
				}
			
			vAGENTS[status(ag)]--;
			vpAG[ag].treated = U;
			vAGENTS[status(ag)]++;	
			
			change=true;
			break;
		//transmission of sensitive pathogen
			//find any Susceptible
			//update infection and treatment status
		case 2:
			//System.out.println("transmission Iw");
			ag = random.nextInt(param.N);
			while (vpAG[ag].infection_state!=S) { //find an S
				ag = random.nextInt(param.N);
				}
			
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = Iw;
			vAGENTS[status(ag)]++;

			vAGENTS[everIw]++;
			
			change=true;
			break;	
		//transmission of resistant pathogen		
		case 3:
			//System.out.println("transmission Iz");
			ag = random.nextInt(param.N);
			while (vpAG[ag].infection_state!=S) { //find an S
				ag = random.nextInt(param.N);
				}
			//System.out.println(status(ag));
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = Iz;
			vAGENTS[status(ag)]++;
			//System.out.println(status(ag));
			vAGENTS[everIz]++;
			
			change=true;
			break;	
		//recover from infection (treated sensitive strain)
		case 4:
			//System.out.println("recover T_IwIy");
			int count = 0;
			ag = random.nextInt(param.N);
			while (vpAG[ag].treated==U || vpAG[ag].infection_state==S|| vpAG[ag].infection_state==Iz|| vpAG[ag].infection_state==R) {
				ag = random.nextInt(param.N);
				//System.out.println(count);
				//count++;
				}

			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = R;
			vAGENTS[status(ag)]++;
			
			
			change=true;
			break;		
			
		//recover from infection (untreated sensitive strain)
		case 5: 	
			//System.out.println("recover U_IwIy");
			ag = random.nextInt(param.N);
			while (vpAG[ag].treated==T || vpAG[ag].infection_state==S|| vpAG[ag].infection_state==Iz|| vpAG[ag].infection_state==R) {
				ag = random.nextInt(param.N);
				}
			
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = R;
			vAGENTS[status(ag)]++;
			
			change=true;
			break;
			
		//recover from resistant infection (treated and untreated)
		case 6: 
			//int count = 0;
			//System.out.println("recover Iz");
			ag = random.nextInt(param.N);
			while ( vpAG[ag].infection_state!=Iz) {
				//System.out.println(count);
				//count++;
				ag = random.nextInt(param.N);
				}
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = R;
			vAGENTS[status(ag)]++;
			
			change=true;
			break;
		case 7:
			//System.out.println("selection Iy->Iz");
			ag=random.nextInt(param.N);
			while (vpAG[ag].infection_state!=Iy && vpAG[ag].treated!=T){ //find a treated-Iy
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = Iz;
			vAGENTS[status(ag)]++;
			
			
			change=true;
			break;
		case 8:
			//System.out.println("selection Iz->Iy");
			ag=random.nextInt(param.N);
			while (vpAG[ag].infection_state!=Iz && vpAG[ag].treated!=U){ //find an untreated-Iz
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = Iy;
			vAGENTS[status(ag)]++;
			
			change=true;
			break;
		case 9:
			System.out.println("loss Iy->Iw");
			ag=random.nextInt(param.N);
			while (vpAG[ag].infection_state!=Iy && vpAG[ag].treated!=U){ //find an untreated-Iy
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = Iw;
			vAGENTS[status(ag)]++;
			
			change=true;
			break;			
		case 10:
			//System.out.println("ingestion unExp->Exp");
			ag=random.nextInt(param.N);
			while (vpAG[ag].exposed!= UEX){ //find an unexposed
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].exposed = EXP;
			vAGENTS[status(ag)]++;
			change=true;
			break;			
		case 11:
			//System.out.println("clearance Exp->unExp");
			ag=random.nextInt(param.N);
			while (vpAG[ag].exposed!= EXP){ //find an exposed
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].exposed = UEX;
			vAGENTS[status(ag)]++;
			change=true;
			break;	
			
		case 12:
			System.out.println("HGT Iw->Iy among exposed (transient to pathogen)");
			ag=random.nextInt(param.N);
			while (vpAG[ag].exposed!= EXP && vpAG[ag].infection_state!= Iw){ //find an exposed Iw
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = Iy;
			vAGENTS[status(ag)]++;
			change=true;
			break;				
		case 13:
			//System.out.println("selection (Y to Z)");
			ag=random.nextInt(param.N);
			while (vpAG[ag].treated!= T && vpAG[ag].comm_state!= Y){ //find a treated Y-commensal
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].comm_state = Z;
			vAGENTS[status(ag)]++;
			change=true;
			break;	
		case 14:
			//System.out.println("selection Z to Y");
			ag=random.nextInt(param.N);
			while (vpAG[ag].treated!= U && vpAG[ag].comm_state!= Z){ //find an untreated Z-commensal
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].comm_state = Y;
			vAGENTS[status(ag)]++;
			change=true;
			break;
		case 15:
			//System.out.println("loss Y to W");
			ag=random.nextInt(param.N);
			while (vpAG[ag].treated!= U && vpAG[ag].comm_state!= Y){ //find an untreated Z-commensal
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].comm_state = W;
			vAGENTS[status(ag)]++;
			change=true;
			break;			
		case 16:
			//System.out.println("HGT (transient to commensal  W->Y)");
			ag=random.nextInt(param.N);
			while (vpAG[ag].exposed!=EXP && vpAG[ag].comm_state!= W){ //find an exposed W-commensal
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].comm_state = Y;
			vAGENTS[status(ag)]++;
			change=true;
			break;
		case 17:
			System.out.println("HGT ( commensal  to pathogen )");
			ag=random.nextInt(param.N);
			while (vpAG[ag].infection_state!=Iw && vpAG[ag].comm_state!= Z){ //find a Iw host with Z-commensal
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].infection_state = Iy;
			vAGENTS[status(ag)]++;
			change=true;
			break;	
		case 18:
			System.out.println("HGT ( commensal  to pathogen )");
			ag=random.nextInt(param.N);
			while (vpAG[ag].infection_state!=Iz && vpAG[ag].comm_state!= W){ //find a Iz host with W-commensal
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].comm_state = Y;
			vAGENTS[status(ag)]++;
			change=true;
			break;	
		case 19:
			//System.out.println("Uptake (EXP to TRA");
			ag=random.nextInt(param.N);
			while (vpAG[ag].exposed!=EXP ){ //find a EXP host
				ag=random.nextInt(param.N);
			}
			vAGENTS[status(ag)]--;
			vpAG[ag].exposed = TRA;
			vAGENTS[status(ag)]++;
			change=true;
			break;	
			
			
					
		default:	
		}
		// calcAvgPathLvls(time);
		 time_last_event = time;
		// tot_Path_Env_Time += timeDelta;
		return change;
	}

/**
 * formats and writes to the output file.
 * also creates the header
 * @param time
 */
	public void logState(double time) {
		int i;
		simpleStats[0][0] = vAGENTS[T];
		simpleStats[0][1] = vAGENTS[U];
		simpleStats[0][2] = vAGENTS[S];
		simpleStats[0][3] = vAGENTS[everIw];
		simpleStats[0][4] = vAGENTS[Iy];
		simpleStats[0][5] = vAGENTS[everIz];
		simpleStats[0][6] = vAGENTS[R];
		header[0][0] = "treated";
		header[0][1] = "untreated";
		header[0][2] = "S";
		header[0][3] = "everIw";
		header[0][4] = "Iy";
		header[0][5] = "everIz";
		header[0][6] = "R";
		for (i = 0; i < this.s_stat; i++) {
		}
	}

	/**
	 *  calculate the propensity for all the events
	 */
	public double calculateEventPropensity() {
		int totTreated = 
				vAGENTS[T+S +UEX+W]+vAGENTS[T+S +EXP+W]+ 
				vAGENTS[T+Iw+UEX+W]+vAGENTS[T+Iw+EXP+W]+ 
				vAGENTS[T+Iy+UEX+W]+vAGENTS[T+Iy+EXP+W]+ 
				vAGENTS[T+Iz+UEX+W]+vAGENTS[T+Iz+EXP+W]+ 
				vAGENTS[T+R +UEX+W]+vAGENTS[T+R +EXP+W]+
				vAGENTS[T+S +UEX+Y]+vAGENTS[T+S +EXP+Y]+ 
				vAGENTS[T+Iw+UEX+Y]+vAGENTS[T+Iw+EXP+Y]+ 
				vAGENTS[T+Iy+UEX+Y]+vAGENTS[T+Iy+EXP+Y]+ 
				vAGENTS[T+Iz+UEX+Y]+vAGENTS[T+Iz+EXP+Y]+ 
				vAGENTS[T+R +UEX+Y]+vAGENTS[T+R +EXP+Y]+
				vAGENTS[T+S +UEX+Z]+vAGENTS[T+S +EXP+Z]+ 
				vAGENTS[T+Iw+UEX+Z]+vAGENTS[T+Iw+EXP+Z]+ 
				vAGENTS[T+Iy+UEX+Z]+vAGENTS[T+Iy+EXP+Z]+ 
				vAGENTS[T+Iz+UEX+Z]+vAGENTS[T+Iz+EXP+Z]+ 
				vAGENTS[T+R +UEX+Z]+vAGENTS[T+R +EXP+Z]+	
				vAGENTS[T+S +TRA+W] +
				vAGENTS[T+Iw+TRA+W]+
				vAGENTS[T+Iy+TRA+W]+
				vAGENTS[T+Iz+TRA+W]+ 
				vAGENTS[T+R +TRA+W]+
				vAGENTS[T+S +TRA+Y]+
				vAGENTS[T+Iw+TRA+Y]+
				vAGENTS[T+Iy+TRA+Y]+ 
				vAGENTS[T+Iz+TRA+Y]+ 
				vAGENTS[T+R +TRA+Y]+
				vAGENTS[T+S +TRA+Z]+
				vAGENTS[T+Iw+TRA+Z]+ 
				vAGENTS[T+Iy+TRA+Z]+
				vAGENTS[T+Iz+TRA+Z]+
				vAGENTS[T+R +TRA+Z];
		
		
		int totUnTreated = 
				vAGENTS[U+S +UEX+W]+vAGENTS[U+S +EXP+W]+ 
				vAGENTS[U+Iw+UEX+W]+vAGENTS[U+Iw+EXP+W]+ 
				vAGENTS[U+Iy+UEX+W]+vAGENTS[U+Iy+EXP+W]+ 
				vAGENTS[U+Iz+UEX+W]+vAGENTS[U+Iz+EXP+W]+ 
				vAGENTS[U+R +UEX+W]+vAGENTS[U+R +EXP+W]+						
				vAGENTS[U+S +UEX+Y]+vAGENTS[U+S +EXP+Y]+ 
				vAGENTS[U+Iw+UEX+Y]+vAGENTS[U+Iw+EXP+Y]+ 
				vAGENTS[U+Iy+UEX+Y]+vAGENTS[U+Iy+EXP+Y]+ 
				vAGENTS[U+Iz+UEX+Y]+vAGENTS[U+Iz+EXP+Y]+ 
				vAGENTS[U+R +UEX+Y]+vAGENTS[U+R +EXP+Y]+
				vAGENTS[U+S +UEX+Z]+vAGENTS[U+S +EXP+Z]+ 
				vAGENTS[U+Iw+UEX+Z]+vAGENTS[U+Iw+EXP+Z]+ 
				vAGENTS[U+Iy+UEX+Z]+vAGENTS[U+Iy+EXP+Z]+ 
				vAGENTS[U+Iz+UEX+Z]+vAGENTS[U+Iz+EXP+Z]+ 
				vAGENTS[U+R +UEX+Z]+vAGENTS[U+R +EXP+Z]+
				vAGENTS[U+S +TRA+W] +
				vAGENTS[U+Iw+TRA+W]+
				vAGENTS[U+Iy+TRA+W]+
				vAGENTS[U+Iz+TRA+W]+ 
				vAGENTS[U+R +TRA+W]+
				vAGENTS[U+S +TRA+Y]+
				vAGENTS[U+Iw+TRA+Y]+
				vAGENTS[U+Iy+TRA+Y]+ 
				vAGENTS[U+Iz+TRA+Y]+ 
				vAGENTS[U+R +TRA+Y]+
				vAGENTS[U+S +TRA+Z]+
				vAGENTS[U+Iw+TRA+Z]+ 
				vAGENTS[U+Iy+TRA+Z]+
				vAGENTS[U+Iz+TRA+Z]+
				vAGENTS[U+R +TRA+Z];
						
		int totUex = 				
				vAGENTS[UEX + U + W + S] +vAGENTS[UEX + U + W + Iw]+vAGENTS[UEX + U + W + Iy] +vAGENTS[UEX + U + W + Iz] +vAGENTS[UEX + U + W + R]+
				vAGENTS[UEX + U + Y + S] +vAGENTS[UEX + U + Y + Iw]+vAGENTS[UEX + U + Y + Iy] +vAGENTS[UEX + U + Y + Iz] +vAGENTS[UEX + U + Y + R]+
				vAGENTS[UEX + U + Z + S] +vAGENTS[UEX + U + Z + Iw]+vAGENTS[UEX + U + Z + Iy] +vAGENTS[UEX + U + Z + Iz] +vAGENTS[UEX + U + Z + R]+
				vAGENTS[UEX + T + W + S] +vAGENTS[UEX + T + W + Iw]+vAGENTS[UEX + T + W + Iy] +vAGENTS[UEX + T + W + Iz] +vAGENTS[UEX + T + W + R]+
				vAGENTS[UEX + T + Y + S] +vAGENTS[UEX + T + Y + Iw]+vAGENTS[UEX + T + Y + Iy] +vAGENTS[UEX + T + Y + Iz] +vAGENTS[UEX + T + Y + R]+
				vAGENTS[UEX + T + Z + S] +vAGENTS[UEX + T + Z + Iw]+vAGENTS[UEX + T + Z + Iy] +vAGENTS[UEX + T + Z + Iz] +vAGENTS[UEX + T + Z + R];	
				
		int totExp = 
				vAGENTS[EXP + U + W + S] +vAGENTS[EXP + U + W + Iw]+vAGENTS[EXP + U + W + Iy] +vAGENTS[EXP + U + W + Iz] +vAGENTS[EXP + U + W + R]+
				vAGENTS[EXP + U + Y + S] +vAGENTS[EXP + U + Y + Iw]+vAGENTS[EXP + U + Y + Iy] +vAGENTS[EXP + U + Y + Iz] +vAGENTS[EXP + U + Y + R]+
				vAGENTS[EXP + U + Z + S] +vAGENTS[EXP + U + Z + Iw]+vAGENTS[EXP + U + Z + Iy] +vAGENTS[EXP + U + Z + Iz] +vAGENTS[EXP + U + Z + R]+
				vAGENTS[EXP + T + W + S] +vAGENTS[EXP + T + W + Iw]+vAGENTS[EXP + T + W + Iy] +vAGENTS[EXP + T + W + Iz] +vAGENTS[EXP + T + W + R]+
				vAGENTS[EXP + T + Y + S] +vAGENTS[EXP + T + Y + Iw]+vAGENTS[EXP + T + Y + Iy] +vAGENTS[EXP + T + Y + Iz] +vAGENTS[EXP + T + Y + R]+
				vAGENTS[EXP + T + Z + S] +vAGENTS[EXP + T + Z + Iw]+vAGENTS[EXP + T + Z + Iy] +vAGENTS[EXP + T + Z + Iz] +vAGENTS[EXP + T + Z + R];	
				
		int treatedIw = 
				vAGENTS[T+Iw+UEX+W]+vAGENTS[T+Iw+EXP+W]+vAGENTS[T+Iw+UEX+Y]+vAGENTS[T+Iw+EXP+Y]+vAGENTS[T+Iw+UEX+Z]+vAGENTS[T+Iw+EXP+Z]
						+vAGENTS[T+Iw+TRA+W]+vAGENTS[T+Iw+TRA+Y]+vAGENTS[T+Iw+TRA+Z];
		int untreatedIw = 
				vAGENTS[U+Iw+UEX+W]+vAGENTS[U+Iw+EXP+W]+vAGENTS[U+Iw+UEX+Y]+vAGENTS[U+Iw+EXP+Y]+vAGENTS[U+Iw+UEX+Z]+vAGENTS[U+Iw+EXP+Z]
						+vAGENTS[U+Iw+TRA+W]+vAGENTS[U+Iw+TRA+Y]+vAGENTS[U+Iw+TRA+Z];
		int treatedIy = 
				vAGENTS[T+Iy+UEX+W]+vAGENTS[T+Iy+EXP+W]+vAGENTS[T+Iy+UEX+Y]+vAGENTS[T+Iy+EXP+Y]+vAGENTS[T+Iy+UEX+Z]+vAGENTS[T+Iy+EXP+Z]
						+vAGENTS[T+Iy+TRA+W]+vAGENTS[T+Iy+TRA+Y]+vAGENTS[T+Iy+TRA+Z];
		int untreatedIy =
				vAGENTS[U+Iy+UEX+W]+vAGENTS[U+Iy+EXP+W]+vAGENTS[U+Iy+UEX+Y]+vAGENTS[U+Iy+EXP+Y]+vAGENTS[U+Iy+UEX+Z]+vAGENTS[U+Iy+EXP+Z]
						+vAGENTS[U+Iy+TRA+W]+vAGENTS[U+Iy+TRA+Y]+vAGENTS[U+Iy+TRA+Z];
		int treatedIz = 
				vAGENTS[T+Iz+UEX+W]+vAGENTS[T+Iz+EXP+W]+vAGENTS[T+Iz+UEX+Y]+vAGENTS[T+Iz+EXP+Y]+vAGENTS[T+Iz+UEX+Z]+vAGENTS[T+Iz+EXP+Z]
						+vAGENTS[T+Iz+TRA+W]+vAGENTS[T+Iz+TRA+Y]+vAGENTS[T+Iz+TRA+Z];
		int untreatedIz =
				vAGENTS[U+Iz+UEX+W]+vAGENTS[U+Iz+EXP+W]+vAGENTS[U+Iz+UEX+Y]+vAGENTS[U+Iz+EXP+Y]+vAGENTS[U+Iz+UEX+Z]+vAGENTS[U+Iz+EXP+Z]
						+vAGENTS[U+Iz+TRA+W]+vAGENTS[U+Iz+TRA+Y]+vAGENTS[U+Iz+TRA+Z];
		int totalS = 
				vAGENTS[T+S +UEX+W]+vAGENTS[T+S +EXP+W]+vAGENTS[T+S +TRA+W]+ vAGENTS[U+S +UEX+W]+vAGENTS[U+S +EXP+W]+vAGENTS[U+S +TRA+W]+
				vAGENTS[T+S +UEX+Y]+vAGENTS[T+S +EXP+Y]+vAGENTS[T+S +TRA+Y]+ vAGENTS[U+S +UEX+Y]+vAGENTS[U+S +EXP+Y]+vAGENTS[U+S +TRA+Y]+
				vAGENTS[T+S +UEX+Z]+vAGENTS[T+S +EXP+Z]+vAGENTS[T+S +TRA+Z]+ vAGENTS[U+S +UEX+Z]+vAGENTS[U+S +EXP+Z]+vAGENTS[U+S +TRA+Z]	;
		int treatedY = 
				vAGENTS[Y+T +S+UEX] +vAGENTS[Y+T +Iw+UEX]+vAGENTS[Y+T +Iy+UEX] +vAGENTS[Y+T +Iz+UEX]  +vAGENTS[Y+T +R+UEX]+
				vAGENTS[Y+T +S+EXP] +vAGENTS[Y+T +Iw+EXP]+vAGENTS[Y+T +Iy+EXP] +vAGENTS[Y+T +Iz+EXP]  +vAGENTS[Y+T +R+EXP]+
				vAGENTS[Y+T +S+TRA] +vAGENTS[Y+T +Iw+TRA]+vAGENTS[Y+T +Iy+TRA] +vAGENTS[Y+T +Iz+TRA]  +vAGENTS[Y+T +R+TRA];
		int untreatedY = 
				vAGENTS[Y+U +S+UEX] +vAGENTS[Y+U +Iw+UEX]+vAGENTS[Y+U +Iy+UEX] +vAGENTS[Y+U +Iz+UEX]  +vAGENTS[Y+U +R+UEX]+
				vAGENTS[Y+U +S+EXP] +vAGENTS[Y+U +Iw+EXP]+vAGENTS[Y+U +Iy+EXP] +vAGENTS[Y+U +Iz+EXP]  +vAGENTS[Y+U +R+EXP]+
				vAGENTS[Y+U +S+TRA] +vAGENTS[Y+U +Iw+TRA]+vAGENTS[Y+U +Iy+TRA] +vAGENTS[Y+U +Iz+TRA]  +vAGENTS[Y+U +R+TRA];
		int untreatedZ = 
				vAGENTS[Z+U +S+UEX] +vAGENTS[Z+U +Iw+UEX]+vAGENTS[Z+U +Iy+UEX] +vAGENTS[Z+U +Iz+UEX]  +vAGENTS[Z+U +R+UEX]+
				vAGENTS[Z+U +S+EXP] +vAGENTS[Z+U +Iw+EXP]+vAGENTS[Z+U +Iy+EXP] +vAGENTS[Z+U +Iz+EXP]  +vAGENTS[Z+U +R+EXP]+
				vAGENTS[Z+U +S+TRA] +vAGENTS[Z+U +Iw+TRA]+vAGENTS[Z+U +Iy+TRA] +vAGENTS[Z+U +Iz+TRA]  +vAGENTS[Z+U +R+TRA];
		
		int traIy =vAGENTS[TRA+Iw+U+W]+vAGENTS[TRA+Iw+T+W] +vAGENTS[TRA+Iw+U+Y]+vAGENTS[TRA+Iw+T+Y]+vAGENTS[TRA+Iw+U+Z]+vAGENTS[TRA+Iw+T+Z];
		
		int traW =vAGENTS[W+TRA+S+U]+vAGENTS[W+TRA+Iw+U]+vAGENTS[W+TRA+Iy+U]+vAGENTS[W+TRA+Iz+U]+vAGENTS[W+TRA+R+U]+
				  vAGENTS[W+TRA+S+T]+vAGENTS[W+TRA+Iw+T]+vAGENTS[W+TRA+Iy+T]+vAGENTS[W+TRA+Iz+T]+vAGENTS[W+TRA+R+T];
		
		int Iw_Z = vAGENTS[Iw+Z+EXP+U]+ vAGENTS[Iw+Z+UEX+U] +vAGENTS[Iw+Z+EXP+T]+ vAGENTS[Iw+Z+UEX+T] +vAGENTS[Iw+Z+TRA+U] + vAGENTS[Iw+Z+TRA+T];
					
		int Iz_W = vAGENTS[Iz+W+EXP+U]+ vAGENTS[Iz+W+UEX+U] +vAGENTS[Iz+W+EXP+T]+ vAGENTS[Iz+W+UEX+T] +vAGENTS[Iz+W+TRA+U] + vAGENTS[Iz+W+TRA+T];
				

				
		vEVENT[0]  =              totUnTreated * param.treat_start; //start treatment
		vEVENT[1]  = vEVENT[0]  + totTreated             * param.treat_stop;  //stop treatment
		vEVENT[2]  = vEVENT[1]  + totalS*(treatedIw +untreatedIw +treatedIy +untreatedIy)*param.betaW/param.N ; //transmission sensitive strain
		vEVENT[3]  = vEVENT[2]  + totalS*(treatedIz +untreatedIz)  *param.betaZ/param.N ;//transmission resistant strain
		vEVENT[4]  = vEVENT[3]  + (treatedIw+treatedIy)* param.gamma_t; //recovery (untreated sensitive infection)
		vEVENT[5]  = vEVENT[4]  + (  untreatedIw+  untreatedIy)* param.gamma;  //recovery (treated sensitive infection)
		vEVENT[6]  = vEVENT[5]  + (untreatedIz+treatedIz)* param.gamma; // recovery resistant infection
		vEVENT[7]  = vEVENT[6]  +   treatedIy* param.selection_resist; // selection for resistant minorities
		vEVENT[8]  = vEVENT[7]  + untreatedIz* param.selection_sens; // selection for sensitive minorites
		vEVENT[9]  = vEVENT[8]  + untreatedIy* param.loss; // loss of resistant minorities
		vEVENT[10] = vEVENT[9]  + totUex* param.exog; // exposure (among unexposed)
		vEVENT[11] = vEVENT[10] + totExp* param.clearance; // clearance (among exposed) 
		vEVENT[12] = vEVENT[11] + traIy* param.HGT_t_p; // HGT (t_p)
		vEVENT[13] = vEVENT[12] + treatedY  *param.selection_resist; // selection (Y to Z among treated)
		vEVENT[14] = vEVENT[13] + untreatedZ*param.selection_sens; // selection (Z to Y among untreated) 		
		vEVENT[15] = vEVENT[14] + untreatedY*param.loss; // selection (Z to Y among untreated) 						
		vEVENT[16] = vEVENT[15] + traW*param.HGT_t_c; // HGT (transient to comm) 	
		vEVENT[17] = vEVENT[16] + Iw_Z*param.HGT_c_p; // HGT (comm to path) 	
		vEVENT[18] = vEVENT[17] + Iz_W*param.HGT_p_c; // HGT (path to comm) 
		vEVENT[19] = vEVENT[18] + totExp*param.uptake; //Uptake (transient exp to transient commensal (EXP to TRA))
		//System.out.println(vEVENT[0]);
		//System.out.println(vEVENT[1]);
		return vEVENT[18];
	}

	public double[][] getStats() {
		return mStats;
	}

	public void checkConsistency() {
		int i;
		int[] cont = { 0, 0, 0 };
		int[] state = { 0, 0, 0 };
		int[] cells = { 0, 0, 0 };
		for (i = 0; i < param.N; i++) {
			state[vpAG[i].infection_state]++;
		}
			if (vAGENTS[i] != state[i]) {
				System.err
						.println("The state of the agents is not consistent: "
								+ i);
				System.err.println("Type last event: " + type_last_event);
				System.exit(-1);
			}
	}

	public void calculateStatistics() {
		int i;
		// statistics of the infection
		int initialI = param.initInf;
	}
/**
 * calc tot event rate.
 * determine event.
 * execute event.
 * repeat till done.
 * 
 *do stats.
 */
	public void runGillespie() {

		Date time_start = new Date();
		boolean done = false;
		boolean update_events = true;
		int num_event;
		double t = 0.0;
		double t_to_log = 0.0;
		double time_event;
		double total_rate = 0.0;
		double t_tot_log;
		double cont;
		int i;
		num_events = 0;
		// run the simulation until the time is ellapsed or the
		// simulation is done

		// we need to print the initial state to file
		logState(0.0);
		t_to_log = param.t_log_time;

		while ((t < param.MAX_TIME) && (!done)) {
			// only calculate propensity if update_events is true
			if (update_events)
				total_rate = calculateEventPropensity();

			time_event = -Math.log(random.nextDouble()) / total_rate;
			num_event = calculateNextEvent();
			type_last_event = num_event;

			t += time_event;
			// checkConsistency();
			update_events = actionEvent(num_event, t);
			// checkConsistency();

			num_events++;
		/*	if (t >= t_to_log) {
				// System.out.println("num_events: " + num_events);
				calculateStatistics();
				
				logState(t);
				t_to_log = (double) ((int) t + param.t_log_time);

				// System.out.println("T: " + t + "SIR: " + vAGENTS[LAB_S] + " "
				// + vAGENTS[LAB_I] + " " + vAGENTS[LAB_R]);
				// check for consistency, remove once the model has been
				// debugged
				// checkConsistency();
			}*/
			/*if (vAGENTS[LAB_I] == 0) {
				// the epidemic is complete, wait until the contamination also
				// disapears
				cont = vPATHOGENS[LAB_FOM] + vPATHOGENS[LAB_AIR]
						+ vPATHOGENS[LAB_HAND] + vPATHOGENS[LAB_INSP];
				if (cont < 0.1) {
					done = true;
				} else if (cont < 0) {
					System.err.println("Total contamination is negative");
					System.exit(-1);
				}

			}*/
		}
		// fill the stats
		for (i = last_stat; i < 1 /*n_stat*/; i++) {
			t = (double) ((int) t /*+ param.t_log_time*/);
			//System.out.println("endTime = " + t);
			//System.out.println("i = " + i);
			logState(t);
			//System.out.println("numAir="+ numAirInf+ "   numFomInf="+numFomInf+"   numInspAInf="+ numINSPaInf);
			// NumSystem.out.println("T: " + t + "SIR: " + vAGENTS[LAB_S] + " "
			// + vAGENTS[LAB_I] + " " + vAGENTS[LAB_R]);
		}

		time_ellapsed = (new Date()).getTime() - time_start.getTime();
		//System.out.println(totInspPath);
	 }

}
