package edu.illinois.cs.cogcomp.conceptrecognizer.reader;

import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputReader 
{
	public InputReader()
	{
	}
	public List<MyTextAnnotation> getMyTextAnnotations(String inputfile)
	{
		
        List<MyTextAnnotation> lt = new ArrayList<MyTextAnnotation>();
        BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int cnt=0;
        while(true)
        {
        	String text = null;
			try {
				text = br.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(text == null)
				break;
        	text = text.replace("<abstract>", "");
        	text = text.replace("</abstract>", "");
        	text = text.trim();
        	text = text.replaceAll("[^\\w\\s\\p{Punct}]", "");
        	MyTextAnnotation ta = new MyTextAnnotation(text);
        	lt.add(ta);
        	cnt++;
        	if(cnt%100==0)
        		System.out.println(cnt+" documents processed");
        }
        try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("Finished reading input file");
	    System.gc();
	    long usedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
	    System.out.println("memory:"+usedMB);
        return lt;
	}
	
	
	public static void main(String[] args)
	{
	}	
	
}
