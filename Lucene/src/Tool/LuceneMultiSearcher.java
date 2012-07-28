package Tool;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;

public class LuceneMultiSearcher {
	private IndexSearcher[] searchers;
	
	
   public void TestMulti(String searchString) throws Exception
   {
	   MultiSearcher searcher = new MultiSearcher(searchers);
	   searcher.setSimilarity(Similarity.getDefault());
	   
	   QueryParser queryparser = new QueryParser(Version.LUCENE_35,"Content",new StandardAnalyzer(Version.LUCENE_30));
       Query query = queryparser.parse(searchString);
       
       org.apache.lucene.search.TopScoreDocCollector collector = 
    		   TopScoreDocCollector.create(10000, true);
       
       searcher.search(query, collector);
       ScoreDoc[] hits = collector.topDocs().scoreDocs;
   }
}
