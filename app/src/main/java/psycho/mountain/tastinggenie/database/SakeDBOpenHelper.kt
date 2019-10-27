package psycho.mountain.tastinggenie.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class SakeDBOpenHelper (context: Context): ManagedSQLiteOpenHelper(context,
    DB_SAKE, null, DB_VERSION) {

    companion object {
        const val DB_SAKE = "sake.db"
        const val DB_VERSION = 1

        const val TABLE_SAKE_LIST = "sake_list"
        const val TABLE_SAKE_REVIEW = "sake_review"

        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_GRADE = "grade"
        const val COL_TYPE = "type"
        const val COL_IMAGE = "image"
        const val COL_MAKER = "maker"
        const val COL_PREF = "prefecture"
        const val COL_SAKE_DEG = "sake_degree"
        const val COL_POL_RATE = "polishing_rate"
        const val COL_ALCOHOL = "alcohol"
        const val COL_RICE = "rice"
        const val COL_YEAST = "yeast"

        const val COL_REVIEW_ID = "review_id"
        const val COL_DATE = "date"
        const val COL_BAR = "bar"
        const val COL_PRICE = "price"
        const val COL_TEMP = "temperature"
        const val COL_COLOR = "color"
        const val COL_SCENT1 = "scent1"
        const val COL_SCENT2 = "scent2"
        const val COL_SCENT3 = "scent3"
        const val COL_SWEET = "sweet"
        const val COL_SOUR = "sour"
        const val COL_BITTER = "bitter"
        const val COL_UMAMI = "umami"
        const val COL_VISCOSITY = "viscosity"
        const val COL_SCENE = "scene"
        const val COL_DISH = "dish"
        const val COL_COMMENT = "comment"
        const val COL_REVIEW = "review"

        private var instance: SakeDBOpenHelper? = null

        fun newInstance(context: Context): SakeDBOpenHelper {
            return instance
                ?: SakeDBOpenHelper(context.applicationContext)!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.run {
            createTable(
                TABLE_SAKE_LIST, ifNotExists = true,
                columns = *arrayOf(
                    COL_ID to INTEGER + PRIMARY_KEY + UNIQUE, // INTEGER + PRIMARY_KEY + UNIQUE,
                    COL_NAME to TEXT,
                    COL_GRADE to TEXT,
                    COL_TYPE to TEXT,
                    COL_IMAGE to TEXT,
                    COL_MAKER to TEXT,
                    COL_PREF to TEXT,
                    COL_SAKE_DEG to REAL,
                    COL_POL_RATE to INTEGER,
                    COL_ALCOHOL to INTEGER,
                    COL_RICE to TEXT,
                    COL_YEAST to TEXT
                )
            )
            createTable(
                TABLE_SAKE_REVIEW, ifNotExists = true,
                columns = *arrayOf(
                    COL_REVIEW_ID to INTEGER + PRIMARY_KEY + UNIQUE, // INTEGER + PRIMARY_KEY + UNIQUE,
                    COL_ID to INTEGER,
                    COL_DATE to TEXT,
                    COL_BAR to TEXT,
                    COL_PRICE to TEXT,
                    COL_TEMP to TEXT,
                    COL_COLOR to TEXT,
                    COL_SCENT1 to TEXT,
                    COL_SCENT2 to TEXT,
                    COL_SCENT3 to TEXT,
                    COL_SWEET to TEXT,
                    COL_SOUR to TEXT,
                    COL_BITTER to TEXT,
                    COL_UMAMI to TEXT,
                    COL_VISCOSITY to TEXT,
                    COL_SCENE to TEXT,
                    COL_DISH to TEXT,
                    COL_COMMENT to TEXT,
                    COL_REVIEW to REAL
                )
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // 今は特にすることなし
    }

}