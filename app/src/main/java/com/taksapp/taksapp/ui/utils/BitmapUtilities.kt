package com.taksapp.taksapp.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class BitmapUtilities {
    companion object {
        fun getBitmapDescriptorForResource(
            @DrawableRes drawableId: Int,
            context: Context): BitmapDescriptor? {
            val drawable = ContextCompat.getDrawable(context, drawableId)
                ?: throw Exception("Drawable with id $drawableId was not found.")

            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight
            drawable.bounds = Rect(0, 0, drawableWidth, drawableHeight)

            val bitmap = Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888)
            drawable.draw(Canvas(bitmap))

            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}