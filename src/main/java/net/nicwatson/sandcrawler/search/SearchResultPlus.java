package net.nicwatson.sandcrawler.search;
import cs1406z.test.SearchResult;

/**
 * SearchResultPlus is a subinterface of SearchResult that includes additional methods
 * for retrieving the URL and page rank of results, as well as the status of whether
 * or not the search was pagerank-boosted
 */
public interface SearchResultPlus extends SearchResult
{
	public String getURL();
	public double getPageRank();
	public boolean isBoosted();
	public void setBoosted(boolean set);
}
