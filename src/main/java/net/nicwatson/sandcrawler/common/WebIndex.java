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
 
package net.nicwatson.sandcrawler.common;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cs1406z.test.SearchResult;
import net.nicwatson.sandcrawler.crawl.Crawler;
import net.nicwatson.sandcrawler.crawl.UnprocessedPage;
import net.nicwatson.sandcrawler.frontend.CrawlProgressResponder;
import net.nicwatson.sandcrawler.frontend.CrawlProgressResponder.ProgressStage;
import net.nicwatson.sandcrawler.search.SearchResultImpl;
import net.nicwatson.sandcrawler.search.SearchResultPlus;

/**
 * The WebIndex object is the nexus of all web page and word incidence data. It stores two lookup maps: the <code>words</code> 
 * map for retrieving GlobalWordStats from word Strings, and the <code>pages</code> map for retrieving indexed pages from URL
 * strings. It also retains metadata such as the total number of words and documents indexed, and the time of indexing.
 * It provides methods for nearly every operation that needs to be performed when searching, some of which pass through to
 * objects housed within the index maps.
 * @author Nic
 *
 */
public class WebIndex implements Serializable
{
	private static final long serialVersionUID = 1823164349044572242L;

	/**
	 * This is the alpha value we will use for calculating page ranks
	 */
	public static final double ALPHA = 0.1;
	
	/**
	 * The Euclidean-distance threshold at which we can stop iterating pagerank calculations
	 */
	public static final double THRESHOLD = 0.0001;

	/**
	 * The URL of the page that was used to seed the web crawl on which this index was based
	 */
	protected String seedURL;
	
	/**
	 * Stores a datestamp/timestamp for when the index was created. This is initialized in the constructor and should
	 * not otherwise be modified.
	 */
	protected final Date crawlTime;

	/**
	 * The number of total documents contained in this index. This is initialized to zero in the constructor, but
	 * updated when the index contents are processed.
	 */
	protected int totalDocs;
	
	/**
	 * The number of total words known in this index. This is initialized to zero in the constructor, but
	 * updated when the index contents are processed.
	 */
	protected int totalWords;

	/**
	 * A mapping of word keys (Strings) onto GlobalWordStat objects, which in turn track which pages contain the word.
	 * <p/>
	 * The declaration is abstract and implementation-agnostic, but LinkedHashMap is the recommended concrete
	 * implementation type for both expedient O(1) lookups and efficient sequential iteration; while the preservation of
	 * insertion order isn't essential to the program, some methods do need to iterate over the elements of the map.
	 * Iterating over a LinkedHashMap will generally be more efficient because runtime will be proportional to the
	 * number of entries rather than the hashtable's capacity.
	 * 
	 * @see LinkedHashMap
	 */
	Map<String, GlobalWordStat> words;

	/**
	 * A mapping of page keys (URL Strings) onto IndexedPage objects, which in turn track stats for the appearance of
	 * words on their respective pages.
	 * <p/>
	 * The declaration is abstract and implementation-agnostic, but LinkedHashMap is the recommended concrete
	 * implementation type for both expedient O(1) lookups and efficient sequential iteration; while the preservation of
	 * insertion order isn't essential to the program, some methods do need to iterate over the elements of the map.
	 * Iterating over a LinkedHashMap will generally be more efficient because runtime will be proportional to the
	 * number of entries rather than the hashtable's capacity.
	 * 
	 * @see LinkedHashMap
	 */
	Map<String, IndexedPage> pages;
	
	/**
	 * Initializes a new WebIndex. The constructor is public due to the need for it to be visible during deserialization.
	 * However, consumers outside the class should use <code>build()</code> or <code>makeIndexFrom()</code> to create a WebIndex instance.
	 * This ensures that the index actually carries out the necessary processing on its page data before any of its fields
	 * or methods can be accessed externally.
	 */
	public WebIndex() {
		this.crawlTime = Calendar.getInstance().getTime();
		this.totalDocs = 0;
		this.totalWords = 0;
		this.words = new LinkedHashMap<>();
		this.pages = new LinkedHashMap<>();
	}
	
	/**
	 * Builds and returns a new WebIndex using data from the provided Crawler, with progress reported back
	 * to the specified CrawlProgressResponder
	 * 
	 * @param crawler A Crawler with a populated list of "unprocessed" pages from a completed web crawl.
	 * @param listener An object that implements CrawlProgressResponder to report progress on crawl tasks
	 * to the user or two other program modules. (This doesn't quite work.)
	 * @return The newly built WebIndex
	 */
	public static WebIndex build(Crawler crawler, CrawlProgressResponder listener)
	{
		return makeIndexFrom(crawler, listener);
	}
	
