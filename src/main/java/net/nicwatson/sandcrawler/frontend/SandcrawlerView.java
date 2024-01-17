package net.nicwatson.sandcrawler.frontend;

import javafx.scene.layout.VBox;
import net.nicwatson.sandcrawler.frontend.panes.StatsPane;
import net.nicwatson.sandcrawler.frontend.panes.BrandingPane;
import net.nicwatson.sandcrawler.frontend.panes.InteractionPane;
import net.nicwatson.sandcrawler.frontend.panes.ResultsPane;

/**
 * The SandcrawlerView is the main GUI View for the interactive component of the application. It
 * manages all of the panes that contain various controls and outputs.
 * @author Nic
 *
 */
public class SandcrawlerView extends VBox
{
	/**
	 * The brandingPane contains the title and logo, and is at the top of the window
	 */
	private BrandingPane brandingPane;
	
	/**
	 * The interactionPane contains the search bux and buttons
	 */
	private InteractionPane interactionPane;
	
	/**
	 * The resultsPane show the search results
	 */
	private ResultsPane resultsPane;
	
	/**
	 * The statsPane provides metadata on the currently-loaded web index / crawl data
	 */
	private StatsPane statsPane;
	
	
	/**
	 * Initializes a new SandcrawlerView
	 */
	public SandcrawlerView()
	{
		// Initialize the inner panes and add them to the view
		this.statsPane = new StatsPane();
		this.interactionPane = new InteractionPane();
		this.brandingPane = new BrandingPane();
		this.resultsPane = new ResultsPane();
		this.getChildren().addAll(brandingPane, interactionPane, resultsPane, statsPane);
		
		
	}
	
	/**
	 * Updates all the output panes from the given data model
	 * @param model The data model
	 */
	public void update(GuiModel model)
	{
		this.brandingPane.updateQuip(model);
		this.resultsPane.update(model);
		this.statsPane.updateCrawlStat(model);
	}

	/**
	 * Getter for the brandingPane
	 * @return
	 */
	public BrandingPane getBrandingPane()
	{
		return this.brandingPane;
	}
	
	/**
	 * Getter for the interactionPane
	 * @return
	 */
	public InteractionPane getInteractionPane()
	{
		return this.interactionPane;
	}
	
	/**
	 * Getter for the resultsPane
	 * @return
	 */
	public ResultsPane getResultsPane()
	{
		return this.resultsPane;
	}
	
	/**
	 * Getter for the statsPane
	 * @return
	 */
	public StatsPane getStatsPane()
	{
		return this.statsPane;
	}
}
