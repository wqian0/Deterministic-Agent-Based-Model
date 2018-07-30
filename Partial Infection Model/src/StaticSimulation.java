import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
	
	private int latentPd;
	private int infectiousPd;
	
	ArrayList<double[]> cumulativeData;

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
		weightRanks=new HashMap<>();
		setWeightRanks();
		cumulativeData = new ArrayList<>();
		
		for(Vertice v: vertices)
			v.setProperties(latentPd, infectiousPd);
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
		cumulativeData = new ArrayList<>();
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
				other.sortingPlaceholder=runningTotal;
			}
		}
		double random = Main.RNG.nextDouble()*runningTotal;
		int startIndex=0;
		int endIndex=result.size()-1;
		int currentIndex=0;
		if(result.size()==0)
			return null;
		if(result.get(0).sortingPlaceholder>random)
			return result.get(0);
		while(endIndex-startIndex!=1)
		{
			currentIndex=(startIndex+endIndex)/2;
			if(result.get(currentIndex).sortingPlaceholder>=random)
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
	
	public void runDay() {
		for (Vertice v : vertices) {
			v.checkRecovery();
		}
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
	}
	public void simul()
	{
		System.out.println("day \t S \t E \t I \t R");
		while(hasRemaining())
		{
			show();
			runDay();
		}
		show();
	}
	
	
	public void setTricklers(ArrayList<Vertice> list)
	{
		for(Vertice v:list)
		{
			v.setCumulation(1.0);
		}
	}	
	public void setTrickler(Vertice v)
	{
		v.setCumulation(1.0);
	}
	public HashMap<Vertice, Double> getWeightRanks(Vertice v) //pre-calculates tProb*contactProb for each directed pair of vertices
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
		double altCumulation=0;
		for(Vertice v:vertices)
		{
			if(v.getCumulation()>0)
			{
				tempMap = weightRanks.get(v);
				for(Vertice x: tempMap.keySet())
				{
					if(!x.getRecoveryState())
					{
						altCumulation=v.getCumulation();
						for(int i=latentPd-1; i<v.getTracker().length; i++)
						{
							altCumulation=1-(1-altCumulation)/v.getTracker()[i].getPNI(x);
						}
						if(altCumulation>1)
							altCumulation=1;
						x.compoundCumulation(1-Math.pow(1-altCumulation*tempMap.get(x),v.getContactsPerDay().get(0)),v);
					}
				}
			}
		}
		//showTrickle2();
		cumulativeData.add(new double[] {numSusceptible(), expectedNumExposed(), expectedNumInfected(), numRecovered()});
		for(Vertice v: vertices)
		{
			v.checkCumulationRecovery(); 
		}
		day++;
	}
	public void trickleSimul()
	{
		currentTotal=expectedNumInfected();
		while(Math.abs(currentTotal-previousTotal)>.5||currentTotal>0.5||day<20)
	//	while(day<75)
		{
			previousTotal=currentTotal;
			runTrickleDay();
			currentTotal=expectedNumInfected();
			totalEverInfected+=currentTotal;
		}
	}
	public void showTrickle2()
	{
		System.out.println("Day "+day + "\t"+ numSusceptible()+"\t"+ expectedNumExposed()+"\t"+expectedNumInfected()+"\t"+ numRecovered());
	}
	public void printTrickle2(PrintWriter pw)
	{
		pw.println("Day "+day + "\t"+ numSusceptible()+"\t"+ expectedNumExposed()+"\t"+expectedNumInfected()+"\t"+ numRecovered());
	}

	public double expectedNumInfected()
	{
		double result=0;
		for(Vertice v: vertices)
			result+=v.getCumulation();
		return result;
	}
	public double expectedNumExposed() // needs to be checked before recoverycheck
	{
		double result=0;
		for(Vertice v: vertices)
			result+=(1-v.getTodayTracker().PNI)*exposedProduct(v);
		return result;
	}
	public double exposedProduct(Vertice v)
	{
		if(v.getRecoveryState()||v.getVaccinationState())
			return 0;
		double result=1;
		result*=v.getTodayTracker().PNI;
		for(int i=1; i<latentPd-1; i++)
			result*=v.getTracker()[i].PNI;
		return 1-result;
	}
	public double numRecovered()
	{
		double result=0;
		for(Vertice v: vertices)
		{
			if(v.getRecoveryState())
				result++;
		}
		return result;
	}
	public double numSusceptible()
	{
		double result=0;
		for(Vertice v: vertices)
		{
			if(!v.getRecoveryState())
				result+=(1-v.getCumulation())*(1-exposedProduct(v));
		}
		return result; 
	}
	public double getTotalEverInfected()
	{
		return totalEverInfected/infectiousPd;
	}
}
