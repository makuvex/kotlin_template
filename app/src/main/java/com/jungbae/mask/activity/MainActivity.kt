package com.jungbae.mask.activity

import android.Manifest
import android.Manifest.*
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import com.jungbae.mask.*
import com.jungbae.mask.BuildConfig
import com.jungbae.mask.R
import com.jungbae.mask.network.*
import com.jungbae.mask.network.preference.PreferenceManager
import com.jungbae.mask.view.increaseTouchArea
import com.jungbae.mask.activity.SearchStoreActivity
import com.naver.maps.map.NaverMapSdk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.naver_mapView
import kotlinx.android.synthetic.main.progress_bar.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.*
import com.naver.maps.map.CameraUpdate.REASON_DEVELOPER
import com.naver.maps.map.CameraUpdate.REASON_GESTURE
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.lang.Exception
import java.net.URL


enum class ActivityResultIndex(val index: Int) {
    SEARCH(0),
    OPTION(1)
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val disposeBag = CompositeDisposable()

    private lateinit var storesList: ArrayList<Store>
    private lateinit var selectedSubject: PublishSubject<Store>
    private lateinit var deleteSubject: PublishSubject<Store>
    private lateinit var backPressedSubject: BehaviorSubject<Long>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var favoriteStores: ArrayList<Store>

    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    val REQUEST_ACCESS_FINE_LOCATION = 1000

    var store: Store? = null
    var countDownTimer: CountDownTimer? = null

    var lastLat: Double = .0
    var lastLng: Double = .0
    var adRewardCount = 0

    private lateinit var myPos: LatLng
    private var myMarker: Marker? = null

    var cameraChangedReason: Int = REASON_DEVELOPER
    private lateinit var mMap: NaverMap
    private lateinit var mapView: MapView
    private lateinit var options: NaverMapOptions
    private var mapList: ArrayList<Marker> = ArrayList()

    // 37.482461, 126.886809

    private lateinit var emitBlockSubject: PublishSubject<Store>

    private lateinit var mRewardedVideoAd: RewardedAd
    private lateinit var rewardedAdLoadCallback: RewardedAdLoadCallback

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

    var t = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("@@@","@@@ onCreate")
        setContentView(R.layout.activity_main)
//
//        intent.extras?.getString("link")?.let {
//            val array = it.split(",")
//            Log.e("@@@","@@@ array ${array}")
//            lastLat = array[0].toDouble()
//            lastLng = array[1].toDouble()
//
//            Toast.makeText(CommonApplication.context, "인텐트 받음 ", Toast.LENGTH_LONG).show()
//        } ?: Toast.makeText(CommonApplication.context, "인텐트 없음 ", Toast.LENGTH_LONG).show()

        initAd()

        supportActionBar?.let {
            it.setDisplayShowCustomEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }

        initializeUI()
        bindUI()
        requestFavoriteStore()

