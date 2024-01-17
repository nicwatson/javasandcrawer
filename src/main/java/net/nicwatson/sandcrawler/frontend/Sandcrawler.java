package net.nicwatson.sandcrawler.frontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.FileSystemException;
import java.util.List;

import cs1406z.test.SearchResult;
import net.nicwatson.sandcrawler.common.WebIndex;
import net.nicwatson.sandcrawler.crawl.Crawler;
import net.nicwatson.sandcrawler.search.SearchResultPlus;

/**
 * The main "engine" program that delegates all the logic of crawling and searching.
 * The Sandcrawler program maintains a reference to the WebIndex object that actually
 * stores most of the data. The Sandcrawler is responsible for loading that data from
 * disk (should it exist), and for saving data from any new crawls.
 * <p/>
 * For convenience, although Sandcrawler itself does not implement ProjectTester, it has
 * pass-through methods for all of the same tasks that are defined by ProjectTester.
 * @author Nic
 * @see ProjectTester
 * @see WebIndex
 */
public class Sandcrawler
{
	/**
	 * This is the full path to the data file where previous crawl data is stored
	 */
	public static final String DATA_PATH = "." + File.separator + "data" + File.separator;
	
	/**
	 * The data file name will begin with these characters
	 */
	public static final String DATA_PREFIX = "crawl";
	
	/**
	 * The data file extension/suffix
	 */
	public static final String DATA_EXT = ".dat";

	/**
	 * The WebIndex containing all the page data to be searched
	 */
	private WebIndex webIndex;
	
	/**
	 * Flags whether the index has actually been populated with crawl data
	 */
	boolean hasIndex;

	/**
	 * Initializes a new Sandcrawler
	 */
	public Sandcrawler()
	{
		webIndex = null;
		hasIndex = false;
	}
	
	/**
	 * Getter for <code>webIndex</code>
	 * @return A reference to the WebIndex
	 */
	public WebIndex getIndex()
	{
		return this.webIndex;
	}

	/**
	 * Loads crawl/index information from file
	 * @param path The binary file containing the data
	 * @return <code>True</code> if the operation was successful
	 */
	public boolean loadIndex(String path)
	{
		try
		{
			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.webIndex = (WebIndex) ois.readObject();
			this.hasIndex = true;
			ois.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Could not access file: " + path);
			e.printStackTrace(System.err);
			return false;
		}
		catch (IOException e)
		{
			System.err.println("Error while reading previous crawl from data file " + path);
			e.printStackTrace(System.err);
			return false;
		}
		catch (ClassNotFoundException e)
		{
			System.err.println("Error while processing previous crawl data from file " + path);
			e.printStackTrace(System.err);
		}
		return true;
	}

