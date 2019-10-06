package br.ufpe.cin.android.podcast.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.ParsePosition


// Deixei de ser preguisoço e fui ver como colocava uma primary key auto incrementavel
// Usando a interface Serializable para ter a capacidade e passar os objetos entre activities.
// Um approach melhor seria utilizar da interface parcelable.
@Entity(tableName= "item_feed")
data class ItemFeed(val title: String, val link: String, val pubDate: String, val description: String, val downloadLink: String, val image: String? = null, var mp3Path: String? = null, var seekPosition: Int?) : Serializable {

   @PrimaryKey(autoGenerate = true)
   var id: Int? = null

    override fun toString(): String {
        return title
    }
}