        current_location.setOnClickListener { view ->
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                createTimerFor(100)
                storesList.clear()
                mapList.map{ it.map = null }
                mapList.clear()
                requestLocation()
            }
        }

        refresh_location.setOnClickListener { view ->
            if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                createTimerFor(100)
                storesList.clear()
                mapList.map{ it.map = null }
                mapList.clear()
                requestApi(lastLat, lastLng, false)
            }
        }
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initLocation()
        } else {
            requestLocationPermission()
        }
    }

    init {
        Log.e("@@@","@@@ init")

        selectedSubject = PublishSubject.create()
        deleteSubject = PublishSubject.create()
        backPressedSubject = BehaviorSubject.createDefault(0L)
        emitBlockSubject = PublishSubject.create()

        favoriteStores = ArrayList()
        storesList = ArrayList()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("@@@","@@@ onNewIntent ${intent?.getStringExtra("link")}")
        try {
            intent?.getStringExtra("link")?.let { link ->
                val array = link.split(",")
                val lat = array[0] as String
                val lng = array[1] as String

                requestApi(lat.toDouble(), lng.toDouble())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.clear()
        stopTimer()
    }

    override fun onBackPressed() {
        backPressedSubject.onNext(System.currentTimeMillis())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("@@@","@@@ onActivityResult ${resultCode} @@@")
        if (requestCode == ActivityResultIndex.SEARCH.index) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val location = data?.getStringExtra("location")
                    Log.e("@@@", "@@@ location ${location}")
                    location?.let {
                        try {
                            val lat = it.split(",")[0].toDouble()
                            val lng = it.split(",")[1].toDouble()
                            requestApi(lat, lng)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    fun requestFavoriteStore(completion: (() -> Unit)? = null) {
        PreferenceManager.userSeq?.let {
            favoriteStores.clear()
            val task = NetworkService.getInstance().keyword(it.toString())
                .throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(ObservableResponse<StoresByKeyword>(
                    onSuccess = {
                        favoriteStores.addAll(it.result)

                        completion?.let{
                            it()
                        }
                        Log.e("@@@@@", "onSuccess keyword list ${it}")
                    }, onError = {
                        Log.e("@@@@@", "@@@@@ error keyword list $it")
                        //swipe_refresh.isRefreshing = false
                        Toast.makeText(CommonApplication.context, "서버와의 통신이 원할하지 않습니다.", Toast.LENGTH_LONG).show()
                    }
                ))
            disposeBag.add(task)

        }
    }
    fun initMap(lat: Double, lot: Double, completion: (() -> Unit)? = null) {
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("syr5kxfrr1")

        mapView = naver_mapView
        mapView.getMapAsync { naverMap ->
            naverMap.moveCamera(CameraUpdate.toCameraPosition(CameraPosition(LatLng(lat, lot), 14.0)))
            mMap = naverMap

            mMap.addOnCameraIdleListener {
                Log.e("@@@","@@@ addOnCameraIdleListener ${mMap.cameraPosition.target}")

                if(cameraChangedReason == REASON_GESTURE) {
                    createTimerFor(100)
                    requestFavoriteStore {
                        requestApi(mMap.cameraPosition.target.latitude, mMap.cameraPosition.target.longitude)
                    }
                } else {
                }
            }

            mMap.addOnCameraChangeListener { reason, animated ->
                cameraChangedReason = reason
            }

            mMap.setOnMapClickListener { point, coord ->
                //Toast.makeText(this, "${coord.latitude}, ${coord.longitude}", Toast.LENGTH_SHORT).show()
//                Log.e("@@@","point ${point}")
//                Log.e("@@@","coord ${coord}")
//                Log.e("@@@","storesList.size ${storesList.size}")
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

//                    Log.e("@@@", "@@@ distance ${distance}")
//                    Log.e("@@@", "@@@ store ${store}")

                    if(dest.distance > distance) {
                        dest = store
                        dest.distance = distance
                    }
                }
                Log.e("@@@", "@@@ distance ${dest.distance}")
                if(dest.distance < 100) {
                    showMaterialDialog(dest)
                }
            }

            stopTimer()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.privacy -> {
                //Toast.makeText(this@MainActivity, "준비중 입니다.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, PrivacyActivity::class.java)?.apply {
                    putExtra("url", "http://makuvex7.cafe24.com/mask_privacy")
                })
            }
            R.id.license -> {
                startActivity(Intent(this, LicenseActivity::class.java)?.apply {
                    putExtra("url", "http://makuvex7.cafe24.com/nemodeal_aos_license")
                })
            }
            R.id.version -> { showSingleDialog("버전 정보", "현재 버전 : " + "${getVersionInfo()}") }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }

    fun getVersionInfo(): String {
        val info: PackageInfo = CommonApplication.context.packageManager.getPackageInfo(CommonApplication.context.packageName, 0)
        return info.versionName
    }

    fun showDialog(title: String, msg: String, completion: ((Boolean) -> Unit)? = null) {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                positiveButton(text = "확인") { _ ->
                    completion?.let{ it(true) }
                }
                negativeButton(text = "취소") { _ ->
                    completion?.let{ it(false) }
                }
                onShow {
                    title(text = title)
                    message(text = msg)
                }
            }
        }
    }

    fun showSingleDialog(title: String, msg: String, completion: ((Boolean) -> Unit)? = null) {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                positiveButton(text = "확인") { _ ->
                    completion?.let{ it(true) }
                }
                onShow {
                    title(text = title)
                    message(text = msg)
                }
            }
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

                    myPos = LatLng(location.latitude, location.longitude)


                    var latitude: Double = if(lastLat != .0) lastLat else location.latitude
                    var longitude: Double = if(lastLng != .0) lastLng else location.longitude

                    initMap(latitude, longitude)

                    requestFavoriteStore {
                        requestApi(latitude, longitude)
                    }

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
                        requestFavoriteStore {
                            requestApi(location.latitude, location.longitude)
                        }


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
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocation()
                }
                return
            }
        }
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

        loadRewardedAd()
    }

    private fun loadRewardedAd() {
        mRewardedVideoAd = RewardedAd(this, BuildConfig.ad_reward_id)
        mRewardedVideoAd.loadAd(
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    //Toast.makeText(this@MainActivity, "onRewardedAdLoaded", Toast.LENGTH_LONG).show()
                }

                override fun onRewardedAdFailedToLoad(errorCode: Int) {
                    //Toast.makeText(this@MainActivity, "onRewardedAdFailedToLoad", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    fun initializeUI() {
       // increaseTouchArea(search, 50)
        increaseTouchArea(option, 80)
        option.isSelected = false

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        drawer_layout.nav_view.itemIconTintList = null
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
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
                startActivity(ActivityResultIndex.SEARCH.index)

                //showMaterialInputDialog()
                Log.d("@@@", "searchDisposable")
            }

        val optionDisposable = option.clicks()
            .throttleFirst(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://m.blog.naver.com/PostList.nhn?permalink=permalink&blogId=kfdazzang&proxyReferer=&proxyReferer=http:%2F%2Fblog.naver.com%2Fkfdazzan")))

                //option.isSelected = !option.isSelected
//                val resId: Int = when(option.isSelected) {
//                    true -> R.drawable.cancel
//                    false -> R.drawable.trash
//                }
//
//                option.setBackgroundResource(resId)
                //cardAdapter.notifyDataSetChangedWith(option.isSelected)
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
            .subscribe { data ->
                stopTimer()
                addFavorite(data)
            }

        disposeBag.addAll(
            backDisposable,
            searchDisposable,
            optionDisposable,
            itemClicksDisposable,
            itemDeleteDisposable,
            fullAdCloseDisposable)
    }

    private fun showInterstitialAd(block: (() -> Unit)?): Unit {
        if(interstitialAd.isLoaded) {
            interstitialAd.show()
        }
    }

    fun reloadData() {
        Log.d("@@@","@@@ reloadData @@@")

        requestApi(lastLat, lastLng)

        storesList.clear()
        //cardAdapter.notifyDataSetChangedWith(option.isSelected)
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

                    if (list != null && list.size > 0) {
                        requestApi(list.first().lat.toDouble(), list.first().lng.toDouble())
                    } else {
                        Toast.makeText(CommonApplication.context, "검색 결과과 없습니다. 띄어쓰기 및 시, 구 입력을 확인해주세요.", Toast.LENGTH_LONG).show()
                    }
//                    list?.first()?.let {
//                        requestApi(it.lat.toDouble(), it.lng.toDouble())
//                    }
                }, onError = {
                    Log.e("@@@@@", "@@@@@ error $it")
                    //swipe_refresh.isRefreshing = false
                    stopTimer()
                    Toast.makeText(CommonApplication.context, "서버와의 통신이 원할하지 않습니다.", Toast.LENGTH_LONG).show()
                }
            ))
        disposeBag.add(task)
    }

    fun requestApi(lat: Double, lng: Double, move: Boolean = true) {
        Log.e("", "@@@ requestApi lat: ${lat}, lng: ${lng}")
        //storesList.clear()
//        mapList.map{ it.map = null }
//        mapList.clear()

        lastLat = lat
        lastLng = lng

        val task = NetworkService.getInstance().getStoresByGeo(lat, lng, 600)
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(ObservableResponse<StoresByData>(
                onSuccess = {
                    var list = it.stores.sortedWith(compareByDescending { data ->
                        when {
                            data.remain_stat == "plenty" -> 3
                            data.remain_stat == "some" -> 2
                            data.remain_stat == "few" -> 1
                            data.remain_stat == "empty" -> 0
                            data.remain_stat == "break" -> -1
                            else -> -2
                        }
                    })

                    list.map { it ->
                        it.convertedLat = it.lat.toDouble()
                        it.convertedLng = it.lng.toDouble()

                        //Log.e("@@@","@@@ convert ${it}")
                        if(!storesList.contains(it)) {
                            storesList.add(it)
                        }
                    }

                    myMarker?.let {
                        it.map = null
                    }

                    // 내위치
                    myMarker = Marker()
                    myMarker?.position = LatLng(myPos.latitude, myPos.longitude)
                    myMarker?.width = 100//Marker.SIZE_AUTO
                    myMarker?.height = 100//Marker.SIZE_AUTO
                    myMarker?.map = mMap
                    myMarker?.icon = OverlayImage.fromResource(R.drawable.man)
                    val cap = InfoWindow()
                    cap.adapter = object : InfoWindow.DefaultTextAdapter(CommonApplication.context) {
                        override fun getText(infoWindow: InfoWindow): CharSequence {
                            return "현위치"
                        }
                    }
                    cap.open(myMarker!!)
                    //mapList.add(myMarker)

                    for(store in list) {
                        val marker = Marker()

                        marker.position = LatLng(store.convertedLat, store.convertedLng)
                        //Log.e("@@@","@@@ position ${marker.position}")

                        marker.width = Marker.SIZE_AUTO
                        marker.height = Marker.SIZE_AUTO

                        marker.map = mMap

//                        marker.captionColor = Color.rgb(42, 77, 133)
//                        marker.captionHaloColor = Color.WHITE
                        marker.infoWindow?.close()
                        var infoText = ""
                        when {
                            store.remain_stat == "plenty" -> {
                                marker.icon = MarkerIcons.GREEN
                                infoText = "100+"
                            }
                            store.remain_stat == "some" -> {
                                marker.icon = MarkerIcons.YELLOW
                                infoText = "30+"
                            }
                            store.remain_stat == "few" -> {
                                marker.icon = MarkerIcons.RED
                                infoText = "2+"
                            }
                            store.remain_stat == "empty" -> {
                                marker.icon = MarkerIcons.GRAY
                                infoText = "품절"
                            }
                            store.remain_stat == "break" -> {
                                marker.icon = MarkerIcons.GRAY
                                infoText = "판매중지"
                            }
                            else -> {
                                marker.icon = MarkerIcons.GRAY
                                infoText = "품절"
                            }
                        }
                        val infoWindow = InfoWindow()
                        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(CommonApplication.context) {
                            override fun getText(infoWindow: InfoWindow): CharSequence {
                                return infoText
                            }
                        }
                        infoWindow.open(marker)


                        if(store.name == "대명약국") {
                            Log.e("@@@","@@@    ")
                        } else if(store.name == "진한약국") {
                            Log.e("@@@","@@@    ")
                        }

                        favoriteStores.filter{ it?.code == store?.code }?.let {
                            if(it.isNotEmpty()) {
                                Log.e("@@@", "@@@ 즐겨찾기 설정됨")
                                marker.icon = OverlayImage.fromResource(R.drawable.favorite)
                            }
                        }

                        val temp = mapList.filter { it.position != marker.position }
                        temp?.let {
                            mapList.add(marker)
                        }
                    }

                    if(move) {
                        val cameraUpdate =
                            CameraUpdate.scrollTo(LatLng(lastLat, lastLng)).animate(CameraAnimation.Easing)
                        mMap.moveCamera(cameraUpdate)
                    }
                    //mMap.moveCamera(CameraUpdate.zoomTo(14.0))
                    //swipe_refresh.isRefreshing = false
                    Log.e("@@@@@", "onSuccess ${it.reflectionToString()}")
                    stopTimer()
                    //Log.d("@@@@@", "onSuccess list ${list}")
                }, onError = {
                    Log.e("@@@@@", "@@@@@ error $it")
                    //swipe_refresh.isRefreshing = false
                    stopTimer()
                    Toast.makeText(CommonApplication.context, "서버와의 통신이 원할하지 않습니다.", Toast.LENGTH_LONG).show()
                }
            ))
        disposeBag.add(task)
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

    fun showMaterialDialog(data: Store) {
        var str: String = if(adRewardCount % 4 == 0) "광고보고[즐겨찾기] 하기" else "즐겨찾기"
        favoriteStores.filter{it.code == data.code}?.let {
            if(it.isNotEmpty()) {
                str = "즐겨찾기 해제"
            }
        }

        MaterialDialog(this).show {
            positiveButton(text = "확인") {}
            negativeButton(text = str) {

                PreferenceManager.userSeq?.let {
                    if(str == "즐겨찾기" || str == "광고보고[즐겨찾기] 하기") {

                        if(favoriteStores.size >= 10) {
                            Toast.makeText(CommonApplication.context, "즐겨찾기는 10개까지 가능합니다. 다른 스토어를 먼저 해제하고 시도하여 주세요.", Toast.LENGTH_SHORT).show()
                        } else if (mRewardedVideoAd.isLoaded) {
                            //Toast.makeText(this@MainActivity, "${adRewardCount%3}", Toast.LENGTH_LONG).show()
                            if(adRewardCount % 4 == 0) {
                                mRewardedVideoAd.show(
                                    this@MainActivity,
                                    object : RewardedAdCallback() {
                                        override fun onUserEarnedReward(rewardItem: RewardItem) {
                                            loadRewardedAd()
                                            addFavorite(data)
                                            adRewardCount += 1
                                        }

                                        override fun onRewardedAdClosed() {
                                            loadRewardedAd()
                                        }
                                    }
                                )
                            } else if(adRewardCount % 2 == 0) {
                                selectedSubject.onNext(data)
                                adRewardCount += 1
                            } else {
                                addFavorite(data)
                            }
                        } else {
                            addFavorite(data)
                        }
                    } else {
                        removeFavorite(data)
                    }
                }
            }
            onShow {
                title(text = data.name )
                val stock_at = if(data.stock_at == null) "미정" else data.stock_at
                var msg = "수량 : " + remainStat(data.remain_stat) + "\n\n주소 : " + data.addr + "\n\n입고시간 : " + stock_at
                msg += "\n\n즐겨찾기 하시면 구매 가능할 때 알림을 받을 수 있습니다."
                message(text = msg)
            }
        }
    }

    fun showMaterialInputDialog() {

        MaterialDialog(this).show {
            input(waitForPositiveButton = false,
                hint = "주소를 입력해주세요!",
                maxLength = 100,
                allowEmpty = false) { dialog, text ->
                    Log.e("@@@","@@@ input text ${text}")

                    //requestApiByAddr(text.toString())

            }
            positiveButton(text = "확인")
            negativeButton(text = "취소")
            onShow {
                title(text = "주소로 검색" )
                message(text = "예) '서울특별시 강남구' or '서울특별시 강남구 논현동'\n('서울특별시' 와 같이 '시'단위만 입력하는 것은 불가능합니다.)")
            }
        }
    }

    fun addFavorite(data: Store) {
        PreferenceManager.userSeq?.let {
            val d = NetworkService.getInstance()
                .registKeyword(data.convertedLat, data.convertedLng, it, data.code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(ObservableResponse<BaseResult>(
                    onSuccess = {
                        CommonApplication.subscribeTopic(data.code)
                        mapList.map { it.map = null }
                        mapList.clear()
                        requestFavoriteStore {
                            requestApi(lastLat, lastLng)
                            Toast.makeText(CommonApplication.context, "즐겨찾기에 ${data.name}이 추가 되었습니다.", Toast.LENGTH_LONG).show()
                        }
                    }, onError = {
                        Log.e("@@@", "@@@ error $it")
                    }
                ))
            disposeBag.add(d)
        }
    }

    fun removeFavorite(data: Store) {
        PreferenceManager.userSeq?.let {
            val t = NetworkService.getInstance()
                .deleteKeyword(data.code, it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(ObservableResponse<BaseResult>(
                    onSuccess = {
                        Log.e("@@@", "@@@ registUser onSuccess ${it.reflectionToString()}")

                        CommonApplication.unsubscribeTopic(data.code)
                        mapList.map{ it.map = null }
                        mapList.clear()
                        requestFavoriteStore {
                            requestApi(lastLat, lastLng)
                            Toast.makeText(CommonApplication.context, "즐겨찾기에 ${data.name}이 제거 되었습니다.", Toast.LENGTH_LONG).show()
                        }
                    }, onError = {
                        Log.e("@@@", "@@@ error $it")
                    }
                ))
            disposeBag.add(t)
        }

    }
    fun createTimerFor(millis: Long) {
        stopTimer()

        //drawer_layout.enableDisableViewGroup(false)
        val max = 10000L
        wrap_progress_bar.visibility = View.VISIBLE
        progress_bar.progress = 0
        countDownTimer = object : CountDownTimer(max, millis) {
            override fun onTick(p0: Long) {
                val f: Float = (max  - p0)/max.toFloat()
                val p = (f * 100).toLong()
                Log.e("@@@","@@@ p0 ${p0}, p ${p}, ${p.toInt()}")

                progress_bar.progress = p.toInt()
            }

            override fun onFinish() {
                Log.e("@@@","@@@ onFinish")
                if(countDownTimer != null) {
                    createTimerFor(100)
                }
            }
        }
        countDownTimer?.start()
    }

    fun stopTimer() {
        wrap_progress_bar.visibility = View.GONE
        countDownTimer?.cancel()
        countDownTimer = null
        ///drawer_layout.enableDisableViewGroup(true)
    }

    fun startActivity(index: Int) {
        when(index) {
            ActivityResultIndex.SEARCH.index ->
                startActivityForResult(Intent(CommonApplication.context, SearchStoreActivity::class.java),
                    ActivityResultIndex.SEARCH.index)
        }
    }

}
