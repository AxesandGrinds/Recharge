package com.app.ej.cs.ui.account

import android.util.Patterns
import com.app.ej.cs.R
import com.app.ej.cs.ui.account.BaseValidator

import com.google.android.material.textfield.TextInputLayout

class EmailFieldValidator(errorContainer: TextInputLayout?) : BaseValidator(errorContainer!!) {

    override fun isValid(charSequence: CharSequence?): Boolean {

        if (charSequence != null) {

            return Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()

        }
        else {

            return false

        }

    }

    init {
        mErrorMessage = mErrorContainer.resources.getString(R.string.fui_invalid_email_address)
        mEmptyMessage = mErrorContainer.resources.getString(R.string.fui_missing_email_address)
    }
}
