package com.jungbae.mask.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.R
import com.jungbae.mask.network.SimpleSchoolData
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.search_school_row.view.*

class SearchSchoolHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.search_school_row, parent, false)) {

    var data: SimpleSchoolData? = null

    fun bind(data: SimpleSchoolData, subject: PublishSubject<SimpleSchoolData>) {
        this.data = data
        itemView.list_title.text = data.name
        itemView.list_description.text = data.address

        itemView.setOnClickListener {
            subject?.let {
                it.onNext(data)
            }
        }
    }
}