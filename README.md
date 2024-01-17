# Java Sandcrawler
A web-crawler and keyword-based search engine, written in Java for the JavaFX runtime.

![sandcrawler](https://github.com/nicwatson/javasandcrawer/assets/587252/ab367528-47de-4f7e-88b4-bffba785972f)

Use "Crawl the Desert" to input a seed URL. The program will crawl web pages by following hyperlinks up to a maximum of 10,000 pages, generate an index, and store it to a local file.
<br/>
Enter a search query and click "Search the junk heap!" to conduct a search on the indexed pages.

The search algorithm uses a vector-space model with tf-idf weighting to determine the relevance of documents based on the incidence of keywords from the search query. (It does not consider phrases, co-locations, or n-grams.)

Optionally, search results can be altered by checking the "Use PageRank Boost" box. This will allow the search engine to consider the PageRank scores of results, potentially boosting some pages higher in the list as a result. This is based on Google's [PageRank algorithm](https://en.wikipedia.org/wiki/PageRank), which assigns higher scores to pages that have more incoming hyperlinks.

<br/>

### Licensing Notice
JavaSandcrawler  Copyright (C) 2022  Nic Watson <br/>
Distributed under GPL v3.0 license. Please see LICENSE file. <br/>
Misrepresenting this work in whole or in part as one's own in a submission for academic purposes (i.e. plagiarism) is prohibited. (Note that this is already implied by the GPL, which requires that license notices remain intact.)
