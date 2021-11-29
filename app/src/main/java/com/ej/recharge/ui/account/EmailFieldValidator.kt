package com.ej.recharge.ui.account

import android.util.Patterns
import com.ej.recharge.R
import com.ej.recharge.ui.account.BaseValidator

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
