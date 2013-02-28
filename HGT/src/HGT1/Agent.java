package HGT1;

public class Agent { 
//2-25--all aspects present???--Version 1.0
	//need to keep track of each host's unique pathogen and commensal ID

	//treatment status (T/F)
	public boolean treated ;
	//transiently exposed to resistant commensal (T/F)
	public boolean exposed;
	//initial infection state in the model
	public int initial_state;
	//infection state (susceptible, inf_sensitive, inf_minor_resist, inf_major_resist)
	public int infection_state ;
	
	//path ID--first digit is the introduction number; second digit is mutation number; 
	//third digit is resistance introduction number; fourth digit is resistance mutation number;
	public int[] pathID = new int[4];
	
	//commensal ID--first digit is the introduction number; second digit is mutation number; 
	//third digit is resistance introduction number; fourth digit is resistance mutation number;
	public int[] commID = new int[4];
	

	public Agent() {
		
	}
	
}
