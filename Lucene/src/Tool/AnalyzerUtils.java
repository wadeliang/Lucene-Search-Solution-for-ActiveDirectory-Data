package Tool;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

public class AnalyzerUtils
{
	public static void displayTokens(Analyzer analyzer,String text) throws IOException
	{
		TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
		String s1 = "", s2 = "",s3 = "",s11="";
		boolean hasnext = stream.incrementToken();
		
		
		while(hasnext)
		{
			TermAttribute ta = stream.getAttribute(TermAttribute.class);
			CharTermAttribute ta1 = stream.getAttribute(CharTermAttribute.class);
			
			s2 = ta.term().toString() + " ";
			s3 = ta1.toString() +" ";
			s1 += s2;
			s11 +=s3;
			hasnext = stream.incrementToken();
		}
		
		System.out.println("["+ s1 +"]");
		System.out.println("\r\n");
		System.out.println("["+ s11 +"]");
		
	}
    
    public static void displayTokensWithFullDetails(Analyzer analyzer,String text) throws IOException
    {
    	TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
		boolean hasnext = stream.incrementToken();
		while(hasnext)
		{
		    OffsetAttribute offsetAttribute = stream.getAttribute(OffsetAttribute.class);
		    CharTermAttribute ta1 = stream.getAttribute(CharTermAttribute.class);
		    
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			
			System.out.println("["+
					ta1.toString()+":"+
					String.valueOf(startOffset)+"-->"+
					String.valueOf(endOffset) +":"+
				
					"]");
			
			
			
			hasnext = stream.incrementToken();
		}
		
    }
}