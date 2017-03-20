package edu.illinois.cs.cogcomp.conceptrecognizer;

import edu.illinois.cs.cogcomp.conceptrecognizer.core.BootStrapper;
import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;
import edu.illinois.cs.cogcomp.conceptrecognizer.reader.InputReader;

import java.util.List;

public class GetConcepts {
	public static void main(String[] args)
	{
		String input = "/Users/ctsai12/Downloads/conceptExtraction_data/abstracts";
		InputReader reader = new InputReader();
		List<MyTextAnnotation> input_files = reader.getMyTextAnnotations(input);
		BootStrapper bsp = new BootStrapper();
		String[] splits = input.split("/");
		String inputfilename = splits[splits.length-1];
		bsp.bootstrap(input_files, inputfilename);
	}
}