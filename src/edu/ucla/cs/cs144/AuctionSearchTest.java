package edu.ucla.cs.cs144;

import java.util.Calendar;
import java.util.Date;


import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.SearchResult;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.FieldName;

public class AuctionSearchTest {
	public static void main(String[] args1) throws Exception
	{
		AuctionSearch as = new AuctionSearch();

		String message = "Test message";
		String reply = as.echo(message);
		System.out.println("Reply: " + reply);		
		
		String query = "superman";

		SearchResult[] basicResults = as.basicSearch(query, 0, 20);
		System.out.println("Basic Seacrh Query: " + query);
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
			
		}
		
		SearchConstraint constraint =
		    new SearchConstraint(FieldName.BuyPrice, "5.99"); 
		SearchConstraint[] constraints = {constraint};
		SearchResult[] advancedResults = as.advancedSearch(constraints, 0, 20);
		System.out.println("Advanced Seacrh");
		System.out.println("Received " + advancedResults.length + " results");
		for(SearchResult result : advancedResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}
		
		String itemId = "1497595357";
		String item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// Add your own test here
		System.out.println("======================================================\n");
		SearchConstraint c1 = new SearchConstraint(FieldName.ItemName, "pan");
		SearchConstraint c2 = new SearchConstraint(FieldName.Category,"kitchenware");
		constraints = new SearchConstraint[2];
		constraints[0] = 	c1;
		constraints[1] = c2;
		basicResults = as.advancedSearch(constraints, 0, 0);
		System.out.println("advanced search: ItemName='pan', Category='kitchenware'");
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
			
		}
		
		
	}
}
