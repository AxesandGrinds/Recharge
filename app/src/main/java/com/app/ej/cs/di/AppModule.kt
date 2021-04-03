package com.app.ej.cs.di

import com.app.ej.cs.presenter.EditFragmentPresenter
import com.app.ej.cs.presenter.ScanFragmentPresenter
import com.app.ej.cs.presenter.impl.EditFragmentPresenterImpl
import com.app.ej.cs.presenter.impl.ScanFragmentPresenterImpl
import dagger.Binds
import dagger.Module

@Module
abstract class AppModule {

  @Binds
  abstract fun provideScanFragmentPresenter(usersAndFriendsRepository: ScanFragmentPresenterImpl): ScanFragmentPresenter

  @Binds
  abstract fun provideEditFragmentPresenter(usersAndFriendsRepository: EditFragmentPresenterImpl): EditFragmentPresenter

}



