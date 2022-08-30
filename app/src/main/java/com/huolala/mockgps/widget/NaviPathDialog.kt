package com.huolala.mockgps.widget

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ScreenUtils
import com.castiel.common.base.BaseListAdapter
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ItemTitleBinding
import java.io.File


/**
 * @author jiayu.liu
 */
class NaviPathDialog(context: Context) : Dialog(context),
    View.OnClickListener {
    private var recycler: RecyclerView
    private var btnConfirm: AppCompatButton
    private var btnCancel: AppCompatButton
    private var naviPathAdapter: NaviPathAdapter? = null
    private var list: MutableList<String> = mutableListOf()
    internal var listener: NaviPathListener? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_navi_path, null, false).apply {
                recycler = findViewById(R.id.recycler)
                btnConfirm = findViewById(R.id.btn_delete_all)
                btnCancel = findViewById(R.id.btn_cancel)
            }
        setContentView(view)

        window?.let {
            val lp: WindowManager.LayoutParams = it.attributes
            lp.width = ScreenUtils.getScreenWidth()
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            it.attributes = lp
        }

        recycler.layoutManager = LinearLayoutManager(context)
        naviPathAdapter = NaviPathAdapter().apply {
            clickListener = object : BaseListAdapter.OnItemClickListener<String> {
                override fun onItemClick(view: View?, t: String, position: Int) {
                    listener?.onItemClick(t)
                    dismiss()
                }

            }
        }
        recycler.adapter = naviPathAdapter

        btnConfirm.setOnClickListener(this)
        btnCancel.setOnClickListener(this)

        var listFiles = context.getExternalFilesDir("nav_path")?.absolutePath?.let {
            val file = File(it)
            file.listFiles()
        }?.map {
            if (it.length() > 0) {
                list.add(it.absolutePath)
            } else {
                FileUtils.delete(it)
            }
        }.also {
            //反转最新的数据排期前
            list.reverse()
            naviPathAdapter?.submitList(list)
        };
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_delete_all -> {
                val path = context.getExternalFilesDir("nav_path")?.absolutePath
                if (!TextUtils.isEmpty(path)) {
                    FileUtils.delete(
                        File(
                            path!!
                        )
                    ).let {
                        val result = if (it) "删除成功" else "删除失败"
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                    }
                }
                naviPathAdapter?.submitList(null)
            }
            R.id.btn_cancel -> {
                dismiss()
            }
            else -> {
            }
        }
    }


    class NaviPathAdapter() :
        BaseListAdapter<String, NaviPathAdapter.ViewHolder>(object :
            DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }) {

        class ViewHolder(val binding: ItemTitleBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val bind = DataBindingUtil.bind<ItemTitleBinding>(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_title, parent, false)
            )
            return ViewHolder(bind!!)
        }

        override fun onBindViewHolderModel(holder: ViewHolder, position: Int) {
            val path = getItem(position)
            try {
                val findLastAnyOf = path.lastIndexOf("/")
                val str = path.subSequence(findLastAnyOf + 1, path.length).toString()
                holder.binding.title = str
            } catch (e: Exception) {
                e.printStackTrace()
                val context = holder.itemView.context
                if (context != null) {
                    Toast.makeText(context, "文件获取异常", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    interface NaviPathListener {
        fun onItemClick(path: String)
    }
}