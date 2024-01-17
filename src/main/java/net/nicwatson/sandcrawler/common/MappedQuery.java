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
import java.util.List;

/**
 * An IndexedQuery is a type of IndexedDocument that only has local word stats, and doesn't need any
 * of the information associated with actual web pages such as title and URL. Typically this is used
 * to represent an ephemeral "document" such as a search query. Instead of using the base class
 * IndexedDocument for this purpose, I have created this dedicated IndexedQuery subclass to split out
 * out its <code>handleNewWord()</code> logic, and to more clearly separate the two conceptual types
 * of IndexedDocument to facilitate hypothetical later extension of both types.
 * @author Nic
 *
 */
public class MappedQuery extends MappedDocument implements Serializable
{
	private static final long serialVersionUID = -4654121544296595904L;

	/**
	 * Creates a new IndexedQuery, owned by the given WebIndex, from the given tokenized text
	 * @param index The WebIndex against which this query's word stats (e.g. IDF) will be measured
	 * @param tokens List of String tokens compiled from the text of the query/document
	 */
	public MappedQuery(WebIndex index, List<String> tokens)
	{
		super(index);
		this.initializeWordMap(tokens);
	
	}

	@Override
	protected void handleNewWord(String word)
	{
		if(this.index.knowsWord(word))
		{
			this.insertWordStat(this.index.getGlobalWordStat(word));
		}
	}
}