	/**
	 * Deletes all data files in the specified directory, if it exists. If it does not exist, it will be created as an
	 * empty directory.
	 * 
	 * @param dataDir <code>File</code> object representing the directory to be cleaned/created
	 * @return <code>false</code> if an existing file could not be deleted; otherwise, <code>true</code>.
	 * @throws FileSystemException If the directory does not exist and cannot be created.
	 */
	private boolean clean(File dataDir) throws FileSystemException
	{
		boolean success = true; // Flag for return value
		if (dataDir.exists())
		{
			// Dir exists. Try to delete everything that starts with "crawl" and ends with ".dat"
			File[] contents = dataDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name)
				{
					return name.startsWith(DATA_PREFIX) && name.endsWith(DATA_EXT);
				}

			});
			for (File f : contents)
			{
				if (!f.delete())
				{
					System.err.println("Error: Could not delete file: " + f.getAbsolutePath());
					// Failure to delete a file is considered non-fatal, but makes the return flag false
					success = false;
				}
			}
		}
		else
		{
			// Dir does not exist. Try to create it.
			if (!dataDir.mkdir())
			{
				System.err.println("Error: Could not create directory: " + dataDir.getAbsolutePath());
				success = false;
				// Failure to create the directory is considered fatal and throws an exception
				throw new FileSystemException(dataDir.getAbsolutePath());
			}
		}
		return success;
	}

	/**
	 * Initializes the crawler (e.g. for running tests via ProjectTester) by cleaning prior crawl data
	 */
	public void initialize()
	{
		File dataDir = new File(DATA_PATH);
		try
		{
			this.clean(dataDir);
		}
		catch (FileSystemException e)
		{
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Initializes a Crawler to start a crawl on the given seed URL
	 * @param seedURL The URL of the page where the crawl should be started
	 */
	public void crawl(String seedURL)
	{
		crawlWithProgressReporting(seedURL, null);

	}
	
	/**
	 * Initializes a Crawler to start a crawl on the given seed URL, reporting progress back
	 * to the given CrawlProgressResponder (if it is not null). Once the crawl is finished, it
	 * is saved to a data file.
	 * @param seedURL The URL of the page where the crawl should be started
	 * @listener The CrawlProgressResponder to report progress to. If it is <b>null</b>, it will be ignored.
	 */
	public void crawlWithProgressReporting(String seedURL, CrawlProgressResponder listener)
	{
		Crawler crawler = new Crawler(seedURL);
		crawler.go(listener);
		this.webIndex = WebIndex.build(crawler, listener);
		this.hasIndex = true;
		this.webIndex.saveTo(DATA_PATH + DATA_PREFIX + DATA_EXT);
	}

	/**
	 * Gets the list of outgoing links (URLs) for the given page. Passes the task through to the WebIndex.
	 * @param url URL string for the page for which out-links should be listed.
	 * @return List of outgoing links from the specified page
	 */
	public List<String> getOutgoingLinks(String url)
	{
		return this.webIndex.getOutgoingLinks(url);
	}

	/**
	 * Gets the list of incoming links (URLs) for the given page. Passes the task through to the WebIndex.
	 * @param url URL string for the page for which in-links should be listed.
	 * @return List of known incoming links to the specified page
	 */
	public List<String> getIncomingLinks(String url)
	{
		return this.webIndex.getIncomingLinks(url);
	}

	/**
	 * Gets the PageRank of the given page. Passes the task through to the WebIndex.
	 * @param url URL string of the page.
	 * @return PageRank value of the specified page
	 */
	public double getPageRank(String url)
	{
		return this.webIndex.getPageRank(url);
	}

	/**
	 * Gets the inverse document frequency of the given word in the WebIndex.
	 * @param word The word for which to get IDF
	 * @return IDF of the word in the corpus
	 */
	public double getIDF(String word)
	{
		return this.webIndex.getIDF(word);
	}

	/**
	 * Gets the term frequency of the given word relative to the given page
	 * @param url The URL string of the page against which the word's TF is measured
	 * @param word The word for which to get TF
	 * @return TF of the word in the given page
	 */
	public double getTF(String url, String word)
	{
		return this.webIndex.getTF(url, word);
	}

	/**
	 * Gets the TF-IDF value for the given word relative to the given page. Passes the task through to the WebIndex.
	 * @param url The URL string of the page against which the word's TF-IDF is measured
	 * @param word The word for which to get TF-IDF
	 * @return TF-IDF of the word in the given page
	 */
	public double getTFIDF(String url, String word)
	{
		return this.webIndex.getTFIDF(url, word);
	}

	/**
	 * Performs a search of the index with the given query, returning a List of SearchResult views of the results,
	 * which is used primarily by the test suite. The size of the returned list is capped at the specified amount.\
	 * <br/>The task is delegated to the underlying WebIndex.
	 * @param query The search query string
	 * @param boost Whether PageRanks should be factored in when sorting the results
	 * @param amount The max number of search results to put in the list
	 * @return A List view of SearchResultPlus
	 * @see SearchResult
	 */	
	public List<SearchResult> search(String query, boolean boost, int X)
	{
		return this.webIndex.search(query, boost, X);
	}
	
	/**
	 * Performs a search of the index with the given query, returning a List of SearchResultPlus views of the results,
	 * which includes more information than a SearchResult does. The size of the returned list is capped at the specified amount.
	 * <br/>The task is delgated to the underlying WebIndex.
	 * @param query The search query string
	 * @param boost Whether PageRanks should be factored in when sorting the results
	 * @param amount The max number of search results to put in the list
	 * @return A List view of SearchResultPlus
	 * @see SearchResultPlus
	 */	
	public List<SearchResultPlus> searchPlus(String query, boolean boost, int X)
	{
		return this.webIndex.searchPlus(query, boost, X);
	}

}
