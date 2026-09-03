package com.yuandao.music.ui

import com.yuandao.music.data.model.Track

object LibrarySearch {
    fun filterTracks(tracks: List<Track>, query: String): List<Track> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return tracks

        return tracks.filter { track ->
            track.title.contains(normalizedQuery, ignoreCase = true) ||
                track.artistName.contains(normalizedQuery, ignoreCase = true) ||
                track.albumTitle.contains(normalizedQuery, ignoreCase = true)
        }
    }
}
