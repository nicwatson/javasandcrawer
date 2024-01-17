package net.nicwatson.sandcrawler.frontend.panes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import net.nicwatson.sandcrawler.frontend.GuiModel;

/**
 * The bottom pane provides some info about the most recent crawl
 * @author Nic
 *
 */
public class StatsPane extends VBox
{
	/**
	 * This text node provides information about the most recent crawl (timestamp, number of pages found, etc.)_
	 */
	private Text crawlStat;
	
	/**
	 * This text node tells the word about the handsome devil who coded this masterpiece and/or the bumbling
	 * oaf who slapped this atrocity together
	 */
	private Text authorTag;
	
	/**
	 * Sets up the Stats Pane
	 */
	public StatsPane()
	{
		// Set up the text nodes and add them to the pane
		this.setAlignment(Pos.TOP_CENTER);
		crawlStat = new Text();
		crawlStat.setStyle("-fx-font: 12 verdana; -fx-text-fill: rgb(255,255,255);");
		crawlStat.setTextAlignment(TextAlignment.CENTER);
		
		authorTag = new Text();
		authorTag.setStyle("-fx-font: 10 verdana; -fx-text-fill: rgb(255,255,255);");
		authorTag.setTextAlignment(TextAlignment.CENTER);
		authorTag.setText("[Made by Nic Watson]");
		
		VBox.setMargin(authorTag, new Insets(2, 0, 0, 0));
		
		this.getChildren().addAll(crawlStat, authorTag);
		
	}
	
	/**
	 * Updates the crawl stats text from the given data model
	 * @param model The data model
	 */
	public void updateCrawlStat(GuiModel model)
	{
		crawlStat.setText(model.getCrawlStats());
		//System.out.println(crawlStat.getText());
	}



}
