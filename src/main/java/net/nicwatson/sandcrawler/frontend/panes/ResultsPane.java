/*
 *   JavaSandcrawler - A keyword-based demo search engine and web crawler for JavaFX runtimes
 *   Copyright (C) 2022  Nic Watson
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, version 3.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package net.nicwatson.sandcrawler.frontend.panes;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import net.nicwatson.sandcrawler.frontend.GuiModel;
import net.nicwatson.sandcrawler.search.SearchResultPlus;

/**
 * The Results Pane is where the search results are displayed.
 * @author Nic
 *
 */
public class ResultsPane extends VBox
{
	/**
	 * Number of search results to show
	 */
	public static int NUM_RESULTS = 10;
	
	/**
	 * List of Search Results as Text nodes. I think it looks nicer than using a ListView.
	 */
	private List<Text> results;
	
	/**
	 * Set up the Results Pane	
	 */
	public ResultsPane()
	{
		this.setAlignment(Pos.TOP_CENTER);
		this.setPadding(new Insets(5, 0, 5, 0));
		
		// Initialize 10 results string sto blank, and add them to the pane
		this.initResultStrings();
		this.getChildren().addAll(results);	
	}
	
	/**
	 * Initializes 10 text nodes, each representing a search result. They are initially blank.
	 */
	public void initResultStrings()
	{
		this.results = new ArrayList<Text>(10);
		for(int i = 0; i < NUM_RESULTS; i++)
		{
			Text t = new Text("");
			t.setStyle("-fx-font: 12 verdana; -fx-text-fill: rgb(0, 0, 0);");
			t.setTextAlignment(TextAlignment.LEFT);
			this.results.add(t);
			VBox.setMargin(t, new Insets(0, 0, 3, 0));
			
		}
	}
	
	/**
	 * Resets all the results strings to blank, as we'd want to do before a new crawl.
	 */
	public void clearResults()
	{
		for(Text t : this.results)
		{
			t.setText("");
		}
	}
	
	/**
	 * Updates the Results Pane based on the data model. If search results are available, this will cause them to be displayed.
	 * @param model The data model, which provides the contents of the search results
	 */
	public void update(GuiModel model)
	{
		if(model.getResultsExist())
		{
			List<SearchResultPlus> searchData = model.getResults();
			for(int i = 0; i < this.results.size(); i++)
			{
				SearchResultPlus hit = searchData.get(i);
				String resultText = String.format("#%d) %s - %-80s\n\t\tScore: %1.4f    Pagerank: %1.5f", i+1, hit.getTitle(), hit.getURL(), hit.getScore(), hit.getPageRank());
				this.results.get(i).setText(resultText);
				// System.out.println(this.results.get(i).getText());
			}
		}
	}
}
