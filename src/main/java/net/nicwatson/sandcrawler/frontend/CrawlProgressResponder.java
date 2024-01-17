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
