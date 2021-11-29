package com.ej.recharge.init

import android.app.Application
import android.content.Context
import com.ej.recharge.repository.impl.MemoryUserAndFriendsRepository
import com.ej.recharge.di.AppComponent
import com.ej.recharge.di.DaggerAppComponent
//import com.facebook.ads.AudienceNetworkAds

class InitApp : Application() {
  
  lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()

    // Initialize the Audience Network SDK
//    AudienceNetworkAds.initialize(this);

    InitApp.appContext = applicationContext

    appComponent = DaggerAppComponent
      .factory()
      .repository(MemoryUserAndFriendsRepository(appContext))

  }

  companion object {

    lateinit  var appContext: Context

  }

  fun appComp() = appComponent
}
