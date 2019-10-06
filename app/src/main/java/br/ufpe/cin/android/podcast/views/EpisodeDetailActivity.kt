package br.ufpe.cin.android.podcast.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.consts.Consts
import br.ufpe.cin.android.podcast.models.ItemFeed
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_episode_detail.*

class EpisodeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)


        val feedItem = (intent.getSerializableExtra(Consts.EXTRA_FEED_ID) as ItemFeed?)

        feed_title.text = feedItem?.title

        // Usando Picasso para lidar com as imagens
        Picasso.get().load(feedItem?.image).into(feed_image)

        // Eu sei que ta deprecated, se eu me lembrar de corrigir eu corrijo
        feed_description.text = Html.fromHtml(feedItem?.description)
        feed_link.text = feedItem?.link

    }
}
