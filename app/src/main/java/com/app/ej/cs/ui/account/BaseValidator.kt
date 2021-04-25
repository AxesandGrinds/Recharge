package com.app.ej.cs.ui.account

import com.google.android.material.textfield.TextInputLayout

open class BaseValidator(protected var mErrorContainer: TextInputLayout) {

    protected var mErrorMessage = ""

    protected var mEmptyMessage: String? = null

    protected open fun isValid(charSequence: CharSequence?): Boolean {
        return true
    }

    fun validate(charSequence: CharSequence?): Boolean {

        return if (mEmptyMessage != null && (charSequence == null || charSequence.length == 0)) {

            mErrorContainer.error = mEmptyMessage
            false

        }
        else if (isValid(charSequence)) {

            mErrorContainer.error = ""
            true

        }
        else {

            mErrorContainer.error = mErrorMessage
            false

        }

    }

}