package cs1406z.test;

/**
 * The SearchResult interface is copied from the provided template. It provides the minimum 
 * information about search results needed by the test suite (page title and search score).
 * It is indireclty implemented by net.nicwatson.sandcrawler.search.SearchResultImpl
 * Note: SearchResultImpl actually implements the sub-interface net.nicwatson.sandcrawler.search.SearchResultPlus
 * which includes methods for retrieving extra information about the search result. The test suite
 * only needs the SearchResult view, but the interactive JavaFX application needs a SearchResultPlus.
 */
public interface SearchResult
{
    /**
     * Returns the title of the page this search result is for.
     * @return Page title of the result
     */
    String getTitle();

    /**
     * Returns the search score for the page this search result is for.
     * @return Search score for the result
     */
    double getScore();
}
