package psycho.mountain.tastinggenie

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AlertDialogLayout
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_sake_list.*
import kotlinx.android.synthetic.main.fragment_sake_list.view.*
import psycho.mountain.tastinggenie.database.SakeDBManager
import psycho.mountain.tastinggenie.database.SakeList
import psycho.mountain.tastinggenie.listview.RecyclerAdapter
import psycho.mountain.tastinggenie.listview.RecyclerViewHolder

class SakeListFragment: Fragment() {

    lateinit var sakeList: List<SakeList>
    private var listener: SakeListListener? = null

    interface SakeListListener {
        fun onItemClick(sake: SakeList)
        fun onItemLongClick(sake: SakeList)
        fun onFabButtonClick()
    }

    companion object {
        fun newInstance() : SakeListFragment {
            return SakeListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: 再読み込み時に，データベースの中身が反映されていない
        arguments?.let{
            sakeList = it.getParcelableArrayList<SakeList>("sake_list") as List<SakeList>
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sake_list, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is SakeListListener) {
            listener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = recycler_list

        context?.let {
            val adapter = RecyclerAdapter(sakeList, object : RecyclerViewHolder.ItemClickListener {
                override fun onItemClick(position: Int) {
                    listener?.onItemClick(sakeList[position])
                }

                override fun onItemLongClick(position: Int) {
                    AlertDialog.Builder(it).apply {
                        setTitle("データベースの削除")
                        setMessage("本当に消しますか？")
                        setPositiveButton("はい", DialogInterface.OnClickListener { _, _ ->
                            listener?.onItemLongClick(sakeList[position])

                            Log.d("sakeList", sakeList.size.toString())
                            val sakeListMutable = sakeList.toMutableList()
                            sakeListMutable.removeAt(position)
                            sakeList = sakeListMutable.toList()
                            Log.d("sakeList", sakeList.size.toString())

                            recyclerView.removeViewAt(position)
                            recyclerView.adapter?.notifyItemRemoved(position)
                            recyclerView.adapter?.notifyItemRangeChanged(position, sakeList.size)
                            recyclerView.adapter?.notifyDataSetChanged()

                        })
                    }.show()
                }
            })

            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = GridLayoutManager(activity, 2)
            recyclerView.adapter = adapter
        }

        view.button_sake_list_add.setOnClickListener {
            listener?.onFabButtonClick()
        }
    }


}