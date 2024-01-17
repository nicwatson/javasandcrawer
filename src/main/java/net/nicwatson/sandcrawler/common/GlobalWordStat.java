package net.nicwatson.sandcrawler.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A GlobalWordStat tracks the incidence of a specific word across the entire corpus of indexed documents.
 * It is used to look up the global occurrence count and inverse document frequency of words, and to retrieve
 * a list of all documents in which a given word appears.
 * @author Nic
 *
 */
public class GlobalWordStat extends AbstractWordStat implements Serializable
{
	private static final long serialVersionUID = 7295490782033321941L;

	/**
	 * A set of all pages in the index in which this word appears at least once
	 */
	protected Set<IndexedPage> pageSet;
	
	/**
	 * Total number of documents in which this word appears at least once.
	 */
	protected int globalOccurrence;
	
	/**
	 * Inverse document frequency of the word in the corpus. This is calculated once and then stored locally for efficiency.
	 */
	protected double inverseDocumentFrequency;
	
	/**
	 * Constructs a new GlobalWordStat from a given String and given WebIndex.
	 * <p/>
	 * The initialized word stat does not contain valid data right away. <code>globalOccurrence</code> is initialized to zero,
	 * and will not be accurate until all pages that contain the word have been added to this word stat's listing.
	 * <br/>
	 * <code>inverseDocumentFrequency</code> is initialized to -1. Its true values are calculated
	 * and stored when first accessed by their corresponding getters. That value will be accurate only if both
	 * this word stat's <code>globalOccurrence</code> property and the WebIndex's document count are accurate. 
	 * @param word The word that this stat is for
	 * @param index The WebIndex to which this word stat will belong
	 */
	public GlobalWordStat(String word, WebIndex index)
	{
		super(word, index);
		
		// Instantiate the pageList as a LinkedHashSet in order to combine fast insertions/lookups with efficient iteration.
		this.pageSet = new HashSet<IndexedPage>();
		
		this.globalOccurrence = 0;
		this.inverseDocumentFrequency = -1;
	}
	
	/**
	 * Getter for <code>globalOccurrence</code>, which is the total number of documents in which this word appears at least once.
	 * @return Value of <code>globalOccurrence</code> property.
	 */
	public int getGlobalOccurrence()
	{
		return this.globalOccurrence;
	}
	
	/**
	 * Getter for <code>pageSet</code>.
	 * <br/>Retrieves a Set view of all the indexed pages in which this word appears at least once.
	 * @return A Set view of all the indexed pages in which this word appears at least once.
	 */
	public Set<IndexedPage> getPageSet()
	{
		return this.pageSet;
	}
	
	/**
	 * Adds the given indexed page to this word stat's set of documents that contain the word, and updates the <code>globalOccurrence</code> property.
	 * @param p The page to add to the set. If the page is already in the set, nothing is added (duplicates are not permitted).
	 * @return The word's new <code>globalOccurrence</code> value.
	 */
	public int addPage(IndexedPage p)
	{
		if(!this.pageSet.contains(p))
		{
			this.pageSet.add(p);
			this.globalOccurrence++;
		}
		return this.globalOccurrence;
	}
	
	/**
	 * Retrieves the inverse document frequency for the word. The value is "cached" in the object the first time it
	 * is requested, so that the calculation does not have to be repeated multiple times. If the value has not yet been
	 * cached (i.e. inverseDocumentFrequency == -1), it will be calculated first.
	 * @return The IDF of this word in the corpus
	 */
	public double getIDF()
	{
		if(this.inverseDocumentFrequency < 0)
		{
			return this.calculateIDF();
		}
		return this.inverseDocumentFrequency;
	}

	/**
	 * Calculates the inverse document frequency for this word in the corpus, and caches it to a local member variable. 
	 * <p/>
	 * <code>IDF = (total number of docs in corpus) / (1 + global occurrence of this word)</code> 
	 * @return The calculated inverse document frequency
	 */
	protected double calculateIDF()
	{
		double ratio = this.getIndex().getTotalDocs() / (1.0 + (double)(this.getGlobalOccurrence()));
		this.inverseDocumentFrequency = MathsHelper.lg(ratio);
		return this.inverseDocumentFrequency;
	}
	

}
