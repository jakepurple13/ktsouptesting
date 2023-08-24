package com.example.common

import ktsoup.KtSoupDocument
import ktsoup.KtSoupParser
import ktsoup.parseRemote

public object Network {
    public suspend fun getReleaseNotes(): List<ReleaseNotes> =
        KtSoupParser.parseRemote("https://developer.android.com/feeds/androidx-release-notes.xml")
            .toReleaseNotes()

    public fun parseReleaseNotes(html: String): List<ReleaseNotes> = KtSoupParser.parse(html)
        .toReleaseNotes()
}

public fun KtSoupDocument.toReleaseNotes(): List<ReleaseNotes> = querySelectorAll("entry")
    .mapNotNull {
        runCatching {
            val content = it.querySelector("content")!!.textContent()
            ReleaseNotes(
                date = it.querySelector("title")!!.textContent(),
                updated = it.querySelector("updated")!!.textContent(),
                link = it.querySelector("link")!!.attr("href")!!,
                content = content,
                links = KtSoupParser.parse(content)
                    .querySelectorAll("a")
                    .map {
                        ReleaseNotesLibrary(
                            name = it.textContent(),
                            link = it.attr("href").orEmpty()
                        )
                    }
            )
        }
            .onFailure { t ->
                println(it.html())
                t.printStackTrace()
            }
            .getOrNull()
    }

public data class ReleaseNotes(
    val date: String,
    val updated: String,
    val link: String,
    val content: String,
    val links: List<ReleaseNotesLibrary>,
)

public data class ReleaseNotesLibrary(
    val name: String,
    val link: String,
)