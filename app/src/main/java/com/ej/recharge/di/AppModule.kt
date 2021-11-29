package com.ej.recharge.di

import com.ej.recharge.presenter.EditFragmentPresenter
import com.ej.recharge.presenter.HomeFragmentPresenter
import com.ej.recharge.presenter.impl.EditFragmentPresenterImpl
import com.ej.recharge.presenter.impl.HomeFragmentPresenterImpl
import dagger.Binds
import dagger.Module

@Module
abstract class AppModule {

  @Binds
  abstract fun provideScanFragmentPresenter(usersAndFriendsRepository: HomeFragmentPresenterImpl): HomeFragmentPresenter

  @Binds
  abstract fun provideEditFragmentPresenter(usersAndFriendsRepository: EditFragmentPresenterImpl): EditFragmentPresenter

}



