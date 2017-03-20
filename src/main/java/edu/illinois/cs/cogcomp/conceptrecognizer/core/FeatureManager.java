package edu.illinois.cs.cogcomp.conceptrecognizer.core;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;
import edu.illinois.cs.cogcomp.conceptrecognizer.Parameters;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
//import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
//import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
//import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
//import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
//import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class FeatureManager {

	/**
	 * @param args
	 * key of stats is the concept name for example technique etc.
	 * value of stats is HashMap that keeps counts of features for that concept name
	 */
	HashMap<String, HashMap<String, Integer>> labeledstats, unlabeledstats;
	HashMap<String, List<String>> pastFeatures;
	HashMap<String, Double> threshold;
	HashSet<String> stopWords;
	ArrayList<String> lastWord;
	HashMap<String, Integer> topK;
	HashSet<String> punct;
	int t_num, a_num;
	
	public FeatureManager()
	{
		t_num=0;
		a_num=0;
		labeledstats = new HashMap<String, HashMap<String,Integer>>();
		unlabeledstats = new HashMap<String, HashMap<String,Integer>>();		
		pastFeatures = new HashMap<String, List<String>>();
		
		lastWord=new ArrayList<String>();
		lastWord.addAll(Arrays.asList("framework","algorithms","tasks","methods","techniques","systems","algorithm","task","method","technique","system","approach","approaches","metric","metrics","model","models","style","styles"));
        punct=new HashSet<String>(Arrays.asList(".",",","/","<",">","?",";",":","'","\"", "[", "]", "{", "}", "|", "\\","~","`","!","@","#","$","%","^","&","*","(",")","-","_","+","="));
		topK=new HashMap<String, Integer>();
		for(String concept: Parameters.categories)
			topK.put(concept, Parameters.TOPFEATURES);
		stopWords = new HashSet<String>();
		threshold=new HashMap<String, Double>();
		for(String concept: Parameters.categories)
			threshold.put(concept, Parameters.FEATURE_TH);
//		InputStreamReader isr = new InputStreamReader(FeatureManager.class.getResourceAsStream("stopwordlist.txt"));
//		BufferedReader br = new BufferedReader(isr);

		String stopwordfile = "src/main/resources/edu/illinois/cs/cogcomp/conceptrecognizer/stopwordlist.txt";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(stopwordfile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String str = null;
		try {
			str = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(str != null)
		{
			stopWords.add(str.toLowerCase());
			try {
				str = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	public void annotateWithFeatures(List<MyTextAnnotation> lt, HashMap<String, HashSet<String>> featureList,boolean train,boolean eval)
	{
		
		for(int i = 0;i < lt.size();i ++)
		{
			MyTextAnnotation ta = lt.get(i);
			List<Integer> concept_start = new ArrayList<Integer>();
			List<Integer> concept_end = new ArrayList<Integer>();
			List<String> concept_tags = new ArrayList<String>();
			
			if(train)
			{
				concept_start=ta.concept_start;
				concept_end=ta.concept_end;
				concept_tags=ta.concept_tags;
			}
			
			for(int j=0; j<ta.chunk_start.size(); j++)
			{
				int chunk_start = ta.chunk_start.get(j);
				int chunk_end = ta.chunk_end.get(j);
				String chunk_tag = ta.chunk_tags.get(j);
				if(!chunk_tag.startsWith("NP") || concept_start.contains(chunk_start))
					continue;
				String concept = getPhraseLabel(ta,j,featureList, Parameters.TRAINATLEASTKFEATURES);
				if(concept.equals("null"))
					continue;
				
				
				// trim unimportant words in the phrase in order to compare with the gold annotations
				if(!train)
				{
	/*				while(chunk_start < chunk_end && stopWords.contains(ta.getToken(chunk_start).toLowerCase()))
						chunk_start+=1;
					for(int k=chunk_end-1; k>=chunk_start; k--)
						if(ta.getToken(k).equals("(") || ta.getToken(k).equals("["))
							chunk_end=k;*/
					if(eval || chunk_end-chunk_start==1)
					{
						if(lastWord.contains(ta.getToken(chunk_end-1).toLowerCase()))
							chunk_end-=1;
					}
				}
				if(chunk_end>chunk_start)
				{
					
					if(concept.equals("Technique"))
					{
							t_num++;
							concept_start.add(chunk_start);
							concept_end.add(chunk_end);
							concept_tags.add(concept);
					}
					else if(concept.equals("Application"))
					{
							a_num++;
							concept_start.add(chunk_start);
							concept_end.add(chunk_end);
							concept_tags.add(concept);
					}
					else
						System.out.println("wtf!!!");
				}
			}
			ta.setConcepts(concept_start, concept_end, concept_tags);
		}
		if(train)
		{
			System.out.println("total tech annotation:"+t_num);
			System.out.println("total app annotation:"+a_num);
		}
	}
	

    public void cleanAnnotation(List<MyTextAnnotation> lt)
    {
		for(int i = 0;i < lt.size();i ++)
		{
			MyTextAnnotation ta = lt.get(i);
			List<Integer> starts = new ArrayList<Integer>();
			List<Integer> ends = new ArrayList<Integer>();
			List<String> tags = new ArrayList<String>();
			
			for(int j=0; j<ta.concept_start.size(); j++)
			{
				int start = ta.concept_start.get(j);
				int end = ta.concept_end.get(j);
				String tag = ta.concept_tags.get(j);
				for(int k=end-1; k>=start; k--)
				{
					String t=ta.getToken(k);
					if(t.startsWith("[") || t.startsWith("("))
						end=k;
				}
				
				if(end>start)
				{
					starts.add(start);
					ends.add(end);
					tags.add(tag);
				}
			}
			ta.setConcepts(starts, ends, tags);
		}
    }
    
    public void cleanAnnotation1(List<MyTextAnnotation> lt)
    {
        for(int i = 0;i < lt.size();i ++)
        {
            MyTextAnnotation ta = lt.get(i);

            List<Integer> cstarts = new ArrayList<Integer>();
            List<Integer> cends = new ArrayList<Integer>();
            List<String> cnames = new ArrayList<String>();

            for(int k=0; k<ta.concept_start.size(); k++)
            {
                String original = ta.getConceptString(k);
                if(original.contains("computational linguistic"))
                	continue;
                int start = ta.concept_start.get(k);
                int end = ta.concept_end.get(k);
                if(end>start+4)
                	continue;
                if(ta.getToken(start).matches("[\\d\\.-]+"))
                	continue;
                boolean bad=false;
                for(int j=start; j<end; j++)
                {
                	if(ta.getToken(j).length()>20)
                		bad=true;
                }
                if(bad)
                	continue;
                

                for(int j=start; j<end && start<end; j++)
                {
                    String token = ta.getToken(j);
                    if(token.startsWith("[") || token.startsWith("("))
                        end = j;
                    if(token.startsWith("]") || token.startsWith(")"))
                        start=j+1;
                }
                while(start < end)
                {
                    String t=ta.getToken(start).toLowerCase();
                    if(stopWords.contains(t)
                            || t.startsWith("cite_")
                            || ta.getPOSTag(start).equals("PRP")
                            || punct.contains(t.substring(0,1))
                            || t.startsWith("e.g.")
                            || t.startsWith("i.e."))
                        start++;
                    else
                        break;
                }
                while(start < end)
                {
                    String t=ta.getToken(end-1).toLowerCase();
                    if(t.startsWith("cite_"))
                        end--;
                    else
                        break;
                }
                //if(end-start==1
                //        && lastWord.contains(ta.getToken(end-1).toLowerCase()))
                        // || ta.getToken(end-1).equals(ta.getToken(end-1).toLowerCase())))
                //    continue;

                if(end>start)
                {
                    cstarts.add(start);
                    cends.add(end);
                    cnames.add(ta.concept_tags.get(k));
                }
            }
            ta.setConcepts(cstarts,cends,cnames);
        }
    }
    
	public List<String> extractFeatures(MyTextAnnotation ta, int chunk_idx)
	{
		List<String> features = new ArrayList<String>();
		int start = ta.chunk_start.get(chunk_idx);
		int end = ta.chunk_end.get(chunk_idx);
		
		int sentence_idx = ta.getSentenceIndex(start);
		int pre_np_idx = ta.getPreviousNPEndIndex(chunk_idx);
		for(int i=start-1; i>=sentence_idx && i>=pre_np_idx; i--)
		{
			if(ta.getPOSTag(i).startsWith("V") )
			{
				features.add("previous:V:"+ta.getToken(i));
				break;
			}
		}
		
		for(int i = start; i < end; i ++)
		{
			String word=ta.getToken(i).toLowerCase();
			if( word.isEmpty() || stopWords.contains(word))
				continue;
			features.add("unigram:" + word);
		}
		for(int i = start;i < end - 1; i ++)
		{
			String first_word=ta.getToken(i).toLowerCase();
			String second_word=ta.getToken(i + 1).toLowerCase();
			if(first_word.isEmpty() || second_word.isEmpty() || stopWords.contains(first_word) || stopWords.contains(second_word))
				continue;
			features.add("bigram:" + first_word + "#" + second_word);
		}
		
		boolean allCaps = true;
		for(int i = start;i < end;i ++)
			try
			{
				if(!Character.isUpperCase(ta.getToken(i).charAt(0)))
					allCaps = false;
			}
			catch(Exception e)
			{
				//System.out.println(ta.getToken(i));
			}
		if(allCaps && end > start + 1)
			features.add("ALLCAPS");
		
		
		for(int i = start - 1;i < end + 1;i ++)
		{
			if(i >= start && i < end)
				continue;
			else if (i < start && i >= 0)
			{
				String word=ta.getToken(i).toLowerCase();
				if(word.isEmpty() || stopWords.contains(word))
					continue;
				features.add("context:unigram:"+(i-start)+":" + word);
			}
			else if(i >= end && i < ta.size())
			{
				String word=ta.getToken(i).toLowerCase();
				if(word.isEmpty() || stopWords.contains(word))
					continue;
				features.add("context:unigram:"+(i - end) + ":" + word);
			}
		}

		for(int i = start - 2;i < end + 2;i ++)
		{
			if(i >= start && i < end)
				continue;
			if (i < start - 1 && i >= 0)
			{
				String first_word=ta.getToken(i).toLowerCase();
				String second_word=ta.getToken(i+1).toLowerCase();
				if(first_word.isEmpty() || second_word.isEmpty() ||stopWords.contains(first_word) || stopWords.contains(second_word))
					continue;
				features.add("context:bigram:" + (i-start) + ":" + first_word + "#" + second_word);
			}
			else if(i >= end+1  && i < ta.size())
			{
				String first_word=ta.getToken(i-1).toLowerCase();
				String second_word=ta.getToken(i).toLowerCase();
				if(first_word.isEmpty() || second_word.isEmpty() ||stopWords.contains(first_word) || stopWords.contains(second_word))
					continue;
				features.add("context:bigram:" + (i - end) + ":" + first_word + "#" + second_word);
			}
		}
		return features;
	}
	
	public void updateLabeledStats(String concept_tag, List<String> features)
	{
		HashMap<String, Integer> st = null;
		
		if(!labeledstats.containsKey(concept_tag))
			st = new HashMap<String, Integer>();
		else 
			st = labeledstats.get(concept_tag);
		
		for(String s : features)
		{
			if(!st.containsKey(s))
				st.put(s, 1);
			else 
				st.put(s, st.get(s) + 1);
		}
		labeledstats.put(concept_tag, st);
	}
	
	public void updateUnlabeledStats(List<String> features)
	{
		HashMap<String, Integer> st = null;
		
		for(String category : Parameters.categories)
		{
			if(!unlabeledstats.containsKey(category))
				st = new HashMap<String, Integer>();
			else 
				st = unlabeledstats.get(category);
		
			for(String s : features)
			{
				if(!st.containsKey(s))
					st.put(s, 1);
				else 
					st.put(s, st.get(s) + 1);
			}
			unlabeledstats.put(category, st);
		}
	}
	
	
	public void calcFeatureStats(List<MyTextAnnotation> tas)
	{
		labeledstats.clear();
		unlabeledstats.clear();

		for(String s : Parameters.categories)
		{
			labeledstats.put(s, new HashMap<String, Integer>());
			unlabeledstats.put(s, new HashMap<String, Integer>());
		}
	
		
		for(int i = 0;i < tas.size();i ++)
		{
			MyTextAnnotation ta = tas.get(i);
			for(int j=0; j<ta.concept_start.size();j++)
			{
				List<String> features = extractFeatures(ta, ta.getChunkIdByConceptId(j));
				updateLabeledStats(ta.concept_tags.get(j), features);
			}
			
			for(int j=0; j<ta.chunk_start.size();j++)
			{
				if(ta.chunk_tags.get(j).startsWith("NP"))
				{
					List<String> features = extractFeatures(ta, j);
					updateUnlabeledStats(features);
				}
			}
		}
	}
	
	public HashMap<String,HashSet<String>> getSelectedFeatures(List<MyTextAnnotation> tas,HashMap<String, HashSet<String>> allFeatures)
	{
		HashMap<String, HashSet<String>> featureList = new HashMap<String, HashSet<String>>(); 
		calcFeatureStats(tas);
		featureList = getKMostDiscriminativeFeatures(labeledstats, unlabeledstats, pastFeatures, topK,allFeatures);
		
		for(String concept : Parameters.categories)
		{
			if(!allFeatures.containsKey(concept))
				allFeatures.put(concept, new HashSet<String>());
			
			HashSet<String> m = new HashSet<String>();
			m.addAll(featureList.get(concept));
			m.addAll(allFeatures.get(concept));
			allFeatures.put(concept, m);
			System.out.println("#"+concept+" features:"+allFeatures.get(concept).size());
		}
		return featureList;
	}
	
	public HashMap<String, HashMap<String, Integer>> getPrunedFeatures(HashMap<String, HashMap<String, Integer>> stats)
	{
		HashMap<String, HashMap<String, Integer>> pruned = new HashMap<String, HashMap<String,Integer>>();
		
		for(String conceptname : Parameters.categories)
		{
			HashMap<String, Integer> fs = stats.get(conceptname);
			HashMap<String, Integer> nfs = new HashMap<String, Integer>();
			
			for(String feature : fs.keySet())
			{
				if(feature.startsWith("context:bigram"))
				{
						int index1=feature.lastIndexOf(":");
						int index2=feature.lastIndexOf("#");
						if(index1>=index2 || index1>=feature.length()-1)
							continue;
						//System.out.println(feature+" "+index1+" "+index2);
						String word1=feature.substring(index1+1, index2);
						String word2=feature.substring(index2+1);
						if(stopWords.contains(word1) || stopWords.contains(word2))
							continue;
				}
				if(feature.startsWith("context:unigram"))
				{
						int index1=feature.lastIndexOf(":");
						if(index1>=feature.length()-1)
							continue;
						String word1=feature.substring(index1+1);
						if(stopWords.contains(word1));
							continue;
				}

				
				if(feature.startsWith("bigram"))
				{
					int bindex = feature.indexOf(":");
					int eindex = feature.indexOf("#");
					if(bindex>=eindex || bindex>=feature.length()-1)
							continue;
					String word1 = feature.substring(bindex + 1, eindex);
					String word2 = feature.substring(eindex + 1);
					if(stopWords.contains(word1) || stopWords.contains(word2))
						continue;
				}
				if(feature.startsWith("unigram"))
				{
					int bindex = feature.indexOf(":");
					if(bindex>=feature.length()-1)
							continue;
					String word = feature.substring(bindex + 1);
					if(stopWords.contains(word))
						continue;
				}
				nfs.put(feature, fs.get(feature));
			}
			pruned.put(conceptname, nfs);
		}
		return pruned;
	}
	public HashMap<String, HashSet<String>> getKMostDiscriminativeFeatures(HashMap<String, HashMap<String, Integer>> labeledStats, HashMap<String, HashMap<String, Integer>> unlabeledStats, HashMap<String, List<String>> pastFeatures, HashMap<String, Integer> topK, HashMap<String, HashSet<String>> allfeatures)
	{
		HashMap<String, HashSet<String>> featureList = new HashMap<String, HashSet<String>>();
		labeledStats = getPrunedFeatures(labeledStats);
		
		PriorityQueue<Pair<String, Double[]>> sset = new PriorityQueue<Pair<String, Double[]>>(10, new Comparator ()
		{
			public int compare(Object o1, Object o2)
			{
				Pair<String, Double[]> pair1 = (Pair<String, Double[]>) o1;
				Pair<String, Double[]> pair2 = (Pair<String, Double[]>) o2;
				
				if(pair2.getSecond()[0] > pair1.getSecond()[0])
					return 1;
				else if(pair2.getSecond()[0] < pair1.getSecond()[0])
					return -1;
				else if(pair2.getSecond()[1] > pair1.getSecond()[1])
					return 1;
				else if(pair2.getSecond()[1] < pair1.getSecond()[1])
					return -1;
				else if(pair2.getFirst().compareTo(pair1.getFirst()) > 0)
					return 1;
				else if(pair2.getFirst().compareTo(pair1.getFirst()) < 0)
					return -1;
				else return 0;
			}
			
		}
		);
		
		for(String concept: Parameters.categories)
		{
			HashSet<String> fl = new HashSet<String>();
			
			HashMap<String, Integer> fs = labeledStats.get(concept);
			HashMap<String, Integer> fus = unlabeledStats.get(concept);
			
			for(String feature : fs.keySet())
			{
				double dl = fs.get(feature);
				double du = fus.get(feature);
				Double[] tmp=new Double[2];
				tmp[0]=dl/du;
				tmp[1]=dl;
				sset.add(new Pair<String, Double[]>(feature, tmp));
			}
			
			int count = 0;
			double sum=0;
			while(!sset.isEmpty())
			{
				Pair<String, Double[]> pair = sset.remove();
				if(count >= Parameters.TOPFEATURES)
					break;
				
				if(pair.getSecond()[0]>=threshold.get(concept))
				{
					if(allfeatures.get(concept).contains(pair.getFirst()))
						continue;
					//if(pair.getFirst().contains("context"))
						//System.out.println("ADD:"+pair.getFirst());
					fl.add(pair.getFirst());
					count ++;
					sum+=pair.getSecond()[0];
				}
				
			}
			if(count<Parameters.DECREASE_TH)
			{
					
				threshold.put(concept,Math.max(threshold.get(concept)-0.1,0));
				System.out.println("Decrease threshold to "+threshold.get(concept)+" "+concept);				
			}
				
			
			featureList.put(concept, fl);
			sset.clear();
		}
		return featureList;
	}
	
	public String getPhraseLabel(MyTextAnnotation ta, int chunk_idx, HashMap<String, HashSet<String>> featureList, int topk)
	{
		List<String> extractedFeatures = extractFeatures(ta, chunk_idx);
		int overlap = 0;
		int maxoverlap = 0;
		String maxConcept = new String("null");
		
		for(String concept : Parameters.categories)
		{
			overlap = 0;
			HashSet<String> features = featureList.get(concept);
				
			for(String feature: extractedFeatures)
				if(features.contains(feature))
					overlap++;
				if(maxoverlap <= overlap)
				{
					maxoverlap = overlap;
					maxConcept = new String(concept);
				}
		}
		
		if(maxoverlap < topk)
			return "null";
		return maxConcept;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
