package com.example.mediaapplication
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey



//@Entity(tableName = "media_table")

/*
@Parcelize
data class Media(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uri: String,
    val type: String // "image" or "video"
): Parcelable
*/


@Entity(tableName = "media_table")

data class Media(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uri: String,
    val type: String,
    val userId: String,
    val name: String,
    val size: String,
    val created: String
 /*   val timestamp: String*/
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(uri)
        parcel.writeString(type)
        parcel.writeString(userId)
        parcel.writeString(name)
        parcel.writeString(size)
        parcel.writeString(created)
       /* parcel.writeString(timestamp)*/
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Media> {
        override fun createFromParcel(parcel: Parcel): Media = Media(parcel)
        override fun newArray(size: Int): Array<Media?> = arrayOfNulls(size)
    }
}

