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
 
package net.nicwatson.sandcrawler.frontend;

import java.util.Date;
import java.util.List;
import java.util.Random;

import net.nicwatson.sandcrawler.frontend.CrawlProgressResponder.ProgressStage;
import net.nicwatson.sandcrawler.search.SearchResultPlus;

/**
 * This is the data model that provides content to the program GUI.
 * The data model only contains information that actually needs to be reported to the GUI, mostly
 * in the form of Strings. In practice it is actually populated/updated by the components of the 
 * Sandcrawler "engine" program. However, the GUI views never pull data directly from the engine,
 * only from this model.
 * <p/>
 * The model has three components: search results, crawl stats, and [REDACTED].
 * @author Nic
 *
 */
public class GuiModel
{
	/**
	 * Initializes a new GuiModel
	 */
	public GuiModel()
	{
		crawlStats = new CrawlStats();
		quipSystem = new PithyQuips();
		results = new SearchResults();
	}
	
	/**
	 * The search results data component
	 */
	SearchResults results;
	
	/**
	 * Crawl stats (date of last crawl, number of documents, etc.)
	 */
	CrawlStats crawlStats;
	
	/**
	 * The system of witty quotations/taglines/memes
	 */
	PithyQuips quipSystem;
	
	/**
	 * Getter for the crawlStats String. This is a single formatted string that includes all the 
	 * vital data/messages about the status of the crawl data.
	 * @return The crawlStats String to be displayed somewhere in the GUI
	 */
	public String getCrawlStats()
	{
		switch(crawlStats.stage)
		{
			case MISSING:
				return "\n\nNo web index data is available.\nTry running a new crawl.";
			case RETRIEVING:
				return "Crawling the dunes (exploring web pages)...\n" +
						"This may take a while and the program may appear to freeze.\n" +
						"Check the console for progress.";
						//"Pages read so far: " + crawlStats.progressDone + "\n" + 
						//"Pages left in queue: " + crawlStats.progressLeft;
			case PARSING:
				return "\n\n\nParsing pages...";
			case LINKING:
				return "\n\n\nProcessing hyperlinks...";
			case RANKING:
				return "\n\n\nCrunching page ranks...";
			case DONE:
				return "The last webcrawl took place at " + crawlStats.crawlTime.toString() + "\n" +
				"Seed URL: " + crawlStats.seedURL + "\n" +
				"Pages discovered: " + crawlStats.numDocs + "\n" +
				"Words indexed: " + crawlStats.numWords;
			default:
				return "Reported stage of crawl progress is invalid. This should never happen.\nThe legal department has been notified.";
		}
	}
	
	/**
	 * Determines if valid data for a completed crawl currently exists (otherwise, we need to warn the user when they attempt to search)
	 * @return <code>true</code> if valid crawl data exists
	 */
	public boolean getCrawlExists()
	{
		return this.crawlStats.crawlExists;
	}
	
	/**
	 * Sets the status of the crawl data (whether it exists or not)
	 * @param state The state to set the crawl data to (true - exists, or false - doesn't exist)
	 */
	public void setCrawlExists(boolean state)
	{
		this.crawlStats.crawlExists = state;
	}
	
	/**
	 * Populates the crawlStats component with data from the program engine
	 * @param program The program engine with a completed Crawler object from which stats will be read
	 */
	public void populateCrawlStats(Sandcrawler program)
	{
		this.crawlStats.populateCrawlStats(program);
		
	}
	
	/**
	 * Resets the crawlStats component so that it is as if a crawl never happened.
	 */
	public void resetCrawlStats()
	{
		this.crawlStats = new CrawlStats();
	}
	
	/**
	 * Sets the progress stage that the model should report for ongiong crawl tasks
	 * @param stage A CrawlProgressResponder.CrawlStage enum value
	 * @param done How much has been done - generally, how many pages crawled. Not currently used.
	 * @param left How much is left - generally, how many pages still in queue. Not currently used.
	 */
	public void setCrawlProgress(ProgressStage stage, int done, int left)
	{
		this.crawlStats.setProgress(stage, done, left);
	}

	/**
	 * Determines if search results exist in the model that are ready to be displayed
	 * @return <code>true</code> if there are search results to show
	 */
	public boolean getResultsExist()
	{
		return this.results.resultsExist;
	}

	/**
	 * Gets the list of SearchResultPlus's that the GUI can then display
	 * @return
	 */
	public List<SearchResultPlus> getResults()
	{
		return this.results.list;
	}

	/**
	 * Sets the list of SearchResultPlus's that this data model should store, and sets the resultsExist flag
	 * @param searchResult
	 */
	public void setSearchResults(List<SearchResultPlus> searchResult)
	{
		this.results.list = searchResult;
		this.results.resultsExist = true;
	}
	