	/**
	 * Builds and returns a new WebIndex using data from the provided Crawler.
	 * 
	 * @param crawler A Crawler with a populated list of "unprocessed" pages from a completed web crawl.
	 * @return The newly built WebIndex
	 */
	public static WebIndex build(Crawler crawler)
	{
		return makeIndexFrom(crawler, null);
	}

	/**
	 * Builds and returns a new WebIndex using data from a provided Crawler after it has completed its crawl
	 * <p/>
	 * Building an index includes the following:
	 * <ul>
	 * <li/>Creating an IndexedPage for each UnprocessedPage
	 * <li/>Populating each such IndexedPage with word count stats
	 * <li/>Calculating TF and TF_IDF for each word in each such IndexedPage
	 * <li/>Populating each IndexedPage's list of out-links using data from the corresponding UnprocessedPage
	 * <li/>Synchronizing each IndexedPage's list of in-links by examining out-links to make sure they are reciprocal
	 * <li/>Adding each unique word discovered to a global word list, with associated word count and IDF
	 * <li/>Calculating page ranks for each IndexedPage
	 * </ul>
	 * 
	 * @param page A Crawler that has finished its crawl and is populated with a list of unprocessed pages
	 * @return The newly built WebIndex
	 */
	public static WebIndex makeIndexFrom(Crawler crawler, CrawlProgressResponder listener)
	{
		Set<UnprocessedPage> pages = crawler.getUnprocessedPages();
		WebIndex newIndex = new WebIndex();

		newIndex.seedURL = crawler.seedUrl;
		
		if(listener != null)
		{
			// Inform the progress listener that we have advanced to the parsing stage
			listener.updateProgress(ProgressStage.PARSING, 0, 0);
		}
		
		// STAGE 1. INITIALIZATION AND PARSING
		// Iterate through the unprocessed pages
		for (UnprocessedPage p : pages)
		{
			// Each unprocessed page is parsed into an IndexedPage and its word stats are calculated.
			IndexedPage ip = new IndexedPage(p.getURLString(), newIndex, p);
			// Parsed pages are added to the index
			newIndex.insertPage(ip);
		}

		if(listener != null)
		{
			// Inform the progress listener that we have advanced to analyzing links
			listener.updateProgress(ProgressStage.LINKING, 0, 0);
		}
		
		// STAGE 2. CALCULATION AND LINK ANALYSIS
		// For each page added to the index, we need to make sure its words' TF-IDF stats (and thus their TF and IDF stats)
		// are calculated and up to date. We also generate a reciprocal in-link on the appropriate page for every out-link
		// found.
		for (IndexedPage doc : newIndex.getPages().values())
		{
			for (DocumentWordStat w : doc.getWordList())
			{
				// We don't care what the value of the TF-IDF is right now, but calling getTFIDF will make sure that it
				// gets calculated and cached in the DocumentWordStat so that it's ready to go when needed. The TFs and
				// IDFs themselves will also be calculated and cached as part of the process.
				w.getTFIDF();
			}
			
			for (String s : doc.getOutLinks())
			{
				if (newIndex.getPages().containsKey(s))
				{
					newIndex.getPages().get(s).addInLink(doc.getURL());
				}
			}
		}

		if(listener != null)
		{
			// Inform the progress listener that we have advanced to calculating page ranks
			listener.updateProgress(ProgressStage.RANKING, 0, 0);
		}
		
		newIndex.crunchPageRanks(ALPHA, THRESHOLD);
		
		if(listener != null)
		{
			// Inform the progress listener that we have completed processing the index.
			listener.updateProgress(ProgressStage.DONE, 0, 0);
		}
		
		return newIndex;
	}
	
	/**
	 * Getter for seedURL
	 * @return The URL of the page that was used to seed the web crawl on which this index was based
	 */
	public String getSeedURL()
	{
		return this.seedURL;
	}
	
	/**
	 * Getter for crawlTime
	 * @return The Date/time at which this index was generated
	 */
	public Date getCrawlTime()
	{
		return this.crawlTime;
	}
	
