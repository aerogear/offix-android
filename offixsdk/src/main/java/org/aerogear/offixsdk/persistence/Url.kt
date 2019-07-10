package org.aerogear.offixsdk.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

//persistent offline mutation object url
@Entity(tableName = "MutationUrl")
data class Url(
    var urlString:String,
    @PrimaryKey(autoGenerate = true) var SNo:Int=0
)
