package com.jungbae.mask.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.network.SimpleSchoolMealData
import io.reactivex.subjects.PublishSubject


class MealDetailRecyclerAdapter(private val list: List<SimpleSchoolMealData>, private var subject: PublishSubject<SimpleSchoolMealData>): RecyclerView.Adapter<MealDetailHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealDetailHolder {
        return MealDetailHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: MealDetailHolder, position: Int) {
       // holder.bind(list.get(position), subject)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}