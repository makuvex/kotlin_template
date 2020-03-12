package com.jungbae.mask.activity

import android.Manifest
import android.Manifest.*
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import com.jungbae.mask.*
import com.jungbae.mask.BuildConfig
import com.jungbae.mask.R
import com.jungbae.mask.network.*
import com.jungbae.mask.network.preference.PreferenceManager
import com.jungbae.mask.network.preference.PreferencesConstant
import com.jungbae.mask.view.HomeRecyclerAdapter
import com.jungbae.mask.view.increaseTouchArea
import com.naver.maps.map.NaverMapSdk
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.search
import kotlinx.android.synthetic.main.activity_search.recycler_view
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.naver_mapView
import kotlinx.android.synthetic.main.home_card_row.view.*
import kotlinx.android.synthetic.main.progress_bar.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.*
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons

enum class SchoolDataIndex(val index: Int) {
    HEAD(0),
    ROW(1),
    RESULT_CODE(1)
}


enum class ActivityResultIndex(val index: Int) {
    SEARCH(0),
    OPTION(1)
}

class MainActivity : AppCompatActivity() {
    private val disposeBag = CompositeDisposable()

    private lateinit var storesList: ArrayList<Store>
    private lateinit var cardAdapter: HomeRecyclerAdapter
    private lateinit var selectedSubject: PublishSubject<Store>
    private lateinit var deleteSubject: PublishSubject<Store>
    private lateinit var backPressedSubject: BehaviorSubject<Long>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    val REQUEST_ACCESS_FINE_LOCATION = 1000

    var store: Store? = null
    var countDownTimer: CountDownTimer? = null

    var lastLat: Double = .0
    var lastLng: Double = .0

    private lateinit var mMap: NaverMap
    private lateinit var mapView: MapView
    private lateinit var options: NaverMapOptions
    private var mapList: ArrayList<Marker> = ArrayList()

    // 37.482461, 126.886809

    private lateinit var emitBlockSubject: PublishSubject<Store>

