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

		String itemId2 = "1045238762";
		String item2 = as.getXMLDataForItemId(itemId2);
		System.out.println("XML data for ItemId: " + itemId2);
		System.out.println(item2);

		String itemId3 = "1045797007";
		String item3 = as.getXMLDataForItemId(itemId3);
		System.out.println("XML data for ItemId: " + itemId3);
		System.out.println(item3);

		// Add your own test here
		
		System.out.println("\n===============================================================\n");
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
		
		System.out.println("\n===============================================================\n");
		c1 = new SearchConstraint(FieldName.ItemName, "Precious Moments");
		c2 = new SearchConstraint(FieldName.SellerId, "waltera317a");
		constraints = new SearchConstraint[2];
		constraints[0] = 	c1;
		constraints[1] = c2;
		basicResults = as.advancedSearch(constraints, 0, 0);
		System.out.println("advanced search: ItemName='Preious Moments', SellerId='waltera317a'");
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
			
		}
		
		System.out.println("\n===============================================================\n");
		c1 = new SearchConstraint(FieldName.EndTime, "Dec-14-01 21:00:05");
		constraints = new SearchConstraint[1];
		constraints[0] = 	c1;
		
		basicResults = as.advancedSearch(constraints, 0, 0);
		System.out.println("advanced search: EndTime='Dec-14-01 21:00:05'");
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
			
		}
		
		System.out.println("\n===============================================================\n");
		c1 = new SearchConstraint(FieldName.BidderId, "surfingmarie");
		c2 = new SearchConstraint(FieldName.BidderId, "parker983");
		constraints = new SearchConstraint[2];
		constraints[0] = 	c1;
		constraints[1] = c2;
		basicResults = as.advancedSearch(constraints, 0, 0);
		//System.out.println("advanced search: item name: christopher radko");
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
			
		}
		/*
		System.out.println("\n===============================================================\n");
		c1 = new SearchConstraint(FieldName.Description, "superman");
		constraints = new SearchConstraint[1];
		constraints[0] = 	c1;
		
		basicResults = as.advancedSearch(constraints, 0, 0);
		System.out.println("advanced search: item name: superman");
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
			
		}*/
	}
}
