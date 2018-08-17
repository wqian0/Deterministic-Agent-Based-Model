import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class StaticSimulation {
	private double tProbability; //baseline; is modified by weighting
	private ArrayList<Vertice> vertices;
	private Graph G;
	private int day;

	private ArrayList<Vertice> temp;
	private HashMap<Vertice,HashMap<Vertice, Double>> weightRanks;

	private double totalEverInfected;
	private double previousTotal; 
	private double currentTotal;
	private double currentInfected;
	private double peakInfected;
	private int peakDayInfected;

	private int latentPd;
	private int infectiousPd;

	ArrayList<double[]> cumulativeData;
	ArrayList<ArrayList<Double>> individualData; //collects data on the probability of infection of individual vertices over time

	public StaticSimulation(Graph G, double tProbability, int latentPd, int infectiousPd)
	{
		this.G=G;
		this.tProbability=tProbability;
		this.latentPd=latentPd;
		this.infectiousPd=infectiousPd;

		day=0;

		vertices=G.getVertices();
		previousTotal=0;
		currentTotal=0;
		totalEverInfected=0;
		currentInfected=0;
		peakInfected=0;
		peakDayInfected=0;
		weightRanks=new HashMap<>();
		setWeightRanks();
		cumulativeData = new ArrayList<>();
		individualData=new ArrayList<>();

		for(Vertice v: vertices)
		{
			v.setProperties(latentPd, infectiousPd);
			individualData.add(new ArrayList<>());
		}
	}

	public Graph getGraph()
	{
		return G;
	}
	public ArrayList<double[]> getData()
	{
		return cumulativeData;
	}

	public void reset(boolean affectVaccinated)
	{
		if(affectVaccinated)
		{
			for(Vertice v: vertices)
			{
				v.reset();
			}
		}
		else
		{
			for(Vertice v: vertices)
			{
				if(!v.getVaccinationState())
					v.reset();
			}
		}
		day=0;
		currentTotal=0;
		previousTotal=0;
		totalEverInfected=0;
		currentInfected=0;
		peakInfected=0;
		peakDayInfected=0;
		cumulativeData = new ArrayList<>();
		individualData=new ArrayList<>();
		for(Vertice v: vertices)
		{
			individualData.add(new ArrayList<>());
		}
	}
	public void setInfected(ArrayList<Vertice> input)
	{
		for(Vertice v: input)
		{
			v.setState(Vertice.HealthState.infected);
		}
	}
	public void setInfected(Vertice input)
	{
		input.setState(Vertice.HealthState.infected);
	}

	public int getDay()
	{
		return day;
	}
	public int getNumSusceptible()
	{
		int count=0;
		for(Vertice v: vertices)
			if(v.getState()==Vertice.HealthState.susceptible)
				count++;
		return count;
	}
	public int getNumExposed()
	{
		int count=0;
		for(Vertice v: vertices)
			if(v.getState()==Vertice.HealthState.exposed)
				count++;
		return count;
	}
	public int getNumInfected()
	{
		int count=0;
		for(Vertice v: vertices)
			if(v.getState()==Vertice.HealthState.infected)
				count++;
		return count;
	}
	
	public double getPeakInfected()
	{
		return peakInfected;
	}
	public int getPeakDayInfected()
	{
		return peakDayInfected;
	}
	public boolean hasRemaining()
	{
		for(Vertice v: vertices)
			if(v.getState()==Vertice.HealthState.infected||v.getState()==Vertice.HealthState.exposed)
				return true;
		return false;
	}

	public int getNumResistant()
	{
		int count=0;
		for(Vertice v: vertices)
			if(v.getState()==Vertice.HealthState.resistant)
				count++;
		return count;
	}
	public boolean hasAdjContact(Vertice input)
	{
		for(Edge e: input.getEdges(0))
			if(e.getOther(input).getContactsRemaining(0)>0)
				return true;	
		return false;
	}
	
	public Vertice generateRandContact(Vertice v)
	{
		if(!v.hasContactsRemaining(0))
			return null;
		ArrayList<Vertice> result = new ArrayList<>();
		double runningTotal=0;
		Vertice other;
		for(Edge e: v.getEdges(0))
		{
			other = e.getOther(v);
			if(other.hasContactsRemaining(0))
			{
				runningTotal+=e.getWeight();
				result.add(other);
				other.searchPlaceholder=runningTotal;
			}
		}
		double random = Main.RNG.nextDouble()*runningTotal;
		int startIndex=0;
		int endIndex=result.size()-1;
		int currentIndex=0;
		if(result.size()==0)
			return null;

		//Binary searching for a range in which random falls. Each range corresponds to a vertex to contact, and are sized based on contact probability. 
		if(result.get(0).searchPlaceholder>random)
			return result.get(0);
		while(endIndex-startIndex!=1)
		{
			currentIndex=(startIndex+endIndex)/2;
			if(result.get(currentIndex).searchPlaceholder>=random)
				endIndex=currentIndex;
			else
				startIndex=currentIndex;
		}
		return result.get(endIndex);
	}
	public void show()
	{
		System.out.println(day+"\t"+getNumSusceptible()+"\t"+getNumExposed()+"\t"+ getNumInfected()+"\t"+getNumResistant());
	}

	//A single timestep (day) in the classic stochastic version of agent-based SEIR.
	public void runDay() {
		
		Vertice current;
		Vertice other;
		temp = new ArrayList<>(vertices);
		while (temp.size()>0) {
			current = temp.get((int)(Main.RNG.nextDouble()*temp.size()));
			other = generateRandContact(current);
			if (other==null) {
				temp.remove(current);
			} else {
				current.contact(other, tProbability, 0);
				other.contact(current, tProbability, 0);
				if (!current.hasContactsRemaining(0))
					temp.remove(current);
				if (!other.hasContactsRemaining(0))
					temp.remove(other);
			}
		}
		day++;
		for (Vertice v : vertices) {
			v.checkRecovery();
		}
	}
	
	public void simul()
	{
	//	System.out.println("day \t S \t E \t I \t R");
		while(hasRemaining())
		{	
	//		show();
			cumulativeData.add(new double[] {getNumSusceptible(), getNumExposed(), getNumInfected(), getNumResistant()});
			runDay();
			if(cumulativeData.get(day-1)[2]>peakInfected)
			{
				peakInfected=cumulativeData.get(day-1)[2];
				peakDayInfected=day-1;
			}
		}
//	show();
		cumulativeData.add(new double[] {getNumSusceptible(), getNumExposed(), getNumInfected(), getNumResistant()});
	}

	//	Functions for the deterministic version below:


	public void setTricklers(ArrayList<Vertice> list)
	{
		for(Vertice v:list)
		{
			v.setCumulation(1.0);		
			v.getTracker()[latentPd-1].PNI=0;
			v.setProbNotRecovered(1);
		}
	}	
	public void setTrickler(Vertice v)
	{
		v.setCumulation(1.0);		
		v.getTracker()[latentPd-1].PNI=0;
		v.setProbInfectedFromContacts(1);
		v.setProbNotRecovered(1);
	}

	//pre-calculates tProb*contactProb for each directed pair of vertices
	public HashMap<Vertice, Double> getWeightRanks(Vertice v) 
	{
		HashMap<Vertice,Double> returnList = new HashMap<>();
		double current=0;
		double total=0;
		for(Edge e: v.getEdges(0))
		{
			current = e.getWeight();
			total+=current;
			returnList.put(e.getOther(v),current);
		}
		for(Vertice x: returnList.keySet())
		{
			returnList.put(x,tProbability*returnList.get(x)/total);
		}
		return returnList;
	}
	public void setWeightRanks()
	{
		for(Vertice v:vertices)
		{
			weightRanks.put(v, getWeightRanks(v));
		}
	}
	public void runTrickleDay()
	{
		HashMap<Vertice, Double> tempMap;
		double altProduct=0;
		double backflowProduct=1;

		for(Vertice v:vertices)
		{
			if(v.getCumulation()>0)
			{
				tempMap = weightRanks.get(v);
				for(Vertice x: tempMap.keySet())
				{	
					altProduct=v.getCumulation();
					//comment out this bracket to skip backflow correction. Has little effect on outbreak dynamics and is around 5 times slower when not skipped.
				
				{	
					altProduct=v.getProbInfectedFromContacts();
					for(int i=latentPd-1; i<v.getTracker().length; i++)
					{
						altProduct=1-(1-altProduct)/v.getTracker()[i].getPNI(x);
					}
					
					//floating point precision error correction
					if(altProduct<0)
						altProduct=0;
					
					altProduct*=v.getProbNotRecovered();
				}
				
					x.compoundCumulation(1-Math.pow(1-altProduct*tempMap.get(x),v.getContactsPerDay().get(0)),v);
				}
			}
		}
		day++;
		for(Vertice v: vertices)
		{
			v.checkCumulationRecovery(); 
		}
	}
	public void trickleSimul()
	{
		currentInfected = expectedNumInfected();
		currentTotal=currentInfected;
		while(Math.abs(currentTotal-previousTotal)>.5||currentTotal>0.5||day<20)
		{
			previousTotal=currentTotal;
			showTrickle();
			cumulativeData.add(new double[] {numSusceptible(), expectedNumExposed(), expectedNumInfected(), numRecovered()});
			addIndividualInfected();
			runTrickleDay();
			currentInfected=expectedNumInfected();
			if(currentInfected>peakInfected)
			{
				peakInfected=currentInfected;
				peakDayInfected=day;
			}
			currentTotal=currentInfected+expectedNumExposed();
			totalEverInfected+=currentInfected;
		}
		showTrickle();
		cumulativeData.add(new double[] {numSusceptible(), expectedNumExposed(), expectedNumInfected(), numRecovered()});
		addIndividualInfected();
	}
	public void showTrickle()
	{
		System.out.println("Day "+day + "\t"+ numSusceptible()+"\t"+ expectedNumExposed()+"\t"+expectedNumInfected()+"\t"+ numRecovered());
	}
	public void printTrickle(PrintWriter pw)
	{
		pw.println("Day "+day + "\t"+ numSusceptible()+"\t"+ expectedNumExposed()+"\t"+expectedNumInfected()+"\t"+ numRecovered());
	}

	public double expectedNumInfected()
	{
		double result=0;
		for(Vertice v: vertices)
		{
			result+=v.getCumulation();
		}
		return result;
	}
	public double expectedNumExposed() // needs to be checked before recoverycheck
	{
		double result=0;
		
		for(Vertice v: vertices)
			result+=(1-v.getProbInfectedFromContacts())*exposedProduct(v)*v.getProbNotRecovered();
			
		return result;
	}
	public double exposedProduct(Vertice v)
	{
		double result=1;
		result*=v.getTodayTracker().PNI;
		for(int i=0; i<latentPd-1; i++)
			result*=v.getTracker()[i].PNI;
		return 1-result;
	}
	public double numRecovered()
	{
		double result=0;
		
		for(Vertice v: vertices)
			if(!v.getVaccinationState())
				result+=1-v.getProbNotRecovered();
				
		return result;
	}
	
	public void addIndividualInfected()
	{
		for(int i=0; i<vertices.size(); i++)
		{
			individualData.get(i).add(vertices.get(i).getCumulation());
		}
	}
	public ArrayList<ArrayList<Double>> getIndividualInfected()
	{
		return individualData;
	}
	public double numSusceptible()
	{
		double result=0;
		
		for(Vertice v: vertices)
			result+=(1-v.getProbInfectedFromContacts())*(1-exposedProduct(v))*v.getProbNotRecovered();
			
		return result; 
	}
	public int numVaccinated()
	{
		int result=0;
		for(Vertice v: vertices)
		{
			if(v.getVaccinationState())
				result++;
		}
		return result;
	}
	
	public double getTotalEverInfected()
	{
		return totalEverInfected/(infectiousPd);
	}
}