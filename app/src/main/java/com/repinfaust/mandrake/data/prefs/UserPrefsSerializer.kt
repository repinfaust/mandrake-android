package com.repinfaust.mandrake.data.prefs

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

object UserPrefsSerializer : Serializer<UserPrefs> {

  override val defaultValue: UserPrefs = UserPrefs.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): UserPrefs {
    try {
      return UserPrefs.parseFrom(input)
    } catch (e: Exception) {
      throw CorruptionException("Cannot read UserPrefs", e)
    }
  }

  override suspend fun writeTo(t: UserPrefs, output: OutputStream) {
    t.writeTo(output)
  }
}