	/**
	 * Returns the number of total pages known to the index.
	 * @return The number of total pages known to the index.
	 */
	public int getTotalDocs()
	{
		return this.totalDocs;
	}
	
	/**
	 * Returns the number of total words known to the index.
	 * @return The number of total words known to the index.
	 */
	public int getTotalWords()
	{
		return this.totalWords;
	}
	
	/**
	 * Getter for <code>pages</code>, the map from URL strings to IndexPages
	 * @return The map from URL strings to IndexPages
	 */
	private Map<String, IndexedPage> getPages()
	{
		return this.pages;
	}

	/**
	 * Getter for <code>pages</code>, the map from word strings to GlobalWordStats
	 * @return The map from word strings to GlobalWordStats
	 */
	@SuppressWarnings("unused")
	private Map<String, GlobalWordStat> getWords()
	{
		return this.words;
	}

	/**
	 * Determines whether the given word exists in the global word index
	 * @param word String representing the word to find
	 * @return <code>true</code> if the word is already in the index, <code>false</code> otherwise.
	 */
	public boolean knowsWord(String word)
	{
		return this.words.containsKey(word);
	}
	
	/**
	 * Determines whether the given page exists in the pages index
	 * @param pageURL String representing the URL of the page to find
	 * @return <code>true</code> if the page is already in the index, <code>false</code> otherwise.
	 */
	public boolean hasPage(String pageURL)
	{
		return this.pages.containsKey(pageURL);
	}
	
	/**
	 * Retrieves the page with the specified URL from the index
	 * @param pageURL String representing the URL of the page to find
	 * @return The <code>IndexedPage</code> representation of the specified page, or <code>null</code> if the page was not found in the index.
	 */
	public IndexedPage getPage(String pageURL)
	{
		if(this.hasPage(pageURL))
		{
			return this.pages.get(pageURL);
		}
		return null;
	}
	
	/**
	 * Retrieves the GlobalWordStat info for the given word from the index's global word list
	 * @param word String representing the word to find
	 * @return The <code>GlobalWordStat</code> for the specified word, or <code>null</code> the word is not indexed.
	 */
	public GlobalWordStat getGlobalWordStat(String word)
	{
		if(this.knowsWord(word))
		{
			return this.words.get(word);
		}
		return null;
	}
	
	/**
	 * Reports how many documents in the index contain the given word. This method simply looks up the corresponding
	 * <code>GlobalWordStat</code>, if it exists, and calls its <code>getGlobalOccurrence()</code> method. If the word is
	 * not indexed, then there is no <code>GlobalWordStat</code> object, and the return value will be 0.
	 * @param word String representing the word to find
	 * @return The number of indexed documents that contain the word. If the word is not indexed, its global occurrence
	 * is zero.
	 */
	public int getGlobalOccurrence(String word)
	{
		if(this.knowsWord(word))
		{
			return this.getGlobalWordStat(word).getGlobalOccurrence();
		}
		return 0;
	}
	

	
	/**
	 * Prompts the index to "learn" a new word by creating a new GlobalWordStat entry for it, if
	 * none yet exists.
	 * @param word The word (a String) to put into the index
	 * @return <code>true</code> if the word was previously unknown.
	 */
	public boolean learnWord(String word)
	{
		if(!this.knowsWord(word))
		{
			learnWordUnchecked(new GlobalWordStat(word, this));
			return true;
		}
		return false;
	}
	
	/**
	 * Prompts the index to "learn" a new word from a given GloalWordStat by putting it in the
	 * wordmap, if it is not already present.
	 * @param word The word (a GlobalWordStat) to put into the index
	 * @return <code>true</code> if the word was previously unknown.
	 */
	public boolean learnWord(GlobalWordStat wordStat)
	{
		if(!this.knowsWord(wordStat.getWord()))
		{
			this.learnWordUnchecked(wordStat);
			return true;
		}
		return false;
	}
	
	/**
	 * Inserts a given GlobalWordStat into the wordmap, and increments the total unique word count,
	 * without checking first if the word is already in the map. This method is declared <b>private</b> 
	 * to prevent erroneously replacing existing word stats or incrementing the word counter. Outside
	 * of the class, consumers will always access this via the safety-checking <code>learnWord()</code> methods.
	 * @param wordStat The word (a GlobalWordStat) to put into the wordmap
	 */
	private void learnWordUnchecked(GlobalWordStat wordStat)
	{
		this.words.put(wordStat.getWord(), wordStat);
		this.totalWords++;
	}
	
