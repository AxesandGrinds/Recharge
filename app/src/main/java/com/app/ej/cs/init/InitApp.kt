package com.app.ej.cs.init

import android.app.Application
import android.content.Context
import com.app.ej.cs.repository.impl.MemoryUserAndFriendsRepository
import com.app.ej.cs.di.AppComponent
import com.app.ej.cs.di.DaggerAppComponent
import com.facebook.ads.AudienceNetworkAds

class InitApp : Application() {
  
  lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()

    // Initialize the Audience Network SDK
    AudienceNetworkAds.initialize(this);

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
