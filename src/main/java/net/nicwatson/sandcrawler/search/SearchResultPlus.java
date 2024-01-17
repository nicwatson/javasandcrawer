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
 
package net.nicwatson.sandcrawler.search;

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
