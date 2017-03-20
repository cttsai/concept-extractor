package edu.illinois.cs.cogcomp.conceptrecognizer.ds;

import edu.illinois.cs.cogcomp.lbj.chunk.Chunker;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.util.ArrayList;
import java.util.List;



public class MyTextAnnotation {
	
	public List<Integer> sentence_start;
	public List<Integer> sentence_end;
	public List<Integer> chunk_start;
	public List<Integer> chunk_end;
	public List<String> chunk_tags;
	public List<Integer> concept_start;
	public List<Integer> concept_end;
	public List<String> concept_tags;
	public List<Word> words;


	public MyTextAnnotation(String text)
	{
		Chunker chunker= new Chunker();
		String[] input = new String[1];
		input[0]=text;
		Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(input)));
		
		sentence_start=new ArrayList<Integer>();
		sentence_end=new ArrayList<Integer>();
		chunk_start=new ArrayList<Integer>();
		chunk_end=new ArrayList<Integer>();
		chunk_tags=new ArrayList<String>();
		concept_start=new ArrayList<Integer>();
		concept_end=new ArrayList<Integer>();
		concept_tags=new ArrayList<String>();
		words = new ArrayList<Word>();
		

		String previous = "";
		int cnt=0;
		sentence_start.add(0);
		for (Word w = (Word) parser.next(); w != null; w = (Word) parser.next()) 
		{
			String prediction = chunker.discreteValue(w);
			if(w.form.equals("(") || w.form.equals(")") || w.form.equals("[") || w.form.equals("]"))
				prediction = "O";
			if (prediction.startsWith("B-") ||
					prediction.startsWith("I-") &&
					(previous.length()<2 || !previous.endsWith(prediction.substring(1))))
			{
				//System.out.print("[" + prediction.substring(2) + " ");
				chunk_start.add(cnt);
				chunk_tags.add(prediction.substring(2));
			}
			words.add(w);
			//System.out.print("(" + w.partOfSpeech + " " + w.form + ") ");
			if (!prediction.equals("O")&&
					(w.next == null||
					((Word)(w.next)).form.equals("(") ||
					((Word)(w.next)).form.equals(")") ||
					((Word)(w.next)).form.equals("[") ||
					((Word)(w.next)).form.equals("]") ||
					chunker.discreteValue(w.next).equals("O")||
					chunker.discreteValue(w.next).startsWith("B-")||
					!chunker.discreteValue(w.next).endsWith(prediction.substring(1))))
			{
				chunk_end.add(cnt+1);
				//System.out.print("] ");
			}
			if (w.next == null)
			{
				//System.out.println();
				sentence_start.add(cnt+1);
			}
			previous = prediction;
		cnt++;
		}
		
		if(chunk_start.size()!=chunk_end.size())
		{
			System.out.println("Size error:"+chunk_start.size()+","+chunk_end.size());
			for(int i=0; i<chunk_start.size() && i<chunk_end.size(); i++)
				System.out.println(chunk_start.get(i)+" "+chunk_end.get(i)+" "+chunk_tags.get(i));
			parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(input)));
			previous = "";
			cnt=0;
			System.out.println(text);
			for (Word w = (Word) parser.next(); w != null; w = (Word) parser.next()) 
			{
				String prediction = chunker.discreteValue(w);
				if(w.form.equals("(") || w.form.equals(")"))
					prediction = "O";
				if (prediction.startsWith("B-")|| prediction.startsWith("I-")&& (previous.length()<2 || !previous.endsWith(prediction.substring(1))))
				{
					System.out.print("[" + prediction.substring(2) + " ");
				}
				System.out.print("(" + prediction+" "+w.partOfSpeech + " " + w.form + " "+cnt+") ");
				if (!prediction.equals("O")&& 
						(w.next == null|| 
						((Word)(w.next)).form.equals("(") || 
						((Word)(w.next)).form.equals(")") ||
						chunker.discreteValue(w.next).equals("O")|| 
						chunker.discreteValue(w.next).startsWith("B-")|| !chunker.discreteValue(w.next).endsWith(prediction.substring(1))))
				{
					System.out.print("] ");
				}
				if (w.next == null)
				{
					System.out.println();
				}
				previous = prediction;
				cnt++;
			}
			
		}
		chunker=null;
		parser=null;
	}
	
	public void setConcepts(List<Integer> start, List<Integer> end, List<String> tags)
	{
		if(start.size()!=end.size())
			System.out.println("Size error!!!");
		this.concept_start=start;
		this.concept_end=end;
		this.concept_tags=tags;
	}
	
	public int getSentenceIndex(int token_idx)
	{
		for(int i=sentence_start.size()-2; i>=0; i--)
		{
			if(token_idx >= sentence_start.get(i))
				return sentence_start.get(i);
		}
		return -1;
	}
	
	public int getPreviousNPEndIndex(int chunk_idx)
	{
		for(int i=chunk_idx-1; i>=0; i--)
		{
			if(chunk_tags.get(i).startsWith("NP"))
			{
				return chunk_end.get(i);
			}
		}
		return -1;
	}
	
	public String getToken(int idx)
	{
		return words.get(idx).form;
	}
	
	public String getPOSTag(int idx)
	{
		return words.get(idx).partOfSpeech;
	}
	
	public int getTokenIdFromCharacterOffset(int offset)
	{
		for(int i=0; i<words.size(); i++)
		{
			if(words.get(i).start<=offset && offset<=words.get(i).end)
				return i;
		}
		return -1;		
	}
	
	public int getChunkIdByConceptId(int id)
	{
		int start = concept_start.get(id);
		int end =concept_end.get(id);
		for(int i=0; i<chunk_start.size(); i++)
		{
			if(start>=chunk_start.get(i) && end<=chunk_end.get(i))
				return i;
		}
		return -1;
	}
	
	public String getConceptCoveringToken(int idx)
	{
		for(int i=0; i<concept_tags.size(); i++)
		{
			if(concept_start.get(i)<=idx && idx<concept_end.get(i))
				return concept_tags.get(i);
		}
		return null;
	}
	
	public String getChunkString(int idx)
	{
		String surface="";
		for(int i=chunk_start.get(idx); i<chunk_end.get(idx); i++)
			surface+=getToken(i)+" ";
		return surface.trim();
	}
	
	public String getConceptString(int idx)
	{
		String surface="";
		for(int i=concept_start.get(idx); i<concept_end.get(idx); i++)
			surface+=getToken(i)+" ";
		return surface.trim();
	}
	
	public String getTokenizedText()
	{
		String out="";
		for(int i=0; i<words.size(); i++)
			out+=words.get(i).form+" ";
		return out.trim();
	}
	
	public String getAnnotatedText()
	{
		String out="";
		for(int i=0; i<words.size(); i++)
		{
			for(int j=0; j<concept_start.size(); j++)
				if(i==concept_start.get(j))
				{
					out+="["+concept_tags.get(j)+" ";
					break;
				}
			out+=getToken(i)+" ";
			for(int j=0; j<concept_end.size(); j++)
				if(i==concept_end.get(j)-1)
				{
					out+="] ";
					break;
				}
		}
		return out;
	}
	
	public int size()
	{
		return sentence_start.get(sentence_start.size()-1);
	}

	public static void main(String[] args){
	}

}
