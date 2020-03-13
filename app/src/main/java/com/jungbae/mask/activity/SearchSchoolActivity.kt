package com.jungbae.mask.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.jakewharton.rxbinding3.view.clicks
//import com.jungbae.mask.FirebaseService
import com.jungbae.mask.R
import com.jungbae.mask.CommonApplication
//import com.jungbae.mask.SchoolFoodPageView
import com.jungbae.mask.network.*
import com.jungbae.mask.network.preference.PreferenceManager
import com.jungbae.mask.view.SearchRecyclerAdapter
import com.jungbae.mask.view.increaseTouchArea
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_search.*
import java.util.concurrent.TimeUnit

class SearchSchoolActivity : AppCompatActivity() {
    private val disposeBag = CompositeDisposable()

    private lateinit var schoolList: MutableList<SimpleSchoolData>
    private lateinit var schoolAdapter: SearchRecyclerAdapter

    private lateinit var selectedBehaviorSubject: PublishSubject<SimpleSchoolData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        //FirebaseService.getInstance().logEvent(SchoolFoodPageView.SEARCH)

        initializeUI()
        bindRxUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.clear()
    }

    init {
        selectedBehaviorSubject = PublishSubject.create()
        schoolList = ArrayList()
        schoolAdapter = SearchRecyclerAdapter(schoolList, selectedBehaviorSubject)
    }

    fun initializeUI() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = schoolAdapter
        }
        increaseTouchArea(search, 50)
        //edit_text.setText("신중")
        edit_text.requestFocus()
    }

    fun bindRxUI() {
        val backDisposable = back.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                finish()
            }

        val searchDisposable = search.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .filter{ edit_text.length() > 0 }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
/*
                NetworkService.getInstance().getSchoolData("json", 1, 100, edit_text.text.toString(), "05b9d532ceeb48dd89238746bd9b0e16")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(ObservableResponse<SchoolData>(
                        onSuccess = {
                            val list = it.schoolInfo.get(1).row.map{ data ->
                                SimpleSchoolData(data.schoolName, data.roadNameAddress, data.schoolCode, data.eduOfficecode)
                            }

                            list?.let {
                                updateUI(it)
                            }

                            Log.d("@@@@@", "onSuccess ${it.reflectionToString()}")
                            Log.d("@@@@@", "onSuccess list ${list}")
                        }, onError = {
                            Log.d("@@@", "@@@@@ error $it")
                        }
                ))
*/

            }

        val subjectDisposable = selectedBehaviorSubject.filter{ it != null }.subscribe {
            //Toast.makeText(SchoolFoodApplication.context, it.name, Toast.LENGTH_SHORT).show()
            showMaterialDialog(it)
        }

        disposeBag.addAll(searchDisposable, subjectDisposable, backDisposable)
    }

    fun showMaterialDialog(data: SimpleSchoolData) {
        MaterialDialog(this).show {
            positiveButton(text = "확인") {
                Toast.makeText(CommonApplication.context, "${data.name}가 추가되었습니다.", Toast.LENGTH_SHORT).show()

                PreferenceManager.addSchoolData(data)
                //FirebaseService.getInstance().createSchoolData(data)

                //SchoolFoodPreference.addSchoolData(data)

//                PreferenceManager.schoolCode = data.schoolCode
//                PreferenceManager.officeCode = data.officeCode

                setResult(RESULT_OK, Intent())
                (windowContext as SearchSchoolActivity).finish()
            }
            negativeButton(text = "취소") {
                //Toast.makeText(SchoolFoodApplication.context, "negativeButton", Toast.LENGTH_SHORT).show()
            }
            onShow {
                title(text = "알림")
                message(text = "${data.name}를 홈카드에 추가 할까요?")
            }

        }
    }

    fun updateUI(list: List<SimpleSchoolData>) {
        schoolList.clear()
        schoolList.addAll(list)
        schoolAdapter.notifyDataSetChanged()

        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }

    }
}
