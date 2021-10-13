package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/** Application scrapes website and displays top10 (and all) word occurrences to a JavaFX GUI.
 * @author derekdileo */
public class Main extends Application {
	
	// Variables for QuestionBox calls
	protected static boolean defaultSite = false;
	protected static String userWebsite = null;
	protected static String sourceHead = null;
	protected static String sourceEnd = null;
	private String defaultWebsite =  "https://www.gutenberg.org/files/1065/1065-h/1065-h.htm";
	private String defaultSourceHead = "<h1>The Raven</h1>";
	private String defaultSourceEnd = "<!--end chapter-->";
	private String sitePrompt = "Please enter a website to evaluate";
	private String startPrompt = "Please paste some text from the first line of text to be evaluated.";
	private String endPrompt = "Please paste some text from the last line of text to be evaluated.";
	private String siteTitle = "Website to Processe?";
	private String startTitle = "Start of Processing?";
	private String endTitle = "End of Processing?";
	protected static String[] userResponses;
	
	// Local Lists and Maps to hold return values from Class methods
	private HashMap<String, Integer> wordFrequencyHashMap = new HashMap<String, Integer>();
	private ArrayList<Word> wordsArrayList = new ArrayList<Word>();
	
	// Varibles used to show / hide text on GUI
	private StringBuilder sbTen;
	private StringBuilder sbAll;
	
	// These are accessed by the four Controller classes to print to GUI 
	protected static String sbTenString;
	protected static String sbAllString;
	
	/** Main method calls launch() to start JavaFX GUI.
	 * @param args mandatory parameters for command line method call */
	public static void main(String[] args) {
		launch();
	}
	
	// Declare stage (window) outside of start() method
	// so it is accessible to closeProgram() method
	static Stage window;
	
