package edu.illinois.cs.cogcomp.conceptrecognizer.core;

import edu.illinois.cs.cogcomp.conceptrecognizer.*;
import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BootStrapper 
{
	FeatureManager fm;
	Propagator pp;
	Tester t;
	HashMap<String, HashSet<String>> allFeatures;
	
	public BootStrapper()
	{
		fm = new FeatureManager();
		pp = new Propagator();
		t=new Tester();
	}
	
	public void initializeFeatureSeeds(List<MyTextAnnotation> lt)
	{
		allFeatures = new HashMap<String, HashSet<String>>();
		allFeatures.put("Technique", Parameters.techACLSeeds);
		allFeatures.put("Application", Parameters.appACLSeeds);
		
		for(int i = 0;i < lt.size();i ++)
		{
			MyTextAnnotation ta = lt.get(i);

			List<String> concept_tags = new ArrayList<String>();
			List<Integer> concept_start = new ArrayList<Integer>();
			List<Integer> concept_end = new ArrayList<Integer>();
			
			boolean match = false;
			for(int j=0; j<ta.chunk_start.size(); j++)
			{
				String chunk_tag = ta.chunk_tags.get(j);
				int chunk_start = ta.chunk_start.get(j);
				int chunk_end = ta.chunk_end.get(j);
				if(!chunk_tag.startsWith("NP"))
					continue;
				String concept = new String();
				match = false;
				List<String> extractedFeatures = fm.extractFeatures(ta, j);
				for(String s : Parameters.categories)
				{
					HashSet<String> seedset = allFeatures.get(s);
					for(int k = 0; k < extractedFeatures.size();k ++)
						if(seedset.contains(extractedFeatures.get(k)) && match==false)
						{
							match = true;
							concept = s;
						}
				}
				if(match)
				{
					concept_start.add(chunk_start);
					concept_end.add(chunk_end);
					concept_tags.add(concept);
					if(concept.equals("Technique"))
						fm.t_num++;
					else if(concept.equals("Application"))
						fm.a_num++;
				}
			}
			ta.setConcepts(concept_start, concept_end, concept_tags);
		}
		System.out.println("Initialize: "+fm.t_num+" techniques and "+fm.a_num+" applications");
	}
	
	
	public void bootstrap(List<MyTextAnnotation> tas, String output_file)
	{
		int sentence=0;
		for(int i=0;i<tas.size();i++)
			sentence+=tas.get(i).sentence_start.size();
		System.out.println("#Documents:"+tas.size()+" #Sentences:"+sentence);
		
		initializeFeatureSeeds(tas);
		//pp.getConceptsPropagated(tas);

		for(int i = 0;i < Parameters.NUM_TRAIN_ROUNDS;i ++)
		{
			System.out.println("---------------------iter#:"+i+"-----------------------");
			fm.annotateWithFeatures(tas, allFeatures,true,false);
			//pp.getConceptsPropagated(tas);
			fm.getSelectedFeatures(tas, allFeatures);
			t.evaluate(allFeatures);
		}
		Utils.save(Parameters.OutputDir + output_file + ".lc", allFeatures);
		fm.cleanAnnotation(tas);
		Utils.printTextAnnotations(tas, output_file, Parameters.OutputDir);
	}
	
	
	public static void main(String[] args)
	{
		
		//InputReader adr = new InputReader();
		//BootStrapper bsp = new BootStrapper();
		//bsp.bootstrap(adr, args[0], Constants.featureDir,Constants.dataDir);
	}
}
