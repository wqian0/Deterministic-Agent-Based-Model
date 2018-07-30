import java.util.ArrayList;
import java.util.HashMap;

public class Vertice{
	private String ID;
	private ArrayList<ArrayList<Edge>>  edges;
	private HealthState state;
	private ArrayList<Double> contactsPerDay;
	private ArrayList<Integer> roundedContacts;
	private int[] remainingContacts;
	private int daysSinceExposed;
	private int daysSinceInfection;
	public double sortingPlaceholder;
	public int day;

	private double expectedSickTime;
	private double cumulatedProbability;
	private int infectiousPd;
	private int latentPd;
	private boolean vaccinated;
	private boolean recovered;
 //holding values in queue to be undone after 3 days
	private PNITracker[] trackers;  //The Q[u] property
	private PNITracker todayTracker; //The T[u] property
	ArrayList<Double> centralities; 
	
	public enum HealthState
	{
		susceptible,
		exposed,
		infected,
		resistant,
		vaccinated
	}
	public Vertice(String ID, int numWeekdays)
	{
		state = HealthState.susceptible;
		this.ID = ID;
		edges = new ArrayList<>();
		for(int i=0; i<numWeekdays; i++)
			edges.add(new ArrayList<Edge>());
		daysSinceInfection=-1;
		daysSinceExposed=-1;
		
		centralities = new ArrayList<>();
		
		recovered=false;
		vaccinated=false;
		
		cumulatedProbability=0;
		expectedSickTime=0;
		todayTracker = new PNITracker();
		contactsPerDay=new ArrayList<>();
		roundedContacts=new ArrayList<>();
	}
	public void reset()
	{
		state=HealthState.susceptible;
		recovered=false;
		cumulatedProbability=0;
		expectedSickTime=0;
		sortingPlaceholder=0;
		resetTrackerArray();
		resetRemainingContacts();
	}

	public String getID()
	{
		return ID;
	}
	public PNITracker[] getTracker()
	{
		return trackers;
	}
	public PNITracker getTodayTracker()
	{
		return todayTracker;
	}
	public void setID(String input)
	{
		ID = input;
	}

	public HealthState getState()
	{
		return state;
	}
	public void setState(HealthState h)
	{
		state = h;
	}
	public void setProperties(int latentPeriod, int infectiousPeriod)
	{
		infectiousPd=infectiousPeriod;
		latentPd=latentPeriod;
		trackers = new PNITracker[infectiousPd+latentPd-1];
		for(int i=0; i<trackers.length; i++)
			trackers[i]=new PNITracker();
	}
	public int getStartingPoint()
	{
		for(int i=0; i<edges.size(); i++)
		{
			if(edges.get(i).size()>0)
			{
				return i;
			}
		}
		return 0;
	}
	public boolean getVaccinationState()
	{
		return vaccinated;
	}
	public void setVaccinationState(boolean input)
	{
		vaccinated=input;
	}
	public ArrayList<Double> getContactsPerDay()
	{
		return contactsPerDay;
	}
	public void resetRemainingContacts()
	{
		for(int i=0; i<remainingContacts.length; i++)
		{
			remainingContacts[i]=roundedContacts.get(i);
		}
	}
	public void setRoundedContacts()
	{
		for(Double d: contactsPerDay)
			roundedContacts.add((int)Math.round(d));
		remainingContacts = new int[roundedContacts.size()];
		resetRemainingContacts();
	}
	public void addEdge(int day, Edge e)
	{
		edges.get(day).add(e);
	}
	public double getContactsRemaining(int weekday)
	{
		return remainingContacts[weekday];
	}
	public boolean hasContactsRemaining(int weekday)
	{
		return remainingContacts[weekday]>0;
	}
	public void setContactsRemaining(int day, int amount)
	{
		remainingContacts[day]=amount;
	}

	public ArrayList<Edge> getEdges(int day)
	{
		return edges.get(day);
	}
	public int daysSinceExposed()
	{
		return daysSinceExposed;
	}
	public void setDaysSinceExposed(int input)
	{
		daysSinceExposed=input;
	}

	public boolean contact(Vertice v, double tProbability, int weekday) //returns true if anyone gets "infected"(enters exposed state)
	{
		if(hasContactsRemaining(weekday))
		{
			remainingContacts[weekday]--;
		}
		else
			return false;
		
		if(state==HealthState.susceptible&&v.getState()==HealthState.infected&&Math.random()<tProbability)
		{
			state=HealthState.exposed;
			daysSinceExposed=0;
			return true;
		}
		return false;
	}
	public void checkRecovery()
	{
		if(state == HealthState.infected)
		{
			daysSinceInfection++;
			if(daysSinceInfection==infectiousPd)
			{
				state=HealthState.resistant;
				daysSinceInfection=-1;
			}
		}
		else if(state==HealthState.exposed)
		{
			daysSinceExposed++;
			if(daysSinceExposed==latentPd)
			{
				state=HealthState.infected;
				daysSinceInfection=0;
				daysSinceExposed=-1;
			}
		}
		resetRemainingContacts();
	}
	public boolean isConnected(int day, Vertice v) 
	{
		for(Edge e: edges.get(day))
		{
			if(e.getOther(this).getID()==v.getID())
				return true;
		}
		return false;
	}
	public double getEdgeWeight(int day, Vertice v) 
	{
		for(Edge e: edges.get(day))
		{
			if(e.getOther(this).getID()==v.getID())
				return e.getWeight();
		}
		return 0;
	}

	//For Prob Diffusion Simul
	public void setCumulation(Double d)
	{
		cumulatedProbability = d;
		for(PNITracker p: trackers)
			p.PNI=1-d;
	}
	public void setExpectedSickTime(double d)
	{
		expectedSickTime=d;
	}
	public void compoundCumulation(double d, Vertice source)
	{
		if(d==0)
			return;
		todayTracker.PNI*=(1-d);
		todayTracker.cL.add(new Contributor(source, 1-d));
	}
	
	public void resetTrackerArray()
	{
		for(int i=0; i<trackers.length; i++)
		{
			trackers[i].reset();
		}
		todayTracker.reset();
	}
	public double getCumulation()
	{
		return cumulatedProbability;
	}
	public boolean getRecoveryState()
	{
		return recovered;
	}
	public void setRecoveryState(boolean input)
	{
		recovered=input;
	}
	public void addNewProbability()
	{
		double t= 1.0;
		for(int i=trackers.length-1; i>=1; i--)
		{
			trackers[i].setValues(trackers[i-1]);
		}
		trackers[0]=todayTracker;
		
		for(int i=trackers.length-1; i>=latentPd-1; i--)
		{
			t*=trackers[i].PNI;
		}
		cumulatedProbability=1-t;
	}
	public void checkCumulationRecovery()
	{
		expectedSickTime+=cumulatedProbability;
		addNewProbability();
		if(expectedSickTime>=infectiousPd) 
		{
			recovered=true;
			cumulatedProbability=0;
		}
		todayTracker = new PNITracker();
	} 

}
