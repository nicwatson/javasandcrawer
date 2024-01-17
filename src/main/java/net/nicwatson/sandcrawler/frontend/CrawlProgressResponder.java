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
 
package net.nicwatson.sandcrawler.frontend;

/**
 * An incomplete feature that would allow a GUI to monitor progress of the web crawl and indexing.
 * @author Nic
 *
 */
public interface CrawlProgressResponder
{
	/**
	 * Defines the stages of crawing/parsing 
	 * @author Nic
	 *
	 */
	public static enum ProgressStage
	{
		MISSING, RETRIEVING, PARSING, LINKING, RANKING, DONE
	}
	
	/**
	 * Informs the CrawlProgressResponder of the current stage and progress of the work
	 */
	public void updateProgress(ProgressStage stage, int done, int left);
}
