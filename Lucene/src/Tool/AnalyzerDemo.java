package Tool;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;

/*
用于分析处理不同的分析器输出的语汇单元的不同效果
 */
public class AnalyzerDemo {
   private static final String[] examples = {
	   "The quick brown fox jumped over the lazy dogs",
	   "XY&Z Corporation - xyz@example.com",
	   "The 中国人民是伟大的人民"
   };
  
   private static final Analyzer[] analyzers = new Analyzer[]{
	   new WhitespaceAnalyzer(),
	   new SimpleAnalyzer(),
	   new StopAnalyzer(Version.LUCENE_29),
	   new StandardAnalyzer(Version.LUCENE_29)
   };
	
	
	public static void main(String[] args) {
		String[] strings = examples;
		for(int i=0;i<strings.length;i++)
		{
		     System.out.println("Analyzing \""+ strings[i] +"\"");
		     for(int j=0;j< analyzers.length;j++)
		     {
		    	 Analyzer analyzer = analyzers[j];
		    	 String name = analyzer.getClass().getName();
		    	 name = name.substring(name.lastIndexOf(".")+1);
		    	 System.out.println(" "+ name +":");
		    	 System.out.print(" ");
		    	 
		    	 try {
					//AnalyzerUtils.displayTokens(analyzer, strings[i]);
					AnalyzerUtils.displayTokensWithFullDetails(analyzer, strings[i]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	 
		    	 System.out.print("\n");
		     }
		}
	}

}


