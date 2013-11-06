package edu.ucla.cs.cs144;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.DriverManager;

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
		SearchResult[] results = new SearchResult[0];
		ArrayList<SearchResult> r = new ArrayList<SearchResult>(); 
		try {
			
			IndexSearcher searcher = new IndexSearcher(System.getenv("LUCENE_INDEX")+"/ebay-index");
			QueryParser parser = new QueryParser("content", new StandardAnalyzer());
			
			Query q = parser.parse(query);        
			Hits hits = searcher.search(q);
			//returns empty set if total values queried is less than the number of values to be skipped 
			if(numResultsToSkip>hits.length()) return new SearchResult[0];
			//Iterator<Hit> iter = hits.iterator();
			int skipped = 0; //keeps track of how many we have skipped
			int added = 0; //keeps track of how many we have returned/added
			//iterate through the results received from lucene
			for (int i = 0; i < hits.length(); i++) {
				if(skipped > numResultsToSkip-1) {
					Document doc = hits.doc(i);
					r.add(new SearchResult(doc.get("ItemID"), doc.get("Name")));
					added++;
				}
				if(added == numResultsToReturn) {
					break;
				}
				skipped++;
			}
		
			results = new SearchResult[r.size()];
			for (int i=0; i < r.size(); i++) {
				results[i] = r.get(i);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	
	/*
	take the field name given in the contraint and return appropriate index name or sql attribute name
	*/
	private String encodeFieldName (String fn) {
		if(fn.equals(FieldName.ItemName)) return "Name";
		else if(fn.equals(FieldName.SellerId)) return "Seller";
		else if(fn.equals(FieldName.BuyPrice)) return "Buy_Price";
		else if(fn.equals(FieldName.EndTime)) return "Ends";
		else return fn;
	}
	
	
	private Connection getConn(boolean readOnly) throws Exception{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/CS144", "cs144", "");
            conn.setReadOnly(readOnly);        
            return conn;
	}
	public SearchResult[] advancedSearch(SearchConstraint[] constraints, 
			int numResultsToSkip, int numResultsToReturn) {
		// TODO: Your code here!
		SearchResult[] fResults = new SearchResult[0];
		
		String lucene_query = ""; //query for lucene index
		String mysql_query = ""; //query for mysql 
		String bidder_value = ""; //hold bidder value
		ArrayList<SearchResult> mysql_results= new ArrayList<SearchResult>();
		String itemid = "";
		String name = "";
		for (int i = 0; i < constraints.length; i++) {
			System.out.println("field name: " +constraints[i].getFieldName());
			//create lucene query 
			if (constraints[i].getFieldName().equals(FieldName.ItemName) || constraints[i].getFieldName().equals(FieldName.Category) || constraints[i].getFieldName().equals(FieldName.Description)) {
				if(lucene_query.equals("") ){
					lucene_query = encodeFieldName(constraints[i].getFieldName()) + ":\"" + constraints[i].getValue() + "\"";
				}
				else {
					lucene_query += " AND " + encodeFieldName(constraints[i].getFieldName()) + ":\"" + constraints[i].getValue() + "\"";
				}
			}
			//build query for sql
			else if(constraints[i].getFieldName().equals(FieldName.SellerId) || constraints[i].getFieldName().equals(FieldName.BuyPrice) || constraints[i].getFieldName().equals(FieldName.BidderId) || constraints[i].getFieldName().equals(FieldName.EndTime)){
				//store the value of bidder id
				if(constraints[i].getFieldName().equals(FieldName.BidderId)) {
					bidder_value = constraints[i].getValue();
				}
				else {
					if(mysql_query.equals("")) {
						mysql_query = " "+ encodeFieldName(constraints[i].getFieldName()) + "=" + constraints[i].getValue();
					}
					else {
						mysql_query += " AND " + encodeFieldName(constraints[i].getFieldName()) + "=" + constraints[i].getValue();
					}
				}
			}
		}
		System.out.println("mysql Query: "+mysql_query);
		try {
				//create a mysql query if queries where issued for mysql indexes
				if(!mysql_query.equals("") || !bidder_value.equals("")) {
					String bidder = "";
					//creating a query on bidders
					if(!bidder_value.equals("")) {
						//if mysql query for items is empty, then just query on bids table
						if(mysql_query.equals("")) {
							bidder = "ItemID IN (SELECT ItemID FROM Bids WHERE UserID=\"" + bidder_value + "\")";
						}
						else {
							bidder = " AND ItemID IN (SELECT ItemID FROM Bids WHERE UserID=\"" + bidder_value + "\")";
						}
					}
					Connection conn = getConn(true);
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT ItemID, Name FROM Items WHERE "+mysql_query+bidder);
					while(rs.next()) {
						itemid = rs.getString("ItemID");
						name = rs.getString("Name");
						mysql_results.add(new SearchResult(itemid, name));
					}
				}
				fResults = new SearchResult[mysql_results.size()];
				for (int i = 0; i < mysql_results.size(); i++) {
					fResults[i] = mysql_results.get(i);
				}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return fResults;
	}

	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!
		return null;
	}
	
	public String echo(String message) {
		return message;
	}

}
