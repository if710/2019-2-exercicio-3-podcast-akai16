package br.ufpe.cin.android.podcast.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.PodcastPlayer
import br.ufpe.cin.android.podcast.services.DownloadIntentService
import br.ufpe.cin.android.podcast.listeners.PodcastPlayerListener
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.consts.Consts
import br.ufpe.cin.android.podcast.models.ItemFeed
import br.ufpe.cin.android.podcast.services.PlayPodcastService
import br.ufpe.cin.android.podcast.views.EpisodeDetailActivity
import kotlinx.android.synthetic.main.itemlista.view.*


class CustomAdapter(
        private val ctx: Context,
        private val itemFeedList: List<ItemFeed>,
        val playerListener: PodcastPlayerListener
    ) :
    RecyclerView.Adapter<CustomAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.itemlista, parent, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount() = itemFeedList.count()

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val feedItem = itemFeedList[position]

        holder.title.text = feedItem.title
        holder.date.text = feedItem.pubDate

        // Ir para EpisodeDetailActivity
        holder.title.setOnClickListener {
            val intent = Intent(ctx.applicationContext, EpisodeDetailActivity::class.java)
            intent.putExtra(Consts.EXTRA_FEED_ID, feedItem)
            ctx.startActivity(intent)
        }

        // Baixar episodio
        holder.downloadButton.setOnClickListener {
            DownloadIntentService.startActionDonwload(ctx, feedItem.id!!, position)
            Toast.makeText(ctx, "Iniciando Download", Toast.LENGTH_SHORT).show()
        }

        holder.playButton.isGone = feedItem.mp3Path == null

        // Tocar episodio
        holder.playButton.setOnClickListener {
            if (feedItem.mp3Path != null) {
                if (playerListener.isPlaying()) {
                    playerListener.pausePodcast(feedItem.id!!)
                }
                else {
                    playerListener.playPodcast(feedItem.id!!)
                    Toast.makeText(this.ctx, "Tocando Podcast", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this.ctx, "Baixe o episódio antes de ouvir", Toast.LENGTH_SHORT).show()
            }
        }

    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.item_title
        val date: TextView = itemView.item_date
        val downloadButton: Button = itemView.item_button
        val playButton: Button = itemView.item_play_button
    }
}