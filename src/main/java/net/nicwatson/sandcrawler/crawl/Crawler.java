package net.nicwatson.sandcrawler.crawl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.nicwatson.sandcrawler.frontend.CrawlProgressResponder;

/**
 * A Crawler object is used to crawl web pages, discovering them via hyperlinks and preparing their contents
 * for indexing.
 */
public class Crawler
{
	/**
	 * Default number of times to retry accessing a page after failure
	 */
	public static final int DEFAULT_TRIES = 3;
	
	/**
	 * After how many page visits do we output a progress report to the console?
	 */
	public static final int CONSOLE_REPORTING_INTERVAL = 10;
	
	/**
	 * After how many page visits do we inform any CrawlProgressResponders of our progress?
	 * (This doesn't quite work -- progress gets reported fine but the GUI doesn't update.)
	 */
	public static final int GUI_REPORTING_INTERVAL = 10;
	
	/**
	 * Seed URL where the crawl will begin
	 */
	public final String seedUrl;
	
	/**
	 * The queue of pages to be crawled. Pages are added to the back as they are discovered, and 
	 * removed from the front as they are visited.
	 */
	protected Queue<String> unvisited;
	
	/**
	 * Keeps track of all URLs that have been previously visited, to avoid adding anything to the 
	 * queue unnecessarily
	 */
	Set<String> previouslyQueued;
	
	/**
	 * Every visit to a page generates an UnprocessedPage, which stores key metadata (URL, title, etc.),
	 * out-link information, and the page's text content for later parsing.
	 */
	Set<UnprocessedPage> unprocessed;
	
	/**
	 * 
	 */
	int maxTries;
	

	/**
	 * Initializes a new crawler with the specified seedURL, and the default maxTries
	 * @param seedUrl The URL of the page where the crawl will start
	 */
	public Crawler(String seedUrl)
	{
		this.seedUrl = seedUrl;
		unvisited = new LinkedList<String>();
		unprocessed = new LinkedHashSet<UnprocessedPage>();
		previouslyQueued = new LinkedHashSet<String>();
		this.maxTries = DEFAULT_TRIES;
	}
	
	/**
	 * Initializes a new crawler with the specified seedURL, and the specified maxTries
	 * @param seedUrl The URL of the page where the crawl will start
	 * @param maxTries Number of times to retry accessing a page after failure
	 */
	public Crawler(String seedUrl, int maxTries)
	{
		this(seedUrl);
		this.maxTries = maxTries;
	}
	
	/**
	 * Getter for the set of pages that have been visited but not indexed
	 * @return The set of UnprocessedPages that have been visited but not indexed
	 */
	public Set<UnprocessedPage> getUnprocessedPages()
	{
		return this.unprocessed;
	}
	
	/**
	 * Starts a crawl, reporting progress back to the given CrawlProgressResponder
	 * @param listener A CrawlProgressResponder to which to report crawl progress
	 * @return A reference to this Crawler itself
	 */
    public Crawler go(CrawlProgressResponder listener)
    {
    	return go(10000, listener);
    }
    
	/**
	 * Starts a crawl, up to a maximum number of pages specified by <code>limit</code>, 
	 * reporting progress back to the given CrawlProgressResponder.
	 * @param limit Maximum number of pages to visit before stopping. If this is 0 or less, the crawl will be unlimited
	 * and will continue until there are no more known unvisited links.
	 * @param listener A CrawlProgressResponder to which to report crawl progress
	 * @return A reference to this Crawler itself
	 */
	public Crawler go(int limit, CrawlProgressResponder listener)
	{
		// Keep track of what requests have failed and how many times
		Map<String, Integer> failures = new HashMap<String, Integer>();
		int counter = 0;				// How many pages have we crawled?
		unvisited.add(seedUrl);
		previouslyQueued.add(seedUrl);
		
		while(!unvisited.isEmpty() && (limit <= 0 || counter < limit))
		{
			// We keep going as long as the queue is not empty, and we haven't exceeded the limit.
			
			String next = unvisited.poll();
			UnprocessedPage page = null;
			try
			{
				page = new UnprocessedPage(next);
				page.fetch();						// Load the page
				counter++;
				unprocessed.add(page);				// Add it to the unprocessed set
				
				// Find all the outlinks from the page
				Set<String> outLinks = page.getLinks();
				for(String s : outLinks)
				{
					if(!previouslyQueued.contains(s))
					{
						// Outlinks URLs that have not been previously queued shoulde added to the queue
						unvisited.add(s);
						previouslyQueued.add(s);
					}
				}
				
				// Report progress to console
				if(counter % CONSOLE_REPORTING_INTERVAL == 0)
				{
					System.out.println("Crawl Progress  -  Visited: " + counter + "     Queued: " + unvisited.size());
				}
				
				/*
				// Report progress to GUI -- which doesn't work.
				if(listener != null)
				{
					if(counter % GUI_REPORTING_INTERVAL == 0)
					{
						listener.updateProgress(CrawlProgressResponder.ProgressStage.RETRIEVING, counter, unvisited.size());
						System.out.println("Updated progress!");
					}
				}
				else
				{
					if(counter % CONSOLE_REPORTING_INTERVAL == 0)
					{
						System.out.println("Crawl Progress  -  Visited: " + counter + "     Queued: " + unvisited.size());
					}
				}*/
				
			}
			catch(MalformedURLException e)	// The URL can't be parsed
			{
				System.err.println(next + " is a malformed URL. Ignoring.");
			}
			catch(IOException e)			// Can't access the page for some reason (e.g. timeout)
			{
				//System.err.println("IOException on: " + next);
				//e.printStackTrace(System.err);
				
				if(failures.containsKey(next))
				{
					// Page has failed before...
					if(failures.get(next) < maxTries)
					{
						// But hasn't exhausted its allotment of failures. Put it back in the queue.
						failures.put(next, failures.get(next) + 1);
						unvisited.add(next);
					}
					else
					{
						// We've run out of patience for this page. We add it to the unprocessed set in its "blank" state
						// so that other pages with links to it will still be valid, but the failed page's content and outlinks
						// won't be accounted for in the index.
						System.err.println("Failed " + maxTries + " times on URL " + next + " ... giving up.");
						if(page != null)
						{
							unprocessed.add(page);
						}
					}
				}
				else
				{
					// Page has not yet failed, so we open a new entry for it in the failures map.
					failures.put(next, 1);
					unvisited.add(next);
				}
			}

		} 
		System.out.println("Done! Visited " + counter + " pages.");
		return this;
	}	
	
	/**
	 * Reads HTML content from the given URL
	 * @param page The target URL
	 * @return String representing the entire document content
	 * @throws IOException
	 */
	public static String readURL(URL page) throws IOException
	{
        StringBuilder response = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(page.openStream()));
        String currentLine = reader.readLine();
        while(currentLine != null)
        {
            response.append(currentLine + "\n");
            currentLine = reader.readLine();
        }    
        return response.toString();
        
    }
}
