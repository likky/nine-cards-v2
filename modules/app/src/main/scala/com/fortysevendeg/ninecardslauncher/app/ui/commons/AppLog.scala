package com.fortysevendeg.ninecardslauncher.app.ui.commons

import android.util.Log
import com.crashlytics.android.Crashlytics

object AppLog {

  val tag = "9cards"

  def printErrorMessage(ex: Throwable, message: Option[String] = None) = {
    try {
      Crashlytics.logException(ex)
      val outputEx = Option(ex.getCause) getOrElse ex
      Log.e(tag, message getOrElse errorMessage(outputEx), outputEx)
    } catch { case _: Throwable => }
  }

  def printErrorTaskMessage(header: String, exs: Seq[Throwable]) = {
    try {
      Log.e(tag, header)
      exs foreach (ex => printErrorMessage(ex, Some(errorMessage(ex))))
    } catch { case _: Throwable => }
  }

  private[this] def errorMessage(ex: Throwable): String =
    Option(ex.getMessage) getOrElse ex.getClass.toString

}
