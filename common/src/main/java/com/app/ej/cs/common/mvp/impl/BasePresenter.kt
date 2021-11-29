package com.ej.recharge.common.mvp.impl

import androidx.annotation.CallSuper
import com.ej.recharge.common.mvp.Model
import com.ej.recharge.common.mvp.Presenter
import com.ej.recharge.common.mvp.View

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