package edu.illinois.cs.cogcomp.conceptrecognizer.reader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
//import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
//import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
//import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
//import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
//import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;

public class ACMXMLtoText 
{
	
	public ACMXMLtoText()
	{
	}
	
	public void getTextAnnotations() 
	{
        
		File folder = new File("/shared/corpora/acmdl/proceeding");
		for(File xmlFile: folder.listFiles())
		{
			System.out.println("processing "+xmlFile.getName()+"...");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(false);
			try {
				dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			}

			DocumentBuilder dBuilder;
			try {

				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(xmlFile);
				doc.getDocumentElement().normalize();			
				NodeList articles = doc.getElementsByTagName("article_rec");
				int abs_count=0;
				int body_count=0;
				int title_count=0;
				BufferedWriter bw_abs = new BufferedWriter(new FileWriter("ACM/"+xmlFile.getName()+".abs"));
				BufferedWriter bw_full = new BufferedWriter(new FileWriter("ACM/"+xmlFile.getName()+".full"));
				BufferedWriter bw_ref = new BufferedWriter(new FileWriter("ACM/"+xmlFile.getName()+".ref"));
				for(int i=0; i< articles.getLength(); i++)
				{
					Node article = articles.item(i);
					Element e = (Element) article;
					String abs="";
					if(e.getElementsByTagName("abstract").getLength()>0)
					{
						abs=e.getElementsByTagName("abstract").item(0).getTextContent();
						abs_count++;					
					}
					//else
					//	continue;
					abs=abs.trim();
					abs=abs.replaceAll("<p>", "");
					abs=abs.replaceAll("</p>", "");
					abs=abs.replaceAll("\n", "");
					abs=removeXmlTags(abs);
					String t="";
					if(e.getElementsByTagName("title").getLength()>0)
					{
						t =e.getElementsByTagName("title").item(0).getTextContent();
						if(e.getElementsByTagName("subtitle").getLength()>0)
							t += " "+e.getElementsByTagName("subtitle").item(0).getTextContent();
						title_count++;					
					}
					t=t.trim();
					t=t.replaceAll("\n", "");
					if(!abs.isEmpty() && !t.isEmpty())
						bw_abs.write(t+". "+abs+"\n");
					
					NodeList ref = e.getElementsByTagName("ref");
					String ref_string ="";
					for(int j=0; j<ref.getLength(); j++)
					{
						Element cited = (Element)ref.item(j);
						String num =cited.getElementsByTagName("ref_seq_no").item(0).getTextContent();
						String text =cited.getElementsByTagName("ref_text").item(0).getTextContent();
						String[] tokens = text.split("[a-z]\\.");
						if(tokens.length<1)
							System.out.println("x1 "+text);
						String[] tokens1 = tokens[0].split("and");
						if(tokens1.length<1)
							System.out.println("x2 "+text);
						String[] tokens2 = tokens1[0].split(",");
						if(tokens2.length<1)
							System.out.println("x3 "+text);
						String[] tokens3 = tokens2[0].split(" ");
						if(tokens3.length<1)
							System.out.println("x4 "+text);
						
						String year=text.substring(text.length()-5, text.length()-1);
						ref_string+=num+":"+tokens3[tokens3.length-1]+"-"+year+" ";
						//System.out.println(num+" "+tokens3[tokens3.length-1]+" "+year);
					}
					
					
					
					
					String body="";
					if(e.getElementsByTagName("ft_body").getLength()>0)
					{
						body=e.getElementsByTagName("ft_body").item(0).getTextContent();
						body_count++;					
					}
					body=body.trim();
					body=body.replaceAll("\n", "");
					if(!body.isEmpty())
					{
						bw_full.write(body+"\n");
						bw_ref.write(ref_string+"\n");
					}
				}
				bw_full.close();
				bw_abs.close();
				bw_ref.close();
				System.out.println("abs count:"+abs_count+" title count:"+title_count+" body count:"+body_count);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

        
        
	}
	
	public String removeXmlTags(String str) {
		return str.replaceAll("\\<.*?\\>", "").trim();
	}


	
	public static void main(String[] args)
	{
		ACMXMLtoText adr = new ACMXMLtoText();
		adr.getTextAnnotations();
	}
	
}
