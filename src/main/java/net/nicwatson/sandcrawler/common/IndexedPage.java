package net.nicwatson.sandcrawler.common;

import java.util.LinkedHashSet;
import java.util.Set;
import net.nicwatson.sandcrawler.crawl.UnprocessedPage;
import java.io.Serializable;

/**
 * An IndexedPage is an IndexedDocument that represents an actual web page that has been crawled 
 * (as opposed to representing an ephemeral "document" such as a search query). In addition to all of
 * the word stats of an IndexedDocument, the IndexedPage retains information about the original 
 * document's URL, title, page rank, and incoming/outgoing hyperlinks.
 * @author Nic
 *
 */
public class IndexedPage extends MappedDocument implements Serializable
{
	private static final long serialVersionUID = 7984052549460749731L;
	
	/**
	 * The urlKey is the URL at which the original document was found during crawl. It is also used as 
	 * a key to distinguish IndexedDocuments from each other. Thus whenever there is a need to tell
	 * IndexedPages apart, consumers must ensure that all such pages have unique urlKeys.
	 */
	protected String urlKey;
	
	/**
	 * The title of the page, as retrieved from its <code>title</code> tag.
	 */
	protected String title;
	
	/**
	 * Calculated PageRank for this page
	 */
	protected double pageRank;
	
	/**
	 * A list of (unique) outgoing hyperlinks from this page 
	 */
	protected Set<String> outLinks;
	
	/**
	 * A list of pages (by URL) that link in to this page
	 */
	protected Set<String> inLinks;
	
	/**
	 * Deserialization seems to require a public default constructor and a non-null urlKey (since urlKey is used for
	 * <code>hashCode()</code> and <code>equals()</code>. In-program consumers should instead use
	 * <code>IndexedPage(String urlKey, WebIndex index, UnprocessedPage page)</code> to instantiate an IndexedPage.
	 */
	public IndexedPage()
	{
		this.urlKey = "dummy";
	}
	
	/**
	 * Creates a new IndexedPage out of a given "unprocessed" page document. The page's word stats are parsed as part
	 * of the process.
	 * @param urlKey The URL of the page. This should be unique to this page.
	 * @param index The WebIndex to which this document will belong.
	 * @param page The base data from which the IndexedPage will be constructed. The UnprocessedPage has the URL, title,
	 * and link information, as well as the raw text content.
	 */
	public IndexedPage(String urlKey, WebIndex index, UnprocessedPage page)
	{
		super(index);
		this.urlKey = urlKey;
		this.title = page.extractTitle();
		this.outLinks = page.getLinks();
		this.inLinks = new LinkedHashSet<String>();
		this.initializeWordMap(MappedDocument.tokenize(page.extractContentTexts()));
	}
	
	/**
	 * Getter for page title
	 * @return The title of the page
	 */
	public String getTitle()
	{
		return this.title;
	}

	/**
	 * Getter for urlKey
	 * @return The URL (and unique identifying key) for the webpage represented by this object
	 */
	public String getURL()
	{
		return this.urlKey;
	}
	
	/**
	 * Getter for outLinks
	 * @return A Set of all URLs to which this page links
	 */
	public Set<String> getOutLinks()
	{
		return this.outLinks;
	}
	
	/**
	 * Getter for inLinks
	 * @return A Set of all URLs for pages that contain links into this page
	 */
	public Set<String> getInLinks()
	{
		return this.inLinks;
	}

	/**
	 * Setter for pageRank
	 * @param rank The value for setting this page's pageRank property
	 */
	public void setPageRank(double rank)
	{
		this.pageRank = rank;
		
	}
	
	/**
	 * Getter for pageRank
	 * @return The PageRank for this page, as calculated by the crawl
	 */
	public double getPageRank()
	{
		return this.pageRank;
	}
	
	/**
	 * Indicates whether this IndexedPage has an outgoing link to another given IndexedPage. The
	 * method is delegated to an overloaded version of <code>linksTo()</code> that looks up the
	 * other page's URL in this page's outLinks set.
	 * @param other The other IndexedPage
	 * @return <code>true</code> iff there is a link from this page to the other page
	 */
	public boolean linksTo(IndexedPage other)
	{
		return this.linksTo(other.getURL());
	}
	
	/**
	 * Indicates whether this IndexedPage has an outgoing link a page with the given URL.
	 * @param other The other page's URL
	 * @return <code>true</code> iff there is a link from this page to the other page
	 */
	public boolean linksTo(String other)
	{
		return this.outLinks.contains(other);
	}
	
	/**
	 * Indicates whether this IndexedPage has an incoming link from another given IndexedPage.
	 * Note that <code>linksTo()</code> and <code>linkedFrom()</code> should always be symmetrical:
	 * i.e. if a.linksTo(b) then b.linkedFrom(a) 
	 * @param other The other IndexedPage
	 * @return <code>true</code> iff there is a link to this page from the other page
	 */
	public boolean linkedFrom(IndexedPage other)
	{
		return other.linksTo(this);
	}
	
	/**
	 * Indicates whether this IndexedPage has an incoming link from another page with a given URL.
	 * Requires looking up the other page's URL in the underlying WebIndex 
	 * @param other The other page's URL
	 * @return <code>true</code> iff there is a link to this page from the other page
	 */
	public boolean linkedFrom(String other)
	{
		return this.index.hasPage(other) && this.index.getPage(other).linksTo(this);
	}
	
	/**
	 * Registers the given URL as an incoming link to this page
	 * @param urlStr The URL of the page from which to register an incoming link
	 * @return <code>true</code> if this page did not previously have the given URL recorded as an incoming link
	 */
	public boolean addInLink(String urlStr)
	{
		return this.inLinks.add(urlStr);
	}

	@Override
	protected void handleNewWord(String word)
	{
		// When encountering a word isn't yet in our wordmap, we need to create a new DocumentWordStat for it.
		// The word might or might not already be known to the web index. If it is not known, we will need to
		// add a new GlobalWordStat for it. Otherwise, we retrieve the GlobalWordStat that the index already
		// has for it.
		GlobalWordStat globalStat = this.index.getOrCreateGlobalWordStat(word);
		this.insertWordStat(globalStat);
		globalStat.addPage(this);
	}
	
	/**
	 * Calculates the hashCode for this IndexedDocument, based on its urlKey
	 */
	@Override
	public int hashCode()
	{
		if(this.urlKey == null)
		{
			// null urlKey is a possibility when deserializing. Some placeholder must be provided to prevent a crash.
			return new String("null").hashCode();
		}
		return this.urlKey.hashCode();
	}
	
	/**
	 * Determines if two IndexedDocuments are equivalent, by looking at thier urlKeys
	 */
	@Override
	public boolean equals(Object other)
	{
		return this.getURL() != null && other != null &&
				other instanceof IndexedPage && 
				((IndexedPage)other).getURL() != null && ((IndexedPage)other).getURL().equals(this.getURL());
	}
}
