package HGT1;

public class Parameters {
	//2-25--not all aspects present???--Version 0.1
	//list all params here
	public int MAX_TIME = 100000;
	public double t_log_time = 100;
	public int initInf = 100;
	public int initInfZ = 100;
	
	public int N = 1000;
	// all  the rates are in days
	
	// rate of starting antibiotic treatment
	public double treat_start = 1.0 / 30.0;
	// rate of stopping antibiotic treatment
	public double treat_stop = 1.0 / 30.0;
	
	// transmission coefficient 
	public double betaW = .150 ;
	public double betaZ = .11;
	//pathogen recovery rate (untreated)
	public double gamma = 1.0/10.0;
	//pathogen recovery rate (treated sensitive pathogen)
	public double gamma_t = 1.0/2.0;
	
	
	//exog trans resistant exposure rate
	public double exog = 0.01;
	//clearance of transient resistants
	public double clearance = 1.0 / 7.0 ;
	//uptake of transient resistant commensals
	public double uptake = 1.0 / 10000;
	//rate of spontaneous loss of resistant minorities
	public double loss = 1.0 / 100.0;
	
	
	//rate of selection for resistant minorities
	public double selection_resist = 1.0 / 2.0;
	//rate of selection for sensitive minorities
	public double selection_sens = 1.0 / 100;
	
	//HGT's
	//hgt transient-to-pathogen
	public double HGT_t_p = 0.01;
	//hgt transient-to-commensal
	public double HGT_t_c = 0.01;
	//hgt commensal-to-pathogen
	public double HGT_c_p = 0.01;
	//hgt pathogen-to-commensal
	public double HGT_p_c = 0.01;
	//hgt antibiotic treatment modifier
	public double HGT_treat = 100.0;
	
	
	//molecular clocks
	//rate of mutation--pathogen
	public double mutate_pathogen =  1.0 / 100000;
	//rate of mutation--commensal
	public double mutate_commensal = 1.0 / 100000;
	

	//regular seeding parameters
	//rate of new pathogens introduced
	public double import_infected = 1.0 / 90.0;
	//rate of 
	
	public Parameters() {
		
	}
	
	public static Parameters load(String filename) {
		return new Parameters();
	}
	
}
