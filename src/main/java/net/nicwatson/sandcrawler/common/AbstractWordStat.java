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

/**
 * Word stats are used to track the incidence of word/document relations in an index.
 * This abstract class defines the characteristics that all word stats have in common --
 * a String representing the word itself, and a reference to the WebIndex that contains it.
 * @author Nic
 *
 */
public class AbstractWordStat implements Serializable
{
	private static final long serialVersionUID = 3462089323970319944L;

	/**
	 * The word that this stat is for.
	 */
	private String word;
	
	/**
	 * The WebIndex that "owns" this word stat
	 */
	private WebIndex index;
	
	/**
	 * Default constructor ensures that null value of word doesn't crash hashCode() or equals() during deserialization
	 */
	protected AbstractWordStat()
	{
		this.word = "";
		this.index = null;
	}
	
	/**
	 * Creates a new word stat with the specified word string and owner WebIndex
	 * @param word The word that this stat is for
	 * @param index The WebIndex that "owns" this word stat
	 */
	public AbstractWordStat(String word, WebIndex index)
	{
		this();
		this.word = word;
		this.index = index;
	}
	
	/**
	 * Getter for the <code>word</code> property
	 * @return A new string initialized to the value of the <code>word</code> property
	 */
	public String getWord()
	{
		return new String(this.word);
	}

	/**
	 * Getter for the <code>index</code> property
	 * @return Reference to the <code>index</code> property -- the WebIndex that owns this word stat
	 */
	public WebIndex getIndex()
	{
		return this.index;
	}
	
	/**
	 * Calculates the hashcode for this word stat, which is the String hashcode of the underlying word
	 * @return Hash code
	 */
	public int hashCode()
	{
		return this.word.hashCode();
	}
	
	/**
	 * Indicates whether two AbstractWordStats are equal.
	 * @return <code>true</code> if the two candidates are AbstractWordStats with the same underlying word strings
	 */
	public boolean equals(Object other)
	{
		return other instanceof AbstractWordStat && this.getWord().equals(((AbstractWordStat)other).getWord());
	}
	

}
