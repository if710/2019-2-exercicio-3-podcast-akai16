package br.ufpe.cin.android.podcast.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = arrayOf(ItemFeed::class), version = 3)
abstract class PodcastDB : RoomDatabase() {

    abstract fun itemFeedDAO(): ItemFeedDAO

    // Aplicando o padrao de Projeto Singleton,
    // para diminuir a quantidade de recursos e possiveis memory leaks
    // que poderiam ser causados em multiplos acessos ao BD.
    companion object {
        private var INSTANCE: PodcastDB? = null

        fun getDatabase(ctx: Context): PodcastDB {

            if (INSTANCE == null) {
                synchronized(PodcastDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        ctx.applicationContext,
                        PodcastDB::class.java,
                        "itemFeed.db"
                    ).fallbackToDestructiveMigration().build()
                }
            }

            return INSTANCE!!
        }
    }


}