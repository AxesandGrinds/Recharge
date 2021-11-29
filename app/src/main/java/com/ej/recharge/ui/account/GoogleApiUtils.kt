package com.ej.recharge.ui.account

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsClient
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class GoogleApiUtils private constructor() {

    companion object {

        fun isPlayServicesAvailable(context: Context): Boolean {
            return GoogleApiAvailability
                .getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        }

        fun getCredentialsClient(context: Context): CredentialsClient {
            val options = CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build()

            return if (context is Activity) {

                Credentials.getClient(context, options)

            }
            else {

                Credentials.getClient(context, options)

            }

        }

    }

    init {

        throw AssertionError("No instance for you!")

    }

}