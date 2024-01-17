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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * An IndexedDocument represents a document in which the statistical incidence of each unique
 * has been calculated relative to the document. Every IndexedDocument has a "wordmap" for 
 * looking up words based on Strings and retrieving their associated DocumentWordStats relative
 * to the document.
 * <p/>
 * IndexedDocument is an abstract class that can represent either a search query (IndexedQuery) or
 * an indexed web page (IndexedPage)
 * @author Nic
 *
 */
public abstract class MappedDocument implements Serializable
{
	private static final long serialVersionUID = -523003523591120234L;

	/**
	 * A helper method that takes a long String representing the contents of a multi-word document, and tokenizes it,
	 * stripping out all non-alphanumeric characters and splitting each whitespace- or punctionat-separated word into
	 * its own token 
	 * @param s The string to tokenize
	 * @return A List containing all the tokens as individual Strings
	 */
	public static List<String> tokenize(String s)
	{
		// First replace all non-alphanumeric characters with whitespace
		s = s.replaceAll("[^A-Za-z0-9]", " ");
		// Then tokenize the string on whitespaces
		StringTokenizer tokenizer = new StringTokenizer(s, " ", false);
		
		List<String> list = new LinkedList<String>();
		while(tokenizer.hasMoreTokens())
		{
			list.add(tokenizer.nextToken().toLowerCase());
		}
		
		return list;
	}
	
	/**
	 * boolean property that tracks whether the document's wordmap has been populated
	 */
	private boolean mapInitialized;

	/**
	 * Total size (word count, including duplicates) for this document
	 */
	protected int numWords;
	
	/**
	 * Number of different unique words found in this document
	 */
	protected int uniqueWords;

	/**
	 * The document's wordmap, which maps word Strings onto DocumentWordStats. The DocumentWordStats
	 * in turn keep track of the incidence of each of those words in the document.
	 */
	protected Map<String, DocumentWordStat> wordMap;

	/**
	 * The WebIndex to which this document belongs
	 */
	protected WebIndex index;
	
	/**
	 * A public default constructor is apparently required for deserialization. However, in-program consumers should
	 * always use <code>IndexedDocument(WebIndex index)</code> to instantiate an IndexedDocument.  
	 */
	public MappedDocument()
	{
	}
	
	/**
	 * Instantiates a new IndexedDocument that will belong to the specified WebIndex
	 * @param index
	 */
	public MappedDocument(WebIndex index)
	{
		this.mapInitialized = false;
		this.index = index;
		this.numWords = 0;
		this.uniqueWords = 0;
		this.wordMap = new HashMap<String, DocumentWordStat>();
	}
	
	/**
	 * Getter for <code>numWords</code>
	 * @return The total number of words (including duplicates) contained in this document's text
	 */
	public int getSize()
	{
		return this.numWords;
	}
	
	/**
	 * Getter for the WebIndex to which this document belongs
	 * @return Reference to the <code>index</code> property
	 */
	public WebIndex getIndex()
	{
		return this.index;
	}
	
	/**
	 * Retrieves a list view of the DocumentWordStats for all the words in this document
	 * @return a list view of the DocumentWordStats for all the words in this document
	 */
	public List<DocumentWordStat> getWordList()
	{
		return List.copyOf(this.wordMap.values());
	}
	
	/**
	 * Getter for the <code>mapInitialized</code> property, which tracks whether the wordmap has been populated
	 * @return <code>true</code> if the wordmap has been populated; <code>false</code> otherwise.
	 */
	public boolean isInitialized()
	{
		return this.mapInitialized;
	}
	
	/**
	 * Initializes (populates) this document representation's word map from a list of string tokens. The method checks if the wordmap has
	 * already been populated. If so, it does nothing.
	 * @param tokens A list of all words appearing in the actual document being represented by this IndexedDocument object.
	 * The list should be built by tokenizing (splitting on whitespace into individual words) the document text. All duplicate words should
	 * be included in the list.
	 * @return <code>false</code> if the map was not previously initialized; <code>true</code> if it was.
	 */
	public boolean initializeWordMap(List<String> tokens)
	{
		boolean alreadyDone = this.mapInitialized;
		if(!alreadyDone)
		{
			for(String s : tokens)
			{
				this.numWords++;
				if(this.containsWord(s))
				{
					// Word already known to have appeared in earlier in the document - increment the stat
					this.wordMap.get(s).increment();
				}
				else
				{
					// This is a new word for this dcument, so a new DocumentWordStat is needed
					this.handleNewWord(s);
				}
			}
			this.mapInitialized = true;
		}
		return alreadyDone;
	}
	
	/**
	 * Indicates whether the given word is found in this document. This is a pass-through method for the <code>containsKey()</code>
	 * method on the underlying wordmap.
	 * @param word
	 * @return <code>true</code> if the word is present in the document at least once; otherwise <code>false</code>
	 */
	public boolean containsWord(String word)
	{
		return this.wordMap.containsKey(word);
	}
	
	/**
	 * Retrieves the DocumentWordStat associated with the given word in this document's wordmap 
	 * @param word
	 * @return The DocumentWordStat for the given word
	 */
	public DocumentWordStat getWordStat(String word)
	{
		return this.wordMap.get(word);
	}
	
	/**
	 * Creates a new DocumentWordStat out of the given GlobalWordStat and puts it in this document's wordmap.
	 * Also updates the count of unique words found in this document. This should only be called for GlobalWordStats
	 * that correspond to words that are actually in the document.
	 * @param globalStat The GlobalWordStat associated with the word to add.
	 */
	protected void insertWordStat(GlobalWordStat globalStat)
	{
		this.uniqueWords++;
		DocumentWordStat localWordStat = new DocumentWordStat(globalStat, this).increment();
		this.wordMap.put(globalStat.getWord(), localWordStat);
	}
	
	/**
	 * When processing the body text of a document, if a new word is encountered that is not previously known to the
	 * IndexedDocument, this method is called to determine if the index needs to be updated with a new DocumentWordStat.
	 * Subclasses will implement the method according to their needs: i.e. an IndexedPage will need to ensure that any
	 * new words also get GlobalWordStats put in the index if they don't already exist.
	 * @param word
	 */
	protected abstract void handleNewWord(String word);
	
	/**
	 * Retrieves the term frequency for the given word in this document. If the word does not appear in the document, its 
	 * term frequency will be zero. Thus we can short-circuit running a <code>get()</code> on the wordmap and just return zero.
	 * @param word The word to look up the term frequency for. 
	 * @return The term frequency of the given word in this document
	 */
	public double getTF(String word)
	{
		if(this.wordMap.containsKey(word))
		{
			return this.wordMap.get(word).getTF();
		}
		return 0;	// If word isn't in the document, then TF = 0
	}
	
	/**
	 * Retrieves the TF-IDF. If the word does not appear in the document, its term frequency will be zero.
	 * If TF is zero, then the TF-IDF formula will always evaluate to zero, so we can short-circuit running any further 
	 * lookups or calculations. 
	 * @param word The word to look up the TF-IDF for. 
	 * @return The TF-IDF of the given word relative to this document
	 */
	public double getTFIDF(String word)
	{
		if(this.wordMap.containsKey(word))
		{
			return this.wordMap.get(word).getTFIDF();
		}
		// If the word isn't in the document, then TF = 0
		// If TF is zero, then TF_IDF = log(1 + 0) * IDF = 0 * IDF = 0
		// So we can skip running the lookups/math with the IDF and just return 0.
		return 0;
	}
}
