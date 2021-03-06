/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cards.nine.process.intents.impl

import android.content.Intent
import cards.nine.commons.contexts.ActivityContextSupport
import cards.nine.commons.services.TaskService
import cards.nine.commons.services.TaskService.TaskService
import cards.nine.commons.test.TaskServiceTestOps._
import cards.nine.commons.test.data.LauncherExecutorTestData
import cards.nine.commons.test.data.LauncherExecutorValues._
import cards.nine.models._
import cards.nine.process.intents.{
  LauncherExecutorProcessException,
  LauncherExecutorProcessPermissionException
}
import cards.nine.services.intents.{
  IntentLauncherServicesException,
  IntentLauncherServicesPermissionException,
  LauncherIntentServices
}
import cats.syntax.either._
import monix.eval.Task
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

trait LauncherExecutorProcessImplSpecification
    extends Specification
    with Mockito
    with LauncherExecutorTestData {

  val intentLauncherServicesException = IntentLauncherServicesException(exceptionMessage)
  val intentLauncherServicesPermissionException = IntentLauncherServicesPermissionException(
    exceptionMessage)

  val serviceRight: TaskService[Unit] =
    TaskService(Task(Either.right((): Unit)))
  val serviceException: TaskService[Unit] =
    TaskService(Task(Either.left(intentLauncherServicesException)))
  val servicePermissionException: TaskService[Unit] =
    TaskService(Task(Either.left(intentLauncherServicesPermissionException)))

  trait LauncherExecutorProcessImplScope extends Scope {

    val mockActivityContext = mock[ActivityContextSupport]

    val mockServices = mock[LauncherIntentServices]

    val mockIntent = mock[NineCardsIntent]

    val process = new LauncherExecutorProcessImpl(config, mockServices)

    def verifyRight(
        processService: (ActivityContextSupport) => TaskService[Unit],
        action: IntentAction): Unit = {
      mockServices.launchIntentAction(any)(any) returns serviceRight

      val result = processService(mockActivityContext).value.run
      result shouldEqual Right((): Unit)

      there was one(mockServices).launchIntentAction(action)(mockActivityContext)
    }

    def verifyLeftPermission(
        processService: (ActivityContextSupport) => TaskService[Unit],
        action: IntentAction): Unit = {
      mockServices.launchIntentAction(any)(any) returns servicePermissionException

      val result = processService(mockActivityContext).value.run
      result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

      there was one(mockServices).launchIntentAction(action)(mockActivityContext)
    }

    def verifyLeft(
        processService: (ActivityContextSupport) => TaskService[Unit],
        action: IntentAction): Unit = {
      mockServices.launchIntentAction(any)(any) returns serviceException

      val result = processService(mockActivityContext).value.run
      result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

      there was one(mockServices).launchIntentAction(action)(mockActivityContext)
    }

  }

  trait WithAppIntent { self: LauncherExecutorProcessImplScope =>

    mockIntent.getAction returns NineCardsIntentExtras.openApp
    mockIntent.extractPackageName() returns Some(launcherExecutorPackageName)
    mockIntent.extractClassName() returns Some(launcherExecutorClassName)

  }

}

class LauncherExecutorProcessImplSpec extends LauncherExecutorProcessImplSpecification {

