package com.ej.recharge.di

import com.ej.recharge.repository.UsersAndFriendsRepository
import com.ej.recharge.ui.edit.EditFragment
import com.ej.recharge.ui.home.HomeFragment

import dagger.BindsInstance
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {

  fun inject(frag: HomeFragment)

  fun inject(frag: EditFragment)


  @Component.Factory
  interface Factory {

    fun repository(@BindsInstance userAndFriendsRepository: UsersAndFriendsRepository): AppComponent

  }

}

