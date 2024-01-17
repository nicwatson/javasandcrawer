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

package net.nicwatson.sandcrawler.crawl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nicwatson.sandcrawler.common.URLFormat;

/**
 * An UnprocessedPage is a page that has been crawled but has not had its word stats calculated and indexed.
 * @author Nic
 *
 */
public class UnprocessedPage
{
	/**
	 * regex pattern used to extract the protocol substring from a URL
	 */
	public static final Pattern PATTERN_PROTOCOL = Pattern.compile("https?:\\/\\/");
	
	/**
	 * regex pattern used to extract the contents of the HTML title tag
	 */
	public static final Pattern TITLE_PATTERN = Pattern.compile("(?s)<.*?title.*?>(.+)<.*?\\/title.*?>");
	
	/**
	 * regex pattern used to extract the contents of HTML anchor tags (hyperlinks)
	 */
	public static final Pattern LINK_PATTERN = Pattern.compile("(?s)<\\s*?a.+?href\\s*?=\\s*?\\\"(.+?)\\\".*?>");
	
	/**
	 * regex pattern used to extract the contents of the HTML p (paragraph) tag
	 */
	public static final Pattern TEXT_PATTERN = Pattern.compile("(?s)<.*?p(?!re|ic).*?>(.+)<\\s*?\\/p.*?\\s*?>");
	
	/**
	 * The URL of the page
	 */
	protected URL url;
	
	/**
	 * The raw text content of the page
	 */
	protected String rawText;

	/**
	 * The set of outgoing hyperlinks
	 */
	protected Set<String> outLinks;
	
	/**
	 * A URLFormat object for this page, used to split the URL up into components for link resolution
	 */
	protected URLFormat urlFormat;
	
	/**
	 * Creates a new UnprocessedPage for the given URL. The URL is parsed into a URLFormat. Other fields are
	 * initialized as null or empty.
	 * @param strURL
	 * @throws MalformedURLException
	 */
	public UnprocessedPage(String strURL) throws MalformedURLException
	{
		// We will assume that if the URL does not end with a / then it is either just a hostname, or
		// ends with a file name.
		this.url = new URL(strURL);
		this.urlFormat = new URLFormat(strURL);
		this.outLinks = null;
		this.rawText = "";			
	}
	
	/**
	 * Creates a new UnprocessedPage for the given URL. This constructor is used for testing as it 
	 * allows the text content of the page to be set explicitly (i.e. this would make a "fake" UnprocessedPage
	 * that wasn't actually retrieved from the web)  
	 * @param strURL The URL of the page, which may not actually be visited but is needed to identify the document
	 * @param text Fake text content to initialize in the document
	 * @throws MalformedURLException
	 */
	protected UnprocessedPage(String strURL, String text) throws MalformedURLException
	{
		this(strURL);
		this.rawText = text;
	}

	/**
	 * Getter for the java.net.URL object representing this page's URL - useful for passing directly to
	 * network I/O methods that expect this type
	 * @return
	 */
	public URL getURL()
	{
		return this.url;
	}
	
	/**
	 * Getter for the String representation of the underlying URLFormat
	 * @return URL of this page (String, from URLFormat)
	 */
	public String getURLString()
	{
		return this.urlFormat.get();
	}
	
	/**
	 * Getter for the page's URLFormat object
	 * @return The page's URLFormat object
	 */
	public URLFormat getURLFormat()
	{
		return this.urlFormat;
	}
	
	/**
	 * Getter for the page's raw text
	 * @return The page's raw text content
	 */
	public String getRawText()
	{
		return this.rawText;
	}
	
	/**
	 * Gets the set of known outlinks from this page
	 * @return A Set of Strings, each of which is a URL of an outlink from this page
	 */
	public Set<String> getLinks()
	{
		return this.getLinks(false);
	}
	
	/**
	 * Gets the set of known outlinks from this page, optionally forcing a re-evaluation of those links
	 * @param forceRecalculate <code>true</code> if the raw text should be scanned again for links, discarding previous link data
	 * @return A Set of Strings, each of which is a URL of an outlink from this page
	 */
	public Set<String> getLinks(boolean forceRecalculate)
	{
		if(forceRecalculate || this.outLinks == null)
		{
			return this.findLinks();
		}
		return this.outLinks;
	}
	
	/** 
	 * Extract the contents of the page's HTML title tag
	 * @return The page title as defined by its HTML title tag
	 */
	public String extractTitle()
	{
		Matcher matcher = TITLE_PATTERN.matcher(this.rawText);
		if(!matcher.find())
		{
			return "<Untitled Page>";
		}
		return matcher.group(1);
	}
	
	/**
	 * Extract and concatenate the text contents of all the HTML P tags
	 * @return String concatenation of all the paragraph text contents
	 */
	public String extractContentTexts()
	{
		String content = "";
		Matcher matcher = TEXT_PATTERN.matcher(this.rawText);
		while(matcher.find())
		{
			content += " " + matcher.group(1);
		}
		return content;
	}
	
	/**
	 * Searches through the document text for hyperlinks in anchor tags. Resolves any relative links.
	 * Adds all found links to the outLinks set
	 * @return A reference to the outLinks set
	 */
	public Set<String> findLinks()
	{
		Set<String> links = new LinkedHashSet<String>();
		Matcher matcher = LINK_PATTERN.matcher(this.rawText);
		while(matcher.find())
		{
			String candidate = matcher.group(1);
			// System.out.println("Candidate: " + candidate);
			String builder = "";
			if(candidate.startsWith("http://") || candidate.startsWith("https://"))
			{
				builder = candidate;
			}
			else
			{
				builder = this.urlFormat.protocol + this.urlFormat.host;
			
				if(candidate.startsWith("./"))
				{
					builder += this.urlFormat.basePath + candidate.substring(2);
				}
				else if(candidate.startsWith("/"))
				{
					builder += candidate;
				}
			}
			links.add(builder);
		}
		this.outLinks = links;
		return links;
	}
	
	/**
	 * Accesses the page's URL over the network to fetch its content and store it in the local rawText field.
	 * @throws IOException
	 */
	public void fetch() throws IOException
	{
		this.rawText = Crawler.readURL(this.getURL());	
	}
	
	@Override
	public String toString()
	{
		return this.urlFormat.toString();
	}
	
	@Override
	public int hashCode()
	{
		return this.urlFormat.hashCode();
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof UnprocessedPage && ((UnprocessedPage)other).getURLString().equals(this.getURLString());
	}
	
	
	
}
