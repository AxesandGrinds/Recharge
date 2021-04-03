package com.app.ej.cs.di

import com.app.ej.cs.repository.UsersAndFriendsRepository
import com.app.ej.cs.ui.edit.EditFragment
import com.app.ej.cs.ui.scan.ScanFragment

import dagger.BindsInstance
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {

  fun inject(frag: ScanFragment)

  fun inject(frag: EditFragment)

  // 1
  @Component.Factory
  interface Factory {
    // 2
    fun repository(@BindsInstance userAndFriendsRepository: UsersAndFriendsRepository): AppComponent

  }

}

