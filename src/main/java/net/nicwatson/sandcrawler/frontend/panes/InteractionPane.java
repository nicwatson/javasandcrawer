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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * The InteractionPane contains the main controls: the input box for entering the search query,
 * the checkbox for specifying PageRank boost, the search button, and the "new crawl" button.
 * @author Nic
 *
 */
public class InteractionPane extends VBox
{
	
	/**
	 * The search button
	 */
	private Button buttonSearch;
	
	/**
	 * The new crawl button
	 */
	private Button buttonCrawl;
	
	/**
	 * The search box
	 */
	private TextField queryBox;
	
	/**
	 * The PageRank boost option checkbox
	 */
	private CheckBox boostCheck;
	

	/**
	 * Getter for the search button control
	 * @return
	 */
	public Button getButtonSearch()
	{
		return this.buttonSearch;
	}
	
	/**
	 * Getter for the crawl button control
	 * @return
	 */
	public Button getButtonCrawl()
	{
		return this.buttonCrawl;
	}
	
	/**
	 * Getter for the search query input box control
	 * @return
	 */
	public TextField getQueryBox()
	{
		return this.queryBox;
	}
	
	/**
	 * Getter for the boost option checkbox control
	 * @return
	 */
	public CheckBox getBoostCheck()
	{
		return this.boostCheck;
	}
	
	/**
	 * Sets up the Interaction Pane
	 */
	public InteractionPane()
	{
		this.setPadding(new Insets(10));
		
		// We have two inner panes
		VBox searchBox = new VBox();	// On top, searchBox will vertically arrange the query box and the boost checkbox
		HBox buttons = new HBox();		// Below, the two buttons are placed side-by-side

		// Set up the searchBox contents, including the query box and the boost checkbox
		queryBox = new TextField();
		queryBox.setPromptText("Enter search query");
		queryBox.setMaxWidth(450);
		queryBox.setMinWidth(350);
		boostCheck = new CheckBox("Use PageRank Boost");
		VBox.setMargin(boostCheck, new Insets(3, 0, 0, 0));
			
		// Set up the buttons
		buttonSearch = new Button("Search the junk heap!");
		buttonSearch.setStyle("-fx-font: 12 verdana; -fx-base: rgb(200,200,200); -fx-text-fill: rgb(0, 0, 0);");
		
		buttonCrawl = new Button("Crawl the desert!");
		buttonCrawl.setStyle("-fx-font: 12 verdana; -fx-base: rgb(255,238,153); -fx-text-fill: rgb(0, 0, 0);");
	
		// Padd the inner frames and adjust alignments
		this.setAlignment(Pos.TOP_CENTER);
		searchBox.setAlignment(Pos.TOP_CENTER);
		searchBox.setPadding(new Insets(0, 0, 3, 0));
		buttons.setAlignment(Pos.TOP_CENTER);
		
		// Add the controls to their respective inner panes, and add those panes to the Interaction Pane
		searchBox.getChildren().addAll(queryBox, boostCheck);
		buttons.getChildren().addAll(buttonSearch, buttonCrawl);
		this.getChildren().addAll(searchBox, buttons);		
	}
}
