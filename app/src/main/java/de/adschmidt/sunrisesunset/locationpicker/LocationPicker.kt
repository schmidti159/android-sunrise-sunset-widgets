package de.adschmidt.sunrisesunset.locationpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract

class LocationPicker : ActivityResultContract<LocationPicker.Params, LocationPicker.Result>() {
    companion object {
        const val PARAMS_KEY = "params"
        const val RESULT_KEY = "result"
    }

    override fun createIntent(ctx: Context, params: Params): Intent {
        val intent = Intent(ctx, LocationPickerActivity::class.java)

        intent.putExtra(PARAMS_KEY, params)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): LocationPicker.Result? {
        if(resultCode != Activity.RESULT_OK || intent == null) {
            return null;
        }
        return intent.getParcelableExtra<LocationPicker.Result>(RESULT_KEY)
    }

    data class Params (
        val latitude : Double,
        val longitude : Double
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeDouble(latitude)
            parcel.writeDouble(longitude)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Params> {
            override fun createFromParcel(parcel: Parcel): Params {
                return Params(parcel)
            }

            override fun newArray(size: Int): Array<Params?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Result (
        val latitude : Double,
        val longitude : Double,
        val altitude : Double,
        val address : String?
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeDouble(latitude)
            parcel.writeDouble(longitude)
            parcel.writeDouble(altitude)
            parcel.writeString(address)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Result> {
            override fun createFromParcel(parcel: Parcel): Result {
                return Result(parcel)
            }

            override fun newArray(size: Int): Array<Result?> {
                return arrayOfNulls(size)
            }
        }
    }

}