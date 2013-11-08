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
	/**
		this will convert any time in a query into MYSQL TImestamp format for query comparison
	**/
	private String  MYSQL_Timestamp(String time) throws Exception {

		SimpleDateFormat firstformat = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
	
		Date parsed = (Date)firstformat.parse(time);
		SimpleDateFormat lastformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return lastformat.format(parsed);
    }
	
	/***
	this is to tell wheter we should convert to timestamp
	***/
	private String encodeValue(String val, boolean isTime) throws Exception{
		if(isTime) 	return MYSQL_Timestamp(val);
		else return val;
	}
	
	/**
	this will check if an item is already on our list to return for the query
	@param ArrayList<SearchResult> result: takes in the results we have gotten so far from querying lucene and mysql
	@param SearchResult test: item to be checked if its already on our result list;
	@return: return true if item is already on our list, otherwise return false
	**/
	private boolean isInResults(ArrayList<SearchResult> result, SearchResult test) {
		for (int i=0;i <result.size(); i++) {
			if(result.get(i).getItemId().equals(test.getItemId())) {
				return true;
			}
		}
		return false;
	}
	
	public SearchResult[] advancedSearch(SearchConstraint[] constraints, 
			int numResultsToSkip, int numResultsToReturn) {
		// TODO: Your code here!
		SearchResult[] results = new SearchResult[0];
		
		String lucene_query = ""; //query for lucene index
		String mysql_query = ""; //query for mysql 
		
		String fName = ""; //stores the field name converted to MYSQL attribute name
		String value = ""; //stores value if it needs time format conversion
		
		ArrayList<SearchResult> combine_results= new ArrayList<SearchResult>();
		ArrayList<SearchResult> lucene_results = new ArrayList<SearchResult>();
		String itemid = "";//stores item id when accessing mysql query
		String name = "";//stores name when accessing mysql query
		
		boolean convertTime = false;
		//loop over the constraints and create mysql and lucene queries depending on the constraints. this should ignore constraints that do not exist
		for (int i = 0; i < constraints.length; i++)
		{
			
			//create lucene query if there is a constraint on item name, category or description
			if (constraints[i].getFieldName().equals(FieldName.ItemName) || constraints[i].getFieldName().equals(FieldName.Category) || constraints[i].getFieldName().equals(FieldName.Description))
			{
				if(constraints[i].getValue().equals("") && constraints.length>1) continue;
				//create a lucene search in lucene indexes
				if(!lucene_query.equals("")) lucene_query += " AND ";
				
				lucene_query += encodeFieldName(constraints[i].getFieldName()) + ":\"" + constraints[i].getValue() + "\"";
				
			}
			/*
			build query for sql if there is a constraint on seller, bidder, buy price, or ending time
			*/
			else if(constraints[i].getFieldName().equals(FieldName.SellerId) || constraints[i].getFieldName().equals(FieldName.BuyPrice) || constraints[i].getFieldName().equals(FieldName.BidderId) || constraints[i].getFieldName().equals(FieldName.EndTime))
			{
				//store the value of bidder id
				if(constraints[i].getFieldName().equals(FieldName.BidderId)) {
					if(!mysql_query.equals("")) mysql_query += " AND ";
					mysql_query += "ItemID IN (SELECT ItemID FROM Bids WHERE UserID=\"" + constraints[i].getValue() + "\")";
				}
				else {
				
					fName = encodeFieldName(constraints[i].getFieldName());
					
					if(constraints[i].getFieldName().equals(FieldName.EndTime)) {
						convertTime = true;
					}
					
					try {
						value = encodeValue(constraints[i].getValue(), convertTime);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//create a sql query for the Items table
					if(!mysql_query.equals("")) {
						mysql_query = " AND ";
					}
				
					mysql_query += fName + "=\"" + value + "\"";
				
					convertTime = false;
				}
				
			}
		}
		
		System.out.println("mysql Query: " + mysql_query);
		System.out.println("lucene Query: " + lucene_query);
		
		try {
				if(!lucene_query.equals("")) {
					IndexSearcher searcher = new IndexSearcher(System.getenv("LUCENE_INDEX")+"/ebay-index");
					QueryParser parser = new QueryParser("content", new StandardAnalyzer());
			
					Query q = parser.parse(lucene_query);        
					Hits hits = searcher.search(q);
					for (int i = 0; i < hits.length(); i++) {
							Document doc = hits.doc(i);
							lucene_results.add(new SearchResult(doc.get("ItemID"), doc.get("Name")));
					}
					//return no results
					if(lucene_results.size()==0) return results;
				}
				//create a mysql query if queries where issued for mysql indexes
				if(!mysql_query.equals(""))
				{
					Connection conn = DbManager.getConnection(true);
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT ItemID, Name FROM Items WHERE " + mysql_query);
					while(rs.next())
					{
						itemid = rs.getString("ItemID");
						name = rs.getString("Name");
						SearchResult tuple = new SearchResult(itemid, name);
						//only add the new tuple if its not already in our list
						if ((lucene_results.size()==0 || !isInResults(lucene_results, tuple))&&!isInResults(combine_results,tuple)) 
						{
							combine_results.add(tuple);
						}
					}
					//close connections
					conn.close();
					stmt.close();
					rs.close();
				} 
				else {
					combine_results=lucene_results;
				}
				if(numResultsToSkip >= combine_results.size()) return results;
				ArrayList<SearchResult> temp = new ArrayList<SearchResult>();
				
				int added = 0;
				for (int i = numResultsToSkip; i < combine_results.size(); i++) {
					
						temp.add(combine_results.get(i));
						added++;
						if(added >= numResultsToReturn) break;
				}
				results = new SearchResult[temp.size()];
				for (int i=0; i < temp.size();i++) {
					results[i] = temp.get(i);
				}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!
		return null;
	}
	
	public String echo(String message) {
		return message;
	}

}
