package HGT1;

/**
 * Single Venue Model 
 * Ian Spicknall
 * Last update: 1/31/09
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


	public static int SUSC = 0;
	public static int INF_sens = 1;
	public static int INF_minorResist = 2;
	public static int INF_majorResist = 3;
	public static int RECOV = 4;
	
	public static int UNTREATED = 0;
	public static int TREATED = 1;
	
	/**
	 * frequency of agents in each infection state {SUSC,INF_sens, INF_minorResist, INF_majorResist, RECOV}
	 */
	int[] vAGENTS = new int[5];

	/**
	 * frequency of agents in each Infected state (INF_sens, INF_minorResist, INF_majorResist,)
	 */
	int[] vAGENTS_I = new int[3];

	// frequency of agents in each treatment category
	int[] vTreatment = new int[2];
	/**
	 * array of event types. need final total number of events below
	 */
	double[] vEVENT = new double[4];

	int numTreatedSensInfection = 0;
	int numUntreatedSensInfection = 0;
	int numSensInfection = 0;
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
		simpleStats = new double[1][2];
		header = new String[1][2];

//initial conditions for the pathogen statuses
		vAGENTS[SUSC] = param.N - param.initInf;
		vAGENTS[INF_sens] = param.initInf;
		vAGENTS[INF_minorResist] = 0;
		vAGENTS[INF_majorResist] = 0;
		vAGENTS[RECOV] = 0;

		// all the infected agents start being sensitive to Ab.
		vAGENTS_I[0] = vAGENTS[INF_sens];

		for (i = 0; i < param.N; i++) {
			vpAG[i] = new Agent();
			// do the seeding first
			if (i < vAGENTS[INF_sens]) {
				vpAG[i].infection_state = INF_sens;
				numSensInfection++;
			} else {
				vpAG[i].infection_state = SUSC;
			}
			vpAG[i].initial_state = vpAG[i].infection_state;
			//initialize the initial treatment status for all agents
			if (random.nextDouble()< (param.treat_start/ (param.treat_start + param.treat_stop)))  {
				vpAG[i].treated = true;
				vTreatment[TREATED]++;
				if (vpAG[i].infection_state==INF_sens){
					numTreatedSensInfection++;
				}
			}
			else {
				vpAG[i].treated = false;
				vTreatment[UNTREATED]++;
				if (vpAG[i].infection_state==INF_sens){
					numUntreatedSensInfection++;
				}
			}
			//initialize the initial transient status for all agents
			vpAG[i].exposed = false;
		}	
	}
	/**
	 * determines the next event (by category number)
	 * 
	 * @return
	 */
	public int calculateNextEvent() {
		double tt = vEVENT[vEVENT.length - 1] * random.nextDouble();
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

	//some methods
	/**
	 * determine whether infection results dose recordkeeping related to
	 * positive/negative/redundant inoculation
	 */
	public void infection (int agent){	
		vpAG[agent].infection_state=INF_sens;
		vAGENTS[SUSC]--;
		vAGENTS[INF_sens]++;
		vAGENTS_I[0]++;	
		
	}
	
	


	/**
	 * executes each event (based on event category number)
	 * 
	 * @param event
	 * @param time
	 * @return
	 */
	public boolean actionEvent(int event, double time) {
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
			ag = random.nextInt(param.N);
			while (vpAG[ag].treated==true) { 
				ag = random.nextInt(param.N);
				}
			vpAG[ag].treated = true;
			vTreatment[TREATED]++;
			vTreatment[UNTREATED]--;
			if (vpAG[ag].infection_state==INF_sens || vpAG[ag].infection_state==INF_minorResist){
				numTreatedSensInfection++;
				numUntreatedSensInfection--;
			}
			//System.out.println("start treatment event");
			change=true;
			//System.out.println(numTreatedSensInfection);
			//System.out.println(numUntreatedSensInfection);
			//System.out.println(numSensInfection );
			break;
		case 1:
			ag = random.nextInt(param.N);
			while (vpAG[ag].treated==false) { 
				ag = random.nextInt(param.N);
				}
			vpAG[ag].treated = false;
			vTreatment[TREATED]--;
			vTreatment[UNTREATED]++;
			if (vpAG[ag].infection_state==INF_sens || vpAG[ag].infection_state==INF_minorResist){
				numTreatedSensInfection--;
				numUntreatedSensInfection++;
				
			}
			//System.out.println("stop treatment event");
			change=true;
			break;
		//recover from infection (treated sensitive strain)
		case 2:
			//System.out.println("recov (treated) event");
			ag = random.nextInt(param.N);
			while (vpAG[ag].treated==true || vpAG[ag].infection_state==SUSC || vpAG[ag].infection_state==INF_majorResist|| vpAG[ag].infection_state==RECOV ) {
				ag = random.nextInt(param.N);
				}
			vpAG[ag].infection_state=RECOV;
			numTreatedSensInfection--;
			numSensInfection--;
			change=true;
			break;		
			
		//recover from infection (untreated sensitive strain)
		case 3: 
			//System.out.println("recov (untreated) event");
			ag = random.nextInt(param.N);
			while (vpAG[ag].treated==false || vpAG[ag].infection_state==SUSC|| vpAG[ag].infection_state==INF_majorResist|| vpAG[ag].infection_state==RECOV) {
				ag = random.nextInt(param.N);
				}
			vpAG[ag].infection_state=RECOV;
			numUntreatedSensInfection--;
			numSensInfection--;
	
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
		simpleStats[0][0] = vTreatment[TREATED];
		simpleStats[0][1] = vTreatment[UNTREATED];
	
		header[0][0] = "treated";
		header[0][1] = "untreated";
		for (i = 0; i < this.s_stat; i++) {
		}
	}

	/**
	 *  calculate the propensity for all the events
	 */
	public double calculateEventPropensity() {

		vEVENT[0] =             vTreatment[UNTREATED] * param.treat_start;
		vEVENT[1] = vEVENT[0] + vTreatment[TREATED]   * param.treat_stop;
		vEVENT[2] = vEVENT[1] + numTreatedSensInfection   * param.gamma_t;
		vEVENT[3] = vEVENT[2] + numUntreatedSensInfection * param.gamma; 

		return vEVENT[1];
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
