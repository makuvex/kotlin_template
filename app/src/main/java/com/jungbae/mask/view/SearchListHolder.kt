package com.jungbae.mask.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.mask.R
import com.jungbae.mask.network.Store
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.search_list_row.view.*


class SearchListHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.search_list_row, parent, false)) {

    var data: Store? = null

    fun bind(data: Store, subject: PublishSubject<Store>) {
        this.data = data

        itemView.list_title.text = "상호명 : " + data.name
        remainStat(itemView.remain_stat, data)
//        itemView.remain_stat.text = "잔여수량 : " + remainStat(data.remain_stat)
        itemView.list_address.text =  "주소 : " + data.addr
        itemView.store_at.text = "입고시간 : " + if(data.stock_at.isNullOrEmpty()) "미정" else data.stock_at


//        itemView.list_title.text = data.name
//        itemView.list_description.text = data.address
//
        itemView.setOnClickListener {
            subject?.let {
                it.onNext(data)
            }
        }
    }
    fun remainStat(view: TextView, data: Store) {
        when(data.remain_stat) {
            "plenty" -> {
                view.setTextColor(Color.GREEN)
                view.text = "잔여수량 : 100개이상"
            }
            "some" -> {
                view.setTextColor(Color.parseColor("#faa805"))
                view.text = "잔여수량 : 30~100개"
            }
            "few" -> {
                view.setTextColor(Color.RED)
                view.text = "잔여수량 : 2~30개"
            }
            "empty" -> {
                view.setTextColor(Color.GRAY)
                view.text = "잔여수량 : 품절"
            }
            "break" -> {
                view.setTextColor(Color.GRAY)
                view.text = "잔여수량 : 판매중지"
            }
            else -> {
                view.setTextColor(Color.GRAY)
                view.text = "잔여수량 : 품절"
            }
        }
    }

    fun remainStat(stat: String?): String {
        when(stat) {
            "plenty" -> { return "100개이상" }
            "some" -> { return "30~100개" }
            "few" -> { return "2~30개" }
            "empty" -> { return "품절" }
            "break" -> { return "판매중지" }
            else -> { return "품절" }
        }
    }

}