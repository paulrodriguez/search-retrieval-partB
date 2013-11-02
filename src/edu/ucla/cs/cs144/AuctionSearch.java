package edu.ucla.cs.cs144;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;

import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.SearchResult;

import java.util.ArrayList;
public class AuctionSearch implements IAuctionSearch {

	/* 
         * You will probably have to use JDBC to access MySQL data
         * Lucene IndexSearcher class to lookup Lucene index.
         * Read the corresponding tutorial to learn about how to use these.
         *
         * Your code will need to reference the directory which contains your
	 * Lucene index files.  Make sure to read the environment variable 
         * $LUCENE_INDEX with System.getenv() to build the appropriate path.
	 *
	 * You may create helper functions or classes to simplify writing these
	 * methods. Make sure that your helper functions are not public,
         * so that they are not exposed to outside of this class.
         *
         * Any new classes that you create should be part of
         * edu.ucla.cs.cs144 package and their source files should be
         * placed at src/edu/ucla/cs/cs144.
         *
         */
	
	public SearchResult[] basicSearch(String query, int numResultsToSkip, 
			int numResultsToReturn) {
		// TODO: Your code here!
		SearchResult[] final_results = new SearchResult[0];
		ArrayList<SearchResult> results = new ArrayList<SearchResult>(); 
		try {
			
			IndexSearcher searcher = new IndexSearcher(System.getenv("LUCENE_INDEX")+"/ebay-index");
			QueryParser parser = new QueryParser("content", new StandardAnalyzer());
			
			Query q = parser.parse(query);        
			Hits hits = searcher.search(q);

			Iterator<Hit> iter =  hits.iterator();
			
			int iterpos = 0;

			while (iter.hasNext()) {
				if(iterpos > numResultsToSkip-1) {
					Hit hit = iter.next();
					Document doc = hit.getDocument();
					results.add(new SearchResult(doc.get("ItemID"), doc.get("Name")));
				}
				iterpos ++;
			}
		
			final_results = new SearchResult[results.size()];
			for (int i=0; i < results.size(); i++) {
				final_results[i] = results.get(i);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return final_results;
	}

	public SearchResult[] advancedSearch(SearchConstraint[] constraints, 
			int numResultsToSkip, int numResultsToReturn) {
		// TODO: Your code here!
		try {
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new SearchResult[0];
	}

	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!
		return null;
	}
	
	public String echo(String message) {
		return message;
	}

}
