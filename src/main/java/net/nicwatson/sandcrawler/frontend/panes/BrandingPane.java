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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import net.nicwatson.sandcrawler.frontend.GuiModel;

/**
 * The BrandingPane contains the program header with title and graphic logo
 * @author Nic
 *
 */
public class BrandingPane extends VBox
{
	private Text title;
	private Image logo;
	private ImageView logoView;		// ImageView container to contain the logo image
	private Text quip;
	
	/**
	 * Sets up the Branding Pane
	 */
	public BrandingPane()
	{
		this.setAlignment(Pos.TOP_CENTER);
		
		// Set up the title text
		title = new Text();
		title.setText("Java Sandcrawler");
		title.setStyle("-fx-font: bold 36px verdana;");
		title.setStroke(Paint.valueOf("rgb(30, 30, 30)"));
		title.setFill(Paint.valueOf("rgb(240,240,240)"));
		title.setTextAlignment(TextAlignment.CENTER);
		title.setTranslateY(-10);
		
		// Attempt to load the logo image, scale it, and put it in an inner pane
		try
		{
			// This lovely drawing was made by my partner, Victoria Puusa
			FileInputStream imgStream = new FileInputStream("." + File.separator + "assets" + File.separator + "logo.png");
			logo = new Image(imgStream);
		}
		catch(FileNotFoundException e)
		{
		}
		StackPane imgPane = new StackPane();
		logoView = new ImageView();
		logoView.setImage(logo);
		logoView.setFitHeight(150);
		logoView.setPreserveRatio(true);
		logoView.setSmooth(true);
		logoView.setCache(true);
		imgPane.setPadding(new Insets(10));
		imgPane.getChildren().addAll(logoView, title);
		
		// Super secret features
		quip = new Text();
		quip.setText("");
		quip.setStyle("-fx-font: italic 12 verdana; -fx-text-fill: rgb(100, 0, 0);");
		quip.setTextAlignment(TextAlignment.CENTER);
		
		this.getChildren().addAll(imgPane, quip);
	}
	
	/**
	 * Updates dynamic content in the branding pane, from the given data model
	 * @param model The data model
	 */
	public void update(GuiModel model)
	{
		this.updateQuip(model);
	}
	
	/**
	 * Updates just the dynamic "quip" component from the data model
	 * @param model The data model
	 */
	public void updateQuip(GuiModel model)
	{
		this.quip.setText(model.getQuip());
	}
}
