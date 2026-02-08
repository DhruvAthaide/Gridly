package com.dhruvathaide.gridly.data.remote

import com.dhruvathaide.gridly.data.remote.F1ApiService.NewsItemDto
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class RssParser {

    fun parse(xml: String): List<NewsItemDto> {
        val items = mutableListOf<NewsItemDto>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true // Important for RSS extensions like content:encoded
            val xpp = factory.newPullParser()
            xpp.setInput(StringReader(xml))

            var eventType = xpp.eventType
            var currentTitle = ""
            var currentLink = ""
            var currentPubDate = ""
            var currentDescription = ""
            var insideItem = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.name.equals("item", ignoreCase = true)) {
                        insideItem = true
                        currentTitle = ""
                        currentLink = ""
                        currentPubDate = ""
                        currentDescription = ""
                    } else if (insideItem) {
                        // We use safeNextText to avoid crashes on empty tags or mixed content
                        when (xpp.name.lowercase()) {
                            "title" -> currentTitle = safeNextText(xpp)
                            "link" -> currentLink = safeNextText(xpp)
                            "pubdate" -> currentPubDate = safeNextText(xpp)
                            "description" -> currentDescription = safeNextText(xpp)
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.name.equals("item", ignoreCase = true)) {
                        insideItem = false
                        // clean description
                        val cleanDesc = currentDescription.replace(Regex("<[^>]*>"), "").trim()
                        
                        // VALIDATION: Only add if we have at least a Title AND a Link
                        if (currentTitle.isNotBlank() && currentLink.isNotBlank()) {
                            items.add(NewsItemDto(
                                title = currentTitle.trim(),
                                link = currentLink.trim(),
                                pubDate = currentPubDate.trim(),
                                description = cleanDesc
                            ))
                        }
                    }
                }
                eventType = xpp.next()
            }
        } catch (e: Exception) {
            println("RssParser Error: ${e.message}")
            // Return whatever we have parsed so far, or empty list if catastrophe
        }
        return items
    }

    private fun safeNextText(xpp: XmlPullParser): String {
        return try {
            if (xpp.next() == XmlPullParser.TEXT) {
                val text = xpp.text
                xpp.nextTag() // Advance to end tag
                text
            } else {
                "" // Result was not text (maybe empty tag <title/> or nested tags)
            }
        } catch (e: Exception) {
            "" // Handle case where next() isn't text or nextTag() fails
        }
    }
}
