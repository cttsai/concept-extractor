package edu.illinois.cs.cogcomp.conceptrecognizer.core;

import edu.illinois.cs.cogcomp.conceptrecognizer.Parameters;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Clustering {
	
	ArrayList<String> filterWords;
	public Clustering(){
//		filterWords=new ArrayList<String>();
//		filterWords.addAll(Arrays.asList("natural language","nlp", "information","conference","machine learning"));
		
	}
	
	public boolean compareMentions(String m1, String m2)
	{
		String m1_abb="";
		String m2_abb="";
		ArrayList<String> m1_words = new ArrayList<String>();
		ArrayList<String> m2_words = new ArrayList<String>();
		
		//for(String t: m1.split("[ -]"))
		for(String t: m1.split(" "))
		{
			t=t.trim();
			if(!t.isEmpty())
			{
				m1_words.add(t);
				m1_abb+=t.charAt(0);
			}
		}
		//for(String t: m2.split("[ -]"))
		for(String t: m2.split(" "))
		{
			t=t.trim();
			if(!t.isEmpty())
			{
				m2_words.add(t);
				m2_abb+=t.charAt(0);
			}
		}
		int m1_len=m1_words.size();
		int m2_len=m2_words.size();
		int cnt1=0, cnt2=0;
		for(String w : m1_words)
		{
			if(m2_words.contains(w))
				cnt1++;
			else if(w.equals(m2_abb))
			{
				cnt1+=w.length();
				m1_len+=w.length()-1;
			}
		}
		
		for(String w : m2_words)
		{
			if(m1_words.contains(w))
				cnt2++;
			else if(w.equals(m1_abb))
			{
				cnt2+=w.length();
				m2_len+=w.length()-1;
			}
		}
		
		if(cnt1 > m1_len* Parameters.MENTION_SIM_TH && cnt2>m2_len*Parameters.MENTION_SIM_TH)
			return true;
		else
			return false;
	}
	
	public boolean compareClusters(ArrayList<String> c1, ArrayList<String> c2)
	{
		HashSet<String> match = new HashSet<String>();
		int cnt=0;
		for(String s2: c2)
		{
			for(String s1: c1)
			{
				//if(compareMentions(s2,s1) && !match.contains(s1))
				if(compareMentions(s2,s1))
				{
					cnt++;
					//match.add(s1);
					break;
				}
			}
		}
		
		HashSet<String> match1 = new HashSet<String>();
		int cnt1=0;
		for(String s1: c1)
		{
			for(String s2: c2)
			{
				//if(compareMentions(s1,s2) && !match1.contains(s2))
				if(compareMentions(s1,s2))
				{
					cnt1++;
					//match1.add(s2);
					break;
				}
			}
		}
		//if(match.size()>c2.size()*Parameters.CLUSTER_SIM_TH && match1.size()>c1.size())
		if(cnt>c2.size()*Parameters.CLUSTER_SIM_TH && cnt1>c1.size()*Parameters.CLUSTER_SIM_TH)
			return true;
		else
			return false;
		
	}
	
	public ArrayList<ArrayList<String>> extractClusters(HashMap<String, ArrayList<String>> cl)
	{
		System.out.println("extracting clusters...");
		
		ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
		for(String key: cl.keySet())
		{
			ArrayList<String> tmp = new ArrayList<String>(new HashSet<String>(cl.get(key)));
			r.add(tmp);
		}
		Collections.sort(r, new Comparator<ArrayList<String>>(){
			public int compare(ArrayList<String> a1, ArrayList<String> a2)
			{
				return a2.size()-a1.size();
			}
		});
		
		return r;
	}
	
	public ArrayList<Integer> getSimilarClusters(int idx, ArrayList<ArrayList<String>> clusters)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int i=idx+1; i<clusters.size(); i++)
		{
			if(compareClusters(clusters.get(idx), clusters.get(i)))
					result.add(i);
		}
		return result;
	}
	
	public ArrayList<ArrayList<String>> merge(ArrayList<ArrayList<String>> clusters)
	{
		System.out.println("merging clusters...");
		ArrayList<ArrayList<String>> clusters_done1= new ArrayList<ArrayList<String>>();
		while(!clusters.isEmpty())
		{
			ArrayList<String> target_cluster = clusters.get(0);
			if(target_cluster.isEmpty())
			{
				clusters.remove(0);
				continue;
			}
			ArrayList<Integer> idx = getSimilarClusters(0, clusters);
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			System.out.println("idx size:"+idx.size());
			if(idx.size()>0)
			{
				for(int id: idx)
				{
					tmp.addAll(getSimilarClusters(id, clusters));
					System.out.println("\t tmp size:"+tmp.size());
				}
				idx.addAll(tmp);
				idx = new ArrayList<Integer>(new HashSet<Integer>(idx));
				Collections.sort(idx);
				for(int j=0; j<idx.size(); j++)
				{
					target_cluster.addAll(clusters.get(idx.get(j)));
				}
				for(int j=idx.size()-1; j>=0; j--)
				{
					clusters.remove((int)idx.get(j));
				}
			}
			clusters.remove(0);
			clusters_done1.add(target_cluster);
			if(clusters_done1.size() % 100 ==0)
				System.out.println(clusters_done1.size());
		}

		Collections.sort(clusters_done1, new Comparator<ArrayList<String>>(){
			public int compare(ArrayList<String> a1, ArrayList<String> a2)
			{
				return a2.size()-a1.size();
			}
		});
		return clusters_done1;
	}
	
	public int mentionCount(String m, ArrayList<String> c)
	{
		int cnt=0;
		for(int i=0; i<c.size(); i++)
			if(compareMentions(m, c.get(i)))
				cnt++;
		return cnt;
	}
	
	public ArrayList<ArrayList<String>> cleanClusters(ArrayList<ArrayList<String>> clusters)
	{
		System.out.println("cleaning clusters...");
		HashSet<String> delete_all = new HashSet<String>();
		
		for(int i=0; i<clusters.size(); i++)
		{
			ArrayList<String> delete = new ArrayList<String>();
			for(int j=0; j<clusters.get(i).size(); j++)
			{
				String mention = clusters.get(i).get(j);
				int theCount = mentionCount(mention, clusters.get(i));
				for(int k=i+1; k<clusters.size(); k++)
				{
					if(theCount>=clusters.get(k).size())
						break;
					int count=mentionCount(mention, clusters.get(k));
					if(count > theCount)
					{
						delete.add(mention);
						delete_all.add(mention);
						break;
					}
				}
			}
			for(String d: delete)
			{
				clusters.get(i).remove(d);
			}
		}
		
		for(String s: delete_all)
		{
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(s);
			clusters.add(tmp);
		}
		return clusters;
	}
	
	public HashMap<String, ArrayList<ArrayList<String>>> mergeClusters(HashMap<String, HashMap<String, ArrayList<String>>> conceptClusters)
	{
		HashMap<String, ArrayList<ArrayList<String>>> output = new HashMap<String, ArrayList<ArrayList<String>>>();
		for(String concept: Parameters.categories)
		{
			System.out.println(concept);
			ArrayList<ArrayList<String>> clusters = extractClusters(conceptClusters.get(concept));
			clusters = cleanClusters(clusters);
			clusters = merge(clusters);
			for(int i=0; i<clusters.size(); i++)
			{
				clusters.set(i, new ArrayList<String>(new HashSet<String>(clusters.get(i))));
			}
			output.put(concept, clusters);
		}
		return output;
	}
	
	public void printClusters(String outdir, String filename, HashMap<String, ArrayList<ArrayList<String>>> clusters)
	{
		for(String concept: Parameters.categories)
		{
			String file_path=outdir+filename+"."+concept+".cluster";
			System.out.println("printing clusters to "+file_path);
			ArrayList<ArrayList<String>> c = clusters.get(concept);
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(file_path));
				for(int i=0; i<c.size();i++)
				{
					bw.write("#\n");
					for(String s: c.get(i))
						bw.write(s+"\n");
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args)
	{
	}

}
