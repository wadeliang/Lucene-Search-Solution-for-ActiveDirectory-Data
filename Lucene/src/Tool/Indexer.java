package Tool;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	public static boolean isFirstTimeIndex = true;
	
	public static void main(String[] args) throws Exception
	{
		File indexDir = new File("Index");
		File dataDir = new File("Data");
		
		long start = new Date().getTime();
		
		//先删除索引文件，然后在跟新索引
		
		
		int numberIndexed =  Index(indexDir,dataDir);
		
		long end = new Date().getTime();
		
		System.out.println("Indexing " +numberIndexed +" files took " + (end-start) + "milliseconds");
		
        Directory indexDirectory = FSDirectory.open(indexDir);
		IndexWriter writer = new IndexWriter(indexDirectory,new StandardAnalyzer(Version.LUCENE_CURRENT),false,IndexWriter.MaxFieldLength.LIMITED);
		writer.close();
		
		//Returns an IndexReader reading the index in the given Directory.
		//You should pass readOnly=true, since it gives much better concurrent performance
		//unless you intend to do write operations (delete documents or change norms) with the reader. 
		IndexReader indexReader = IndexReader.open(FSDirectory.open(indexDir),true);
		IndexSearcher indexSearch = new IndexSearcher(indexReader);
		
		//删除文档操作
		//System.out.println(indexReader.maxDoc());  //下一个可以得到的文档内部编号,所有的文档编号都是从0开始计算
		//System.out.println(indexReader.numDocs()); //索引中的文档数量
		
        
		
		
		//indexReader.deleteDocument(1);
		//indexReader.deleteDocuments(new Term("filename","*.txt*"));
		
		//indexReader.undeleteAll();
		
		//indexReader.clone();
		
		System.out.println("maxDoc():"+indexReader.maxDoc()); //没有进行任何处理
		System.out.println("numDocs():"+indexReader.numDocs()); //numDocs可以立即发现索引被删除
	}
	
	@SuppressWarnings("deprecation")
	public static int Index(File indexFolder,File dataFolder) throws IOException
	{
		
		
		if(!dataFolder.exists()||!dataFolder.isDirectory())
		{
			throw new IOException(dataFolder +"does not exist or is not a directory");
		}
		
		
		Directory indexDirectory = FSDirectory.open(indexFolder);
		try
		{
		SegmentInfos infos = new SegmentInfos();
		infos.read(indexDirectory);
		
		System.out.println("info version:"+infos.getVersion());
		System.out.println("info Counter:"+infos.counter);
		System.out.println("info Seg Count:" + infos.size());
		
		isFirstTimeIndex = false;
		
		for (int i = 0; i < infos.size(); i++) {  
            SegmentInfo info = infos.info(i);
            System.out.println("****************** segment [" + i + "]");  
            System.out.println("segment name:" + info.name);  
            System.out.println("the doc count in segment:" + info.docCount);  
            System.out  
                    .println("del doc count in segment:" + info.getDelCount());  
            System.out.println("segment doc store offset:"  
                    + info.getDocStoreOffset());  
            if (info.getDocStoreOffset() != -1) {  
                System.out.println("segment's DocStoreSegment:"  
                        + info.getDocStoreSegment());  
                System.out.println("segment's DocStoreIsCompoundFile:"  
                        + info.getDocStoreIsCompoundFile());  
            }  
            System.out.println("segment  IsCompoundFile :"  
                    + info.getDocStoreIsCompoundFile());  
            System.out.println("segment's delcount:" + info.getDelCount());  
            System.out.println("segment's is hasprox:" + info.getHasProx());  
            Map infodiag = info.getDiagnostics();  
            Iterator keyit = infodiag.keySet().iterator();  
            while (keyit.hasNext()) {  
                String key = keyit.next().toString();  
                System.out.println("Diagnostic key:" + key  
                        + " Diagnostic value:" + infodiag.get(key));  
            }  
            Map userdatas = infos.getUserData();  
            Iterator datait = userdatas.keySet().iterator();  
            while(datait.hasNext()){  
                String key = datait.next().toString();  
                System.out.println("user data key:"+key+" value:"+userdatas.get(key));  
            }  
		}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isFirstTimeIndex = true;
		}
		
		IndexWriter writer;
		
		//创建IndexWriter对象，准备把索引文件写入IndexFolder文件夹
		//create - true to create the index or overwrite the existing one; false to append to the existing index
		if(isFirstTimeIndex){
		writer = new IndexWriter(indexDirectory
				,new StandardAnalyzer(Version.LUCENE_CURRENT)
		        ,true
		        ,IndexWriter.MaxFieldLength.LIMITED);
		writer.setUseCompoundFile(true);
		writer.setMergeFactor(10);
		writer.setMaxMergeDocs(10);
		writer.setMaxBufferedDocs(10);
		}
		else
		{
	        writer = new IndexWriter(indexDirectory
					,new StandardAnalyzer(Version.LUCENE_CURRENT)
			        ,false
			        ,IndexWriter.MaxFieldLength.LIMITED);
			writer.setUseCompoundFile(false);
			writer.setMergeFactor(10);
			writer.setMaxMergeDocs(10);
			writer.setMaxBufferedDocs(10);
		}
		
		IndexDirectory(writer,dataFolder);
		
		IndexWriter.getDefaultInfoStream();
		
		
		int docsAccount = writer.numDocs();
		writer.optimize();
		
		
		writer.commit();
		writer.close();
		
		
		return docsAccount;
	}
	
	private static void IndexDirectory(IndexWriter writer,File dir) throws IOException
	{
		File[] files = dir.listFiles();
		for(int i=0;i<files.length;i++)
		{
			File f = files[i];
			if(f.isDirectory())
			{
				IndexDirectory(writer,f);
			}else
			{
				if(f.getName().endsWith(".txt"))
				{
					if(f.isHidden()||!f.exists()||!f.canRead())
					{
						return;
					}
					else
					{
						System.out.println("Indexing " + f.getCanonicalPath());
						
						//FileReader reader = new FileReader(f);
						FileInputStream fileStream = new FileInputStream(f);
						BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fileStream,"Utf-8"));
						
						String tempString = "";
						String content = "";
						
						Document doc = new Document();
						
						ADUserBean ADUser = new ADUserBean();
						
						while((tempString = bufferReader.readLine())!=null)
						{
							if(tempString!="" && tempString!=null)
							{
								
								if(tempString.startsWith("DistinguishedName")
								  ||tempString.startsWith("Name")
								  ||tempString.startsWith("ObjectGUID")
								  ||tempString.startsWith("UserPrincipalName"))
								{
									if(tempString.startsWith("DistinguishedName"))
									{
									  String DistinguishedName = tempString.substring(tempString.indexOf(":")+2).trim();
									  Field DistinguishedNameField = new Field("DistinguishedName",DistinguishedName,Field.Store.YES,Field.Index.ANALYZED);
									  doc.add(DistinguishedNameField);
									  
									  ADUser.setDistinguishedName(DistinguishedName);
									  System.out.println("Index DistinguishedName :" + DistinguishedName);
									}
									
									if(tempString.startsWith("Name"))
									{
										String name = tempString.substring(tempString.indexOf(":")+2).trim();
										Field NameField = new Field("AccountName",name,Field.Store.YES,Field.Index.ANALYZED);
										doc.add(NameField);
										
										ADUser.setName(name);
										System.out.println("Index Name :" + name);
									}
									
									if(tempString.startsWith("ObjectGUID"))
									{
										String Guid = tempString.substring(tempString.indexOf(":")+2).trim();
										Field GuidField = new Field("Guid",Guid,Field.Store.YES,Field.Index.ANALYZED);
										doc.add(GuidField);
										
										ADUser.setGuid(Guid);
										System.out.println("Index Guid :" + Guid);
									}
									
									if(tempString.startsWith("UserPrincipalName"))
									{
										System.out.println(ADUser.getGuid());
										
										if(!isFirstTimeIndex)
										{
										switch(ExistChange(ADUser))
										{
										case Change:
										    //Query query = new TermQuery(new Term("Guid",ADUser.getGuid()));
											
											QueryParser queryParser = new QueryParser(Version.LUCENE_CURRENT, "Guid", new StandardAnalyzer(Version.LUCENE_CURRENT));
											queryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
											Query query;
											try {
												query = queryParser.parse(ADUser.getGuid());
												writer.deleteDocuments(query);
											} catch (ParseException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} 
											
											System.out.println("删除Query查询" + ADUser.getGuid());
											System.out.println("Change Object");
											
											writer.addDocument(doc);
											System.out.println("/r/n");
											break;
										case New:
											writer.addDocument(doc);
											System.out.println("/r/n");
											System.out.println("New Object");
											break;
										case NoChange:
											System.out.println("NoChange Object");
											break;
										}
										}
										else
										{
											writer.addDocument(doc);
											System.out.println("New Object");
										}	
											
										
										doc = new Document();
										ADUser = new ADUserBean();
									}
								}
								

							}
						}
						
						//Field field = new Field("contents",content,Field.Store.YES,Field.Index.ANALYZED);
						//Field field1 = new Field("filename",f.getCanonicalPath(),Field.Store.YES,Field.Index.NOT_ANALYZED);
						//Field field2 = new Field("filereader",reader);
						//Field field3 = new Field("ID",new String("Id1"),Field.Store.YES,Field.Index.ANALYZED);
					    
						//field.setBoost((float) (1.2));
						
						//doc.setBoost((float)1.5);
						
						//把内存索引当作缓冲区定期刷新到硬盘上
						//RAMDirectory ramDir = new RAMDirectory();
						//writer.addIndexes(ramDir);
					}
				}
			}
		}
	}
	
	public void displayHits(Query query,Sort sort)
	{
		File indexDir = new File("Index");
		Directory indexDirectory;
		try {
			indexDirectory = FSDirectory.open(indexDir);
			IndexSearcher searcher = new IndexSearcher(indexDirectory,true);
			
			TopDocs topDocs = searcher.search(query, 100, sort);
			System.out.println("\nResults for: " + query.toString() + " sorted by " + sort);
			
			
			System.out.println();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static ObjectIndexStatus ExistChange(ADUserBean adUser)
	{
		try {
	    File indexDir = new File("Index");
		IndexReader indexReader = IndexReader.open(FSDirectory.open(indexDir),false);
					
		IndexSearcher indexSearch = new IndexSearcher(indexReader);
		
		
		QueryParser queryParser = new QueryParser(Version.LUCENE_CURRENT, "Guid", new StandardAnalyzer(Version.LUCENE_CURRENT));
		queryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
		Query query = queryParser.parse(adUser.getGuid()); 
				
		TopDocs topDocs = indexSearch.search(query, 20);
		ScoreDoc[] hits = topDocs.scoreDocs;

		//indexSearch.close();
		
		if(hits.length > 0)
		{
			int docId = hits[0].doc;
			Document doc = indexSearch.doc(docId);
			String OldName = doc.get("AccountName");
			if(!OldName.equalsIgnoreCase(adUser.getName()))
			{
				return ObjectIndexStatus.Change;
			}else
			{
				return ObjectIndexStatus.NoChange;
			}
			
		}
		else
		{
			return ObjectIndexStatus.New;
		}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ObjectIndexStatus.Error;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ObjectIndexStatus.Error;
		}
		
	}

	public enum ObjectIndexStatus
	{
		New,
		Change,
		NoChange,
		Error
	}

}
