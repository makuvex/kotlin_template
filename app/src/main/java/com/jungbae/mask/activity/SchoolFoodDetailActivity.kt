package com.jungbae.mask.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.jakewharton.rxbinding3.view.clicks
//import com.jungbae.mask.FirebaseService
import com.jungbae.mask.R
//import com.jungbae.mask.SchoolFoodPageView
import com.jungbae.mask.network.*
import com.jungbae.mask.network.preference.PreferencesConstant
import com.jungbae.mask.view.MealDetailRecyclerAdapter
import com.jungbae.mask.view.increaseTouchArea
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_meal_detail.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class SchoolFoodDetailActivity : AppCompatActivity() {
    private val disposeBag = CompositeDisposable()

    private lateinit var mealList: ArrayList<SimpleSchoolMealData>
    private lateinit var mealAdapter: MealDetailRecyclerAdapter

    var schoolCode: String? = null
    var officeCode: String? = null
    var schoolName: String? = null

    private lateinit var selectedBehaviorSubject: PublishSubject<SimpleSchoolMealData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_detail)

        intent?.let {
            officeCode = it.getStringExtra(PreferencesConstant.OFFICE_SC_CODE)
            schoolCode = it.getStringExtra(PreferencesConstant.SCHOOL_CODE)
            schoolName = it.getStringExtra(PreferencesConstant.SCHOOL_NAME)
        }
        //FirebaseService.getInstance().logEvent(SchoolFoodPageView.DETAIL)
        initializeUI()
        bindRxUI()

        requestMealInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.clear()
    }

    init {
        selectedBehaviorSubject = PublishSubject.create()
        mealList = ArrayList()
        mealAdapter = MealDetailRecyclerAdapter(mealList, selectedBehaviorSubject)
    }

    fun initializeUI() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = mealAdapter
        }
        increaseTouchArea(back, 50)
        detail_title.text = schoolName
    }

    fun bindRxUI() {
        val backDisposable = back.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                finish()
            }
//
//        val subjectDisposable = selectedBehaviorSubject.filter{ it != null }.subscribe {
//            //Toast.makeText(SchoolFoodApplication.context, it.name, Toast.LENGTH_SHORT).show()
//            showMaterialDialog(it)
//        }
//
        disposeBag.addAll(backDisposable)
    }

    fun showMaterialDialog() {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                positiveButton(text = "확인") {
                    (windowContext as SchoolFoodDetailActivity).finish()
                }
                onShow {
                    title(text = "알림")
                    message(text = "급식 정보가 없습니다.")
                }
            }
        }
    }

    fun requestMealInfo() {
        val scCode = schoolCode?.let {it} ?: return
        val ofCode = officeCode?.let {it} ?: return


//        val current = LocalDateTime.now()
//        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
//        val today = current.format(formatter)
//        val todayDesc = current.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))

        val current = System.currentTimeMillis()
        val date = Date(current)
        var sdfNow = SimpleDateFormat("yyyyMMdd")
        val today = sdfNow.format(current)

        Log.d("", "@@@ requestMealInfo")
        mealList.clear()

        val cal = Calendar.getInstance()
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE))
        val lastDayOfMonth = cal.getTime()

        cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE))
        val firstDayOfMonth = cal.getTime()

        val sif = SimpleDateFormat("yyyyMMdd")

        Log.e("@@@","start ${sif.format(firstDayOfMonth)}, last ${sif.format(lastDayOfMonth)}")

        val dispose =
            NetworkService.getInstance().getSchoolMealData("json", 1, 100, ofCode, scCode, "05b9d532ceeb48dd89238746bd9b0e16", sif.format(firstDayOfMonth), sif.format(lastDayOfMonth))
            .subscribeWith(ObservableResponse<SchoolMealData>(
                onSuccess = {
                    val data = it.mealServiceDietInfo.get(1).row.map { data ->
                        SimpleSchoolMealData(data.schoolName, data.mealTime, data.dishName, data.mealName, data.calInfo, data.schoolCode, data.eduOfficecode)
                    }

                    data?.let {
                        addCard(it)
                        val index = it.indexOfFirst { data ->
                            data.date == today
                        }

                        if(index > 0) {
                            recycler_view.postDelayed(Runnable {
                                recycler_view.scrollToPosition(index)
                            }, 100)
                        }
                        //updateUI(it)
                        //updateCard(it)
                    }

                    Log.e("@@@@@", "onSuccess ${it.reflectionToString()}")
                    //Log.d("@@@@@", "onSuccess list ${list}")
                }, onError = {
                    Log.e("@@@@@", "@@@@@ error $it")
                    showMaterialDialog()
                }
            ))

        disposeBag.add(dispose)


    }

    fun addCard(list: List<SimpleSchoolMealData>) {
        AndroidSchedulers.mainThread().scheduleDirect {
            mealList.addAll(list)

            mealAdapter.notifyDataSetChanged()
        }
    }

    fun updateCard(data: SimpleSchoolMealData) {
        AndroidSchedulers.mainThread().scheduleDirect {
            mealList.add(data)
            mealAdapter.notifyDataSetChanged()
//            val index = mealList.indexOfFirst{ it.name == data.name }
//            schoolMealList.set(index, data)
//            cardAdapter.notifyDataSetChanged()
        }
    }

}
