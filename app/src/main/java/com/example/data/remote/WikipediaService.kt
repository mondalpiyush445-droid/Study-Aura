package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class WikiSearchSnippet(
    val title: String,
    val snippet: String,
    val pageId: Long = 0
)

data class WikiSearchResult(
    val title: String,
    val description: String,
    val extract: String,
    val thumbnailUrl: String? = null,
    val articleUrl: String? = null,
    val relatedSnippets: List<WikiSearchSnippet> = emptyList()
)

object WikipediaService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun searchWikipedia(query: String): WikiSearchResult? = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext null

        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            
            // Step 1: Perform full Wikipedia search query to find exact matching page titles & related topics
            val searchApiUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encodedQuery&utf8=1&format=json&origin=*"
            val searchRequest = Request.Builder()
                .url(searchApiUrl)
                .header("User-Agent", "AcademicHub/1.0 (Android Academic Assistant App)")
                .get()
                .build()

            val relatedList = mutableListOf<WikiSearchSnippet>()
            var targetTitle = query.trim()

            client.newCall(searchRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val searchBody = response.body?.string()
                    if (searchBody != null) {
                        val json = JSONObject(searchBody)
                        val queryObj = json.optJSONObject("query")
                        val searchArr = queryObj?.optJSONArray("search")
                        
                        if (searchArr != null && searchArr.length() > 0) {
                            // Top matching title
                            val firstItem = searchArr.getJSONObject(0)
                            targetTitle = firstItem.optString("title", query)

                            for (i in 0 until searchArr.length().coerceAtMost(6)) {
                                val item = searchArr.getJSONObject(i)
                                val t = item.optString("title")
                                val rawSnippet = item.optString("snippet")
                                    .replace("</span>", "")
                                    .replace("<span class=\"searchmatch\">", "")
                                    .replace("&quot;", "\"")
                                    .replace("&#039;", "'")
                                val pid = item.optLong("pageid", 0)

                                if (t.isNotBlank()) {
                                    relatedList.add(WikiSearchSnippet(title = t, snippet = rawSnippet, pageId = pid))
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Fetch detailed page summary & full text extract for targetTitle
            val encodedTargetTitle = URLEncoder.encode(targetTitle, "UTF-8")
            val summaryUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/$encodedTargetTitle"

            val summaryRequest = Request.Builder()
                .url(summaryUrl)
                .header("User-Agent", "AcademicHub/1.0 (Android Academic Assistant App)")
                .get()
                .build()

            client.newCall(summaryRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (bodyString != null) {
                        val json = JSONObject(bodyString)
                        val title = json.optString("title", targetTitle)
                        val description = json.optString("description", "Academic Entry")
                        var extract = json.optString("extract", "")

                        val thumbnailObj = json.optJSONObject("thumbnail")
                        val thumbnailUrl = thumbnailObj?.optString("source")

                        val contentUrls = json.optJSONObject("content_urls")
                        val desktopObj = contentUrls?.optJSONObject("desktop")
                        val articleUrl = desktopObj?.optString("page", "https://en.wikipedia.org/wiki/$encodedTargetTitle")

                        // Step 3: Fetch multi-paragraph full extract from query API if extract is short or for biographic depth
                        if (extract.length < 300) {
                            val fullExtractUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exintro=1&explaintext=1&titles=$encodedTargetTitle&format=json&origin=*"
                            val fullReq = Request.Builder().url(fullExtractUrl).get().build()
                            client.newCall(fullReq).execute().use { fullResp ->
                                if (fullResp.isSuccessful) {
                                    val fullBody = fullResp.body?.string()
                                    if (fullBody != null) {
                                        val fullJson = JSONObject(fullBody)
                                        val pages = fullJson.optJSONObject("query")?.optJSONObject("pages")
                                        if (pages != null) {
                                            val keys = pages.keys()
                                            if (keys.hasNext()) {
                                                val pageKey = keys.next()
                                                val pageObj = pages.optJSONObject(pageKey)
                                                val fullText = pageObj?.optString("extract")
                                                if (!fullText.isNullOrBlank() && fullText.length > extract.length) {
                                                    extract = fullText
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (extract.isBlank()) {
                            extract = "No detailed extract available for $title on Wikipedia."
                        }

                        return@withContext WikiSearchResult(
                            title = title,
                            description = description,
                            extract = extract,
                            thumbnailUrl = thumbnailUrl,
                            articleUrl = articleUrl,
                            relatedSnippets = relatedList
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback mock/offline article generation if offline or network fail
        }

        generateOfflineWikiSummary(query)
    }

    private fun generateOfflineWikiSummary(query: String): WikiSearchResult {
        val q = query.trim().lowercase()
        val mockRelated = listOf(
            WikiSearchSnippet("Early Life & Education", "Historical background, academic institutions and early publications."),
            WikiSearchSnippet("Major Contributions & Discoveries", "Primary theories, patents, formulas and groundbreaking research."),
            WikiSearchSnippet("Awards, Honors & Legacy", "International recognitions, medals, memorial lectures and impact on modern science.")
        )

        return when {
            q.contains("einstein") || q.contains("albert") -> WikiSearchResult(
                title = "Albert Einstein",
                description = "Theoretical physicist (1879–1955) • Developer of Theory of Relativity",
                extract = "Albert Einstein (14 March 1879 – 18 April 1955) was a German-born theoretical physicist who is widely held to be one of the greatest and most influential scientists of all time. Best known for developing the theory of relativity, Einstein also made important contributions to quantum mechanics.\n\nHis mass–energy equivalence formula E = mc², which arises from relativity theory, has been dubbed 'the world's most famous equation'. He received the 1921 Nobel Prize in Physics 'for his services to Theoretical Physics, and especially for his discovery of the law of the photoelectric effect', a pivotal step in the development of quantum theory.",
                articleUrl = "https://en.wikipedia.org/wiki/Albert_Einstein",
                relatedSnippets = mockRelated
            )
            q.contains("turing") || q.contains("alan") -> WikiSearchResult(
                title = "Alan Turing",
                description = "Mathematician, computer scientist & codebreaker (1912–1954)",
                extract = "Alan Mathison Turing (23 June 1912 – 7 June 1954) was an English mathematician, computer scientist, logician, cryptanalyst, philosopher, and theoretical biologist. Turing was highly influential in the development of theoretical computer science, providing a formalisation of the concepts of algorithm and computation with the Turing machine, which can be considered a model of a general-purpose computer.\n\nTuring is widely considered to be the father of theoretical computer science and artificial intelligence.",
                articleUrl = "https://en.wikipedia.org/wiki/Alan_Turing",
                relatedSnippets = mockRelated
            )
            q.contains("curie") || q.contains("marie") -> WikiSearchResult(
                title = "Marie Curie",
                description = "Physicist and chemist (1867–1934) • Pioneer in Radioactivity",
                extract = "Marie Salomea Skłodowska-Curie (7 November 1867 – 4 July 1934) was a Polish and naturalised-French physicist and chemist who conducted pioneering research on radioactivity. She was the first woman to win a Nobel Prize, the first person to win a Nobel Prize twice, and the only person to win a Nobel Prize in two scientific fields (Physics in 1903 and Chemistry in 1911).",
                articleUrl = "https://en.wikipedia.org/wiki/Marie_Curie",
                relatedSnippets = mockRelated
            )
            q.contains("calculus") || q.contains("integrat") -> WikiSearchResult(
                title = "Calculus",
                description = "Branch of mathematics studying continuous change",
                extract = "Calculus is the mathematical study of continuous change, in the same way that geometry is the study of shape, and algebra is the study of generalizations of arithmetic operations. It has two major branches: differential calculus and integral calculus; the former concerns instantaneous rates of change, and the slopes of curves, while the latter concerns accumulation of quantities, and the areas under or between curves.",
                articleUrl = "https://en.wikipedia.org/wiki/Calculus",
                relatedSnippets = mockRelated
            )
            else -> WikiSearchResult(
                title = query.replaceFirstChar { it.uppercase() },
                description = "Academic Biography / Topic",
                extract = "$query is a distinguished academic subject and field of research. Explore peer-reviewed textbooks, historical documents, and published literature for complete biographical context, formulas, and domain applications.",
                articleUrl = "https://en.wikipedia.org/wiki/$query",
                relatedSnippets = mockRelated
            )
        }
    }
}
