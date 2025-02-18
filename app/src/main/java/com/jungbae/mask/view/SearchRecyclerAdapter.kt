package com.jungbae.mask.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.network.Store
import io.reactivex.subjects.PublishSubject


class SearchRecyclerAdapter(private val list: List<Store>, private var subject: PublishSubject<Store>): RecyclerView.Adapter<SearchListHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListHolder {
        return SearchListHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: SearchListHolder, position: Int) {
//        val name = list.get(position).name
//        val address = list.get(position).address

        holder.bind(list.get(position), subject)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}