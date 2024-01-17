package net.nicwatson.sandcrawler.frontend;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import net.nicwatson.sandcrawler.search.SearchResultPlus;

/**
 * Main GUI controller for the search engine app
 * @author Nic
 *
 */
public class GuiController extends Application implements CrawlProgressResponder
{
	/**
	 * Minimum width of resizable window (px)	
	 */
	public static int MIN_WIDTH = 800;
	
	/**
	 * Minimum height of resizable window (px)
	 */
	public static int MIN_HEIGHT = 400;
	
	/**
	 * Initial/default width of window (px)
	 */
	public static int DEFAULT_WIDTH = 800;
	
	/**
	 * Initial/default height of window (px)
	 */
	public static int DEFAULT_HEIGHT = 700;
	
	/**
	 * Reference to the data model used to update the GUI views.
	 */
	private GuiModel model;
	
	/**
	 * Reference to the main Sandcrawler program that does all the crawl and search work.
	 */
	private Sandcrawler program;
	
	/**
	 * The main view that manages the UI components of the program
	 */
	private SandcrawlerView view;
	
	/**
	 * Sound to play on pressing search button
	 */
	private AudioClip searchSound;
	
	/**
	 * Whether sound should be played. If the javafx.media module is not loaded through VM arguments, the program will 
	 * crash when attempting to instantiate any AudioClip object, so we use this flag to prevent that from happening.
	 */
	private boolean playSound;

	/**
	 * Creates a new controller for the search app. The controller makes its own data model and program engine.
	 */
	public GuiController()
	{
		model = new GuiModel();
		program = new Sandcrawler();
	}
	
	@Override
    public void start(Stage primaryStage) throws Exception
    {
		// First try to load existing crawl data
    	String dataFilePath = Sandcrawler.DATA_PATH + Sandcrawler.DATA_PREFIX + Sandcrawler.DATA_EXT;
    	if(program.loadIndex(dataFilePath))		// The loading itself is done through the program engine
    	{
    		System.out.println("Successfully loaded crawl data from file " + dataFilePath);
    		// Once we have crawl data, we can populate the data model with key information about the crawl
    		model.setCrawlExists(true);
    		model.populateCrawlStats(program);
    	}
    	
    	// This is the root pane
    	StackPane root = new StackPane();
    	
    	// Inside the root pane is a scrolling pane with a vertical scrollbar only
    	ScrollPane scroller = new ScrollPane();
    	scroller.setHbarPolicy(ScrollBarPolicy.NEVER);
    	scroller.setVbarPolicy(ScrollBarPolicy.ALWAYS);
    	root.getChildren().addAll(scroller);
    	
    	// Inside the scrolling pane is the SandcrawlerView
    	view = new SandcrawlerView();
    	scroller.setContent(view);
    	    	
    	// Set up the primary stage with the root pane
    	primaryStage.setTitle("Java Sandcrawler");
        primaryStage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        root.setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        view.setPrefSize(DEFAULT_WIDTH - 40, DEFAULT_HEIGHT - 10);

        primaryStage.show();
    	
        // Update the view (e.g. to show crawl stats)
        view.update(model);
        
        // Now we try to set up the sound system.
        playSound = false;
		try
		{
			// Success!
			searchSound = new AudioClip("FILE:assets/utini.mp3");
			playSound = true;
			
		}
		catch(NoClassDefFoundError e)
		{
			// javafx.media module isn't loaded so we'll have to do without sound effects.
			System.err.println("javafx.media module not loaded; skipping audio setup.");
			System.err.println("For the best experience with JavaSandcrawler, we recommend adding \"javafx.media\" to the --add-modules switch of your VM arguments.");
			playSound = false;
		}
		
		// Set event handler for the search button
		view.getInteractionPane().getButtonSearch().setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				doSearch(event);
			}
		});
		
		// Set event handler for the crawl button
		view.getInteractionPane().getButtonCrawl().setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				doNewCrawl(event);
			}
		});
        
    }
    
	/**
	 * Carries out the search logic after the user presses the search button
	 * @param event (Ignored)
	 */
	public void doSearch(ActionEvent event)
	{
		if(model.getCrawlExists())		// We can only search if crawl data is available
		{
			if(playSound)
			{
				searchSound.play();
			}
			else
			{
				System.err.println("Utinni!");
			}
			model.cycleQuips();
			
			// Grab the query from the input box
			String query = view.getInteractionPane().getQueryBox().getText();
			if(!query.isBlank())
			{
				// Use the program engine to build a results list
				List<SearchResultPlus> results = program.searchPlus(query, view.getInteractionPane().getBoostCheck().isSelected(), 10);
				// Pass the search results to the model
				model.setSearchResults(results);
			}
			
			// Update the view from the model
			view.update(model);
		}
		else		// Couldn't search because no crawl data available
		{
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("No crawl data is available. You will need to run a new web crawl before searching.");
			alert.show();
		}

	}
	
	/**
	 * Upon pressing the crawl button, the user is prompted to enter a seed URL for a new crawl. If the URL is valid, a new crawl begins.
	 * @param event (Ignored)
	 */
    public void doNewCrawl(ActionEvent event)
    {
    	String seedURL = javax.swing.JOptionPane.showInputDialog("Enter a seed URL for a new crawl. NOTE: The crawl can take several minutes to complete.\nWARNING: This will erase any previously saved crawl data!");
    	if(seedURL != null && !seedURL.isBlank())
    	{
    		boolean success = true;
    		try
    		{
    			new URL(seedURL);
    		}
    		catch(MalformedURLException e)	// Invalid URL
    		{
    			success = false;
    			Alert alert = new Alert(AlertType.ERROR);
    			alert.setContentText("An invalid/malformed URL was entered. Please try again.");
    			alert.show();
    		}
    		if(success)						// Valid URL
    		{
    			view.getResultsPane().clearResults();		// Clear prior search results
    			CrawlProgressResponder callback = this;
    			this.updateProgress(ProgressStage.RETRIEVING, 1, 1);
    			// Run the new crawl. This is supposed to happen in another thread so as not to freeze the GUI but it doesn't work.
    			Platform.runLater(() -> program.crawlWithProgressReporting(seedURL, callback));
    			
    		}
    	}
    }



    /**
     * As defined by the CrawlProgressResponder interface, this updates the model and then the view based on the reported crawl progress.
     */
	@Override
	public void updateProgress(ProgressStage progressStage, int done, int left)
	{
		if(progressStage != ProgressStage.MISSING)
		{
			this.model.setCrawlProgress(progressStage, done, left);
			view.getStatsPane().updateCrawlStat(model);
		}
	}
	

    public static void main(String[] args)
    {
        launch(args);
    }
}