package com.jungbae.mask.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.R
import com.jungbae.mask.SchoolFoodApplication
import com.jungbae.mask.network.SimpleSchoolMealData
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.home_card_row.view.*

//HomeCardEmptyHolder
//class HomeCardHolder(inflater: LayoutInflater, parent: ViewGroup):
//    RecyclerView.ViewHolder(inflater.inflate(R.layout.home_card_row, parent, false)) {

class HomeCardHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.home_card_row, parent, false)) {

    var data: SimpleSchoolMealData? = null

    fun bind(data: SimpleSchoolMealData, selectSubject: PublishSubject<SimpleSchoolMealData>, deleteSubject: PublishSubject<SimpleSchoolMealData>, option: Boolean = false) {
        this.data = data

        itemView.school_name.text = data.name
        itemView.date.text = data.date

        if(data.meal.isNotEmpty()) {
            itemView.meal_info.text = data.meal.replace("<br/>", "\n")
            itemView.more.visibility = View.VISIBLE
        } else {
            itemView.meal_info.text = "급식 정보 없음\n(휴일, 방학 혹은 학교에서 급식 정보를\n제공하지 않습니다)"
            //itemView.more.visibility = View.GONE
        }
        itemView.increaseTouchArea(itemView.delete, 50)
        itemView.meal_time.text = data.mealKind
        itemView.extra_info.text = data.cal

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
                val ani = AnimationUtils.loadAnimation(SchoolFoodApplication.context, R.anim.shake)
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