	/**
	 * A convenience method that checks if the given word is in the wordmap. If it is, the existing GlobalWordStat
	 * is looked up and returned. If not, a new GlobalWordStat is created, put in the wordmap, and returned.
	 * @param wordStat The word (a GlobalWordStat) to put into the wordmap
	 * @return The GlobalWordStat (retrieved or new) for the given word
	 */
	public GlobalWordStat getOrCreateGlobalWordStat(String word)
	{
		if(!this.knowsWord(word))
		{
			GlobalWordStat newWord = new GlobalWordStat(word, this);
			this.learnWordUnchecked(newWord);
			return newWord;
		}
		return this.getGlobalWordStat(word);
	}

	/**
	 * Inserts the given IndexedPage document into the pagemap, and increments the count of total documents
	 * @param doc The IndexedPage to put into the index
	 * @return <code>true</code> if the document was not already present in the pagemap
	 */
	public boolean insertPage(IndexedPage doc)
	{
		if (!this.hasPage(doc.getURL()))
		{
			this.pages.put(doc.getURL(), doc);
			this.totalDocs++;
			return true;
		}
		return false;
	}
	
	/**
	 * Reports the inverse document frequency (IDF) of the given word within this index. If the word is indexed, this method
	 * looks up the corresponding <code>GlobalWordStat</code> and calls its <code>getIDF()</code> method. If the word is
	 * not indexed, the IDF is zero.
	 * @param word The word for which to retrieve IDF
	 * @return The IDF of the word
	 */
	public double getIDF(String word)
	{
		if (this.knowsWord(word))
		{
			return this.getGlobalWordStat(word).getIDF();
		}
		return 0;
	}
	
	/**
	 * Reports the term frequency (TF) of the given word within the given page. If the page is indexed, this method
	 * looks up the corresponding <code>IndexedPage</code> and calls its <code>getTF()</code> method for the word. If
	 * the word is not found in the document, the TF will be zero. If the page is not indexed, the return value will
	 * be zero and additionally a warning message is printed to the console. Note that this method does not check
	 * the word against the global word list.
	 * @param url URL string for the page
	 * @param word The word for which to retrieve TF
	 * @return The TF of the word
	 */
	public double getTF(String url, String word)
	{
		if (this.hasPage(url))
		{
			return this.getPage(url).getTF(word);
		}
		return 0;
	}

	/**
	 * Reports the tf_idf of the given word within the given page. If the page is indexed, this method
	 * looks up the corresponding <code>IndexedPage</code> and calls its <code>getTFIDF()</code> method for the word.
	 * <br/>If the word isn't in the global word list, idf = 0 and therefore tf_idf = 0.
	 * <br/>If the page doesn't contain the word, or the page isn't indexed at all, tf = 0 and therefore tf_idf = 0 
	 * @param url URL string for the page
	 * @param word The word for which to retrieve tf_idf
	 * @return The TF of the word
	 */
	public double getTFIDF(String url, String word)
	{
		if (this.knowsWord(word) && this.hasPage(url))
		{
			return this.getPage(url).getTFIDF(word);
		}
		return 0;
	}
	
	/**
	 * Reports the PageRank of the page with the given URL. If the page is indexed, this method
	 * looks up the corresponding <code>IndexedPage</code> and calls its <code>getPageRank()</code> method.	
	 * @param url The URL of the page for which to fetch the PageRank score
	 * @return The PageRank score of the page for the given URL. If the page is not indexed, the return value is -1.
	 */
	public double getPageRank(String url)
	{
		if(this.hasPage(url))
		{
			return this.getPage(url).getPageRank();
		}
		return -1;
	}
	
	/**
	 * Returns a list of all known links that link to the page with the given URL (looks up and passes through to the 
	 * IndexedPage for the given URL)	
	 * @param url
	 * @return A list of all known links that link to the page with the given URL
	 */
	public List<String> getIncomingLinks(String url)
	{
		if (!this.hasPage(url))
		{
			return null;
		}
		return List.copyOf(this.getPage(url).getInLinks());
	}

	/**
	 * Returns a list of all known links from the page with the given URL (looks up and passes through to the 
	 * IndexedPage for the given URL)	
	 * @param url
	 * @return A list of all known links from the page with the given URL. (Only links to other pages that are 
	 * also indexed are included.)
	 */
	public List<String> getOutgoingLinks(String url)
	{
		if (!this.hasPage(url))
		{
			return null;
		}
		return List.copyOf(this.getPage(url).getOutLinks());
	}
		