	/**
	 * Fetches the currently-set witty one-liner from the quip system
	 * @return
	 */
	public String getQuip()
	{
		return PithyQuips.quips[this.quipSystem.selectedQuip];
	}
	
	/**
	 * Chooses and returns a new random quip
	 * @return
	 */
	public String cycleQuips()
	{
		return PithyQuips.quips[this.quipSystem.cycle()];
	}
	
	/**
	 * The CrawlStats component
	 * @author Nic
	 *
	 */
	public class CrawlStats
	{
		/**
		 * Current progress of any ongoing Crawler task
		 */
		CrawlProgressResponder.ProgressStage stage;
		
		/**
		 * Does crawl data currently exist?
		 */
		boolean crawlExists;
		
		/**
		 * Timestamp from the last crawl
		 */
		Date crawlTime;
		
		/**
		 * Seed URL for the last crawl
		 */
		String seedURL;
		
		/**
		 * Number of documents visited in the last crawl
		 */
		int numDocs;
		
		/**
		 * Number of words indexed in the last crawl
		 */
		int numWords;
		
		/**
		 * UNUSED. For ongoing crawl tasks, how much work is done
		 */
		int progressDone;
		
		/**
		 * UNUSED. For ongoing crawl tasks, how much work is left.
		 */
		int progressLeft;
		
		/**
		 * Initialize a new blank-slate CrawlStats
		 */
		CrawlStats()
		{
			crawlExists = false;
			stage = ProgressStage.MISSING;
			crawlTime = new Date(0);  // Party like it's 1970!
			seedURL = "";
			numDocs = 0;
			numWords = 0;
			progressDone = 0;
			progressLeft = 0;
		}
		
		/**
		 * Populates the CrawlStats from a Sandcrawler program that has a populated WebIndex
		 * @param program
		 */
		void populateCrawlStats(Sandcrawler program)
		{
			this.stage = ProgressStage.DONE;
			this.crawlTime = program.getIndex().getCrawlTime();
			this.numDocs = program.getIndex().getTotalDocs();
			this.numWords = program.getIndex().getTotalWords();
			this.seedURL = program.getIndex().getSeedURL();
			this.progressDone = numDocs;
			this.progressLeft = 0;
		}
		
		/**
		 * Sets the progress stage for an ongoing crawl process that the GUI is tryign to monitor
		 * (doesn't completely work)
		 * @param stage
		 * @param done
		 * @param left
		 */
		void setProgress(ProgressStage stage, int done, int left)
		{
			this.stage = stage;
			this.progressDone = done;
			this.progressLeft = left;
			if(stage != ProgressStage.DONE)
			{
				this.crawlExists = false;
			}
			else
			{
				this.crawlExists = true;
			}
		}
		
	}
	
	/**
	 * The SearchResults component
	 * @author Nic
	 *
	 */
	public class SearchResults
	{
		/**
		 * Do results exist that can be displayed to the user?
		 */
		boolean resultsExist;
		
		/**
		 * List of search results
		 */
		List<SearchResultPlus> list;
		
		/**
		 * Initialize a new blank-slate SearchResults
		 */
		public SearchResults()
		{
			resultsExist = false;
		}
	}
	
	/**
	 * The PithyQuips module
	 * @author Nic
	 *
	 */
	public class PithyQuips
	{
		/** 
		 * Random number generator used by this component
		 */
		Random random;
		
		/**
		 * Initialize a new PithyQuips component with a default quip selection and a Random object
		 */
		public PithyQuips()
		{
			this.random = new Random();
			this.selectedQuip = 0;
		}
		
		private static final String[] quips =
		{
				"Has a bad motivator!",
				"Doesn't speak Bocce!",
				"Fluent in under six forms of communication!",
				"Utinni! Utinni!",
				"Guess I'm going nowhere!",
				"You will never find a more wretched hive of scum and villainy.",
				"I don't like sand. It's coarse and rough and irritating and it gets everywhere.",
				"We all got a chicken-duck-woman-thing waiting for us.",
				"Instead of a big dark blur, I see a big bright blur.",
				"After this, let's go to the Tosche Station to pick up some power converters.",
				"Looks like someone's taken an interest in my handiwork!",
				"What a piece of junk!",
				"I've got a bad feeling about this...",
				"Will NOT make point-five past lightspeed!",
				"Impossible, even for a computer!",
				"More precise than Imperial Storm Troopers.",
				"One does not simply walk into... no, wait. That meme doesn't belong here."
				
		};
		private static final int size = quips.length;
		
		/**
		 * Which quip is currently selected?
		 */
		int selectedQuip;
		
		/**
		 * Randomly selects a new quip from the list
		 * @return The randomly-selected index number of the new quip
		 */
		public int cycle()
		{
			int next = this.random.nextInt(size);
			selectedQuip = next;
			return next;
		}
	}

	
}