  "execute for openApp" should {

    "call to the service with the application action for an openApp intent with package and class name" in
      new LauncherExecutorProcessImplScope with WithAppIntent {
        mockServices.launchIntentAction(any)(any) returns serviceRight

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result shouldEqual Right((): Unit)

        there was one(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

    "call to the service with the launch application action for an openApp intent when the application action " +
      "returns an Exception" in
      new LauncherExecutorProcessImplScope with WithAppIntent {
        mockServices.launchIntentAction(===(appAction))(any) returns serviceException
        mockServices.launchIntentAction(===(appLauncherAction))(any) returns serviceRight

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result shouldEqual Right((): Unit)

        there was one(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

    "call to the service with the play store action for an openApp intent when both the application action and " +
      "the application launch action returns an Exception" in
      new LauncherExecutorProcessImplScope with WithAppIntent {
        mockServices.launchIntentAction(===(appAction))(any) returns serviceException
        mockServices.launchIntentAction(===(appLauncherAction))(any) returns serviceException
        mockServices.launchIntentAction(===(appGooglePlayAction))(any) returns serviceRight

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result shouldEqual Right((): Unit)

        there was one(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

    "call to the service with the launch application action for an openApp intent with package but no class name" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(any)(any) returns serviceRight

        mockIntent.getAction returns NineCardsIntentExtras.openApp
        mockIntent.extractPackageName() returns Some(launcherExecutorPackageName)
        mockIntent.extractClassName() returns None

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result shouldEqual Right((): Unit)

        there was no(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception " +
      "for the application action" in
      new LauncherExecutorProcessImplScope with WithAppIntent {
        mockServices.launchIntentAction(===(appAction))(any) returns servicePermissionException

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

        there was one(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns an exception for " +
      "the application action and a Permission exception for the application launch action" in
      new LauncherExecutorProcessImplScope with WithAppIntent {
        mockServices.launchIntentAction(===(appAction))(any) returns serviceException
        mockServices.launchIntentAction(===(appLauncherAction))(any) returns servicePermissionException

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

        there was one(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns an exception " +
      "for both the application action and the application launch action but a Permission exception " +
      "for google play store action" in
      new LauncherExecutorProcessImplScope with WithAppIntent {
        mockServices.launchIntentAction(===(appAction))(any) returns serviceException
        mockServices.launchIntentAction(===(appLauncherAction))(any) returns serviceException
        mockServices.launchIntentAction(===(appGooglePlayAction))(any) returns servicePermissionException

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

        there was one(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception for all actions" in
      new LauncherExecutorProcessImplScope with WithAppIntent {
        mockServices.launchIntentAction(any)(any) returns serviceException

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was one(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the intent doesn't have a package name" in
      new LauncherExecutorProcessImplScope {

        mockIntent.getAction returns NineCardsIntentExtras.openApp
        mockIntent.extractPackageName() returns None
        mockIntent.extractClassName() returns None

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was no(mockServices).launchIntentAction(appAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appLauncherAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

  }

  "execute for openNoInstalledApp" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openNoInstalledApp
        mockIntent.extractPackageName() returns Some(launcherExecutorPackageName)

        verifyRight(process.execute(mockIntent)(_), appGooglePlayAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openNoInstalledApp
        mockIntent.extractPackageName() returns Some(launcherExecutorPackageName)

        verifyLeftPermission(process.execute(mockIntent)(_), appGooglePlayAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openNoInstalledApp
        mockIntent.extractPackageName() returns Some(launcherExecutorPackageName)

        verifyLeft(process.execute(mockIntent)(_), appGooglePlayAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the intent doesn't have a package name" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openNoInstalledApp
        mockIntent.extractPackageName() returns None

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was no(mockServices).launchIntentAction(appGooglePlayAction)(mockActivityContext)
      }

  }

  "execute for openSms" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openSms
        mockIntent.extractPhone() returns Some(launcherExecutorPhoneNumber)

        verifyRight(process.execute(mockIntent)(_), phoneSmsAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openSms
        mockIntent.extractPhone() returns Some(launcherExecutorPhoneNumber)

        verifyLeftPermission(process.execute(mockIntent)(_), phoneSmsAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openSms
        mockIntent.extractPhone() returns Some(launcherExecutorPhoneNumber)

        verifyLeft(process.execute(mockIntent)(_), phoneSmsAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the intent doesn't have a phone number" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openSms
        mockIntent.extractPhone() returns None

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was no(mockServices).launchIntentAction(phoneSmsAction)(mockActivityContext)
      }

  }

  "execute for openPhone" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openPhone
        mockIntent.extractPhone() returns Some(launcherExecutorPhoneNumber)

        verifyRight(process.execute(mockIntent)(_), phoneCallAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openPhone
        mockIntent.extractPhone() returns Some(launcherExecutorPhoneNumber)

        verifyLeftPermission(process.execute(mockIntent)(_), phoneCallAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openPhone
        mockIntent.extractPhone() returns Some(launcherExecutorPhoneNumber)

        verifyLeft(process.execute(mockIntent)(_), phoneCallAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the intent doesn't have a phone number" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openPhone
        mockIntent.extractPhone() returns None

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was no(mockServices).launchIntentAction(phoneCallAction)(mockActivityContext)
      }

  }

  "execute for openEmail" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openEmail
        mockIntent.extractEmail() returns Some(launcherExecutorEmail)

        verifyRight(process.execute(mockIntent)(_), emailAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openEmail
        mockIntent.extractEmail() returns Some(launcherExecutorEmail)

        verifyLeftPermission(process.execute(mockIntent)(_), emailAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openEmail
        mockIntent.extractEmail() returns Some(launcherExecutorEmail)

        verifyLeft(process.execute(mockIntent)(_), emailAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the intent doesn't have an email" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openEmail
        mockIntent.extractEmail() returns None

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was no(mockServices).launchIntentAction(emailAction)(mockActivityContext)
      }

  }

  "execute for openContact" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openContact
        mockIntent.extractLookup() returns Some(launcherExecutorLookupKey)

        verifyRight(process.execute(mockIntent)(_), contactAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openContact
        mockIntent.extractLookup() returns Some(launcherExecutorLookupKey)

        verifyLeftPermission(process.execute(mockIntent)(_), contactAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openContact
        mockIntent.extractLookup() returns Some(launcherExecutorLookupKey)

        verifyLeft(process.execute(mockIntent)(_), contactAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the intent doesn't have a contact lookup" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns NineCardsIntentExtras.openContact
        mockIntent.extractLookup() returns None

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was no(mockServices).launchIntentAction(contactAction)(mockActivityContext)
      }

  }

  "execute for an unknown action" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns unknownAction
        val androidIntent = mock[Intent]
        mockIntent.toIntent returns androidIntent
        mockServices.launchIntent(any)(any) returns serviceRight

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result shouldEqual Right((): Unit)

        there was one(mockServices).launchIntent(androidIntent)(mockActivityContext)

      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns unknownAction
        val androidIntent = mock[Intent]
        mockIntent.toIntent returns androidIntent
        mockServices.launchIntent(any)(any) returns servicePermissionException

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

        there was one(mockServices).launchIntent(androidIntent)(mockActivityContext)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        mockIntent.getAction returns unknownAction
        val androidIntent = mock[Intent]
        mockIntent.toIntent returns androidIntent
        mockServices.launchIntent(any)(any) returns serviceException

        val result = process.execute(mockIntent)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was one(mockServices).launchIntent(androidIntent)(mockActivityContext)
      }

  }

  "executeContact" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.executeContact(launcherExecutorLookupKey)(_), contactAction)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(process.executeContact(launcherExecutorLookupKey)(_), contactAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.executeContact(launcherExecutorLookupKey)(_), contactAction)
      }

  }

  "launchShare" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchShare(shareText)(_), shareAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(process.launchShare(shareText)(_), shareAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchShare(shareText)(_), shareAction)
      }

  }

  "launchSearch" should {

    "call to the services with the global search action parameter" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchSearch(_), searchGlobalAction)
      }

    "call to the services with the web search action if the service returns an exception " +
      "for the global search action" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(anyOf(SearchGlobalAction))(any) returns serviceException
        mockServices.launchIntentAction(anyOf(SearchWebAction))(any) returns serviceRight

        val result = process.launchSearch(mockActivityContext).value.run
        result shouldEqual Right((): Unit)

        there was one(mockServices).launchIntentAction(searchGlobalAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(searchWebAction)(mockActivityContext)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception " +
      "for the global search action" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(anyOf(SearchGlobalAction))(any) returns servicePermissionException

        val result = process.launchSearch(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

        there was one(mockServices).launchIntentAction(searchGlobalAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(searchWebAction)(mockActivityContext)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns an exception for " +
      "the global search action and permission exception for the web search" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(anyOf(SearchGlobalAction))(any) returns serviceException
        mockServices.launchIntentAction(anyOf(SearchWebAction))(any) returns servicePermissionException

        val result = process.launchSearch(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

        there was one(mockServices).launchIntentAction(searchGlobalAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(searchWebAction)(mockActivityContext)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception " +
      "for both search actions" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(anyOf(SearchGlobalAction))(any) returns serviceException
        mockServices.launchIntentAction(anyOf(SearchWebAction))(any) returns serviceException

        val result = process.launchSearch(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was one(mockServices).launchIntentAction(searchGlobalAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(searchWebAction)(mockActivityContext)
      }

  }

  "launchGoogleWeather" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchGoogleWeather(_), googleWeatherAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(process.launchGoogleWeather(_), googleWeatherAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchGoogleWeather(_), googleWeatherAction)
      }

  }

  "launchVoiceSearch" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchVoiceSearch(_), searchVoiceAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(process.launchVoiceSearch(_), searchVoiceAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchVoiceSearch(_), searchVoiceAction)
      }

  }

  "launchSettings" should {

    "call to the services with the app settings action parameter" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchSettings(launcherExecutorPackageName)(_), appSettingsAction)
      }

    "call to the services with the global settings action if the service returns an exception " +
      "for the app settings action" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(any[AppSettingsAction])(any) returns serviceException
        mockServices.launchIntentAction(anyOf(GlobalSettingsAction))(any) returns serviceRight

        val result =
          process.launchSettings(launcherExecutorPackageName)(mockActivityContext).value.run
        result shouldEqual Right((): Unit)

        there was one(mockServices).launchIntentAction(appSettingsAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(globalSettingsAction)(mockActivityContext)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception " +
      "for the app settings action" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(any[AppSettingsAction])(any) returns servicePermissionException

        val result =
          process.launchSettings(launcherExecutorPackageName)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

        there was one(mockServices).launchIntentAction(appSettingsAction)(mockActivityContext)
        there was no(mockServices).launchIntentAction(globalSettingsAction)(mockActivityContext)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns an exception for " +
      "the app settings action and permission exception for the global settings search" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(any[AppSettingsAction])(any) returns serviceException
        mockServices.launchIntentAction(anyOf(GlobalSettingsAction))(any) returns servicePermissionException

        val result =
          process.launchSettings(launcherExecutorPackageName)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessPermissionException, _]]

        there was one(mockServices).launchIntentAction(appSettingsAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(globalSettingsAction)(mockActivityContext)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception " +
      "for both search actions" in
      new LauncherExecutorProcessImplScope {
        mockServices.launchIntentAction(any[AppSettingsAction])(any) returns serviceException
        mockServices.launchIntentAction(anyOf(GlobalSettingsAction))(any) returns serviceException

        val result =
          process.launchSettings(launcherExecutorPackageName)(mockActivityContext).value.run
        result must beAnInstanceOf[Left[LauncherExecutorProcessException, _]]

        there was one(mockServices).launchIntentAction(appSettingsAction)(mockActivityContext)
        there was one(mockServices).launchIntentAction(globalSettingsAction)(mockActivityContext)
      }

  }

  "launchUninstall" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchUninstall(launcherExecutorPackageName)(_), appUninstallAction)
      }

    "returns a eft[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(
          process.launchUninstall(launcherExecutorPackageName)(_),
          appUninstallAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchUninstall(launcherExecutorPackageName)(_), appUninstallAction)
      }

  }

  "launchDial" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchDial(Some(launcherExecutorPhoneNumber))(_), phoneDialAction)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(
          process.launchDial(Some(launcherExecutorPhoneNumber))(_),
          phoneDialAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchDial(Some(launcherExecutorPhoneNumber))(_), phoneDialAction)
      }

  }

  "launchPlayStore" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchPlayStore(_), googlePlayStoreAction)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(process.launchPlayStore(_), googlePlayStoreAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchPlayStore(_), googlePlayStoreAction)
      }

  }

  "launchApp" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchApp(launcherExecutorPackageName)(_), appLauncherAction)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(process.launchApp(launcherExecutorPackageName)(_), appLauncherAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchApp(launcherExecutorPackageName)(_), appLauncherAction)
      }

  }

  "launchGooglePlay" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchGooglePlay(launcherExecutorPackageName)(_), appGooglePlayAction)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(
          process.launchGooglePlay(launcherExecutorPackageName)(_),
          appGooglePlayAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchGooglePlay(launcherExecutorPackageName)(_), appGooglePlayAction)
      }

  }

  "launchUrl" should {

    "call to the services with the right parameters" in
      new LauncherExecutorProcessImplScope {
        verifyRight(process.launchUrl(launcherExecutorUrl)(_), urlAction)
      }

    "returns a Left[LauncherExecutorProcessPermissionException, _] if the service returns a Permission exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeftPermission(process.launchUrl(launcherExecutorUrl)(_), urlAction)
      }

    "returns a Left[LauncherExecutorProcessException, _] if the service returns an exception" in
      new LauncherExecutorProcessImplScope {
        verifyLeft(process.launchUrl(launcherExecutorUrl)(_), urlAction)
      }

  }

}