	/**
	 * Calculates the cosine similarity between two given IndexedDocuments. Typically, one of them will represent a
	 * search query, while the other represents a search hit.
	 * 
	 * @param q The first document to compare (the search query)
	 * @param d The second document ot compare (a web page found by the search)
	 * @return The cosine similarity calculated between the two documents. This will be a number between 0 and 1, where
	 *         0 signifies a very poor match, and 1 is a perfect match.
	 */
	public double cosineSimilarity(MappedDocument q, MappedDocument d)
	{
		double sum_qd = 0;
		double sum_q2 = 0;
		double sum_d2 = 0;
		for (DocumentWordStat word : q.getWordList())
		{
			sum_q2 += Math.pow(q.getTFIDF(word.getWord()), 2);
			
			if (d.containsWord(word.getWord()))
			{
				sum_qd += (q.getTFIDF(word.getWord()) * d.getTFIDF(word.getWord()));
				sum_d2 += Math.pow(d.getTFIDF(word.getWord()), 2);
			}
		}
		if(sum_d2 == 0 || sum_q2 == 0)
		{
			return 0;
		}
		return sum_qd / ((Math.sqrt(sum_d2) * Math.sqrt(sum_q2)));
	}

	/**
	 * Calculates the page ranks for all the IndexedPages in the index
	 * @param alpha The alpha value to use
	 * @param convergence The Euclidean distance threshold between iterations at which to stop the calculation 
	 */
	public void crunchPageRanks(double alpha, double convergence)
	{
		// Matrices and vectors for temporarily storing intermediate calculations
		double [][] pageRankMatrix = new double[totalDocs][totalDocs];		// Used to gradually build the transition probability matrix
		IndexedPage [] allDocs = new IndexedPage[totalDocs];				// An index of all documents, for looking up in-links
		allDocs = pages.values().toArray(allDocs);

		for (int i = 0; i < totalDocs; i++)		// For each row in the matrix
		{ 
			int rowTally = 0;
			LinkedList<Integer> revisitables = new LinkedList<>();
			for (int j = 0; j < totalDocs; j++)	// Iterate along the row
			{
				// We don't actually need a separate binary adjacency matrix if we do lookups into allDocs array for in-links on the fly
				boolean adjacent = allDocs[i].linksTo(allDocs[j]);
				if (adjacent)
				{
					// Equivalent to counting the number of 1's in a row. 
					rowTally++;
					// We keep a list of the places were there was an adjacency, so we don't have to iterate back over the entire row later.
					revisitables.add(j);
				}
				// Instead of adding alpha/N to every cell at the end, we can start with it as an intial value 
				pageRankMatrix[i][j] = (alpha / totalDocs);
			}
			if (rowTally > 0)
			{
				// There was at least one '1' in this row
				for (Integer num : revisitables)
				{
					// Divide each '1' by the tally of 1's in the row
					// We can also multiply in the (1 - alpha) at this point.
					pageRankMatrix[i][num] += (1.0 - alpha) / (double) rowTally;
				}
			}
			else
			{
				// No 1's in this row. This is a sink.
				for (int num = 0; num < totalDocs; num++)
				{
					// Replace each transition probability with 1/N.
					// We can also multiply in the (1 - alpha) at this point.
					pageRankMatrix[i][num] += (1.0 - alpha) / (double) totalDocs;
				}
			}
		}

		// Initialize the PageRank vector
		double vector[] = new double[totalDocs]; 
		Arrays.fill(vector, 1 / (double)totalDocs);
		
		// To calculate Euclidean distance between iterations, we'll need to keep track of the previous vector at each stage
		double prevVector[];

		int i = 1;
		do
		{
			prevVector = Arrays.copyOf(vector, totalDocs);									// Save a copy of the previous vector
			vector = MathsHelper.vectorMultiplySquareMatrix(prevVector, pageRankMatrix);	// Multiply vector by transition matrix
			i++;
		} while (MathsHelper.euclideanDistance(vector, prevVector) > convergence);
		// The threshold has now been reached and we can stop.
		
		// Update every document with its calculated PageRank
		for (i = 0; i < totalDocs; i++)
		{
			allDocs[i].setPageRank(vector[i]);
		}
	}

