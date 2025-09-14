package com.repinfaust.mandrake.util
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

fun subtleHaptic(context: Context) {
  val vib = if (android.os.Build.VERSION.SDK_INT >= 31) {
    val vm = context.getSystemService(VibratorManager::class.java)
    vm.defaultVibrator
  } else context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
  val effect = VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
  vib.vibrate(effect)
}
