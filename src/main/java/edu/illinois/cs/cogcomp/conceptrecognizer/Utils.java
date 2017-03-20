package edu.illinois.cs.cogcomp.conceptrecognizer;

import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Utils {
	
	static boolean begin = true;
	static boolean concept_begin = true;

	public static void printTextAnnotations(List<MyTextAnnotation> tas, String rdgname, String outputdir)
	{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputdir+rdgname+".annotations", !begin));
			if(begin)
				bw.write("<FILES>\n");
			begin = false;
			for(int i = 0;i < tas.size();i ++)
			{
				MyTextAnnotation ta = tas.get(i);
				bw.write("<FILE>\n");
				bw.write("<original_text>" + ta.getTokenizedText() + "</original_text>\n");
				bw.write("<Technique_Application>" + ta.getAnnotatedText() + "</Technique_Application>" + "\n");
				bw.write("</FILE>\n");
				bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void printConcepts(List<MyTextAnnotation> tas, String filename, String outputdir)
	{
		BufferedWriter bw_app = null;
		BufferedWriter bw_tech = null;
		try {
			bw_app = new BufferedWriter(new FileWriter(outputdir+filename+".mentions.app", !concept_begin));
			bw_tech = new BufferedWriter(new FileWriter(outputdir+filename+".mentions.tech", !concept_begin));
			concept_begin = false;
			for(int i = 0;i < tas.size();i ++)
			{
				MyTextAnnotation ta = tas.get(i);
				String out_str_app="";
				String out_str_tech="";
				System.out.println(ta.concept_start.size());
				for(int j=0; j<ta.concept_start.size();j++)
				{
					if(ta.concept_tags.get(j).equals("Technique"))
					{
						if(!out_str_tech.isEmpty())
							out_str_tech+=";"+ta.getConceptString(j);
						else
							out_str_tech+=ta.getConceptString(j);
					}
					else
					{
						if(!out_str_app.isEmpty())
							out_str_app+=";"+ta.getConceptString(j);
						else
							out_str_app+=ta.getConceptString(j);
					}
				}
				bw_app.write(out_str_app+"\n");
				bw_tech.write(out_str_tech+"\n");
				bw_app.flush();
				bw_tech.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void save(String filename,HashMap<String, HashSet<String>> allFeatures)
	{
		System.out.println("Writing to " + filename);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(filename));
			oos.writeObject(allFeatures);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