	/**
	 * Performs a search of the index with the given query, and builds a TreeSet of all the pages sorted by their score
	 * for the search.
	 * @param query The search query string
	 * @param boost Whether PageRanks should be factored in when sorting the results
	 * @return A TreeSet of all the indexed pages, sorted by their score for the search
	 */
	public TreeSet<SearchResultPlus> searchTree(String query, boolean boost)
	{
		// Parse the query string into an IndexedQuery
		MappedDocument queryDoc = new MappedQuery(this, MappedDocument.tokenize(query));

		TreeSet<SearchResultPlus> sortedResults = new TreeSet<SearchResultPlus>();

		for (IndexedPage candidate : this.getPages().values())
		{
			// Score each page and then put it into the tree for sorting
			double boostFactor = 1;
			if(boost)
			{
				boostFactor = candidate.getPageRank();
			}
			double score = cosineSimilarity(queryDoc, candidate) * boostFactor;
			SearchResultPlus result = new SearchResultImpl(candidate, score);
			result.setBoosted(boost);
			sortedResults.add(result);
		}
		return sortedResults;
	}

	/**
	 * Performs a search of the index with the given query, returning a List of SearchResult views of the results,
	 * which is used primarily by the test suite.
	 * @param query The search query string
	 * @param boost Whether PageRanks should be factored in when sorting the results
	 * @return A List view of SearchResults
	 * @see SearchResult
	 */
	public List<SearchResult> search(String query, boolean boost)
	{
		return List.copyOf(searchTree(query, boost));
	}
	
	/**
	 * Performs a search of the index with the given query, returning a List of SearchResultPlus views of the results,
	 * which includes more information than a SearchResult does.
	 * @param query The search query string
	 * @param boost Whether PageRanks should be factored in when sorting the results
	 * @return A List view of SearchResultPlus
	 * @see SearchResultPlus
	 */
	public List<SearchResultPlus> searchPlus(String query, boolean boost)
	{
		return List.copyOf(searchTree(query, boost));
	}
	
	/**
	 * Performs a search of the index with the given query, returning a List of SearchResult views of the results,
	 * which is used primarily by the test suite. The size of the returned list is capped at the specified amount.
	 * @param query The search query string
	 * @param boost Whether PageRanks should be factored in when sorting the results
	 * @param amount The max number of search results to put in the list
	 * @return A List view of SearchResultPlus
	 * @see SearchResult
	 */	
	public List<SearchResult> search(String query, boolean boost, int amount)
	{
		List<SearchResult> masterList = this.search(query, boost);
		return masterList.subList(0, Math.min(amount, masterList.size()));
	}

	/**
	 * Performs a search of the index with the given query, returning a List of SearchResultPlus views of the results,
	 * which includes more information than a SearchResult does. The size of the returned list is capped at the specified amount.
	 * @param query The search query string
	 * @param boost Whether PageRanks should be factored in when sorting the results
	 * @param amount The max number of search results to put in the list
	 * @return A List view of SearchResultPlus
	 * @see SearchResultPlus
	 */	
	public List<SearchResultPlus> searchPlus(String query, boolean boost, int amount)
	{
		List<SearchResultPlus> masterList = this.searchPlus(query, boost);
		return masterList.subList(0, Math.min(amount, masterList.size()));
	}

	/**
	 * Saves this WebIndex object to the specified file (by passing through to the static method)
	 * @param path The path to the data file where the index should be saved 
	 * @return <code>true</code> if the operation was successful
	 */
	public boolean saveTo(String path)
	{
		return saveIndexTo(this, path);
	}
	
	/**
	 * Saves the given indexed crawl information (WebIndex) to the specified data file
	 * @param index The WebIndex to save
	 * @param path The file to which the data should be saved.
	 * @return <code>true</code> if the operation was successful
	 */
	public static boolean saveIndexTo(WebIndex index, String path)
	{
		System.out.println("\n\nWriting output file...");
		try
		{
			FileOutputStream fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(index);
			oos.close();
			System.out.println("Done!");
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Could not write file: " + path);
			e.printStackTrace(System.err);
			return false;
		}
		catch (IOException e)
		{
			System.err.println("Error while writing crawl data to file " + path);
			e.printStackTrace(System.err);
			return false;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return "\n=====\nTime: " + this.crawlTime + "\n" + "Num docs: " + this.totalDocs + "\n" + "Index: "
				+ this.words.toString();

	}
}
