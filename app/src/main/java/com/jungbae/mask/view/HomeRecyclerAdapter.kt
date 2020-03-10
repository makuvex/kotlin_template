package com.jungbae.mask.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.network.SimpleSchoolMealData
import io.reactivex.subjects.PublishSubject


class HomeRecyclerAdapter(private val list: List<SimpleSchoolMealData>, private var selectSubject: PublishSubject<SimpleSchoolMealData>, private var deleteSubject: PublishSubject<SimpleSchoolMealData>): RecyclerView.Adapter<HomeCardHolder>() {

    private var option: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCardHolder {
        return HomeCardHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: HomeCardHolder, position: Int) {
        holder.bind(list.get(position), selectSubject, deleteSubject, option)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyDataSetChangedWith(option: Boolean) {
        this.option = option
        this.notifyDataSetChanged()
    }

}