	/** The start method (which is called on the JavaFX Application Thread) 
	 * is the entry point for this application and is called after the init 
	 * method has returned- most of the application logic takes place here. */
	@Override
	public void start(Stage primaryStage) {
		
		// Get user input for website, startLine & endLine...
		// Or set to default values if none are entered by user
		userResponses = processUserInput();
		
		// This boolean is used to determine which scene is loaded 
		// (with or without EAP's The Raven graphic elements) 
		if (userResponses[0].equals(defaultWebsite)) {
			defaultSite = true;
		}
		
		// HashMap created from text file generated by WebScrape.parseSite()
		// with Key=word and value=frequency of occurrence
		wordFrequencyHashMap = WebScrape.parseSite(userResponses[0], userResponses[1], userResponses[2]);
		
		// Iterate through HashMap<String,Int>, instantiate a Word for each entry
		// and return ArrayList<Word> to be sorted and displayed on the GUI
		wordsArrayList = processHashMap(wordFrequencyHashMap);
		
		// Sort wordsArrayList<Word> by frequency
		Collections.sort(wordsArrayList);

		// Reverse for highest frequency first
		Collections.reverse(wordsArrayList);
		
		// New versions of Java do not add the word "" to the list. However, Java version 1.8 does
		// This "" occurs 73 times on defaultSite and, therefore, appears at the top of the words list.
		// I tried for hours to rectify, but in the end, I had to bandaid with this. 
		//wordsArrayList.remove(0);
		
		processText(wordsArrayList);
		
		// Rename stage to window for sanity
		window = primaryStage;
		
		// Set stage title
		window.setTitle("Word Frequency Analyzer");
		
		// Handle close button request. 
		// Launch ConfirmBox to confirm if user wishes to quit
		window.setOnCloseRequest(e -> {
			// Consume the event to allow closeProgram() to do its job
			e.consume();
			closeProgram();
		});
		
		try {
			if (defaultSite) {
				BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Main.fxml"));				
				Scene scene = new Scene(root,800,600);
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				window.setScene(scene);
				window.show();
			} else {
				BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("MainDefault.fxml"));				
				Scene scene = new Scene(root,800,600);
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				window.setScene(scene);
				window.show();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	private String[] processUserInput() {
		// Gather URL from user (or set to default for EAP The Raven) 
		userWebsite = QuestionBox.display(siteTitle, sitePrompt, defaultWebsite);
		
		
		// If userWebsite is not EAP, call overloaded QB.display which has
		// validation to not allow a blank response. 
		if (userWebsite.equals(defaultWebsite)) {
			sourceHead = QuestionBox.display(startTitle, startPrompt, defaultSourceHead);
			sourceEnd = QuestionBox.display(endTitle, endPrompt, defaultSourceEnd);			
		} else {
			sourceHead = QuestionBox.display(startTitle, startPrompt);
			sourceEnd = QuestionBox.display(endTitle, endPrompt);			
		}
		String[] responses = {userWebsite, sourceHead, sourceEnd};
		return responses;
	}
	
	/**
	 * Method prints all results to console and GUI with top 10 showing on first page
	 * and all results showing on second page.
	 * @author Derek DiLeo */
	protected void processText(ArrayList<Word> wordsArrayList) {

		// Build a string of top 10 results to push to Main.fxml GUI
		sbTen = new StringBuilder();
		sbTen.append("Top Ten Results\n\n");
		
		// Build a string of all results to push to AllResults.fxml GUI
		sbAll = new StringBuilder();
		sbAll.append("All Results\n\n");
		
		// Print all results to console and append all results to sbAll
		System.out.println("\nSorted:");
		
		int size = wordsArrayList.size();
		
		// Process results based on size of wordsArrayList (total number of unique words).
		// This is meant to prevent a null pointer exception for the second for loop
		if(size >= 10) {
			// For first 10 results, begin printing to console and add to both results sets
			for (int index = 0; index < 10; index++) {
				System.out.println(wordsArrayList.get(index).toString(index));
				sbTen.append(wordsArrayList.get(index).toString(index) + "\n");
				sbAll.append(wordsArrayList.get(index).toString(index) + "\n");
			}
			
			// Continue printing to console and adding to sbAll 
			for (int index = 10; index < wordsArrayList.size(); index++) {
				System.out.println(wordsArrayList.get(index).toString(index));
				sbAll.append(wordsArrayList.get(index).toString(index) + "\n");
			}
			
		} else { 
			// Print results to console and add to both results sets
			for (int index = 0; index < size; index++) {
				System.out.println(wordsArrayList.get(index).toString(index));
				sbTen.append(wordsArrayList.get(index).toString(index) + "\n");
				sbAll.append(wordsArrayList.get(index).toString(index) + "\n");
			}
			// Print null for remaining entries until top10 can be displayed
			for (int index = size; index < 10; index++) {
				System.out.println(wordsArrayList.get(index).toString(index));
				sbTen.append("\n" + (index + 1) + ") Word: null \tFrequency: null");
				sbAll.append("\n" + (index + 1) + ") Word: null \tFrequency: null");
			}
			
		}
		
		// Save results to String variables which are called from either:
		// MainC-, MainDefaultC-, AllResultsC- or AllResultsDefaultC- ontrollers to push to GUI
		sbTenString = sbTen.toString();
		sbAllString = sbAll.toString();
		
	}
	
	/**
	 * Method instantiates a Word object with each entry of HashMap (K/V = word/frequency) 
     * then adds all to (and returns) an ArrayList(of Word).  
	 * @param hm is a HashMap(of String, Integer) with Key/Value = word/frequency 
	 * @return ArrayList(of Word) which enables the sorting and printing of each 
	 * entry(Word implements Comparable(for Word)). */
	private ArrayList<Word> processHashMap(HashMap<String, Integer> hm) {

		// ArrayList to hold Word objects for later comparison
		ArrayList<Word> wordsUnsorted = new ArrayList<Word>();

		// Loop through each entry in HashMap
		for (@SuppressWarnings("rawtypes")
		Map.Entry entry : hm.entrySet()) {

			// Pull key/value pairs for each word in hm
			String wordKey = entry.getKey().toString();
			String wordValue = entry.getValue().toString();
			int wordValueInt = Integer.parseInt(wordValue);

			// Use pairs to create new instances of Word class
			// (which implements Comparable<Word>)
			Word word = new Word(wordKey, wordValueInt);
			wordsUnsorted.add(word);
		}
		
		return wordsUnsorted;
		
	}
	
	// Variables for testing createTable method
	private static String columnOne = "word varchar(255) NOT NULL UNIQUE";
	private static String columnTwo = "frequency int NOT NULL";
	private static String primaryKey = "PRIMARY KEY(word)";

	
	/** Method uses ConfirmBox class to confirm if user wants to quit. */
	protected static void closeProgram() {
       // Ask if user wants to exit
       Boolean answer = ConfirmBox.display("", "Are you sure you want to quit?");
       if (answer) {
           // Run any necessary code before window closes:
		   try {
	    		// Drop and re-create words table
				Database.deleteTable("words");
				Database.createTable("words", columnOne, columnTwo, primaryKey);
				System.out.println("Window Closed!");
				window.close();
		   } catch (Exception e) {
			   System.out.println(e.getMessage());
			   e.printStackTrace();
		   }
    	   
       }
       
	}
	
}
