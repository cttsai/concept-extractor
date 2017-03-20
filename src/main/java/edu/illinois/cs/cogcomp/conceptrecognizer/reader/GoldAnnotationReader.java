package edu.illinois.cs.cogcomp.conceptrecognizer.reader;

import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GoldAnnotationReader 
{
	List<String> lines;
	int linecounter;
	
	public GoldAnnotationReader(String file)
	{
		System.out.println("Reading test data:"+file);
		try {
			lines = LineIO.read(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		linecounter = 0;
		System.out.println("finish reading test");
	}
	
	public List<MyTextAnnotation> getMyTextAnnotations()
	{
		Stack<String> st = new Stack<String>();
		List<MyTextAnnotation> tas = new ArrayList<MyTextAnnotation>();
		
		for(int i = 0;i < lines.size();i ++)
			if(lines.get(i).startsWith("<abstract>"))
			{
				st.clear();
				String abstracts = lines.get(i);
				abstracts = abstracts.replace("<abstract>", "");
				abstracts = abstracts.replace("</abstract>", "");
				if(abstracts.trim().isEmpty()) continue;
				int curOffset = 0;
				List<Integer> starts = new ArrayList<Integer>();
				List<Integer> ends = new ArrayList<Integer>();
				List<String> labels = new ArrayList<String>();
				
				List<Integer> cstarts = new ArrayList<Integer>();
				List<Integer> cends = new ArrayList<Integer>();
				List<String> clabels = new ArrayList<String>();
				
				String newStr = new String();
				
				for(int j = 0; j < abstracts.length();j ++)
				{
					//System.out.println(abstracts);
					if(j < abstracts.length() - 1  && abstracts.charAt(j) == '[' && (abstracts.charAt(j + 1) == 'T' || abstracts.charAt(j + 1) == 'P' || abstracts.charAt(j + 1) == 'A' || abstracts.charAt(j + 1) == 'F'))
					{
						//System.out.println("xx");
						st.push("" + abstracts.charAt(j + 1));
						st.push("" + curOffset);
						j +=2;
						while(j < abstracts.length() && abstracts.charAt(j) == ' ')
							j++;
						j--;
					}
					else if(j < abstracts.length() - 1 && abstracts.charAt(j + 1) == ']' && (abstracts.charAt(j) == 'T' || abstracts.charAt(j) == 'P' || abstracts.charAt(j) == 'A' || abstracts.charAt(j) == 'F'))
					{
						starts.add(Integer.parseInt(st.pop()));
						ends.add(curOffset);
						labels.add(st.pop());
						//System.out.println("yy"+labels.get(labels.size()-1));
						j+=2;
						while(j < abstracts.length() && abstracts.charAt(j) == ' ')
							j++;
						j--;
					}
					else if(j < abstracts.length())
					{
						curOffset++;
						newStr = newStr + abstracts.charAt(j);
						//System.out.println(newStr);
					}
				}
				
				MyTextAnnotation ta = new MyTextAnnotation(newStr);
				
				for(int j = 0;j < starts.size();j ++)
				{
					cstarts.add(ta.getTokenIdFromCharacterOffset(starts.get(j)));
					cends.add(ta.getTokenIdFromCharacterOffset(ends.get(j)));
					if(labels.get(j).equals("T"))
					{
						clabels.add("Technique");
					}
					else if(labels.get(j).equals("A")) 
					{
						clabels.add("Application");
					}
					else System.out.println("wtf:" + labels.get(j));
				}
				ta.setConcepts(cstarts, cends, clabels);
				tas.add(ta);
			}
		System.out.println("finished get gold annotations");
		return tas;
	}
	
	public static void main(String[] args)
	{
		GoldAnnotationReader gar = new GoldAnnotationReader(args[0]);
		gar.getMyTextAnnotations();
	}
}
