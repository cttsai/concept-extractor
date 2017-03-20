package edu.illinois.cs.cogcomp.conceptrecognizer.core;

import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;
import edu.illinois.cs.cogcomp.conceptrecognizer.Parameters;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CitationManager {

	HashMap<String, ArrayList<String>> outGoing;
	HashMap<String, ArrayList<String>> inComing;
	HashMap<String, ArrayList<String>> paperInfo;
	HashMap<String, HashMap<String, ArrayList<String>>> citedConcepts;
	FeatureManager fm;
	
	public CitationManager()
	{
		fm = new FeatureManager();
		//readNetworkACL();
		//readMetaACL();
	}
	
	
	public ArrayList<String> getCitingPaperACM(int endspan, TextAnnotation ta, HashMap<String,String> refs)
	{
		ArrayList<String> papers=new ArrayList<String>();
		
		String pattern="\\w{1,2}\\b";
		Pattern p = Pattern.compile(pattern);
	
		int doc_len=ta.getTokens().length;
		int end=endspan-1;
		if(end-1>=0 && ta.getToken(end-1).startsWith("["))
				end--;
		else if(ta.getToken(end).startsWith("["))
				end=end;
		else if(doc_len>end+1 && ta.getToken(end+1).startsWith("["))
				end++;
		else
				end=doc_len;
		String tmp="";
		while(end<doc_len)
		{
			tmp=tmp+ta.getToken(end)+" ";
			if(ta.getToken(end).endsWith("]"))
				break;
			end++;
		}
		if(end==doc_len) 
			return papers;
		Matcher m = p.matcher(tmp);
		while(m.find())
		{
			String citenum=tmp.substring(m.start(), m.end()).trim();
			System.out.println(citenum);
			if(refs.containsKey(citenum))
				papers.add(refs.get(citenum));
		}		
		return papers;
	}

	public ArrayList<String> getCitingPaper2(int endspan, MyTextAnnotation ta)
	{
		ArrayList<String> papers=new ArrayList<String>();
		
		String pattern="[\\w\\.\\s]+\\s,\\s\\d{4}\\b";
		Pattern p = Pattern.compile(pattern);
	
		int doc_len=ta.words.size();
		int end=endspan-1;
		if(end-1>=0 && ta.getToken(end-1).startsWith("("))
				end--;
		else if(ta.getToken(end).startsWith("("))
				end=end;
		else if(doc_len>end+1 && ta.getToken(end+1).startsWith("("))
				end++;
		else
				end=doc_len;
		String tmp="";
		while(end<doc_len)
		{
			tmp=tmp+ta.getToken(end)+" ";
			if(ta.getToken(end).endsWith(")"))
				break;
			end++;
		}
		if(end==doc_len) 
			return papers;
		Matcher m = p.matcher(tmp);
		while(m.find())
		{
			String citeStr=tmp.substring(m.start(), m.end()).trim();
			int idx=citeStr.indexOf(",");
			String year=citeStr.substring(idx+1).trim();
			int idx1=citeStr.indexOf(" ");
			String lastname=null;
			if(idx1<idx)
				lastname=citeStr.substring(0,idx1).trim();
			else
				lastname=citeStr.substring(0,idx).trim();
			if(!lastname.toLowerCase().equals(lastname))
				papers.add(lastname+","+year);
		}		
		return papers;
	}
	
	public ArrayList<String> getCitingPaper(int endspan, TextAnnotation ta, String paper_name)
	{
		ArrayList<String> papers=new ArrayList<String>();
		ArrayList<String> references=null;
		if(outGoing.containsKey(paper_name))
			references = outGoing.get(paper_name);
		else
			return papers;
		
		String pattern="[\\w\\.\\s]+\\s,\\s\\d{4}\\b";
		Pattern p = Pattern.compile(pattern);
	
		int doc_len=ta.getTokens().length;
		int end=endspan-1;
		if(end-1>=0 && ta.getToken(end-1).startsWith("("))
				end--;
		else if(ta.getToken(end).startsWith("("))
				end=end;
		else if(doc_len>end+1 && ta.getToken(end+1).startsWith("("))
				end++;
		else
				end=doc_len;
		String tmp="";
		while(end<doc_len)
		{
			tmp=tmp+ta.getToken(end)+" ";
			if(ta.getToken(end).endsWith(")"))
				break;
			end++;
		}
		if(end==doc_len) 
			return papers;
		Matcher m = p.matcher(tmp);
		//System.out.println("orig: "+tmp);
		while(m.find())
		{
			String citeStr=tmp.substring(m.start(), m.end()).trim();
			//System.out.println(citeStr);
			int idx=citeStr.indexOf(",");
			String year=citeStr.substring(idx+1).trim();
			int idx1=citeStr.indexOf(" ");
			String lastname=null;
			if(idx1<idx)
				lastname=citeStr.substring(0,idx1).trim();
			else
				lastname=citeStr.substring(0,idx).trim();
			int j;
			for(j=0; j<references.size(); j++)
			{
				ArrayList<String> info=null;
				if(paperInfo.containsKey(references.get(j)))
					info=paperInfo.get(references.get(j));
				else
					continue;
				if(info.get(0).equals(lastname) &&info.get(1).equals(year))
				{
					papers.add(references.get(j));
					break;
				}												
			}
			if(j==references.size())
				papers.add(lastname+","+year);
		}		
		return papers;
	}
	
	public String filterMentionString(String mention)
	{
		String[] words=mention.split("\\s");
		String result = "";
		if(words.length > 4)
			return result;
		for(String word: words)
		{
			if(!word.matches("\\d+") && !fm.stopWords.contains(word))
				result+=" "+word;
		}
		return result.trim();
	}
	
	public HashMap<String, HashMap<String, HashSet<String>>> findCitations(List<TextAnnotation> ta, String ref_filename)
	{
		ArrayList<HashMap<String, String>> refs = readReferences(ref_filename);
		if(refs.size()!=ta.size())
			System.out.println("In findCitations: size error "+refs.size()+" "+ta.size());
		
		HashMap<String, HashMap<String, HashSet<String>>> citedConcepts = new HashMap<String, HashMap<String, HashSet<String>>>();
		HashMap<String, ArrayList<String>> uncited=new HashMap<String, ArrayList<String>>();
		for(int i=0; i< Parameters.categories.length; i++)
		{
			citedConcepts.put(Parameters.categories[i], new HashMap<String, HashSet<String>>());
			uncited.put(Parameters.categories[i], new ArrayList<String>());
		}
		for(int i=0; i<ta.size(); i++)
		{
			SpanLabelView spv = (SpanLabelView)ta.get(i).getView("Concept");
			for(Constituent c : spv.getConstituents())
			{
				String mention = filterMentionString(c.getSurfaceString());
				if(mention.trim().isEmpty())
					continue;
				String concept=c.getLabel();

				ArrayList<String> citingpapers=getCitingPaperACM(c.getEndSpan(),ta.get(i),refs.get(i));
				//ArrayList<String> citingpapers=getCitingPaper(c.getEndSpan(),ta.get(i),meta.get(i));
				if(citingpapers.size()==0)
				{
					ArrayList<String> tmp=uncited.get(concept);
					tmp.add(mention);
					uncited.put(concept, tmp);
				}
				for(int j=0; j<citingpapers.size(); j++)
				{
					String citingname=citingpapers.get(j);
					HashMap<String, HashSet<String>> tmp=citedConcepts.get(concept);
					HashSet<String> hs=null;
					if(tmp.containsKey(citingname))
						hs=tmp.get(citingname);
					else
						hs=new HashSet<String>();
					hs.add(mention.toLowerCase());
					tmp.put(citingname, hs);
					citedConcepts.put(concept, tmp);
				}							
			}
			ta.set(i, null);
		}
	
		
/*		HashMap<String, HashMap<String, HashSet<String>>> newUncited = new HashMap<String, HashMap<String, HashSet<String>>>();
		for(String c: uncited.keySet())
		{
			HashMap<String, HashSet<String>> paper_mention = new HashMap<String, HashSet<String>>();
			for(int i=0; i< uncited.get(c).size(); i++)
			{
				HashSet<String> tmp = new HashSet<String>();
				tmp.add(uncited.get(c).get(i));
				paper_mention.put(Integer.toString(i), tmp);
			}
			newUncited.put(c, paper_mention);
		}*/
		System.out.println("Finished grouping concepts by citation context");
		return citedConcepts;
	}
	
	public ArrayList<HashMap<String, String>> readReferences(String ref_filename)
	{
		ArrayList<HashMap<String, String>> refs = new ArrayList<HashMap<String,String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(ref_filename));
			String text = br.readLine();
			while(text!=null)
			{
				HashMap<String,String> map = new HashMap<String,String>();
				String[] tokens = text.split(" ");
				for(String t: tokens)
				{
					String[] ts = t.split(":");
					if(ts.length>1)
					{
						map.put(ts[0], ts[1]);
					}
				}
				refs.add(map);
				text=br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return refs;
	}
	//public HashMap<String, HashMap<String, HashSet<String>>> findCitations(List<MyTextAnnotation> tas)
	public HashMap<String, HashMap<String, ArrayList<String>>> findCitations(List<MyTextAnnotation> tas, String filename, String outputdir)
	{
		
	//	HashMap<String, HashMap<String, HashSet<String>>> citedConcepts = new HashMap<String, HashMap<String, HashSet<String>>>();
		HashMap<String, HashMap<String, ArrayList<String>>> citedConcepts = new HashMap<String, HashMap<String, ArrayList<String>>>();
		HashMap<String, ArrayList<String>> uncited=new HashMap<String, ArrayList<String>>();
		BufferedWriter bw_app = null;
		BufferedWriter bw_tech = null;
		try {
			bw_app = new BufferedWriter(new FileWriter(outputdir+filename+".mentions.cited.app"));
			bw_tech = new BufferedWriter(new FileWriter(outputdir+filename+".mentions.cited.tech"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<Parameters.categories.length; i++)
		{
			//citedConcepts.put(Parameters.categories[i], new HashMap<String, HashSet<String>>());
			citedConcepts.put(Parameters.categories[i], new HashMap<String, ArrayList<String>>());
			uncited.put(Parameters.categories[i], new ArrayList<String>());
		}
		for(int i=0; i<tas.size(); i++)
		{
			MyTextAnnotation ta = tas.get(i);
			String str_app="";
			String str_tech="";
			for(int j=0; j<ta.concept_start.size(); j++)
			{
				String mention = filterMentionString(ta.getConceptString(j)).toLowerCase();
				if(mention.trim().isEmpty())
					continue;
				String concept=ta.concept_tags.get(j);

				ArrayList<String> citingpapers=getCitingPaper2(ta.concept_end.get(j),ta);
				//ArrayList<String> citingpapers=getCitingPaper(c.getEndSpan(),ta.get(i),meta.get(i));
				if(citingpapers.size()==0)
				{
					ArrayList<String> tmp=uncited.get(concept);
					tmp.add(mention);
					uncited.put(concept, tmp);
				}
				
				if(citingpapers.size()>0)
				{
					if(concept.equals("Technique"))
					{
						if(str_tech.isEmpty())
							str_tech+=mention;
						else
							str_tech+=";"+mention;
					}
					else
					{
						if(str_app.isEmpty())
							str_app+=mention;
						else
							str_app+=";"+mention;
					}
				}
				
				for(int k=0; k<citingpapers.size(); k++)
				{
					String citingname=citingpapers.get(k);
					//HashMap<String, HashSet<String>> tmp=citedConcepts.get(concept);
					HashMap<String, ArrayList<String>> tmp=citedConcepts.get(concept);
					//HashSet<String> hs=null;
					ArrayList<String> hs=null;
					if(tmp.containsKey(citingname))
						hs=tmp.get(citingname);
					else
						//hs=new HashSet<String>();
						hs=new ArrayList<String>();
					hs.add(mention);
					tmp.put(citingname, hs);
					citedConcepts.put(concept, tmp);
					
					if(concept.equals("Technique"))
						str_tech+="|"+citingname;
					else
						str_app+="|"+citingname;
				}							
			}
			try {
				bw_tech.write(str_tech+"\n");
				bw_app.write(str_app+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bw_tech.close();
			bw_app.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finished grouping concepts by citation context");
		return citedConcepts;
	}
	
	public static void main(String[] args)
	{
		CitationManager cm=new CitationManager();
	}
	
}

