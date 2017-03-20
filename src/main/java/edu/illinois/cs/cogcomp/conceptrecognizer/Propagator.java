package edu.illinois.cs.cogcomp.conceptrecognizer;

import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;

import java.util.List;


public class Propagator {

	/**
	 * @param args
	 */
	
	public Propagator()
	{
	}
	
    public boolean toPrune(String ci)
    {  
        if(!ci.matches("[a-zA-Z]+"))
            return true;
        return false;
    }

	
	public void getAppositivesMarked(List<MyTextAnnotation> tas)
	{
		for(int i = 0;i < tas.size();i ++)
		{
			MyTextAnnotation ta = tas.get(i);
			
			List<String> labels = ta.concept_tags;
			List<Integer> cstarts = ta.concept_start;
			List<Integer> cends = ta.concept_end;
			
			
			for(int j=0; j<ta.chunk_start.size(); j++)
			{
				int start = ta.chunk_start.get(j);
				int end = ta.chunk_end.get(j);
				String tag = ta.chunk_tags.get(j);
				
				if(end>start+1 && tag.equals("NP") && start>=2 && ta.getToken(start-1).equals(",") && ta.getConceptCoveringToken(start - 2)!=null && ta.getConceptCoveringToken(start)==null)
				{
					String concept = ta.getConceptCoveringToken(start - 2); 
					if(toPrune(concept))
						continue;
					
					labels.add(concept);
					cstarts.add(start);
					cends.add(end);
				}
			}
			ta.setConcepts(cstarts, cends, labels);
		}
	}

	public void getAcronymsMarked(List<MyTextAnnotation> tas)
	{
		for(int i = 0;i < tas.size();i ++)
		{
			MyTextAnnotation ta = tas.get(i);
		
			List<String> labels = ta.concept_tags;
			List<Integer> cstarts = ta.concept_start;
			List<Integer> cends = ta.concept_end;
			
			for(int j=0; j<ta.chunk_start.size(); j++)
			{
				int start = ta.chunk_start.get(j);
				int end = ta.chunk_end.get(j);
				String label = ta.chunk_tags.get(j);
				
				if(end==start && start>=2 && end<ta.size() && label.equals("NP") && ta.getToken(start-1).equals("(") && ta.getToken(end).equals(")") && ta.getConceptCoveringToken(start-2)!= null && ta.getConceptCoveringToken(start)==null && ta.getChunkString(j).equals(ta.getChunkString(j).toUpperCase()))
				{
					String concept = ta.getConceptCoveringToken(start-2);	
					if(toPrune(concept))
						continue;
					
					labels.add(concept);
					cstarts.add(start);
					cends.add(end);
				}
				ta.setConcepts(cstarts, cends, labels);
			}
		}
	}

	private boolean isAcronym(String acronym, String fulltext) {
		// TODO Auto-generated method stub
		String[] words = fulltext.split("\\s+");
		if(words.length > acronym.length() + 2 || words.length < acronym.length())
			return false;
		String fullacronym = new String();
		
		for(int i = 0;i < words.length;i ++)
			fullacronym = fullacronym + words[i].toUpperCase().charAt(0);
		
		if(fullacronym.contains(acronym))
				return true;
		
		return false;
	}

	public void getConceptsPropagated(List<MyTextAnnotation> tas)
	{
		getAppositivesMarked(tas);
		getAcronymsMarked(tas);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
