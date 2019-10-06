package br.ufpe.cin.android.podcast.models

import androidx.room.*
import br.ufpe.cin.android.podcast.models.ItemFeed

@Dao
interface ItemFeedDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFeedItem(vararg feedItemFeed: ItemFeed)

    @Query("SELECT * FROM item_feed")
    fun getAllFeedItem() : List<ItemFeed>

    @Query("SELECT * FROM item_feed WHERE :link = link")
    fun getItemByLink(link: String): ItemFeed?

    @Query("SELECT * FROM item_feed WHERE :id = id")
    fun getFeedItemByID(id: Int): ItemFeed

    @Update
    fun updateFeedItem(feedItem: ItemFeed)

}