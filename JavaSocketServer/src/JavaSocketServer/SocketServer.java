package JavaSocketServer;

import java.io.*;
import java.net.Socket;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.Parser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
//import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SocketServer implements Runnable {
    private Socket server;
	private String line,input;
	private static IndexReader indexReader;
	
	public SocketServer(Socket server)
	{
		this.server = server;
	}
	
	public void run() {
		input = "";
		try
		{
			InputStreamReader inputReader = new InputStreamReader(server.getInputStream(), "utf8");
			BufferedReader d = new BufferedReader(inputReader);
			
			//DataInputStream in = new DataInputStream(d);
			PrintStream out = new PrintStream(server.getOutputStream(),false,"UTF-8");
			
			while((line = d.readLine())!=null
				&& !line.equals("."))
			{
				input = input + line;
				
			}
			
			File indexDir = new File("Index");
			indexReader = IndexReader.open(FSDirectory.open(indexDir));
					//open(FSDirectory.open(indexDir),true);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			IndexSearcher indexSearch = new IndexSearcher(indexReader);
			QueryParser queryParser = new QueryParser(Version.LUCENE_30, "AccountName", analyzer);
			queryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
			
			Query query = new WildcardQuery(new Term("AccountName","*"+input+"*"));
			//SpanTermQuery query = new SpanTermQuery(new Term("AccountName",input));
			//SpanQuery spanFirstQuery = new SpanFirstQuery(query,1);

			//SpanNearQuery query1 = new SpanNearQuery(new SpanQuery[]{query,query2},100,false);
			
			//TermQuery termQuery = new TermQuery();
					//new FuzzyQuery(new Term("AccountName",input));
					//queryParser.parse(input + " OR "  + input + "*");
		       
		       //org.apache.lucene.search.TopScoreDocCollector collector = TopScoreDocCollector.create(10000, true);
		       
			   SortField sortField = new SortField("Guid", SortField.STRING);
		       TopFieldDocs topFieldDocs = indexSearch.search(query,null,1000,new Sort(sortField));
		       ScoreDoc[] hits = topFieldDocs.scoreDocs;
		       
		       QueryScorer fragmentScore = new QueryScorer(query);
		       SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color='red'>","</font>");
		       
		       org.apache.lucene.search.highlight.Highlighter highlighter = new org.apache.lucene.search.highlight.Highlighter(formatter,fragmentScore);
		       highlighter.setTextFragmenter(new SimpleFragmenter(400));
		       
		    		   //collector.topDocs().scoreDocs;
		       
		       System.out.println("All result is sorting by the Field of "+sortField.getField());
		       
		       for(int i=0;i< hits.length;i++)
				{
					int docId = hits[i].doc;
					
					Document doc = indexSearch.doc(docId);
					String accountName = doc.get("AccountName");
					
					TokenStream tokenStream = analyzer.tokenStream("AccountName", new StringReader(accountName));
					String bestFragment =  highlighter.getBestFragment(tokenStream, accountName);
					System.out.println(bestFragment);
					//System.out.println("The IndexId of document is:" + String.valueOf(hits[i].doc));
					
					//out.flush();
					out.println("{\"AccountName\":"+"\""+bestFragment+"\"}");
					//out.println("DistinguishedName:"+doc.get("DistinguishedName"));
				}
			
		    out.println("end");
		    //Now write to the client
			System.out.println("Search message is:" + input);
			System.out.println("Search result is:" + hits.length);
			
			server.close();
		}
		catch(Exception ioe)
		{
			System.out.println("IOException on socket listen:" + ioe);
			ioe.printStackTrace();
		}
		
	}

}
