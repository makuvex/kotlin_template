package com.jungbae.mask.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.R

import com.jungbae.mask.network.SimpleSchoolMealData
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.meal_detail_row.view.*


class MealDetailHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.meal_detail_row, parent, false)) {

    var data: SimpleSchoolMealData? = null

    fun bind(data: SimpleSchoolMealData, subject: PublishSubject<SimpleSchoolMealData>) {
        this.data = data

        itemView.school_name.text = data.name
        itemView.date.text = data.date

        if(data.meal.isNotEmpty()) {
            itemView.meal_info.text = data.meal.replace("<br/>", "\n")

        } else {
            itemView.meal_info.text = "급식 정보 없음"
        }

        itemView.meal_time.text = data.mealKind
        itemView.extra_info.text = data.cal

//        itemView.setOnClickListener {
//            subject?.let {
//                it.onNext(data)
//            }
//        }
    }
}