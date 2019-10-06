package br.ufpe.cin.android.podcast.listeners

interface PodcastPlayerListener {

    fun playPodcast(feedItemId: Int)

    fun pausePodcast(feedItemId: Int)

    fun isPlaying(): Boolean
}