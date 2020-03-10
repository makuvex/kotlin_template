package com.jungbae.mask.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.network.SimpleSchoolData
import io.reactivex.subjects.PublishSubject


class SearchRecyclerAdapter(private val list: List<SimpleSchoolData>, private var subject: PublishSubject<SimpleSchoolData>): RecyclerView.Adapter<SearchSchoolHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSchoolHolder {
        return SearchSchoolHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: SearchSchoolHolder, position: Int) {
        val name = list.get(position).name
        val address = list.get(position).address

        holder.bind(list.get(position), subject)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}