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
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A URLFormat object represents a URL that has been divided into protocol, hostname, base path,
 * and file name components. It uses string parsing to convert offered URL strings into a consistent
 * format and enforce key assumptions that other Sandcrawler classes make about URL input data.
 * <p/>
 * Consider the example URL: <code><u>https://people.scs.carleton.ca/~davidmckenney/fruits/N-0.html</u></code>
 * <p/>
 * The <b>protocol</b> component string contains all characters up to and including the first
 * double-slash. Sandcrawler only supports (and assumes) true application-layer protocols (i.e.
 * no opaque URI schemes like "mailto"), so the double-slash should always be present. Furthermore,
 * the current version only supports http:// and https:// protocols. Protocol characters are always
 * converted to lower case when parsing.
 * <p/>
 * The <b>host</b> component is the network address of the (physical or virtual) server hosting
 * the page (e.g. <code><u>people.scs.carleton.ca</u></code> If the crawler encounters a seed address that
 * includes a port number, it is incorporated as part of the host string. The host string is always detached
 * from the protocol prefix, and has any trailing forward slashes stripped. Hostname characters are always
 * converted to lower case when parsing.
 * <p/>
 * The <b>basePath</b> is the directory path from the host root to the target file. In the example
 * above, the basePath is <code><u>/~davidmckenney/fruits/</u></code> . The base path is never empty. It always
 * starts and terminates with a forward slash (<code>'/'</code>) -- although a base path may be <code>"/"</code> in
 * which case the same forward slash character acts as both the start and terminator. Characters in a basePath
 * retain their case from the original string.
 * <p/>
 * The <b>fileName</b> is the name of the requested target document, without any leading path information.
 * In the example above, the fileName is <code><u>N-0.html</u></code> . The file name may an empty string, corresponding
 * to the case in which a web client requests a base path without a file name
 * (e.g. <code><u>http://people.scs.carleton.ca/~davidmckenney/</u></code>) and the server determines what file is
 * sent in reply (conventionally <code><u>index.html</u></code> but other options are possible). Note that the fileName
 * never contains any forward slashes. Characters in a fileName retain their case from the original string.
 * <p/>
 * A complete formatted URL string, available via the <code>get()</code> method,
 * is a concatenation of <code>protocol + host + basePath + file</code>. Note that this means that, for a URL
 * that implicitly targets a host's document root (i.e. host name with no trailing slash, as in <code><u>https://people.scs.carleton.ca</u></code>) 
 * the trailing slash will always be added from the basePath. The fully concatenated URL thus always has at least three slashes: 
 * exactly two from the protocol and at least one from the basePath. The parsing should ensure that equivalent URL strings
 * become identical in <code>URLFormat</code> form (e.g. <code><u>HTTP://People.Scs.Carleton.Ca/</u></code> and 
 * <code><u>http://people.scs.carleton.ca</u></code> are both converted to <code><u>http://people.scs.carleton.ca/</u></code>)
 * (but see the caveats below).
 * <p/>
 * Crucially, when parsing a URL string into a URLFormat,
 * <em>everything following the final forward slash in a protocol-stripped URL is assumed to be a file name</em>. 
 * This means that URLFormat (and, by extension, the Sandcrawler suite) treats  <code><u>https://people.scs.carleton.ca/~davidmckenney/fruits/</u></code>
 * differently from <code><u>https://people.scs.carleton.ca/~davidmckenney/fruits</u></code>: in the former case, the basePath resolves as
 * <code><u>/~davidmckenney/fruits/</u></code> and the fileName is blank; in the latter, the basePath is <code><u>/~davidmckenney/</u></code> and
 * the fileName is <code><u>fruits</u></code>. This has implications for how relative links are resolved on the page. Suppose we request both of these
 * URLs separately. The server will reply with the same index document, but in the case of <code><u>https://people.scs.carleton.ca/~davidmckenney/fruits</u></code>, 
 * Sandcrawler is unable to tell whether it has received an index document in the <code><u>/~davidmckenney/fruits/</u></code> directory, or the contents of a file
 * called <code><u>fruits</u></code> located in the <code><u>/~davidmckenney/</u></code> directory. It assumes the latter. If the document contains
 * a relative link to <code><u>./hypotheticalFile.html</u></code>, then in the first case it will resolve to
 * <code><u>https://people.scs.carleton.ca/~davidmckenney/fruits/hypotheticalFile.html</u></code>, while in the second case it will resolve to
 * <code><u>https://people.scs.carleton.ca/~davidmckenney/hypotheticalFile.html</u></code>.
 * <p/>
 * Note that Sandcrawler must make <em>some</em> determination about the two example URLs represent the same or different artifacts, because 
 * it must keep track of which pages are visited in order to avoid duplicating pages in the index. Modern web browsers can use a variety of tricks
 * to determine (most of the time) when they are getting the same reply for two apparently-different URL request strings, but the <code>URL</code>,
 * <code>URI</code>, and <code>URLRequest</code> classes provided in the Java SE API do not provide any single straightforward way to do this.
 * This version of Sandcrawler thus compromises by making and enforcing the assumptions given above.
 * 
 * @author Nic
 *
 */

public class URLFormat implements Serializable
{
	private static final long serialVersionUID = 3940849481925262156L;
	
	/**
	 * The host part of the URL, e.g. people.scs.carleton.ca 
	 */
	public final String host;
	
	/**
	 * The base path leading up to the target file of the URL, e.g. /~davidmckenney/fruits/
	 */
	public final String basePath;
	
	/**
	 * The destination file of the URL, e.g. N-0.html
	 */
	public final String file;
	
	/**
	 * The URL's protocol string, e.g. https://
	 */
	public final String protocol;
	
	/**
	 * Creates a new URLFormat object from the given <code>java.net.URL</code>. It converts from the
	 * idioms used in <code>URL</code> to the standard format expected by Sandcrawler.
	 * 
	 * @param urlObj URL object to be converted
	 */
	public URLFormat(URL urlObj)
	{
		// Members are declared as final; use temporary staging strings and then assign them at the end
		String strProtocol = urlObj.getProtocol() + "://";
		String strHost = urlObj.getHost();
		String strBasePath = "/";				// Default basePath will always be "/"
		String strFile = "";					// Default file name is blank
		
		String temp = urlObj.getPath();			// URL's full path includes file name, and may be blank
		int lastSlash = temp.lastIndexOf("/");
		if(lastSlash >= 0)
		{
			// The URL object's path has a slash in it somewhere, so we can extract basePath as
			// the path up to the final slash, and fileName as everything after. If urlObj's path
			// terminated with the slash, fileName will remain blank. If urlObj's entire path
			// was blank, we never would've gotten into this if-block anyway.
			strBasePath = temp.substring(0, lastSlash + 1);
			if(lastSlash + 1 != temp.length())
			{
				strFile = temp.substring(lastSlash + 1);
			}
		}
		
		// Finalize member assignment
		this.protocol = strProtocol;
		this.host = strHost;
		this.basePath = strBasePath;
		this.file = strFile;
	}
	
	/**
	 * Creates a new URLFormat object from the given String. The String is first converted
	 * to a <code>java.net.URL</code> for an initial format validity check (i.e. an opportunity
	 * to throw <code>MalformedURLException</code>).
	 * @param urlStr Potentially non-standardized string representation of a URL
	 */
	public URLFormat(String urlStr) throws MalformedURLException
	{
		this(new URL(urlStr));
	}
	
	@Override
	public int hashCode()
	{
		return this.get().hashCode();
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof URLFormat && ((URLFormat)other).get().equals(this.get());
	}
	
	/**
	 * Concatenates and returns a complete URL based on its stored components
	 * @return The whole URL represented by this URLFormat
	 */
	public String get()
	{
		return this.protocol + this.host + this.basePath + this.file;
	}
	
	/**
	 * Resolves a given URL string "candidate" against the URL represented by this URLFormat, as if the
	 * given URL is a link's href property from the page represented by this URLFormat. This method determines if the
	 * candidate is an abosolute or relative URL, and parses relative URLs using this page as a reference. Some ambiguous
	 * boundary cases may produce unpredictable results, but none of those boundary cases should show up within the scope
	 * of tests used for the assignment. See the general class description for details. 
	 * @param candidate The URL/href to resolve
	 * @return
	 */
	public String resolveAgainst(String candidate)
	{
		try
		{
			String builder = "";
			if(candidate.startsWith("http://") || candidate.startsWith("https://"))
			{
				// We have an absolute URL, and can use it as-is (will still need format touch-up)
				builder = candidate;
			}
			else
			{
				// URL is relative (we assume that if it doesn't begin with a protocol, it's relative)
				
				// Build the base URL
				builder = this.protocol + this.host;
			
				if(candidate.startsWith("./"))
				{
					// Link is relative to the current page - take the containing directory and append href as filename
					builder += this.basePath + candidate.substring(2);
				}
				else if(candidate.startsWith("/"))
				{
					// Link is relative to the document root - append href to (protocol + host)
					builder += candidate;
				}
			}
			// Can now do format touch-up, verifying that the URL is not malformed
			return new URLFormat(builder).get();
		}
		catch(MalformedURLException e)
		{
			// We ended up with a malformed URL after parsing!
			System.err.println("Attempted to build a malformed URL from a hyperlink.");
			System.err.println("Raw link: " + candidate);
			System.err.println("Resolved against: " + this.get());
			System.err.println(e.toString());
			return this.get();
		}
	}
}
