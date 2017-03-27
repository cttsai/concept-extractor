package edu.illinois.cs.cogcomp.conceptrecognizer;

import edu.illinois.cs.cogcomp.conceptrecognizer.core.BootStrapper;
import edu.illinois.cs.cogcomp.conceptrecognizer.ds.MyTextAnnotation;
import edu.illinois.cs.cogcomp.conceptrecognizer.reader.InputReader;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GetConcepts {

	public static void annotateDocuments(String dir, String modelfile){

		InputReader reader = new InputReader();
		Tester tester = new Tester();
		HashMap<String, HashSet<String>> features = tester.read(modelfile);

		for(File f: new File(dir).listFiles()){
		    if(f.getName().endsWith("meta"))
		    	continue;

			List<MyTextAnnotation> docs = reader.getMyTextAnnotations(f.getAbsolutePath());

			tester.fm.annotateWithFeatures(docs, features, false,true);
			tester.fm.cleanAnnotation1(docs);

			Utils.printTextAnnotations(docs, f.getName(), Parameters.OutputDir);
		}
	}

	public static void train(){
		String input = "data/abstracts.train";
		InputReader reader = new InputReader();
		List<MyTextAnnotation> input_files = reader.getMyTextAnnotations(input);
		BootStrapper bsp = new BootStrapper();
		String[] splits = input.split("/");
		String inputfilename = splits[splits.length-1];
		bsp.bootstrap(input_files, inputfilename);
	}

	public static void main(String[] args)
	{
	    String test_dir = "/Users/ctsai12/Downloads/conceptExtraction_data/tmp";
	    String model_file = "/Users/ctsai12/Downloads/ConceptExtractor/output/abstracts.lc";

	    annotateDocuments(test_dir, model_file);


	}
}