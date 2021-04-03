package com.app.ej.cs.common.mvp.impl

import androidx.annotation.CallSuper
import com.app.ej.cs.common.mvp.Model
import com.app.ej.cs.common.mvp.Presenter
import com.app.ej.cs.common.mvp.View

/**
 * Base class for Presenters¬
 */
abstract class BasePresenter<M1 : Model, M2 : Model, V : View<M1, M2>> : Presenter<M1, M2, V> {

  /**
   * The View
   */
  protected var view: V? = null

  @CallSuper
  override fun bind(v: V) {
    view = v
  }

  @CallSuper
  override fun unbind() {
    view = null
  }

}