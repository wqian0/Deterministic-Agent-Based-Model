/*
Main.java - Class is used to execute the StaticSimulation and DynamicSimulation classes. 
Available functions include:
  -Executing Reactionary Vaccination Simulations
  -Executing Preemptive Vaccination Simulations
  -Executing Simulations with Static Graphs
  -Executing Simulations with Dynamic Graphs
  -Creating Meta-graphs
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.SplittableRandom;

public class Main {

	// Set this to true for simulations and analysis using the full graph G_F
	static final boolean fullGraphMode = false;

	// Windows-based directory. Use forward slash for Linux. 
	static final String inputDirectory = "C:\\Simulation Input\\";

	//Values for T, alpha, gamma, and contacts per hour. Alpha must be >=1.
	static final double transmissionProbability=.1;
	static final int latentPeriod=1;
	static final int infectiousPeriod=3;
	static final int contactsPerHour=3;

	//Used for vaccination distribution analysis
	static final int numCentralities=4;

	//number of graphs rotated through in the dynamic graph
	static final int numDayGraphs=5;

	//seeded random for use in stochastic model.  
	static final SplittableRandom RNG = new SplittableRandom(1977);

	// read edges from an input file, assuming the vertices are in the hashmap
	public static ArrayList<Edge> getEdges(Scanner sc, HashMap<String,Vertice> map, int day)
	{
		ArrayList<Edge> edges = new ArrayList<>();
		Vertice v1;
		Vertice v2;
		double weight;
		Edge temp;
		while(sc.hasNextLine())
		{
			v1=map.get(sc.next());
			v2=map.get(sc.next());
			weight = sc.nextDouble();
			temp = new Edge(v1,v2,weight);
			edges.add(temp);

			v1.addEdge(day, temp);
			v2.addEdge(day, temp);	
		}
		sc.close();
		return edges;
	}

	//Useful for rendering.
	public static ArrayList<Edge> getEdgesNoInput(Scanner sc, HashMap<String,Vertice> map)
	{
		ArrayList<Edge> edges = new ArrayList<>();
		Vertice v1;
		Vertice v2;
		double weight;
		while(sc.hasNextLine())
		{
			v1=map.get(sc.next());
			v2=map.get(sc.next());
			weight = sc.nextDouble();
			edges.add(new Edge(v1,v2,weight));	
		}
		sc.close();
		return edges;
	}
	public static ArrayList<Vertice> getVertices(HashMap<String, Vertice> map, Scanner sc)
	{
		ArrayList<Vertice> output = new ArrayList<>();
		while(sc.hasNextLine())
		{
			output.add(map.get(sc.next())); 
		}
		sc.close();
		return output;
	}
	public static void vaccGlobalPeaks(ArrayList<Vertice> vertices, int index, int numOfVaccines, boolean pickHigh)
	{
		ArrayList<Vertice> vCopy = new ArrayList<>(vertices);
		Collections.sort(vCopy, new Comparator<Vertice>() {
			@Override
			public int compare(Vertice v1,Vertice v2) {
				return v1.centralities.get(index).compareTo(v2.centralities.get(index));
			}
		});
		if(pickHigh)
		{
			for(int j=vCopy.size()-1; j>=vCopy.size()-numOfVaccines; j--)
			{
				if(vCopy.get(j).getCumulation()==0)
				{
					vCopy.get(j).setRecoveryState(true);
					vCopy.get(j).setVaccinationState(true);
				}
			}
		}
		else
		{
			for(int j=0; j<numOfVaccines; j++)
			{
				if(vCopy.get(j).getCumulation()==0)
				{
					vCopy.get(j).setRecoveryState(true);
					vCopy.get(j).setVaccinationState(true);
				}
			}
		}
	}
	public static void globalVaccDS(DynamicSimulation DS, HashMap<String, Vertice> map,int traitID, int vaccines, boolean pickHigh)
	{
		ArrayList<Vertice> vertices = new ArrayList<>(map.values());
		ArrayList<Double> analysisArray = new ArrayList<>();
		ArrayList<Double> analysisArrayRecovered = new ArrayList<>();
		vaccGlobalPeaks(vertices, traitID, vaccines, pickHigh);

		for(Vertice v: vertices)
		{
			if(!v.getVaccinationState())
			{
				DS.setTrickler(v);
				DS.setStartDay(v.getStartingPoint());
				DS.trickleSimul();
				analysisArray.add(DS.getTotalEverInfected());
				analysisArrayRecovered.add(DS.numRecovered());
				System.out.println(v.getID()+"\t" +traitID+"\t"+ DS.getTotalEverInfected());
				DS.reset(false);
			}
		}
		DS.reset(true);

		// Prints the mean, standard error, and standard deviation of the expected total ever infected/expected number of recovered to console only.

		double mean = getMean(analysisArray);
		double stdError = getstdError(analysisArray);
		double stdDev = getstdDev(analysisArray);
		double meanR = getMean(analysisArrayRecovered);
		double stdErrorR = getstdError(analysisArrayRecovered);
		double stdDevR = getstdDev(analysisArrayRecovered);
		System.out.println(mean + "\t" + stdDev+"\t"+stdError+"\t --- \t"+meanR+"\t"+stdDevR+"\t"+stdErrorR);
	}
	public static void globalVaccSS(StaticSimulation SS, HashMap<String, Vertice> map,int traitID, int vaccines, boolean pickHigh)
	{
		ArrayList<Vertice> vertices = new ArrayList<>(map.values());
		ArrayList<Double> analysisArray = new ArrayList<>();
		ArrayList<Double> analysisArrayRecovered = new ArrayList<>();
		vaccGlobalPeaks(vertices, traitID, vaccines, pickHigh);

		for(Vertice v: vertices)
		{
			if(!v.getVaccinationState())
			{
				SS.setTrickler(v);
				SS.trickleSimul();
				analysisArray.add(SS.getTotalEverInfected());
				analysisArrayRecovered.add(SS.numRecovered());
				System.out.println(v.getID()+"\t" +traitID+"\t"+ SS.getTotalEverInfected());
				SS.reset(false);
			}
		}
		SS.reset(true);

		// Prints the mean, standard error, and standard deviation of the expected total ever infected/expected number of recovered to console only.

		double mean = getMean(analysisArray);
		double stdError = getstdError(analysisArray);
		double stdDev = getstdDev(analysisArray);
		double meanR = getMean(analysisArrayRecovered);
		double stdErrorR = getstdError(analysisArrayRecovered);
		double stdDevR = getstdDev(analysisArrayRecovered);
		System.out.println(mean + "\t" + stdDev+"\t"+stdError+"\t --- \t"+meanR+"\t"+stdDevR+"\t"+stdErrorR);
	}

	public static void vaccRandomNodes(ArrayList<Vertice> vertices, int num)
	{
		ArrayList<Integer> temp = new ArrayList<>();
		int random;
		for(int i=0; i<vertices.size(); i++)
			temp.add(i);
		for(int i=0; i<num; i++)
		{
			random=(int)(RNG.nextDouble()*temp.size());
			if(!vertices.get(random).getRecoveryState())
			{
				vertices.get(random).setRecoveryState(true);
				vertices.get(random).setVaccinationState(true);
			}
			else
				i--;
			temp.remove((Integer)random);
		}
	}


	public static void getMetaGraph(Scanner communities, HashMap<String, Vertice> map, PrintWriter output)
	{
		HashMap<Integer,ArrayList<String>> tempCommMap = CommunityAnalysis.getCommunities(communities);
		HashMap<Integer,ArrayList<Vertice>> commMap = new HashMap<>();

		ArrayList<Vertice> tempVertices = new ArrayList<>();
		for(Integer x: tempCommMap.keySet())
		{
			for(String s: tempCommMap.get(x))
				tempVertices.add(map.get(s));
			commMap.put(x, tempVertices);
			tempVertices=new ArrayList<>();
		}
		ArrayList<Integer> communityList = new ArrayList<>(commMap.keySet());
		double weight=0;
		for(int i=0; i<communityList.size(); i++)
		{
			for(int j=i+1; j<communityList.size(); j++)
			{
				for(Vertice x: commMap.get(communityList.get(i)))
				{
					for(Vertice y: commMap.get(communityList.get(j)))
					{
						weight+=x.getEdgeWeight(0, y);
					}
				}
				if(weight!=0)
					output.println(communityList.get(i)+"\t"+communityList.get(j)+"\t"+weight);
				weight=0;
			}
		}
	}
	public static double getEdgeWeight(ArrayList<Edge> edges, Vertice v1, Vertice v2)
	{
		for(Edge e: edges)
		{
			if(e.getSource()==v1||e.getTarget()==v1)
				if(e.getOther(v1)==v2)
					return e.getWeight();
		}
		return 0;
	}

	public static double[][] allPairsSP(Graph communities, PrintWriter results)
	{
		HashMap<Integer, Integer> IDs = new HashMap<>(); //vertex ID + Community ID
		for(int i=0; i<communities.getVertices().size(); i++)
		{
			IDs.put(i, Integer.parseInt((communities.getVertices().get(i).getID())));
		}
		double[][] dist = new double[IDs.size()][IDs.size()];
		for(int i=0; i<dist.length; i++)
		{
			for(int j=0; j<dist.length; j++)
			{
				dist[i][j]=Double.POSITIVE_INFINITY;
			}
		}
		for(int i=0; i<dist.length; i++)
		{
			dist[i][i]=0;
		}
		for(int i=0; i<communities.getVertices().size(); i++)
		{
			for(int j=0; j<communities.getVertices().size(); j++)
			{
				dist[i][j]= 1/getEdgeWeight(communities.getEdges(),communities.getVertices().get(i),communities.getVertices().get(j));
			}
		}
		for(int k=0; k<IDs.size(); k++)
			for(int i=0; i<IDs.size(); i++)
				for(int j=0; j<IDs.size(); j++)
				{
					if(dist[i][j]>dist[i][k]+dist[k][j])
						dist[i][j]=dist[i][k]+dist[k][j];
				}
		if(results!=null)
		{
			results.println("Vertex 1 \t Vertex 2 \t Weight");
			for(int i=0; i<IDs.size(); i++)
				for(int j=0; j<IDs.size(); j++)
				{
					results.println(IDs.get(i)+"\t"+IDs.get(j)+"\t"+dist[i][j]);
				}
		}
		return dist;
	}

	//This function is used to create and initialize a list of Course objects, which each contain a list of students, a course ID, and the start/end time of the course
	/*	The format of the cl scanner is as follows:
	 *	<Class ID>	<Student ID> 	<Student ID> 	....
	 *	.
	 *	.
	 *	.
	 * 	
	 * 	The format of the cs scanner is as follows:
	 * 	class_nbr	pattern		mtg_start		mtg_end
	 * <Class ID>	<weekday>	<start time>	<end time>
	 * 33684		F		08.30.00.000000		11.20.00.000000
	 * 
	 */


	public static ArrayList<Course> initCourses(HashMap<String, Vertice> map, Scanner cl, Scanner cs)
	{
		HashMap<Integer, Course> courses = new HashMap<>();
		cl.nextLine();
		int cNum=0;
		double sTime=0;
		double eTime=0;
		String days;
		ArrayList<Integer> dayList = new ArrayList<>();
		String single;
		String start;
		String end;
		while(cl.hasNextLine())
		{
			cNum=cl.nextInt();
			days = cl.next();
			start =cl.next();
			sTime = Double.parseDouble(start.substring(0,2));
			sTime+=Double.parseDouble(start.substring(3, 5))/60;
			end=cl.next();
			eTime = Double.parseDouble(end.substring(0,2));
			eTime+=Double.parseDouble(end.substring(3, 5))/60;
			for(int i=0; i<days.length(); i++)
			{
				single = days.substring(i, i+1);
				if(single.equals("M"))
					dayList.add(0);
				else if(single.equals("W"))
					dayList.add(2);
				else if (single.equals("F"))
					dayList.add(4);
				else
				{
					if(days.substring(i).length()>1)
					{
						if(days.substring(i+1,i+2).equals("H"))
						{	
							dayList.add(3);
							i++;
						}
						else
							dayList.add(1);	
					}
					else
						dayList.add(1);
				}
			}
			courses.put(cNum,new Course(cNum, new Time(dayList,sTime, eTime))); 
			dayList = new ArrayList<>();
		}
		StringTokenizer st;
		String temp;
		while(cs.hasNextLine())
		{
			st = new StringTokenizer(cs.nextLine());
			cNum = Integer.parseInt(st.nextToken());
			while(st.hasMoreTokens())
			{
				temp=st.nextToken();
				if(map.containsKey(temp)&&courses.containsKey(cNum))
					courses.get(cNum).addStudent(map.get(temp));
			}
		}
		cl.close();
		cs.close();
		return new ArrayList<>(courses.values());
	}

	public static void showCoursesPerDay(ArrayList<ArrayList<Course>> coursesPerDay)
	{
		int day = 0; 
		String days[]={new String("Monday"), new String("Tuesday"), new String("Wednesday"), new String("Thursday"), new String("Friday")};
		for(ArrayList<Course> cs: coursesPerDay)
		{
			System.out.println(" courses on day "+days[day++]);
			for(Course c: cs)
			{
				c.show();
			}
		}
	}

	public static Graph getGraph(Scanner v, Scanner e,HashMap<String, Vertice> map, int day) 
	{
		ArrayList<Vertice> temp = getVertices(map, v);
		Graph result = new Graph(temp,getEdges(e, map, day),day);
		return result;
	}

	//This function initializes the number of contacts each vertex expends per day based on duration of time spent in class

	/*	Input:
	 * 	vertices: ArrayList of vertices
	 * 	coursesPerDay: A List of Course objects per weekday
	 * 	contactFactor: number of contacts per hour in class
	 */
	public static void setContactsByDuration(ArrayList<Vertice> vertices, ArrayList<ArrayList<Course>> coursesPerDay, double contactFactor, boolean fullGraph)
	{
		ArrayList<Double> temp = new ArrayList<>();
		HashMap<Vertice, ArrayList<Double>> storedResults = new HashMap<>();
		double sum=0;
		for(int i=0; i<numDayGraphs; i++)
			temp.add(0.0);
		for(Vertice v: vertices)
			storedResults.put(v, new ArrayList<>(temp));
		for(int i=0; i<coursesPerDay.size(); i++)
		{
			for(Course c: coursesPerDay.get(i))
			{
				for(Vertice s: c.getStudents())
				{
					storedResults.get(s).set(i, storedResults.get(s).get(i)+c.getTime().getDuration());
				}
			}
		}
		if(!fullGraph)
		{
			for(Vertice v: vertices)
			{
				for(int i=0; i<numDayGraphs; i++)
				{
					v.getContactsPerDay().add(storedResults.get(v).get(i)*contactFactor);
				}
				v.setRoundedContacts();
			}
		}
		else
		{
			for(Vertice v: vertices)
			{
				for(int i=0; i<numDayGraphs; i++)
				{
					sum+=storedResults.get(v).get(i)*contactFactor;
				}
				v.getContactsPerDay().add(sum/=numDayGraphs);
				v.setRoundedContacts();
			}
		}
	}


	public static double getMean(ArrayList<Double> array)
	{
		double sum = 0.0;
		for(double a : array)
			sum += a;
		return sum/array.size();
	}
	public static double getVariance(ArrayList<Double> array)
	{
		double mean = getMean(array);
		double temp = 0;
		for(double a :array)
			temp += (a-mean)*(a-mean);
		return temp/(array.size()-1);
	}
	public static double getstdDev(ArrayList<Double> array)
	{
		return Math.sqrt(getVariance(array));
	}
	public static double getstdError(ArrayList<Double> array)
	{
		return getstdDev(array)/Math.sqrt(array.size());
	}

	//Reads file with properties of each vertex and inputs them in the centralities arraylist.
	/* Input file format is as follows:
	name	BetweennessCentrality	ClosenessCentrality	Degree	CCC
	...		...						...					...		...
	 */
	public static void addCentralities(Scanner nodeProperties, HashMap<String, Vertice> map, int numProperties)
	{
		//Skipping the title line
		nodeProperties.nextLine();
		String next;
		ArrayList<Double> temp;
		while(nodeProperties.hasNextLine())
		{
			next = nodeProperties.next();
			temp= map.get(next).centralities;
			for(int i=0; i<numProperties; i++)
			{
				temp.add(nodeProperties.nextDouble());
			}
			nodeProperties.nextLine();
		}
	}

	//This function is used to calculate how many vaccines are given to each community in the reactionary vaccination schema
	/*	Inputs:
	 * 	communities: Community meta-graph
	 * 	commMap: A HashMap pairing Community IDs to Vertex IDs
	 * 	dist: All-pairs shortest path between each pair of communities on the meta-graph
	 * 	targetCommID: ID of the community to be targeted by the vaccine distribution strategy
	 * 	totalVaccines: Number of vaccines to be distributed
	 */
	public static HashMap<Integer, Integer> getRingPartition(Graph communities, HashMap<Integer, ArrayList<String>> commMap, double[][] dist,int targetCommID, int totalVaccines)
	{
		int index=0;
		int vaccinesLeft=totalVaccines;
		int tempVaccineAmount=0;
		double totalDistance=0;
		HashMap<Integer, Integer> partition = new HashMap<>(); // integer1: community ID . Integer 2: How many vaccines they get
		for(int i=0; i<communities.getVertices().size(); i++)
		{
			if(Integer.parseInt(communities.getVertices().get(i).getID())==targetCommID)
			{
				index=i;
			}
		}
		for(int j=0; j<communities.getVertices().size(); j++)
		{
			totalDistance+=1/dist[index][j];
		}
		for(int j=0; j<communities.getVertices().size(); j++)
		{
			tempVaccineAmount= (int)(((1/dist[index][j])/totalDistance)*totalVaccines);
			if(commMap.get(Integer.parseInt(communities.getVertices().get(j).getID())).size()<tempVaccineAmount)
				tempVaccineAmount=commMap.get(Integer.parseInt(communities.getVertices().get(j).getID())).size();
			partition.put(Integer.parseInt(communities.getVertices().get(j).getID()), tempVaccineAmount);
			vaccinesLeft-=tempVaccineAmount;
		}
		while(vaccinesLeft>0)
		{
			for(int j=0; j<communities.getVertices().size(); j++)
			{
				if(partition.get(Integer.parseInt(communities.getVertices().get(j).getID()))<commMap.get(Integer.parseInt(communities.getVertices().get(j).getID())).size())
				{
					partition.put(Integer.parseInt(communities.getVertices().get(j).getID()), partition.get(Integer.parseInt(communities.getVertices().get(j).getID()))+1);
					vaccinesLeft--;
					break;
				}
			}
		}
		return partition;
	}

	// This function is used to vaccinate vertices according the reactionary vaccination schema
	/*	input:
	 * 	communities: Community meta-graph
	 * 	commMap: A HashMap pairing Community IDs to Vertex IDs
	 * 	dist: All-pairs shortest path between each pair of communities on the meta-graph
	 * 	targetCommID: ID of the community to be targeted by the vaccine distribution strategy
	 * 	totalVaccines: Number of vaccines to be distributed
	 * 	traitID: ID of trait used to select vertices for vaccination
	 * 	
	 */
	public static void runRingVacc(Graph communities, HashMap<Integer, ArrayList<String>> commMap, HashMap<String, Vertice> map, double[][] dist, int targetCommID, int totalVaccines, int traitID, boolean pickHigh)
	{
		HashMap<Integer, Integer> partition = getRingPartition(communities, commMap, dist, targetCommID, totalVaccines);
		HashMap<Integer,ArrayList<Vertice>> verticeCommMap = new HashMap<>();
		ArrayList<Vertice> tempVertices = new ArrayList<>();

		for(Integer x: commMap.keySet())
		{
			for(String s: commMap.get(x))
				tempVertices.add(map.get(s));
			verticeCommMap.put(x, tempVertices);
			tempVertices=new ArrayList<>();
		}
		for(Integer x: partition.keySet())
		{
			Collections.sort(verticeCommMap.get(x), new Comparator<Vertice>() { 
				@Override
				public int compare(Vertice v1,Vertice v2) {
					return v1.centralities.get(traitID).compareTo(v2.centralities.get(traitID));
				}
			});
		}
		if(pickHigh)
		{
			for(Integer x: partition.keySet())
			{
				for(int i=verticeCommMap.get(x).size()-1; i>=verticeCommMap.get(x).size()-partition.get(x); i--)
				{
					verticeCommMap.get(x).get(i).setRecoveryState(true);
					verticeCommMap.get(x).get(i).setVaccinationState(true);
				}
			}
		}
		else
		{
			for(Integer x: partition.keySet())
			{
				for(int i=0; i<partition.get(x); i++)
				{
					verticeCommMap.get(x).get(i).setRecoveryState(true);
					verticeCommMap.get(x).get(i).setVaccinationState(true);
				}
			}
		}
	}

	// this function is used to run a single set of experiments using the reactionary vaccination strategy
	/*
	 * 	Input: 
	 * 	DS: DynamicSimulation object used for simulations
	 * 	Meta: community-meta graph
	 * 	dist: Distances between each pair of communities on the meta-graph. Indices correspond to the position of Community IDs when sorted least to highest
	 * 	commMap: HashMap pairing Community IDs to lists of IDs of vertices
	 * 	map: HashMap pairing vertex IDs to vertice objects
	 * 	vaccines: Number of vaccines to be distributed in trial
	 * 	traitID: specifies the trait used to select vertices for vaccination
	 * 	pickHigh: If true, picks vertices with highest values of trait specified by traitID. Else, picks lowest.
	 */
	public static void runReactionaryVaccDS(DynamicSimulation DS, Graph Meta, double[][] dist, HashMap<Integer, ArrayList<String>> commMap, HashMap<String, Vertice> map, int vaccines,int traitID, boolean pickHigh)
	{
		ArrayList<Double> analysisArray = new ArrayList<>();
		ArrayList<Double> analysisArrayRecovered = new ArrayList<>();

		for(Integer x: commMap.keySet())
		{
			runRingVacc(Meta, commMap,map, dist, x, vaccines, traitID, pickHigh);
			for(String s: commMap.get(x))
			{
				DS.setTrickler(map.get(s));
				DS.setStartDay(map.get(s).getStartingPoint());
				DS.trickleSimul();
				analysisArray.add(DS.getTotalEverInfected());
				analysisArray.add(DS.numRecovered());
				DS.reset(false);
			}
			DS.reset(true);
		}
		// Prints the mean, standard error, and standard deviation of the expected total ever infected/expected num recovered to console only
		double mean = getMean(analysisArray);
		double stdError = getstdError(analysisArray);
		double stdDev = getstdDev(analysisArray);
		double meanR = getMean(analysisArrayRecovered);
		double stdErrorR = getstdError(analysisArrayRecovered);
		double stdDevR = getstdDev(analysisArrayRecovered);
		System.out.println(mean + "\t" + stdDev+"\t"+stdError+"\t --- \t"+meanR+"\t"+stdDevR+"\t"+stdErrorR);
	}

	public static void runReactionaryVaccSS(StaticSimulation SS, boolean monteCarlo, Graph Meta, double[][] dist, HashMap<Integer, ArrayList<String>> commMap, HashMap<String, Vertice> map, int vaccines,int traitID, boolean pickHigh)
	{
		ArrayList<Double> analysisArray = new ArrayList<>();
		ArrayList<Double> analysisArrayRecovered = new ArrayList<>();

		for(Integer x: commMap.keySet())
		{
			runRingVacc(Meta, commMap,map, dist, x, vaccines, traitID, pickHigh);
			if(monteCarlo)
			{
				for(String s: commMap.get(x))
				{
					SS.setTrickler(map.get(s));
					SS.trickleSimul();
					analysisArray.add(SS.getTotalEverInfected());
					analysisArray.add(SS.numRecovered());
					SS.reset(false);
				}
				SS.reset(true);
			}
			else
			{
				for(String s: commMap.get(x))
				{
					SS.setInfected(map.get(s));
					SS.simul();
					analysisArray.add(SS.getTotalEverInfected());
					analysisArray.add(SS.numRecovered());
					SS.reset(false);
				}
				SS.reset(true);
			}
		}
		// Prints the mean, standard error, and standard deviation of the expected total ever infected/expected num recovered to console only
		double mean = getMean(analysisArray);
		double stdError = getstdError(analysisArray);
		double stdDev = getstdDev(analysisArray);
		double meanR = getMean(analysisArrayRecovered);
		double stdErrorR = getstdError(analysisArrayRecovered);
		double stdDevR = getstdDev(analysisArrayRecovered);
		System.out.println(mean + "\t" + stdDev+"\t"+stdError+"\t --- \t"+meanR+"\t"+stdDevR+"\t"+stdErrorR);
	}

	// this function is used to generate a graph object for a particular day. 
	// Input: day, name of a weekday, Capitalize first letter
	// Input: weekday index
	// Input: map, string id of a vertice to vertice object
	public static Graph processDay(String day,int weekday, HashMap<String, Vertice> map) throws FileNotFoundException
	{
		Graph result=null;
		Scanner edges = new Scanner(new File(inputDirectory+ day+"\\duration_edges.txt"));
		Scanner vertices = new Scanner(new File(inputDirectory+day+"\\IDList.txt"));
		result= getGraph(vertices,edges,map, weekday);
		edges.close();
		vertices.close();
		return result;
	}

	public static void runStaticSimulation(StaticSimulation SS, Vertice initInfectious, boolean monteCarlo, boolean affectVaccinated)
	{
		if(monteCarlo)
		{
			SS.setInfected(initInfectious);
			SS.simul();
		}
		else
		{
			SS.setTrickler(initInfectious);
			SS.trickleSimul();
		}
		SS.reset(affectVaccinated);
	}
	
	//Used to take day-by-day averages of stochastic outbreak trials.
	public static void runStaticSimulationTrials(StaticSimulation SS, Vertice initInfectious, int numTrials, int outbreakThreshold, int outbreakTrialThreshold, PrintWriter pw)
	{
		ArrayList<Double> analysisArray = new ArrayList<>();
		int recovered=0;
		int count=0;
		
		ArrayList<ArrayList<double[]>> data = new ArrayList<>();
		ArrayList<ArrayList<Double>> suscData = new ArrayList<>();
		ArrayList<ArrayList<Double>> exposedData = new ArrayList<>();
		ArrayList<ArrayList<Double>> infectedData = new ArrayList<>();
		ArrayList<ArrayList<Double>> recoveredData = new ArrayList<>();
		
		for(int i=0; i<100; i++)
		{
			suscData.add(new ArrayList<>());
			exposedData.add(new ArrayList<>());
			infectedData.add(new ArrayList<>());
			recoveredData.add(new ArrayList<>());
		}
		for(int i=0; i<numTrials; i++)
		{
			System.out.println(i);
			SS.setInfected(initInfectious);
			SS.simul();
			recovered=SS.getNumResistant();
			if(recovered<outbreakThreshold)
			{
				i--;
				count++;
				if(count>outbreakTrialThreshold)
				{
					System.out.println(initInfectious.getID()+"\t No outbreak observed");
					pw.println(initInfectious.getID()+"\t No outbreak observed");
					return;
				}
			}
			else 
			{
				analysisArray.add((double)SS.getNumResistant());
				data.add(SS.getData());
				SS.reset(true);
			}
		}
		double mean = getMean(analysisArray);
		double stdError = getstdError(analysisArray);
		double stdDev = getstdDev(analysisArray);
		System.out.println(initInfectious.getID()+"\t"+mean + "\t" + stdDev+"\t"+stdError);
		pw.println(initInfectious.getID()+"\t"+mean + "\t" + stdDev+"\t"+stdError);
		
		for(int i=0; i<data.size(); i++)
		{
			for(int j=0; j<100; j++)
			{
				if(data.get(i).size()>j)
				{
					suscData.get(j).add(data.get(i).get(j)[0]);
					exposedData.get(j).add(data.get(i).get(j)[1]);
					infectedData.get(j).add(data.get(i).get(j)[2]);
					recoveredData.get(j).add(data.get(i).get(j)[3]);
				}
			}
		}
		for(int i=0; i<100; i++)
		{
			System.out.print(getMean(suscData.get(i))+"\t"+getMean(exposedData.get(i))+"\t"+getMean(infectedData.get(i))+"\t"+getMean(recoveredData.get(i)));
			System.out.print("\t"+getstdError(suscData.get(i))+"\t"+getstdError(exposedData.get(i))+"\t"+getstdError(infectedData.get(i))+"\t"+getstdError(recoveredData.get(i)));
			System.out.println();
		}
	}
	
	public static void runDynamicSimulation(DynamicSimulation DS, Vertice initInfectious, boolean monteCarlo, boolean affectVaccinated)
	{
		DS.setStartDay(initInfectious.getStartingPoint());
		if(monteCarlo)
		{
			DS.setInfected(initInfectious);
			DS.simul();
			System.out.println(initInfectious.getID()+"\t"+DS.getNumResistant());
		}
		else
		{
			DS.setTrickler(initInfectious);
			DS.trickleSimul();
			System.out.println(initInfectious.getID()+"\t"+DS.getTotalEverInfected()+"\t"+DS.numRecovered());
		}
		DS.reset(affectVaccinated);
	}
	
	
	//Used to take day-by-day averages of stochastic outbreak trials.
	public static void runDynamicSimulationTrials(DynamicSimulation DS, Vertice initInfectious, int numTrials, int outbreakThreshold, int outbreakTrialThreshold, PrintWriter pw)
	{
		ArrayList<Double> analysisArray = new ArrayList<>();
		int recovered=0;
		int count=0;
		int startingPoint = initInfectious.getStartingPoint();
		
		ArrayList<ArrayList<double[]>> data = new ArrayList<>();
		ArrayList<ArrayList<Double>> suscData = new ArrayList<>();
		ArrayList<ArrayList<Double>> exposedData = new ArrayList<>();
		ArrayList<ArrayList<Double>> infectedData = new ArrayList<>();
		ArrayList<ArrayList<Double>> recoveredData = new ArrayList<>();
		for(int i=0; i<150; i++)
		{
			suscData.add(new ArrayList<>());
			exposedData.add(new ArrayList<>());
			infectedData.add(new ArrayList<>());
			recoveredData.add(new ArrayList<>());
		}
		for(int i=0; i<numTrials; i++)
		{
			System.out.println(i);
			DS.setInfected(initInfectious);
			DS.setStartDay(startingPoint);
			DS.simul();
			recovered=DS.getNumResistant();
			if(recovered<outbreakThreshold)
			{
				i--;
				count++;
				if(count>outbreakTrialThreshold)
				{
					System.out.println(initInfectious.getID()+"\t"+DS.getNumResistant()+"\t No outbreak observed");
					pw.println(initInfectious.getID()+"\t No outbreak observed");
					DS.reset(true);
				//	return;
				}
			}
			else 
			{
				analysisArray.add((double)DS.getNumResistant());
				data.add(DS.getData());
				count=0;
			}
			DS.reset(true);
		}
		double mean = getMean(analysisArray);
		double stdError = getstdError(analysisArray);
		double stdDev = getstdDev(analysisArray);
		System.out.println(initInfectious.getID()+"\t"+mean + "\t" + stdDev+"\t"+stdError);
		pw.println(initInfectious.getID()+"\t"+mean + "\t" + stdDev+"\t"+stdError);
		
		for(int i=0; i<data.size(); i++)
		{
			for(int j=0; j<150; j++)
			{
				if(data.get(i).size()>j)
				{
					suscData.get(j).add(data.get(i).get(j)[0]);
					exposedData.get(j).add(data.get(i).get(j)[1]);
					infectedData.get(j).add(data.get(i).get(j)[2]);
					recoveredData.get(j).add(data.get(i).get(j)[3]);
				}
			}
		}
		for(int i=0; i<150; i++)
		{
			System.out.print(getMean(suscData.get(i))+"\t"+getMean(exposedData.get(i))+"\t"+getMean(infectedData.get(i))+"\t"+getMean(recoveredData.get(i)));
			System.out.print("\t"+getstdError(suscData.get(i))+"\t"+getstdError(exposedData.get(i))+"\t"+getstdError(infectedData.get(i))+"\t"+getstdError(recoveredData.get(i)));
			System.out.println();
		}
	}
	
	
	
	// Specific to rendering the original data-set using CytoScape location data. For use with JavaFX.
	public static HashMap<Vertice, Location> getLocations(Scanner sc, HashMap<String, Vertice> map)
	{
		HashMap<Vertice, Location> result= new HashMap<>();
		Vertice temp;
		double tempX;
		String tempString;
		String tempString2;
		double tempY;
		for(int i=0; i<map.size(); i++)
		{
			sc.nextLine();
			sc.nextLine();
			sc.next();
			sc.next();
			tempString2=sc.next();
			tempString2=tempString2.substring(1, tempString2.length()-2);
			temp=map.get(tempString2);
			for(int j=0; j<7; j++)
				sc.nextLine();
			sc.next();
			sc.next();
			tempString =sc.next();
			tempX=Double.parseDouble(tempString.substring(0,tempString.length()-1));
			sc.nextLine();
			sc.next();
			sc.next();
			tempY=sc.nextDouble();
			result.put(temp,new Location(tempX,tempY));
			for(int j=0; j<4; j++)
				sc.nextLine();
		}
		sc.close();
		return result;
	}

	public static void main(String args[]) throws IOException
	{ 
		Scanner c_FullD = new Scanner(new File(inputDirectory+"Full Graph Duration Communities.txt"));
		HashMap<Integer, ArrayList<String>> commMap = CommunityAnalysis.getCommunities(c_FullD);
		// read IDs of nodes
		Scanner IDs = new Scanner(new File(inputDirectory+"IDList.txt"));

		// maps ID to vertices
		HashMap<String, Vertice> map = new HashMap<String, Vertice>();

		String temp;
		while(IDs.hasNextLine())
		{
			temp =IDs.next();
			map.put(temp, new Vertice(temp,numDayGraphs));
		}
		IDs.close();
		ArrayList<Vertice> vertices = new ArrayList<>(map.values());

		Scanner nodeProperties = new Scanner(new File(inputDirectory+"Node Properties.txt"));
		addCentralities(nodeProperties,map, numCentralities);

		PrintWriter pw = new PrintWriter("outfile.txt");
		PrintWriter experiment = new PrintWriter("experiment.txt");


		Scanner v_Meta = new Scanner(new File(inputDirectory+"Meta ID List.txt"));
		HashMap<String, Vertice> metaMap = new HashMap<String, Vertice>();


		String metaTemp;
		while(v_Meta.hasNextLine())
		{
			metaTemp =v_Meta.nextLine();
			metaMap.put(metaTemp, new Vertice((String)metaTemp, 1));
		}
		v_Meta.close();
		v_Meta = new Scanner(new File(inputDirectory+"Meta ID List.txt"));
		Scanner e_Meta = new Scanner(new File(inputDirectory+"Meta Edge List.txt"));
		Graph Meta = getGraph(v_Meta, e_Meta, metaMap, 0);
		v_Meta.close();
		double[][] dist = allPairsSP(Meta, pw);

		Scanner courseList = new Scanner(new File(inputDirectory+"Courses.txt"));
		Scanner courseTimes = new Scanner(new File(inputDirectory+"CourseTimes.txt"));
		ArrayList<Course> courses = initCourses(map,courseTimes,courseList);
		ArrayList<ArrayList<Course>> coursesPerDay = new ArrayList<>();

		for(int i=0; i<numDayGraphs; i++)
		{
			coursesPerDay.add(new ArrayList<Course>());
		}
		for(Course c: courses)
			for(Integer day:c.getTime().getDays())
				coursesPerDay.get((int)day).add(c);

		setContactsByDuration(vertices,coursesPerDay,contactsPerHour, fullGraphMode);

		if(fullGraphMode)
		{
			Scanner e_Full = new Scanner(new File(inputDirectory+"out_Alld.txt"));

			ArrayList<Edge> edges_Full = getEdges(e_Full, map, 0);
			Graph Full = new Graph(vertices,edges_Full,0);
			e_Full.close();
			c_FullD.close();

			StaticSimulation SS = new StaticSimulation(Full,transmissionProbability,latentPeriod,infectiousPeriod);
			runStaticSimulation(SS,vertices.get(0),false,false);
		}
		else
		{
			Graph mondayGraph = processDay("Monday",0, map);
			Graph tuesdayGraph = processDay("Tuesday",1, map);
			Graph wednesdayGraph = processDay("Wednesday",2, map);
			Graph thursdayGraph = processDay("Thursday",3, map);
			Graph fridayGraph = processDay("Friday",4, map);

			Graph[] graphList = {mondayGraph,tuesdayGraph,wednesdayGraph,thursdayGraph,fridayGraph};

			DynamicSimulation DS = new DynamicSimulation(graphList,vertices,transmissionProbability,latentPeriod,infectiousPeriod);
			runDynamicSimulation(DS,vertices.get(0),false,false);
		}
		experiment.close();
	}
}
