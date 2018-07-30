import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class CommunityAnalysis {
	public static HashMap<Integer, ArrayList<String>> getCommunities(Scanner sc)
	{
		HashMap<Integer, ArrayList<String>> communities = new HashMap<>(); 
		ArrayList<String> temp = new ArrayList<>();
		sc.nextLine();
		int current=0;
		
		while(sc.hasNextLine())
		{
			current=sc.nextInt();
			if(communities.containsKey(current))
				communities.get(current).add(sc.next());
			else
			{
				temp.add(sc.next());
				communities.put(current, new ArrayList<String>(temp));
				temp.clear();
			}
		}
		//sc.close();
		return communities;
	}
	public static ArrayList<Community> convertFormat(HashMap<Integer, ArrayList<String>> input)
	{
		Iterator<Integer> it = input.keySet().iterator();
		ArrayList<Community> result = new ArrayList<>();
		Community temp;
		Integer x;
		while(it.hasNext())
		{
			x= it.next();
			temp = new Community();
			temp.list=input.get(x);
			temp.communityID=x;
			result.add(temp);
		}
		return result;
	}
	public static ArrayList<ArrayList<String>> convertToList(HashMap<Integer, ArrayList<String>> input)
	{
		ArrayList<ArrayList<String>> result= new ArrayList<>();
		Iterator<Integer> it = input.keySet().iterator();
		while(it.hasNext())
		{
			result.add(input.get(it.next()));
		}
		return result;
	}
	public static int countUnion(ArrayList<ArrayList<String>> c1, ArrayList<ArrayList<String>> c2)
	{
		int intersection=0;
		int total=0;
		for(ArrayList<String> list1: c1)	
		{
			for(String s1: list1)
			{
				for(ArrayList<String> list2: c2)
				{
					for(String s2: list2)
					{
						if(s1.equals(s2))
							intersection++;
					}
				}
			}
		}
		for(ArrayList<String> list1: c1)	
		{
			total+=list1.size();
		}
		for(ArrayList<String> list2: c2)	
		{
			total+=list2.size();
		}
		return total-intersection;
	}
	public static double jaccardIndex(Community c1, Community c2)
	{
		double intersection=0;
		double total=0;
		for(String s1: c1.list)
		{
			for(String s2: c2.list)
			{
				if(s1.equals(s2))
					intersection++;
			}
		}
		total+=c1.list.size();
		total+=c2.list.size();
		return intersection/(total-intersection);
	}
	public static double calculateNMI(ArrayList<ArrayList<String>> c1, ArrayList<ArrayList<String>> c2, double totalSize)
	{
		double numerator=0;
		double denominator=0;
		double tempP=0;
		for(int i=0; i<c1.size(); i++)
		{
			for(int j=0; j<c2.size(); j++)
			{
				tempP=P(c1.get(i),c2.get(j),totalSize);
				if(tempP!=0)
					numerator+=tempP*Math.log10(tempP/(Pi_(c1.get(i),c2,totalSize)*P_j(c1,c2.get(j),totalSize)));
			}
		}
		numerator*=-2;
		for(int i=0; i<c1.size();i++)
		{
			tempP=Pi_(c1.get(i),c2,totalSize);
			if(tempP!=0)
				denominator+=tempP*Math.log10(tempP);
		}
		for(int j=0; j<c2.size();j++)
		{
			tempP=P_j(c1,c2.get(j),totalSize);
			if(tempP!=0)
				denominator+=tempP*Math.log10(tempP);
		}
		//System.out.println("N \t"+numerator);
		//System.out.println("D \t"+ denominator);
		return numerator/denominator;
	}
	public static double P(ArrayList<String> L1, ArrayList<String> L2, double totalSize)
	{
		double count=0;
		for(String s: L1)
			for(String t: L2)
			{
				if(s.equals(t))
				{
					count++;
				}
			}
		return count/totalSize;
	}
	public static double Pi_(ArrayList<String> i, ArrayList<ArrayList<String>> j, double totalSize)
	{
		double total=0;
		for(int c=0; c<j.size(); c++)
			total+=P(i,j.get(c),totalSize);
		return total;
	}
	public static double P_j(ArrayList<ArrayList<String>> i, ArrayList<String> j, double totalSize)
	{
		double total=0;
		for(int c=0; c<i.size(); c++)
			total+=P(i.get(c),j,totalSize);
		return total;
	}
	public static double[][] getTable(ArrayList<Scanner> input)
	{
		double[][] results = new double[input.size()][input.size()];
		ArrayList<ArrayList<ArrayList<String>>> list = new ArrayList<>();
		int count=0;
		for(Scanner s: input)
		{
			count++;
			System.out.println(count);
			list.add(convertToList(getCommunities(s)));
			s.close();
		}
		int tempTotal;
		for(int i=0; i<list.size(); i++)
		{
			for(int j=0; j<list.size(); j++)
			{
				tempTotal = countUnion(list.get(i), list.get(j));
				results[i][j] = calculateNMI(list.get(i),list.get(j), tempTotal);
				
			}
		}
		return results;
	}
	public static double[][] getTable(Scanner M, Scanner T, Scanner W, Scanner Th, Scanner F)
	{
		
		ArrayList<ArrayList<String>> Monday = convertToList(getCommunities(M));
		ArrayList<ArrayList<String>> Tuesday = convertToList(getCommunities(T));
		ArrayList<ArrayList<String>> Wednesday = convertToList(getCommunities(W));
		ArrayList<ArrayList<String>> Thursday = convertToList(getCommunities(Th));
		ArrayList<ArrayList<String>> Friday = convertToList(getCommunities(F));
		
		ArrayList<ArrayList<ArrayList<String>>> list = new ArrayList<>(); //absurd but gets the job done w/o much hassle
		double[][] results = new double[5][5];
		list.add(Monday);
		list.add(Tuesday);
		list.add(Wednesday);
		list.add(Thursday);
		list.add(Friday);
		int tempTotal;
		for(int i=0; i<list.size(); i++)
		{
			for(int j=0; j<list.size(); j++)
			{
				tempTotal=countUnion(list.get(i),list.get(j));
				results[i][j]=calculateNMI(list.get(i),list.get(j), tempTotal);
			}
		}
		M.close();
		T.close();
		W.close();
		Th.close();
		F.close();
		return results;
	}
	public static HashMap<String, ArrayList<Double>> getAvgValues(Scanner M, Scanner T, Scanner W, Scanner Th, Scanner F)
	{
		HashMap<String, ArrayList<Double>> map = new HashMap<>();
		ArrayList<Double> empty = new ArrayList<>();
		for(int i=0; i<5; i++)
			empty.add(0.0);
		M.nextLine(); T.nextLine(); W.nextLine(); Th.nextLine(); F.nextLine();
		String temp;
		while(M.hasNext())
		{
			temp = M.next();
			if(!map.containsKey(temp))
				map.put(temp, new ArrayList<>(empty));
			for(int i=0; i<5; i++)
				map.get(temp).set(i,map.get(temp).get(i)+M.nextDouble());
		}
		while(T.hasNext())
		{
			temp = T.next();
			if(!map.containsKey(temp))
				map.put(temp, new ArrayList<>(empty));
			for(int i=0; i<5; i++)
				map.get(temp).set(i,map.get(temp).get(i)+T.nextDouble());
		}
		while(W.hasNext())
		{
			temp = W.next();
			if(!map.containsKey(temp))
				map.put(temp, new ArrayList<>(empty));
			for(int i=0; i<5; i++)
				map.get(temp).set(i,map.get(temp).get(i)+W.nextDouble());
		}
		while(Th.hasNext())
		{
			temp = Th.next();
			if(!map.containsKey(temp))
				map.put(temp, new ArrayList<>(empty));
			for(int i=0; i<5; i++)
				map.get(temp).set(i,map.get(temp).get(i)+Th.nextDouble());
		}
		while(F.hasNext())
		{
			temp = F.next();
			if(!map.containsKey(temp))
				map.put(temp, new ArrayList<>(empty));
			for(int i=0; i<5; i++)
				map.get(temp).set(i,map.get(temp).get(i)+F.nextDouble());
		}
		for(String s: map.keySet())
		{
			for(int i=0; i<5; i++)
			{
				map.get(s).set(i, map.get(s).get(i)/5.0);
			}
		}
		M.close(); T.close(); W.close(); Th.close(); F.close();
		return map;
	}
	public static void main(String[] args) throws IOException
	{
		
	}
}
