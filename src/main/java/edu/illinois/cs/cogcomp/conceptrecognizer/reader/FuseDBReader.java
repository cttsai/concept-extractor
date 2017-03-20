package edu.illinois.cs.cogcomp.conceptrecognizer.reader;

/*import org.bson.types.ObjectId;
import com.sri.fuse.api.repository.entity.Document;
import com.sri.fuse.api.repository.query.mongo.QueryFactoryImpl;
import com.sri.fuse.util.DataSourceIds;
import com.sri.fuse.util.LanguageIds;*/


public class FuseDBReader 
{
	int processed_total;
	boolean endflag;
	
	public FuseDBReader()
	{
		processed_total = 0;
		endflag = false;
	}
	
	/*public List<TextAnnotation> getTextAnnotations(String inputfile, int max_abs) 
	{
		// TODO Auto-generated method stub
        List<TextAnnotation> lt = new ArrayList<TextAnnotation>();
        CuratorClient cc = new CuratorClient(Parameters.getCuratorHost(), Parameters.curatorPort,false);
        long start = System.currentTimeMillis();

        
        int curline = 0;
        int curatorlinecount = 0;
        int bad_count=0;
		QueryFactoryImpl q = new QueryFactoryImpl();
		Iterator<Document<ObjectId>> doc = q.getDocuments().withSource(DataSourceIds.ACL).execute().iterator();
        
		while(doc.hasNext())
		{
			Document<ObjectId> d=doc.next();
	
			String text = null;
			try{
				text = d.getAbstract(LanguageIds.English);
			}
			catch(Exception e)
			{
				continue;
			}
			if(text == null)
				continue;
        	curline ++;
        	if(curline > processed_total)
        	{
                text = text.trim();
                TextAnnotation ta = null;
                text = text.replaceAll("[^\\w\\s\\p{Punct}]", "");

                if(text.isEmpty())
                {
                	System.out.println("Empty Line:"+curline);
                	continue;
                }
                try
                {
                	ta = cc.getTextAnnotation("", "", text, false);
    				cc.addChunkView(ta, false);
            		cc.addPOSView(ta, false);
            		curatorlinecount+=ta.getNumberOfSentences();
                }
                catch(Exception e)
                {
                	//System.out.println(text);
                    bad_count++;
                //    e.printStackTrace();
                    continue;
                }
    			
               
                if(curline % 100 == 0 && curline != 0)
                {
                	long end = System.currentTimeMillis();
                	System.out.println("Processed " + curline + " " + "lines " + "(" + curatorlinecount + " sentences) in " + (end - start)/1000.0 + " " + "seconds");
                }
                lt.add(ta);
            }
        }
        
        System.out.println("End of reading file,"+lt.size());
        ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream("absFuse.obj"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
	//		oos.writeObject(allFeatures);
			oos.writeObject(lt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return lt;
	}
*/

	
	public static void main(String[] args)
	{
		FuseDBReader r = new FuseDBReader();
		//r.getTextAnnotations("", 1);
	
	}
	
	
}
