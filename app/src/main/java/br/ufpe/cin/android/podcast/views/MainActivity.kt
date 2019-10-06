package br.ufpe.cin.android.podcast.views

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.ufpe.cin.android.podcast.*
import br.ufpe.cin.android.podcast.adapters.CustomAdapter
import br.ufpe.cin.android.podcast.consts.Consts
import br.ufpe.cin.android.podcast.listeners.PodcastPlayerListener
import br.ufpe.cin.android.podcast.models.ItemFeed
import br.ufpe.cin.android.podcast.models.PodcastDB
import br.ufpe.cin.android.podcast.services.PlayPodcastService
import br.ufpe.cin.android.podcast.utils.Parser
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    internal var podcastPlayerService: PlayPodcastService? = null
    internal var isBound = false
    internal var feedAdapter: CustomAdapter? = null

    var itemFeedListFromDB: List<ItemFeed> = ArrayList()

    private var mDonwloadFinishedReceiver: PodcastDownloadFinishedReceiver? = null

    private val serviceCon = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            podcastPlayerService = null
            isBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            val mBinder = binder as PlayPodcastService.MusicBinder
            podcastPlayerService = mBinder.service
            isBound = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Timer do Worker para Atualizar RSS Feed
        val sharedPref = this.defaultSharedPreferences
        val rssUpdateTime = sharedPref.getString("feed_update", "15")?.toLong() ?: 15

        val downloadWorkRequest = PeriodicWorkRequestBuilder<DownloadWorker>( rssUpdateTime, TimeUnit.MINUTES ).build()
        WorkManager.getInstance(this).enqueue(downloadWorkRequest)


        // Criando Receiver
        mDonwloadFinishedReceiver = PodcastDownloadFinishedReceiver()


        // Usando doAsync da Anko
        doAsync {

            // Usando HttpURLConnection como Client HTTP para requisitar RSS
            // Uma permissão no AndroidManifest para uso de Internet tb foi necessaria
            val httpCon = (URL(Consts.RSS_FEED_URL).openConnection() as HttpURLConnection)

            val feedStr: String?

            // Pegando referencia ao banco de dados
            val itemFeedDB = PodcastDB.getDatabase(this@MainActivity)

            // Logica para baixar RSS e armazenar no DB
            try {

                val inStream = BufferedInputStream(httpCon.inputStream)
                feedStr = inStream.bufferedReader().readText()

                // Ao tentar usar o operador de spread (*), um erro de tipagem ocorria
                // Achei mais simples fazer um for each.
                Parser.parse(feedStr).forEach {
                    val oldItemFeed = itemFeedDB.itemFeedDAO().getItemByLink(it.link)

                    if (oldItemFeed == null) {
                        itemFeedDB.itemFeedDAO().addFeedItem(it)
                    }
                }

            } catch (e: IOException) {
                Log.d(Consts.DEBUG_TAG, "Erro ao baixar RSS")
            } finally {
                httpCon.disconnect()
            }

            // Logica para recuperar os itens do DB
            try {
                itemFeedListFromDB = itemFeedDB.itemFeedDAO().getAllFeedItem()
            } catch (e: IOException) {
                Log.d(Consts.DEBUG_TAG, "Erro ao acessar DB")
            }

            uiThread {
                feedAdapter = CustomAdapter(this@MainActivity, itemFeedListFromDB, PlayerListener())

                val linearManager = LinearLayoutManager(this@MainActivity)

                recycler_feed.adapter = feedAdapter
                recycler_feed.layoutManager = linearManager
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pref_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.preferences -> {
                val intent = Intent(this, PreferenceActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val bindIntent = Intent(this, PlayPodcastService::class.java)
            isBound = bindService(bindIntent, serviceCon, Context.BIND_AUTO_CREATE)
        }

        val podcastServiceIntent = Intent(this, PlayPodcastService::class.java)
        startService(podcastServiceIntent)
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter()
        intentFilter.addAction(Consts.BROADCAST_DOWNLOAD_FINISHED)

        LocalBroadcastManager.getInstance(this).registerReceiver(mDonwloadFinishedReceiver as PodcastDownloadFinishedReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()

        if (mDonwloadFinishedReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDonwloadFinishedReceiver as PodcastDownloadFinishedReceiver)
        }
    }

    override fun onStop() {
        unbindService(serviceCon)
        isBound = false

        super.onStop()
    }

    // Listener para acessar propriedades da MainActivity pelo CustomAdapter
    override fun onDestroy() {
        // Destruir o Intent para evitar Memory Leaks
        stopService(Intent(this, PlayPodcastService::class.java))
        super.onDestroy()
    }

    internal inner class PlayerListener : PodcastPlayerListener {
        override fun playPodcast(feedItemId: Int) {
            this@MainActivity.podcastPlayerService?.startPlayer(feedItemId)
        }

        override fun pausePodcast(feedItemId: Int) {
            this@MainActivity.podcastPlayerService?.pausePlayer(feedItemId)
        }

        override fun isPlaying(): Boolean {
            return this@MainActivity.podcastPlayerService?.isPlaying() ?: false
        }
    }


    // Criado dentro da MainActivity para ter acesso a mesma
    internal inner class PodcastDownloadFinishedReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            Log.d(Consts.DEBUG_TAG, "Download receiver ativo")

            if (intent.action == Consts.BROADCAST_DOWNLOAD_FINISHED) {
                Toast.makeText(this@MainActivity, "Download completo", Toast.LENGTH_SHORT).show()
                val position = intent.extras?.getInt(Consts.EXTRA_FEED_ITEM_POSITION)


                doAsync {

                    val itemFeedDAO = PodcastDB.getDatabase(this@MainActivity).itemFeedDAO()
                    itemFeedListFromDB = itemFeedDAO.getAllFeedItem()

                    this@MainActivity.feedAdapter?.notifyItemChanged(position!!)
                    this@MainActivity.recycler_feed.refreshDrawableState()
                }

            }
        }
    }



}
