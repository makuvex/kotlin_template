package com.jungbae.mask.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.R
import io.reactivex.subjects.PublishSubject

class HomeCardEmptyHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.home_card_empty_row, parent, false)) {

    fun bind(subject: PublishSubject<Boolean>) {
        itemView.setOnClickListener {
            subject?.let {
                it.onNext(true)
            }
        }
    }
}