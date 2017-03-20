package edu.illinois.cs.cogcomp.conceptrecognizer;

import edu.illinois.cs.cogcomp.conceptrecognizer.core.CitationManager;
import edu.illinois.cs.cogcomp.conceptrecognizer.core.Clustering;
import edu.illinois.cs.cogcomp.conceptrecognizer.core.FeatureManager;
import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;
import edu.illinois.cs.cogcomp.conceptrecognizer.reader.GoldAnnotationReader;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Tester {

	/**
	 * @param args
	 */
	FeatureManager fm;
	ArrayList<String> meta;
	GoldAnnotationReader gr;
	List<MyTextAnnotation> gold;
	List<MyTextAnnotation> predicted;
	List<TextAnnotation> gold_;
	Propagator pp;
	
	public Tester()
	{
		fm = new FeatureManager();
		pp = new Propagator();
		gr = new GoldAnnotationReader(Parameters.TESTDATA);
		//readMeta("dev.meta");
		//gr = new GoldAnnotationReader(Constants.dataDir+"dev");
		gold=gr.getMyTextAnnotations();
		predicted=gr.getMyTextAnnotations();
		
		//gold_=gr.getTextAnnotations_();
		
	}
	
	public static HashMap<String, HashSet<String>> read(String rdgname)
	{
		//System.out.println("Reading from:" + rdgname + ".lc");
		HashMap<String, HashSet<String>> features = null;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(rdgname));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			features = (HashMap<String, HashSet<String>>) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return features;
	}
	/*public void predict(String inputfilename, String featurefilename)
	{
		CuratorClient cc = new CuratorClient(Parameters.getCuratorHost(), Parameters.curatorPort);
		HashMap<String, HashSet<String>> features = read(featurefilename);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputfilename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String str = null;
		try {
			str = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		List<TextAnnotation> inputs = new ArrayList<TextAnnotation>();
		int line=0;
		while(str != null)
		{
			line++;
			str = str.replaceAll("[^\\w\\s\\p{Punct}]", "");
			str=str.trim();
			if(str.isEmpty())
			{
				System.out.println("Empty:"+line);
				continue;
			}
			TextAnnotation ta = null;
			try {
				ta = cc.getTextAnnotation("", "", str, false);
				cc.addChunkView(ta, false);
				cc.addPOSView(ta, false);
			} catch (Exception e) {
				//e.printStackTrace();
				//System.out.println("Line:"+line);
				continue;
			}
			inputs.add(ta);
	
			
			if(line%100==0)
				System.out.println(line);
			try {
				str = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		List<TextAnnotation> predicts = bs.annotateWithFeatures(inputs, features, false,true);
		
		predicts = fm.cleanAnnotation(predicts);
		try {
			BufferedWriter bw_app = new BufferedWriter(new FileWriter(Parameters.OutputDir+inputfilename+"concept.app"));
			BufferedWriter bw_tech = new BufferedWriter(new FileWriter(Parameters.OutputDir+inputfilename+"concept.tech"));
			for(int i=0; i<predicts.size(); i++)
			{
				SpanLabelView spv = (SpanLabelView) predicts.get(i).getView("Concept");
				for(Constituent c: spv.getConstituents())
				{
					String mention = filterMentionString(c.getSurfaceString());
					if(mention.isEmpty())
						continue;
					if(c.getLabel().equals("Application"))
						bw_app.write(mention+":");
					if(c.getLabel().equals("Technique"))
						bw_tech.write(mention+":");
				}
				bw_app.write("\n");
				bw_tech.write("\n");
			}
			bw_app.close();
			bw_tech.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	/*public String filterMentionString(String mention)
	{
		String[] words=mention.split("\\s");
		String result = "";
		if(words.length > 4)
			return result;
		for(String word: words)
		{
			if(!word.matches("\\d+") && !FeatureSelection.stopWords.contains(word))
				result+=" "+word;
		}
		return result.trim();
	}*/
	
	HashMap<String, HashMap<String, ArrayList<String>>> mergeCollect(ArrayList<HashMap<String, HashMap<String, ArrayList<String>>>> collect)
	{
		HashMap<String, HashMap<String, ArrayList<String>>> result = new HashMap<String, HashMap<String, ArrayList<String>>>();
		for(int i=0; i<collect.size(); i++)
		{
			for(String type: collect.get(i).keySet())
			{
				HashMap<String, ArrayList<String>> clusters_all = result.get(type);
				for(String key: collect.get(i).get(type).keySet())
				{
					if(clusters_all.containsKey(key))
					{
						ArrayList<String> cluster = clusters_all.get(key);
						cluster.addAll(collect.get(i).get(type).get(key));
						clusters_all.put(key, cluster);
					}
					else
					{
						clusters_all.put(key, collect.get(i).get(type).get(key));
					}
				}
				result.put(type, clusters_all);
			}
		}
		return result;
	}
	
	public void predict(String inputfilename, String featurefilename)
	{
		HashMap<String, HashSet<String>> features = read(featurefilename);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputfilename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String str = null;
		try {
			str = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] splits = inputfilename.split("/");
		CitationManager cm=new CitationManager();
		ArrayList<HashMap<String, HashMap<String, ArrayList<String>>>> collect_clusters = new ArrayList<HashMap<String, HashMap<String, ArrayList<String>>>>();
		
		int line=0;
		List<MyTextAnnotation> inputs = new ArrayList<MyTextAnnotation>();
		while(str != null)
		{
			line++;
			str = str.replaceAll("[^\\w\\s\\p{Punct}]", "");
			str=str.trim();
			if(str.isEmpty())
				continue;
			MyTextAnnotation ta = new MyTextAnnotation(str); 
			inputs.add(ta);
			
			if(line%100==0)
				System.out.println(line+" lines are processed");
			try {
				str = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			/*
			if(line % 5000 ==0)
			{
				fm.annotateWithFeatures(inputs, features, false,true);
				fm.cleanAnnotation1(inputs);
				Utils.printConcepts(inputs, splits[splits.length-1], Parameters.OutputDir);
				HashMap<String, HashMap<String, ArrayList<String>>> conceptClusters = cm.findCitations(inputs);
				collect_clusters.add(conceptClusters);
				inputs.clear();
			}
			*/
		}
		//Utils.printTextAnnotations(inputs, splits[splits.length-1], Parameters.OutputDir);
		//HashMap<String, HashMap<String, HashSet<String>>> conceptClusters = cm.findCitations(inputs);
		//HashMap<String, HashMap<String, ArrayList<String>>> conceptClusters = mergeCollect(collect_clusters);
		fm.annotateWithFeatures(inputs, features, false,true);
		fm.cleanAnnotation1(inputs);
		
		String filename = splits[splits.length-1];
		Utils.printConcepts(inputs, filename, Parameters.OutputDir);
		HashMap<String, HashMap<String, ArrayList<String>>> conceptClusters = cm.findCitations(inputs,filename, Parameters.OutputDir);
		inputs.clear();
		for(String concept: conceptClusters.keySet())
		{
			System.out.println("====="+concept+"=====");
			for(String key: conceptClusters.get(concept).keySet())
			{
				System.out.println(key);
				for(String men: conceptClusters.get(concept).get(key))
					System.out.println("\t"+men);
			}
		}
		Clustering clu = new Clustering();
		HashMap<String, ArrayList<ArrayList<String>>> final_clusters = clu.mergeClusters(conceptClusters);
		clu.printClusters(Parameters.OutputDir, filename, final_clusters);
	}
	
	public void evaluate(HashMap<String, HashSet<String>> features)
	{
		System.out.println("---------------------------Testing---------------------------");
		
		HashMap<String, int[]> hp = new HashMap<String, int[]>();
		for(String cat: Parameters.categories)
		{
			hp.put(cat, new int[]{0,0,0});
		}
		HashMap<String, Integer> mis = new HashMap<String, Integer>();
		int maxoverlap, overlap, start, end;
		double precision, recall, f1, avgf1 = 0;
		fm.annotateWithFeatures(predicted, features, false,false);
		//pp.getAcronymsMarked(predicted);
		
		HashMap<String, HashMap<String, Pair<Integer,Integer>>> collect=new HashMap<String, HashMap<String, Pair<Integer,Integer>>>();
		for(int i=0; i<Parameters.categories.length;i++)
			collect.put(Parameters.categories[i], new HashMap<String, Pair<Integer,Integer>>());
		for(int i = 0;i < gold.size();i ++)
		{
			// count the number of concepts in gold annotations
			for(int j=0; j<gold.get(i).concept_tags.size(); j++)
			{
				String label = gold.get(i).concept_tags.get(j);
				int[] tmp = hp.get(label);
				tmp[0]+=1;
				hp.put(label, tmp);
			}

			// count the number of concepts in predicted annotations
			for(int j=0; j<predicted.get(i).concept_tags.size(); j++)
			{
				String label = predicted.get(i).concept_tags.get(j);
				int[] tmp = hp.get(label);
				tmp[1]+=1;
				hp.put(label, tmp);
			}

			// count the number of correct predictions
			for(int j=0; j<gold.get(i).concept_tags.size(); j++)
			{
				int gold_start = gold.get(i).concept_start.get(j);
				int gold_end = gold.get(i).concept_end.get(j);
				String gold_tag = gold.get(i).concept_tags.get(j);
				maxoverlap = 0;
				int mis_overlap=0;
				for(int k=0; k<predicted.get(i).concept_tags.size(); k++)
				{
					int predicted_start = predicted.get(i).concept_start.get(k);
					int predicted_end = predicted.get(i).concept_end.get(k);
					String predicted_tag = predicted.get(i).concept_tags.get(k);
					if(predicted_start > gold_start)
						start = predicted_start;
					else 
						start = gold_start;
					if(predicted_end > gold_end)
						end = gold_end;
					else 
						end = predicted_end;
					if(start > end)
						continue;

					overlap = (end - start);
					if(predicted_tag.equals(gold_tag))
					{
						maxoverlap+=overlap;
					}
					else
					{
						mis_overlap+=overlap;
					}
				}
				if(maxoverlap >= Parameters.MATCHSCORE * (gold_end-gold_start))
				{
					int[] tmp = hp.get(gold_tag);
					tmp[2]+=1;
					hp.put(gold_tag, tmp);
				}
				if(mis_overlap >= Parameters.MATCHSCORE * (gold_end-gold_start))
				{
					if(mis.containsKey(gold_tag))
						mis.put(gold_tag, mis.get(gold_tag)+1);
					else
						mis.put(gold_tag, 1);
				}
			}
		}
		
		for(String concept : hp.keySet())
		{
			int[] counts = hp.get(concept);
			System.out.println("# "+counts[0]+" "+counts[1]+" "+counts[2]);	
			if(counts[1] == 0)
				precision = 0;
			else 
				precision = counts[2]*1.0/counts[1];
			if(counts[0] == 0)
				recall = 0;
			else 
				recall = counts[2]*1.0/counts[0];
			if(precision < 1e-5 || recall < 1e-5)
				f1 = 0;
			else f1 = 2 * precision * recall / (precision + recall); 
			avgf1 += f1;
		
			System.out.printf("%s precision %.4f recall %.4f f1 %.4f\n", concept, precision, recall,f1);
			//System.out.println(mis.get(concept));
		}
		
		System.out.println("Average F1:" + avgf1/hp.size());
	}
	
	public static void main(String[] args) {
		Tester t = new Tester();
		//t.readMeta(args[1]);
		t.predict(args[0], args[1]);
		//t.predict(args[0], args[1]);
	}

}
