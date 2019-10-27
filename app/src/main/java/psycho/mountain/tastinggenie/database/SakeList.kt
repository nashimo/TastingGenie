package psycho.mountain.tastinggenie.database

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
class SakeList(var id: Int, var name: String, var grade: String, var type: String,
               var image: String, var maker: String, var prefecture: String, var sake_deg: Float,
               var pol_rate: Int, var alcohol: Int, var rice: String, var yeast: String): Parcelable