    private lateinit var interstitialAd: InterstitialAd
    private var interstitialAdBlock: () -> Unit = {
        if(interstitialAd.isLoaded) {
            interstitialAd.show()
        } else {
            store?.let {
                createTimerFor(100)
                emitBlockSubject.onNext(it)
                //emitBlockSubject.onComplete()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("@@@","@@@ onCreate")
        setContentView(R.layout.activity_main)



        initAd()
        //FirebaseService.getInstance().logEvent(SchoolFoodPageView.MAIN)

//        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setDisplayShowTitleEnabled(false)
            //it.setDisplayHomeAsUpEnabled(false)
            //it.setHomeAsUpIndicator(R.drawable.btn_back)
        }

        initializeUI()
        bindUI()


        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initLocation()
        } else {
            requestLocationPermission()
        }

        //initMap()
        //requestApi()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            }



            //출처: https://duzi077.tistory.com/263 [개발하는 두더지]
        }






//        swipe_refresh.setOnRefreshListener {
//            requestApi()
//        }

    }

    init {
        Log.e("@@@","@@@ init")

        selectedSubject = PublishSubject.create()
        deleteSubject = PublishSubject.create()
        backPressedSubject = BehaviorSubject.createDefault(0L)
        emitBlockSubject = PublishSubject.create()

        storesList = ArrayList()
        cardAdapter = HomeRecyclerAdapter(storesList, selectedSubject, deleteSubject)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.clear()
        stopTimer()
    }

    override fun onPause() {
        super.onPause()
        //fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBackPressed() {
        backPressedSubject.onNext(System.currentTimeMillis())
    }

    fun initMap(lat: Double, lot: Double) {
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("syr5kxfrr1")

        mapView = naver_mapView
        mapView.getMapAsync { naverMap ->
            naverMap.moveCamera(CameraUpdate.toCameraPosition(CameraPosition(LatLng(lat, lot), 12.0)))
            mMap = naverMap

            mMap.setOnMapClickListener { point, coord ->
                //Toast.makeText(this, "${coord.latitude}, ${coord.longitude}", Toast.LENGTH_SHORT).show()
                Log.e("@@@","point ${point}")
                Log.e("@@@","coord ${coord}")
                Log.e("@@@","storesList.size ${storesList.size}")
                val currentLoc = Location("current").apply {
                    latitude = coord.latitude
                    longitude = coord.longitude
                }


                var dest: Store = storesList.first()
                dest.distance = 1000.0f
                for(store in storesList) {
                    val distance = currentLoc.distanceTo(Location("destination").apply {
                        latitude = store.convertedLat
                        longitude = store.convertedLng
                    })

                    Log.e("@@@", "@@@ distance ${distance}")
                    Log.e("@@@", "@@@ store ${store}")

                    if(dest.distance > distance) {
                        dest = store
                        dest.distance = distance
                    }
                }

                showMaterialDialog(dest)


//                val store = storesList.filter {
//                    val est = currentLoc.distanceTo(Location("destination").apply {
//                        latitude = it.convertedLat
//                        longitude = it.convertedLng
//                    })
//                    Log.e("@@@","@@@ distance ${distance}")
//
//                    if(distance > est) {
//                        distance = est
//                    }
//                    distance > est
//
//                    //coord.latitude.toString().contains(it.convertedLat.toString()) && coord.longitude.toString().contains(it.convertedLng.toString())
//                }
//                store?.let {
//                    Log.e("@@@","@@@ ${it}")
//                   // showMaterialDialog(it)
//                } ?: Toast.makeText(this, "좌표 정보 불일", Toast.LENGTH_SHORT).show()


            }

            mMap.addOnCameraIdleListener {

            }

            mMap.addOnCameraChangeListener { reason, animated ->
                // reason : reason camera changing
                // animated : is animated when camera changed
            }
            stopTimer()
        }

    }



    private fun initLocation() {
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        createTimerFor(100)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if(location == null) {
                    Log.e("@@@", "location get fail")
                } else {
                    Log.e("@@@", "init location get success ${location.latitude} , ${location.longitude} @@@")
//                    stopTimer()


                    initMap(location.latitude, location.longitude)
                    requestApi(location.latitude, location.longitude)
                }
            }
            .addOnFailureListener {
                Log.e("@@@", "location error is ${it.message}")
                it.printStackTrace()
            }
        locationRequest = LocationRequest.create()
    }

    fun requestLocation() {

        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0//60 * 1000
        }

        createTimerFor(100)
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for((i, location) in it.locations.withIndex()) {
                        Log.e("@@@", "@@@ 현재 요청 위치 #$i ${location.latitude} , ${location.longitude} @@@")
                        fusedLocationClient.removeLocationUpdates(locationCallback)

                        initMap(location.latitude, location.longitude)
                        requestApi(location.latitude, location.longitude)
                        //stopTimer()
                    }
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun requestLocationPermission() {


        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // 허용되지 않았다면 다시 확인.
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
// 권한이 승인 됐다면
                    initLocation()
                } else {
// 권한이 거부 됐다면

                }
                return
            }
        }

    }

    fun addLocationListener() {
        //fusedLocationClient?.let { it.requestLocationUpdates(locationRequest, locationCallback, null)}
        //위치 권한을 요청해야 함.
        // 액티비티가 잠깐 쉴 때,
        // 자신의 위치를 확인하고, 갱신된 정보를 요청
    }


    fun initAd() {
        MobileAds.initialize(this, getString(com.jungbae.mask.R.string.admob_app_id))
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = BuildConfig.ad_full_banner_id //resources.getString(R.string.full_ad_unit_id_for_test)
        interstitialAd.adListener = object: AdListener() {
            override fun onAdOpened() {
                Log.e("@@@","interstitialAd onAdOpened")
                //FirebaseService.getInstance().logEvent(SchoolFoodPageView.FULL_AD)
            }
            override fun onAdLoaded() { Log.e("@@@","interstitialAd onAdLoaded") }
            override fun onAdFailedToLoad(errorCode : Int) { Log.e("@@@","interstitialAd onAdFailedToLoad code ${errorCode}") }
            override fun onAdClosed() {
                Log.e("@@@","onAdClosed")
                interstitialAd.loadAd(AdRequest.Builder().build())
                store?.let {
                    createTimerFor(100)
                    emitBlockSubject.onNext(it)
                    //emitBlockSubject.onComplete()
                }
            }
        }
        interstitialAd.loadAd(AdRequest.Builder().build())

        adView.loadAd(AdRequest.Builder().build())
        adView.adListener = object: AdListener() {
            override fun onAdLoaded() { Log.e("@@@","banner onAdLoaded") }
            override fun onAdFailedToLoad(errorCode : Int) { Log.e("@@@","banner onAdFailedToLoad code ${errorCode}") }
            override fun onAdOpened() {
                Log.e("@@@","onAdOpened")
                //FirebaseService.getInstance().logEvent(SchoolFoodPageView.BANNER)
            }
            override fun onAdLeftApplication() { Log.e("@@@","onAdLeftApplication") }
            override fun onAdClosed() { Log.e("@@@","onAdClosed") }
        }
    }

    fun initializeUI() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = cardAdapter
        }
        increaseTouchArea(search, 50)
        increaseTouchArea(option, 50)
        option.isSelected = false


    }

    fun bindUI() {

        val backDisposable =
            backPressedSubject
                .buffer(2, 1)
                .map{ Pair(it[0], it[1]) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.second - it.first < TimeUnit.SECONDS.toMillis(2)) {
                        finish()
                    } else {
                        Toast.makeText(this, "뒤로 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                    }
                }

        val searchDisposable = search.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //startActivity(ActivityResultIndex.SEARCH.index)
                showMaterialInputDialog()
                Log.d("@@@", "searchDisposable")
            }

        val optionDisposable = option.clicks()
            .throttleFirst(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext {
                if(option.isSelected) search.visibility = View.INVISIBLE else search.visibility = View.VISIBLE
            }
            .subscribe {
                option.isSelected = !option.isSelected
                val resId: Int = when(option.isSelected) {
                    true -> R.drawable.cancel
                    false -> R.drawable.trash
                }

                option.setBackgroundResource(resId)
                cardAdapter.notifyDataSetChangedWith(option.isSelected)
                Log.d("@@@", "optionDisposable")
            }


        val itemClicksDisposable = selectedSubject
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { meal ->
                Log.e("@@@", "item clicks ${meal}")
                store = meal
                interstitialAdBlock()
            }

        val itemDeleteDisposable = deleteSubject
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.e("@@@", "item delete ${it}")
                showMaterialDialog(it)
            }

        val fullAdCloseDisposable = emitBlockSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { meal ->
                stopTimer()
//                val intent = Intent(this, SchoolFoodDetailActivity::class.java)
//                intent.putExtra(PreferencesConstant.SCHOOL_CODE, meal.schoolCode)
//                intent.putExtra(PreferencesConstant.OFFICE_SC_CODE, meal.officeCode)
//                intent.putExtra(PreferencesConstant.SCHOOL_NAME, meal.name)
//
//                simpleSchoolMealData = null
//                startActivity(intent)
            }

        disposeBag.addAll(
            backDisposable,
            searchDisposable,
            optionDisposable,
            itemClicksDisposable,
            itemDeleteDisposable,
            fullAdCloseDisposable)
    }

    private fun combineBlock(block: () -> Unit, ob2: Observable<SimpleSchoolMealData>): Observable<SimpleSchoolMealData> {
        block()
        return ob2
    }

    private fun showInterstitialAd(block: (() -> Unit)?): Unit {
        if(interstitialAd.isLoaded) {
            interstitialAd.show()
        }
//        block?.let{
//            commonBlock = it
//        }
    }

    fun startActivity(index: Int) {

        when(index) {
            ActivityResultIndex.SEARCH.index ->
                startActivityForResult(Intent(this, SearchSchoolActivity::class.java), ActivityResultIndex.SEARCH.index)
            ActivityResultIndex.OPTION.index ->
                startActivity(Intent(this, SearchSchoolActivity::class.java))
        }
    }

    fun reloadData() {
        Log.d("@@@","@@@ reloadData @@@")

        requestApi(lastLat, lastLng)

        storesList.clear()
        cardAdapter.notifyDataSetChangedWith(option.isSelected)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("@@@","@@@ onActivityResult ${resultCode} @@@")
        if (requestCode == ActivityResultIndex.SEARCH.index) {
            when (resultCode) {
                Activity.RESULT_OK -> reloadData()
            }
        }
    }

    fun requestApiByAddr(addr: String) {
        Log.e("", "@@@ requestApiByAddr")


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

                    list?.first()?.let {
                        requestApi(it.lat.toDouble(), it.lng.toDouble())
                    }
                }, onError = {
                    Log.e("@@@@@", "@@@@@ error $it")
                    //swipe_refresh.isRefreshing = false
                    stopTimer()
                    Toast.makeText(SchoolFoodApplication.context, "서버와의 통신이 원할하지 않습니다.", Toast.LENGTH_LONG).show()
                }
            ))
        disposeBag.add(task)
    }

    fun requestApi(lat: Double, lng: Double) {
        Log.e("", "@@@ requestApi lat: ${lat}, lng: ${lng}")
        storesList.clear()
        mapList.map{ it.map = null }
        mapList.clear()

        lastLat = lat
        lastLng = lng


        val task = NetworkService.getInstance().getStoresByGeo(lat, lng, 1000)
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

                    list.map {
                        it.convertedLat = it.lat.toDouble()
                        it.convertedLng = it.lng.toDouble()

                        Log.e("@@@","@@@ convert ${it}")
                        storesList.add(it)
                    }


                    for(store in list) {
                        val marker = Marker()

                        marker.position = LatLng(store.lat.toDouble(), store.lng.toDouble())
                        Log.e("@@@","@@@ position ${marker.position}")
                        marker.map = mMap
                        marker.width = Marker.SIZE_AUTO
                        marker.height = Marker.SIZE_AUTO

                        marker.captionColor = Color.rgb(42, 77, 133)
                        marker.captionHaloColor = Color.WHITE
                        when {
                            store.remain_stat == "plenty" -> {
                                marker.icon = MarkerIcons.GREEN
                                marker.captionText = "100개이상"
                            }
                            store.remain_stat == "some" -> {
                                marker.icon = MarkerIcons.YELLOW
                                marker.captionText = "30~100개"
                            }
                            store.remain_stat == "few" -> {
                                marker.icon = MarkerIcons.RED
                                marker.captionText = "2~30개"
                            }
                            store.remain_stat == "empty" -> {
                                marker.icon = MarkerIcons.GRAY
                                marker.captionText = "1개 이하"
                            }
                            else -> {
                                marker.icon = MarkerIcons.GRAY
                                marker.captionText = "없음"
                            }
                        }

                        mapList.add(marker)
                    }

                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(lastLat, lastLng)).animate(CameraAnimation.Easing)
                    mMap.moveCamera(cameraUpdate)

                    //mMap.moveCamera(CameraUpdate.zoomTo(14.0))
                    //swipe_refresh.isRefreshing = false
                    Log.e("@@@@@", "onSuccess ${it.reflectionToString()}")
                    stopTimer()
                    //Log.d("@@@@@", "onSuccess list ${list}")
                }, onError = {
                    Log.e("@@@@@", "@@@@@ error $it")
                    //swipe_refresh.isRefreshing = false
                    stopTimer()
                    Toast.makeText(SchoolFoodApplication.context, "서버와의 통신이 원할하지 않습니다.", Toast.LENGTH_LONG).show()
                }
            ))
        disposeBag.add(task)
    }

    fun remainStat(stat: String?): String {
        when(stat) {
            "plenty" -> {
                return "100개이상"
            }
            "some" -> {
                return "30~100개"
            }
            "few" -> {
                return "2~30개"
            }
            "empty" -> {
                return "1개 이하"
            }
            else -> {
                return "없음"
            }
        }
    }

    fun addCard(data: Store) {
        AndroidSchedulers.mainThread().scheduleDirect {
            storesList.add(data)
            cardAdapter.notifyDataSetChangedWith(option.isSelected)
        }
    }

    fun updateCard(data: Store) {
        AndroidSchedulers.mainThread().scheduleDirect {
            val index = storesList.indexOfFirst{ it.name == data.name }
            storesList.set(index, data)
            cardAdapter.notifyDataSetChangedWith(option.isSelected)
        }
    }

    fun updateUI(list: List<Store>?) {
        AndroidSchedulers.mainThread().scheduleDirect {
            list?.let {
                if (it.isNotEmpty()) {
                    home_no_data_view.visibility = View.GONE
                    //schoolMealList.clear()
                    storesList.addAll(it)
                    cardAdapter.notifyDataSetChangedWith(option.isSelected)
                } else {
                    home_no_data_view.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                }
                return@scheduleDirect
            }
            home_no_data_view.visibility = View.VISIBLE
            recycler_view.visibility = View.GONE
        }
    }

    fun reloadRecylerView() {
        cardAdapter.notifyDataSetChangedWith(option.isSelected)
    }

    fun showMaterialDialog(data: Store) {
        MaterialDialog(this).show {
            positiveButton(text = "확인") {
                //Toast.makeText(SchoolFoodApplication.context, "${data.name} 홈카드가 삭제 되었습니다.", Toast.LENGTH_SHORT).show()

//                if(storesList.removeIf { school ->  school.schoolCode == data.schoolCode && school.officeCode == data.officeCode }) {
//                    PreferenceManager.removeSchoolData(data.officeCode, data.schoolCode)
//                    reloadRecylerView()
//                }
//                if(storesList.size == 0) {
//                    //option.callOnClick()
//                }
                //PreferenceManager.addSchoolData(data)
            }
            onShow {
                title(text = data.name )
                val msg = "수량 : " + remainStat(data.remain_stat) + "\n\n주소 : " + data.addr + "\n\n입고시간 : " + data.stock_at
                message(text = msg)
            }

        }
    }

    fun showMaterialInputDialog() {
        MaterialDialog(this).show {
            input(hint = "주소를 입력해주세요!", maxLength = 100, allowEmpty = false){ dialog, text ->
                Log.e("@@@","@@@ input text ${text.toString()}")
                requestApiByAddr(text.toString())

            }
            positiveButton(text = "확인")
            negativeButton(text = "취소")
            onShow {
                title(text = "주소로 검색" )
                message(text = "예) '서울특별시 강남구' or '서울특별시 강남구 논현동'\n('서울특별시' 와 같이 '시'단위만 입력하는 것은 불가능합니다.)")
            }
        }

    }

    fun createTimerFor(millis: Long) {
        val max = 10000L
        wrap_progress_bar.visibility = View.VISIBLE
        progress_bar.progress = 0
        countDownTimer = object : CountDownTimer(max, millis) {
            override fun onTick(p0: Long) {
                val f: Float = (max  - p0)/max.toFloat()
                val p = (f * 100).toLong()
                //Log.e("@@@","@@@ p0 ${p0}, p ${p}, ${p.toInt()}")

                progress_bar.progress = p.toInt()
            }

            override fun onFinish() {
                createTimerFor(100)
            }
        }
        countDownTimer?.start()
    }

    fun stopTimer() {
        wrap_progress_bar.visibility = View.GONE
        countDownTimer?.cancel()
    }
}
