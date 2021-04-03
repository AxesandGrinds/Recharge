package com.app.ej.cs.common.mvp

/**
 * A Presenter in the MVP architecture. It's bound to a specific View
 */
interface Presenter<M1 : Model, M2 : Model, V : View<M1, M2>> {

  /**
   * Binds the view to the Presenter
   */
  fun bind(v: V)

  /**
   * Unbinds the View
   */
  fun unbind()
}