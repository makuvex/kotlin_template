package com.jungbae.mask.view

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.R
import com.jungbae.mask.CommonApplication
import com.jungbae.mask.network.SimpleSchoolMealData
import com.jungbae.mask.network.Store
import com.jungbae.mask.network.StoresByData
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.home_card_row.view.*
import kotlinx.android.synthetic.main.home_card_row.view.date
import kotlinx.android.synthetic.main.home_card_row.view.extra_info
import kotlinx.android.synthetic.main.home_card_row.view.info
import kotlinx.android.synthetic.main.meal_detail_row.view.*

//HomeCardEmptyHolder
//class HomeCardHolder(inflater: LayoutInflater, parent: ViewGroup):
//    RecyclerView.ViewHolder(inflater.inflate(R.layout.home_card_row, parent, false)) {

class HomeCardHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.home_card_row, parent, false)) {

    var data: Store? = null

    fun type(v: String): String {
        when(v) {
            "01" -> { return "약국" }
            "02" -> { return "우체국" }
            "03" -> { return "농협" }
        }
        return "기타"
    }

    fun bind(data: Store,
             selectSubject: PublishSubject<Store>,
             deleteSubject: PublishSubject<Store>,
             option: Boolean = false) {

        this.data = data

        itemView.name.text = data.name
        itemView.date.text = "재고상태: " + data.remain_stat

        itemView.info.text = "주소: " + data.addr
        itemView.more.visibility = View.VISIBLE
        itemView.extra_info.text = this.type(data.type)
        itemView.type.text = "입고시간: " + data.stock_at

        //Log.e("@@@","@@@ data ${data} @@@@")
        data.remain_stat?.let {
            when (it) {
                "plenty" -> {
                    itemView.date.setTextColor(Color.parseColor("#4dc412"))
                }
                "some" -> {
                    itemView.date.setTextColor(Color.parseColor("#e0d312"))
                }
                "few" -> {
                    itemView.date.setTextColor(Color.parseColor("#e03b12"))
                }
                "empty" -> {
                    itemView.date.setTextColor(Color.parseColor("#5c5454"))
                }
                else -> {
                    itemView.date.setTextColor(Color.parseColor("#5c5454"))
                }
            }
        }

//        if(data.meal.isNotEmpty()) {
//            itemView.meal_info.text = data.meal.replace("<br/>", "\n")
//            itemView.more.visibility = View.VISIBLE
//        } else {
//            itemView.meal_info.text = "급식 정보 없음\n(휴일, 방학 혹은 학교에서 급식 정보를\n제공하지 않습니다)"
//            //itemView.more.visibility = View.GONE
//        }
        itemView.increaseTouchArea(itemView.delete, 50)
        //itemView.time.text = data.created_at
        //itemView.extra_info.text = ""

        itemView.setOnClickListener {
            if(option) {
                return@setOnClickListener
            }
            selectSubject?.let {
                it.onNext(data)
            }
        }

        itemView.delete.setOnClickListener{
            Log.e("@@@","@@@ delete click")
            deleteSubject?.let {
                it.onNext(data)
            }
        }

        updateUI(option)
    }

    fun updateUI(option: Boolean) {
        when(option) {
            true ->  {
                val ani = AnimationUtils.loadAnimation(CommonApplication.context, R.anim.shake)
                itemView.delete.startAnimation(ani)
                itemView.delete.visibility = View.VISIBLE
            }
            false -> {
                itemView.delete.clearAnimation()
                itemView.delete.visibility = View.INVISIBLE
            }
        }
    }
}