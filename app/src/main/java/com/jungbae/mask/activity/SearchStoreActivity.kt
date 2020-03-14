package com.jungbae.mask.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.jakewharton.rxbinding3.view.clicks
import com.jungbae.mask.CommonApplication
import com.jungbae.mask.R
import com.jungbae.mask.network.*
import com.jungbae.mask.network.preference.PreferenceManager
import com.jungbae.mask.view.increaseTouchArea
import com.jungbae.mask.view.SearchRecyclerAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_search.*
import java.util.concurrent.TimeUnit

class SearchStoreActivity : AppCompatActivity() {
    private val disposeBag = CompositeDisposable()

    private lateinit var storeList: MutableList<Store>
    private lateinit var storeAdapter: SearchRecyclerAdapter

    private lateinit var selectedBehaviorSubject: PublishSubject<Store>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initializeUI()
        bindRxUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.clear()
    }

    init {
        selectedBehaviorSubject = PublishSubject.create()
        storeList = ArrayList()
        storeAdapter = SearchRecyclerAdapter(storeList, selectedBehaviorSubject)
    }

    fun initializeUI() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = storeAdapter
        }
        increaseTouchArea(search, 50)
        edit_text.requestFocus()
    }

    fun requestApiByAddr(addr: String) {
        val task = NetworkService.getInstance().getStoresByAddr(addr)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(ObservableResponse<StoresByData>(
                onSuccess = {
                    val list = it.stores.sortedWith(compareByDescending { data ->
                        when {
                            data.remain_stat == "plenty" -> 3
                            data.remain_stat == "some" -> 2
                            data.remain_stat == "few" -> 1
                            data.remain_stat == "empty" -> 0
                            else -> -1
                        }
                    })

                    list?.let {
                        updateUI(it)
                    }
                    if(list.isNotEmpty()) {
                        recycler_view.visibility = View.VISIBLE
                        no_data_view.visibility = View.GONE
                    } else {
                        recycler_view.visibility = View.GONE
                        no_data_view.visibility = View.VISIBLE
                    }
                }, onError = {
                    Log.e("@@@@@", "@@@@@ error $it")
                    //swipe_refresh.isRefreshing = false
                    //stopTimer()
                    Toast.makeText(CommonApplication.context, "서버와의 통신이 원할하지 않습니다.", Toast.LENGTH_LONG).show()
                }
            ))
        disposeBag.add(task)
    }

    fun bindRxUI() {
        val source = Observable.create<CharSequence> { emitter ->
            edit_text.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let {
                        if(it.isNotEmpty() && it.split(" ").count() > 1) {
                            emitter.onNext(it)
                        }
                    }
                }
            })
        }

        val textChanged = source.debounce(1000L, TimeUnit.MILLISECONDS)
            .filter { !TextUtils.isEmpty(it) }
            .observeOn(AndroidSchedulers.mainThread()) //Toast must be running on UI Thread
            .subscribe {
                //Toast.makeText(this, "searching => $it",Toast.LENGTH_SHORT).show()
                requestApiByAddr(it.toString())
            }

        recycler_view.addOnScrollListener(object: OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    val view = this@SearchStoreActivity.currentFocus
                    view?.let { v ->
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
        })

        val backDisposable = back.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                finish()
            }

        val searchDisposable = search.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .filter{ edit_text.length() > 0}
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{

                requestApiByAddr(edit_text.text.toString())
//                NetworkService.getInstance().getSchoolData("json", 1, 100, edit_text.text.toString(), "05b9d532ceeb48dd89238746bd9b0e16")
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeWith(ObservableResponse<Store>(
//                        onSuccess = {
//                            val list = it.schoolInfo.get(1).row.map{ data ->
//                                Store(data.schoolName, data.roadNameAddress, data.schoolCode, data.eduOfficecode)
//                            }
//
//                            list?.let {
//                                updateUI(it)
//                            }
//
//                            Log.d("@@@@@", "onSuccess ${it.reflectionToString()}")
//                            Log.d("@@@@@", "onSuccess list ${list}")
//                        }, onError = {
//                            Log.d("@@@", "@@@@@ error $it")
//                        }
//                ))
            }

        val subjectDisposable = selectedBehaviorSubject.filter{ it != null }.subscribe {
            //Toast.makeText(SchoolFoodApplication.context, it.name, Toast.LENGTH_SHORT).show()
            showMaterialDialog(it)
        }

        disposeBag.addAll(searchDisposable, subjectDisposable, backDisposable, textChanged)
    }

    fun showMaterialDialog(data: Store) {
        MaterialDialog(this).show {
            positiveButton(text = "확인") {
                //Toast.makeText(CommonApplication.context, "${data.name}가 추가되었습니다.", Toast.LENGTH_SHORT).show()

//                PreferenceManager.addSchoolData(data)

                //SchoolFoodPreference.addSchoolData(data)

//                PreferenceManager.schoolCode = data.schoolCode
//                PreferenceManager.officeCode = data.officeCode

                val intent = Intent().apply {
                    val location = data.lat.toString() + "," + data.lng.toString()
                    putExtra("location", location)
                }
                setResult(RESULT_OK, intent)
                (windowContext as SearchStoreActivity).finish()
            }
            negativeButton(text = "취소") {
                //Toast.makeText(SchoolFoodApplication.context, "negativeButton", Toast.LENGTH_SHORT).show()
            }
            onShow {
                title(text = "알림")
                message(text = "지도에서 ${data.name}의 위치를 확인 할까요?")
            }
        }
    }

    fun updateUI(list: List<Store>) {
        storeList.clear()
        storeList.addAll(list)
        storeAdapter.notifyDataSetChanged()

//        val view = this.currentFocus
//        view?.let { v ->
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//            imm?.hideSoftInputFromWindow(v.windowToken, 0)
//        }

    }
}
