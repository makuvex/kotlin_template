package com.jungbae.mask.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.R

import com.jungbae.mask.network.SimpleSchoolMealData
import com.jungbae.mask.network.Store
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.home_card_row.view.*
import kotlinx.android.synthetic.main.meal_detail_row.view.*
import kotlinx.android.synthetic.main.meal_detail_row.view.date
import kotlinx.android.synthetic.main.meal_detail_row.view.info


class MealDetailHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.meal_detail_row, parent, false)) {

    var data: Store? = null

    fun bind(data: Store, subject: PublishSubject<Store>) {
        this.data = data

        itemView.name.text = data.name
        itemView.date.text = data.created_at

        itemView.info.text = "이거 내용 채워 넣어야 함"

//        if(data.meal.isNotEmpty()) {
//            itemView.meal_info.text = data.meal.replace("<br/>", "\n")
//
//        } else {
//            itemView.meal_info.text = "급식 정보 없음"
//        }
//
        itemView.time.text = data.stock_at
        //itemView.extra_info.text = data.cal

//        itemView.setOnClickListener {
//            subject?.let {
//                it.onNext(data)
//            }
//        }
    }
}