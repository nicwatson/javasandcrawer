package net.nicwatson.sandcrawler.common;

import java.io.Serializable;

/**
 * A DocumentWordStat tracks the incidence of a specific word in a specific document. It also
 * calculates and stores the TF and TF-IDF of the word relative to the document.
 * @author Nic
 *
 */
public class DocumentWordStat extends AbstractWordStat implements Serializable
{
	private static final long serialVersionUID = 3799187837351180675L;
	
	/**
	 * The document that this word stat is for
	 */
	private MappedDocument containingDoc;
	
	/**
	 * Number of times the word appears in the document
	 */
	private int count;
	
	/**
	 * Term frequency of the word in the document. This is calculated once and then stored locally for efficiency.
	 */
	private double termFrequency;
	
	/**
	 * TF-IDF of the word in the document. This is calculated once and then stored locally for efficiency.
	 * TF-IDF depends on the TF, which is stored locally, and IDF, which is a global property of the word.
	 */
	private double tf_idf;
	
	/**
	 * A reference to the corresponding GlobalWordStat counterpart of this DocumentWordStat. The GlobalWordStat
	 * tracks the incidence of the same word across the entire corpus. DocumentWordStats maintain a reference
	 * to their global counterparts chiefly for the purpose of looking up IDF when calculating TF-IDF. The
	 * reference is kept locally to avoid the need for repeated lookups in the index.
	 */
	private GlobalWordStat globalStat;
	
	/**
	 * Constructs a new DocumentWordStat, given the GlobalWordStat for the same word, and an IndexedDocument
	 * against which the local word stats will be calculated.
	 * <p/>
	 * The initialized word stat does not contain valid data right away. <code>count</code> is initialized to zero,
	 * and must be updated through the <code>increment()</code> methods before it is meaningful.
	 * <br/>
	 * <code>termFrequency</code> and <code>tf_idf</code> are initialized to -1. Their true values are calculated
	 * and stored when first accessed by their corresponding getters. Those values will be accurate only if both
	 * this word stat's <code>count</code> property and the GlobalWordStat's IDF have been accurately set. 
	 * @param wordStat The GlobalWordStat for the word
	 * @param owner The document for which word stats are being tracked
	 */
	public DocumentWordStat(GlobalWordStat wordStat, MappedDocument containing)
	{
		super(wordStat.getWord(), containing.getIndex());
		this.globalStat = wordStat;
		this.containingDoc = containing;
		this.count = 0;
		this.termFrequency = -1;
		this.tf_idf = -1;
	}
	
	/**
	 * Getter for the <code>count</code> property.
	 * @return Value of the <code>count</code> property
	 */
	public double getCount()
	{
		return this.count;
	}
	
	/**
	 * Getter for the <code>containingDoc</code> property, which is the document to which this word stat applies
	 * @return Reference to the <code>containingDoc</code> property
	 */
	public MappedDocument getDocument()
	{
		return this.containingDoc;
	}
	
	/**
	 * Increments the <code>count</code> property by 1
	 * @return The DocumentWordStat object for convenient method chaining
	 */
	public DocumentWordStat increment()
	{
		return this.increment(1);
	}
	
	/**
	 * Increments the <code>count</code> property by the specified amount
	 * @param amt The amount to add to the count
	 * @return The DocumentWordStat object for convenient method chaining
	 */
	public DocumentWordStat increment(int amt)
	{
		this.count += amt;
		return this;
	}
	
	/**
	 * Shortcut to get the IDF of a given word. This method passes the request through to the corresponding GlobalWordStat
	 * @return The inverse document frequency for the given word in the entire indexed corpus
	 */
	public double getIDF()
	{
		return this.globalStat.getIDF();
	}
	
	/**
	 * Retrieves the term frequency for the word in this document. The value is "cached" in the object the first time it
	 * is requested, so that the calculation does not have to be repeated multiple times. If the value has not yet been
	 * cached (i.e. termFrequency == -1), it will be calculated first.
	 * @return The term frequency of this word in this document
	 */
	public double getTF()
	{
		if(this.termFrequency < 0)
		{
			// termFrequency not initialized -- need to calculate it
			return this.calculateTF();
		}
		return this.termFrequency;
	}
	
	/**
	 * Calculates the term frequency for the word in this document, and caches it to a local member variable. 
	 * <p/>
	 * <code>termFrequency = (number of times word appears in doc) / (number of words in doc)</code> 
	 * @return The calculated term frequency
	 */
	protected double calculateTF()
	{
		this.termFrequency = this.count / (double)(containingDoc.getSize());
		return this.termFrequency;
	}

	/**
	 * Retrieves the TF-IDF for the word in this document. The value is "cached" in the object the first time it
	 * is requested, so that the calculation does not have to be repeated multiple times. If the value has not yet been
	 * cached (i.e. tf_idf == -1), it will be calculated first.
	 * @return The TF-IDF of this word in this document
	 */
	public double getTFIDF()
	{
		if(this.tf_idf < 0)
		{
			return this.calculateTFIDF();
		}
		return this.tf_idf;
	}
	
	/**
	 * Calculates the TF-IDF for the word in this document, and caches it to a local member variable. 
	 * <p/>
	 * <code>tf_idf = log(1 + TF) * IDF<>/code> 
	 * @return The calculated tf-idf
	 */
	protected double calculateTFIDF()
	{
		this.tf_idf = MathsHelper.lg(1.0 + this.getTF()) * this.getIDF();
		return this.tf_idf;
	}
	
	
}
