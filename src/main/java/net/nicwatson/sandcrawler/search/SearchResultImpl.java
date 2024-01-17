package net.nicwatson.sandcrawler.search;

import net.nicwatson.sandcrawler.common.IndexedPage;


/**
 * The SearchResultImpl class represents a single result returned by the search engine: a page
 * (IndexedPage) with a score (double) between 0 (poor match) and 1 (perfect match).
 * <p/>
 * SearchResultImpl implements Comparable<SearchResultImpl> to facilitate ranking results by their
 * score using any data structure or method that calls compareTo(). Results are sorted
 * descending by their scores (rounded to three decimal places), and then lexicographically
 * ascending by page title.
 * 
 * @author Nic Watson
 *
 */
public class SearchResultImpl implements SearchResultPlus, Comparable<SearchResultImpl>
{
	/**
	 * How many digits after the decimal point should we consider when sorting results?
	 */
	private static final int DEFAULT_PRECISION = 3;
	
	/**
	 * Formatting string used to round scores and prepare them for lexicographic comparison
	 */
	private static final String SCORE_FORMAT = "%1." + DEFAULT_PRECISION + "f";
	
	
	/**
	 * The document found for this search result
	 */
	protected IndexedPage document;
	
	/**
	 * The score of this search result
	 */
	protected double score;
	
	/**
	 * Is this search result from a PageRank-boosted search?
	 */
	private boolean boosted;
	
	/**
	 * Creates a new SearchResult for the given document, with the given score, and a
	 * caller-specified sorting precision.
	 * @param doc The document found for this seach result
	 * @param score The score of this document for this search
	 */
	public SearchResultImpl(IndexedPage doc, double score)
	{
		this.document = doc;
		this.score = score;
		this.boosted = false;
	}
	
	/**
	 * Getter method for score. 
	 * @return The score of this document for this search
	 */
	public double getScore()
	{
		return this.score;
	}
	
	/**
	 * Retrieves the page title of the found document. 
	 * @return Page title from the document found by this search result
	 */
	public String getPageTitle()
	{
		return this.document.getTitle();
	}
	
	/**
	 * Getter for <code>boosted</code> property
	 * @return <code>true</code> iff this search result is from a PageRank-boosted search
	 */
	public boolean isBoosted()
	{
		return this.boosted;
	}
	
	/**
	 * Setter for <code>boosted</code> property
	 * @param boolean value to set for <code>boosted</code> property (i.e. true if this is a boosted search)
	 */
	public void setBoosted(boolean set)
	{
		this.boosted = set;
	}
			
	/**
	 * Determines the sort order priority between two SearchResults.
	 * The candidates are first sorted in <em>descending</em> order of score, by embedding the 
	 * rounded scores into strings that are then compared using <code>String</code>'s own
	 * <code>compareTo()</code> method.
	 * If the score strings are identical, the candidates are ordered in <em>ascending</em>
	 * lexicographic order by page title.
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(SearchResultImpl other)
	{
		String myScoreStr = String.format(SCORE_FORMAT, this.getScore());
		String otherScore = String.format(SCORE_FORMAT, other.getScore());

		int compResult = 0 - myScoreStr.compareTo(otherScore);
		if(compResult != 0)
		{
			return compResult;
			
		}
		
		return this.document.getTitle().compareTo(other.document.getTitle());
	}

	@Override
	public String getTitle()
	{
		return this.document.getTitle();
	}

	@Override
	public String getURL()
	{
		return this.document.getURL();
	}

	@Override
	public double getPageRank()
	{
		return this.document.getPageRank();
	}
}
