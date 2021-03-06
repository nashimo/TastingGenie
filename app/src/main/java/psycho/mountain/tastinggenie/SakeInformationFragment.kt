package psycho.mountain.tastinggenie

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.dialog_hint.*
import kotlinx.android.synthetic.main.fragment_sake_information.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk27.coroutines.onFocusChange
import org.jetbrains.anko.sdk27.coroutines.onGroupClick
import org.jetbrains.anko.textColor
import psycho.mountain.tastinggenie.database.SakeList
import psycho.mountain.tastinggenie.listview.CustomExpandableListAdapter
import psycho.mountain.tastinggenie.listview.ExpandableCheckListAdapter
import psycho.mountain.tastinggenie.utility.*
import java.lang.NullPointerException
import java.lang.NumberFormatException
import java.util.ArrayList
import java.util.HashMap

class SakeInformationFragment : Fragment() {

    private var sakeList: SakeList? = null  // データ更新時は既存の値をBundleで渡される
    private var listener: SakeInformationFragmentListener? = null

    interface SakeInformationFragmentListener {
        fun onClickAddButton(sakeList: SakeList)
        fun onClickAddPhotoButton(imageView: ImageView)
    }

    companion object {
        fun newInstance() : SakeInformationFragment{
            return SakeInformationFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            sakeList = it.getParcelable("sake") as SakeList
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is SakeInformationFragmentListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sake_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sakeList?.let {
            setupView(it)
        }

        context?.let {
            sake_information_grade.setOnClickListener {
                dialogSakeGrade(context!!)
            }
            sake_information_type.setOnClickListener {
                dialogSakeType(context!!)
            }
            sake_information_prefecture.setOnClickListener {
                dialogSakePrefecture(context!!)
            }

            checkbox_same_rice.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    layout_kake_mai.visibility = TextView.GONE
                    layout_kake_pol.visibility = TextView.GONE
                    text_koji_mai.text = getString(R.string.sake_kome)
                } else {
                    layout_kake_mai.visibility = TextView.VISIBLE
                    layout_kake_pol.visibility = TextView.VISIBLE
                    sake_information_kake_mai.text = sake_information_koji_mai.text
                    sake_information_kake_pol.text = sake_information_koji_pol.text
                    text_koji_mai.text = getString(R.string.sake_koji_mai)
                }
            }

            if (sake_information_kake_mai.text.toString() != "") {
                if ((sake_information_koji_mai.text.toString() == sake_information_koji_mai.text.toString())
                    && (sake_information_koji_pol.text.toString() == sake_information_koji_pol.text.toString())
                ) {
                        checkbox_same_rice.isChecked = true
                }
            }

            // 甘辛を自動で入力
            sake_information_sake_deg.onFocusChange { _, _ ->
                try {
                    val deg = sake_information_sake_deg.text.toString().toFloat()
                    sake_information_sake_deg_level.text = degToSweetLevel(deg, it)
                }
                catch(e: Exception){
                    when(e) {
                        is NullPointerException, is NumberFormatException -> {
                            e.printStackTrace()
                            sake_information_sake_deg_level.text = ""
                        }
                    }
                }
            }

            // DBに追加する処理
            button_add_sake_list.setOnClickListener {
                // 入力チェックOKならば DB 操作して状態遷移
                if (validateSakeList()) {
                    val sakeList = makeSakeList()
                    listener?.onClickAddButton(sakeList)
                }
            }

            // 写真追加処理
            button_add_sake_photo.setOnClickListener{
                listener?.onClickAddPhotoButton(sake_information_image)
            }

            // 長押しで写真削除
            sake_information_image.setOnLongClickListener {
                AlertDialog.Builder(context!!).apply {
                    setTitle("画像の削除")
                    setMessage("本当に消しますか？")
                    setPositiveButton("はい", DialogInterface.OnClickListener { _, _ ->
                        sake_information_image.setImageResource(R.drawable.empty_image)
                        val uri = Uri.parse(sake_information_image.contentDescription.toString())
                        deleteFileByUri(uri, context)
                    })
                    setNegativeButton("いいえ", DialogInterface.OnClickListener{_, _ -> })
                    show()
                }
                true

            }
        }
    }

    private fun setupView(sake: SakeList) {
        if (sake.image != "") {
            Log.d("sake.image", sake.image)
            sake_information_image.setImageURI(Uri.parse(sake.image))
            sake_information_image.contentDescription = sake.image
        }
        if (sake.name != "") {sake_information_name.setText(sake.name)}
        if (sake.grade != "") {sake_information_grade.text = sake.grade}
        if (sake.type != "") {
            sake_information_type.text = sake.type

            val regex = Regex("その他")
            if (regex.containsMatchIn(sake.type)) {
                sake_information_type_other_layout.visibility = EditText.VISIBLE
                if (sake.type_other != "") {sake_information_type_other.setText(sake.type_other)}
            }
        }

        if (sake.maker != "") {sake_information_maker.setText((sake.maker))}
        if (sake.prefecture != "") {sake_information_prefecture.text = sake.prefecture}
        if (sake.city != "") {sake_information_city.setText(sake.city)}
        if (sake.alcohol >= 0) {sake_information_alcohol.setText(sake.alcohol.toString())}
        if (sake.koji_mai != "") {sake_information_koji_mai.setText(sake.koji_mai)}
        if (sake.koji_pol >= 0) {sake_information_koji_pol.setText(sake.koji_pol.toString())}
        if (sake.kake_mai != "") {sake_information_kake_mai.setText(sake.kake_mai)}
        if (sake.kake_pol >= 0) {sake_information_kake_pol.setText(sake.kake_pol.toString())}

        if (sake.sake_deg >= 0.0F) {
            sake_information_sake_deg.setText(sake.sake_deg.toString())
            sake_information_sake_deg_level.text = degToSweetLevel(sake.sake_deg, context!!)
        }
        if (sake.acidity >= 0) {sake_information_acidity.setText(sake.acidity.toString())}
        if (sake.amino >= 0) {sake_information_amino.setText(sake.amino.toString())}

        if (sake.yeast != "") {sake_information_yeast.setText(sake.yeast)}
        if (sake.water != "") {sake_information_water.setText(sake.water)}

        if (sake.id != -1) {sake_information_hidden_id.text = sake.id.toString()}
        button_add_sake_list.text = "更新"
    }

    private fun makeSakeList(): SakeList {
        if (checkbox_same_rice.isChecked) {
            sake_information_kake_mai.text = sake_information_koji_mai.text
            sake_information_kake_pol.text = sake_information_koji_pol.text
        }
        return SakeList(viewToInt(sake_information_hidden_id),// IDはダミー．DBに自動入力して貰う
            viewToString(sake_information_name),
            viewToString(sake_information_grade),
            viewToString(sake_information_type),
            viewToString(sake_information_type_other),
            if (sake_information_image.contentDescription == null) ""
            else sake_information_image.contentDescription.toString(),
            viewToString(sake_information_maker),
            viewToString(sake_information_prefecture),
            viewToString(sake_information_city),
            viewToInt(sake_information_alcohol),
            viewToString(sake_information_yeast),
            viewToString(sake_information_water),
            viewToFloat(sake_information_sake_deg),
            viewToFloat(sake_information_acidity),
            viewToFloat(sake_information_amino),
            viewToString(sake_information_koji_mai),
            viewToInt(sake_information_koji_pol),
            viewToString(sake_information_kake_mai),
            viewToInt(sake_information_kake_pol)
        )
    }

    private fun validateSakeList() : Boolean{
        var msg: String = ""

        // 必須項目: 酒名
        if (sake_information_name.text.toString() == "") {
            sake_information_must1.visibility = View.VISIBLE
            msg += "酒名 "
        }
        else {
            sake_information_must1.visibility = View.GONE
        }

        // 必須項目: 酒類
        if (sake_information_grade.text.toString() == "") {
            sake_information_must2.visibility = View.VISIBLE
            msg += "酒類 "
        }
        else {
            sake_information_must2.visibility = View.GONE
        }

        if (msg != "") {
            msg += "は必須項目です"
            sake_information_scroll.fullScroll(ScrollView.FOCUS_UP)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

        }

        return msg == ""
    }

    // ---------------------------------------------------
    // -------------       Dialogs     -------------------
    // ---------------------------------------------------
    private fun dialogSakeGrade(context : Context) {
        val gradeList : Array<String> = arrayOf("純米大吟醸", "大吟醸", "純米吟醸",
            "吟醸", "特別純米", "特別本醸造", "純米", "本醸造", "その他")

        // 選択されていた値を取り出し
        var grade : Int = gradeList.indexOf(sake_information_grade.text)
        if (grade < 0) { grade = 0}

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.sake_grade)
            .setSingleChoiceItems(gradeList, grade) { _, which ->
                grade = which
            }
            .setPositiveButton("OK") { _, _ ->
                sake_information_grade.text = gradeList[grade]
            }
            .show()
    }

    private fun dialogSakeType(context : Context) {
        // ヒントの設定
        val listDialogs: MutableList<AlertDialog> = arrayListOf()
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("生酛", "酒母を手作業で作る製法．あらかじめ乳酸を加えずに，自然に生じる乳酸菌で酵母を殖やす"),
            Pair("山廃酛", "生酛を製造する手順のうち，山卸（米をすり潰す作業）を廃したもの．"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("生詰め酒", "火入れして貯蔵し，火入れせずに瓶詰めするもの．"),
            Pair("生貯蔵酒", "火入れせずに貯蔵し，火入れして瓶詰するもの．"),
            Pair("生酒", "貯蔵時も瓶詰時も火入れしないもの"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("原酒", "加水せずに瓶詰した酒"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("冷やおろし", "火入れ後に，春・夏を蔵で貯蔵し，秋に火入れせずに瓶詰したもの"),
            Pair("雪室貯蔵", "雪室で貯蔵したもの"),
            Pair("樽酒", "樽で貯蔵したもの"),
            Pair("長期熟成酒", "満3年以上蔵元で熟成させた、糖類添加酒を除く清酒"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("新酒", "酒造年度（7月～翌6月）内に出荷されたもの．冷やおろしは，秋口に出荷するため，新酒とはならない"),
            Pair("古酒", "製造後1年以上経ったもの"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("にごり酒", "醪を目の粗い布で濾したもの"),
            Pair("おり酒", "上槽後に，沈澱するのを待たずに澱と一緒に瓶詰したもの"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("荒走り", "絞り最初の部分を集めたもの"),
            Pair("中汲み", "絞り途中の部分を集めたもの"),
            Pair("責め", "絞りの最後の部分を集めたもの"),
            Pair("雫酒", "醪の詰まった酒袋を吊るし，滴り落ちる雫だけを集めたもの"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("凍結酒", "シャーベット状に凍らせたもの"),
            Pair("発砲酒", "炭酸ガスをふくむもの．スパークリング日本酒．発酵による方法や炭酸ガスを注入する方法がある．"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("生一本", "ひとつの製造場だけで醸造した純米酒．原義は，寒造りした酒を容器からそのまま瓶詰したもの．"),
            Pair("貴醸酒", "仕込みの水の代わりに，日本酒を利用したもの"))))
        listDialogs.add(dialogHintMsg(context, arrayListOf(
            Pair("その他", "[内容を記載ください]"))))

        val listData = LinkedHashMap<String, List<String>>()

        listData["酛"] = arrayListOf("生酛", "山廃酛")
        listData["火入れ"] = arrayListOf("生詰め酒", "生貯蔵酒", "生酒")
        listData["加水"] = arrayListOf("原酒")
        listData["貯蔵"] = arrayListOf("冷やおろし", "雪室貯蔵", "樽酒", "長期熟成酒")
        listData["新旧"] = arrayListOf("新酒", "古酒")
        listData["濾し"] = arrayListOf("にごり酒", "おり酒", "どぶろく")
        listData["絞り"] = arrayListOf("荒走り", "中汲み", "責め", "雫酒")
        listData["食感"] = arrayListOf("凍結酒", "発泡酒")
        listData["製造"] = arrayListOf("生一本", "貴醸酒")
        listData["その他"] = arrayListOf("その他")

        val view = ExpandableListView(context)
        val titleList: ArrayList<String> = ArrayList(listData.keys)
        val adapter = ExpandableCheckListAdapter(context, titleList, listData, sake_information_type.text.split(","), listDialogs)
        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.sake_type)
            .setView(view)
            .setPositiveButton("OK") { _,_ ->
                val types = adapter.getCheckedItems().joinToString()
                sake_information_type.text = types
                val regex = Regex("その他")
                if (regex.containsMatchIn(types)) {
                    sake_information_type_other_layout.visibility = EditText.VISIBLE
                }
                else {
                    sake_information_type_other_layout.visibility = EditText.GONE
                }
            }
            .create()

        view.setAdapter(adapter)
        // グループを展開しておく (展開・圧縮させない)
        for (i in 0 until adapter.groupCount) {
            view.expandGroup(i)
        }
        view.setOnGroupClickListener { _, _, _, _ ->
            true
        }
        dialog.show()
    }

    private fun dialogSakePrefecture(context : Context) {
        val listData = LinkedHashMap<String, List<String>>()

        listData["北海道"] = arrayListOf("北海道")
        listData["東北"] = arrayListOf("青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県")
        listData["関東"] = arrayListOf("茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県")
        listData["甲信越"] = arrayListOf("山梨県", "長野県", "新潟県")
        listData["北陸"] = arrayListOf("富山県", "石川県", "福井県")
        listData["東海"] = arrayListOf("岐阜県", "静岡県", "愛知県", "三重県")
        listData["関西"] = arrayListOf("滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県")
        listData["中国"] = arrayListOf("鳥取県", "島根県", "岡山県", "広島県", "山口県")
        listData["四国"] = arrayListOf("徳島県", "香川県", "愛媛県", "高知県")
        listData["九州・沖縄"] = arrayListOf("福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県")

        val view = ExpandableListView(context)
        val titleList: ArrayList<String> = ArrayList(listData.keys)
        val adapter = CustomExpandableListAdapter(context, titleList, listData)

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.sake_prefecture)
            .setView(view)
            .create()

        view.setAdapter(adapter)
        view.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            sake_information_prefecture.text = listData[titleList[groupPosition]]!!.get(childPosition)
            dialog.dismiss()
            true
        }
        dialog.show()
    }

    private fun dialogHintMsg(context : Context, msg: List<Pair<String, String>>): AlertDialog {
        val viewLinearLayout = LinearLayout(context)
        viewLinearLayout.orientation = LinearLayout.VERTICAL
        viewLinearLayout.setPadding(20, 10, 20, 10)

        // TODO: サイズのハードコード
        msg.forEach {
            val (title, description) = it
            val viewTitle = TextView(context)
            viewTitle.text = title
            viewTitle.textSize = 22f //resources.getDimension(R.dimen.text_large)
            viewTitle.textColor = Color.rgb(0x00, 0x00, 0x00)
            val viewDescription = TextView(context)
            viewDescription.text = description
            viewDescription.textSize = 16f //resources.getDimension(R.dimen.text_normal)
            viewDescription.setPadding(0, 5, 0, 18)

            viewLinearLayout.addView(viewTitle)
            viewLinearLayout.addView(viewDescription)
        }

        val dialog = AlertDialog.Builder(context)
            .setView(viewLinearLayout)
            .create()
        return dialog
    }
}