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

package cards.nine.repository.provider

import android.database.Cursor
import cards.nine.repository.model.User
import cards.nine.repository.Conversions._

case class UserEntity(id: Int, data: UserEntityData)

case class UserEntityData(
    email: String,
    apiKey: String,
    sessionToken: String,
    deviceToken: String,
    marketToken: String,
    name: String,
    avatar: String,
    cover: String,
    deviceName: String,
    deviceCloudId: String)

object UserEntity {
  val table         = "User"
  val email         = "email"
  val apiKey        = "apiKey"
  val sessionToken  = "sessionToken"
  val deviceToken   = "deviceToken"
  val marketToken   = "marketToken"
  val name          = "name"
  val avatar        = "avatar"
  val cover         = "cover"
  val deviceName    = "deviceName"
  val deviceCloudId = "deviceCloudId"

  val allFields = Seq[String](
    NineCardsSqlHelper.id,
    email,
    apiKey,
    sessionToken,
    deviceToken,
    marketToken,
    name,
    avatar,
    cover,
    deviceName,
    deviceCloudId)

  def userEntityFromCursor(cursor: Cursor): UserEntity =
    UserEntity(
      id = cursor.getInt(cursor.getColumnIndex(NineCardsSqlHelper.id)),
      data = UserEntityData(
        email = cursor.getString(cursor.getColumnIndex(email)),
        apiKey = cursor.getString(cursor.getColumnIndex(apiKey)),
        sessionToken = cursor.getString(cursor.getColumnIndex(sessionToken)),
        deviceToken = cursor.getString(cursor.getColumnIndex(deviceToken)),
        marketToken = cursor.getString(cursor.getColumnIndex(marketToken)),
        name = cursor.getString(cursor.getColumnIndex(name)),
        avatar = cursor.getString(cursor.getColumnIndex(avatar)),
        cover = cursor.getString(cursor.getColumnIndex(cover)),
        deviceName = cursor.getString(cursor.getColumnIndex(deviceName)),
        deviceCloudId = cursor.getString(cursor.getColumnIndex(deviceCloudId))))

  def userFromCursor(cursor: Cursor): User = toUser(userEntityFromCursor(cursor))

  def createTableSQL: String =
    s"""CREATE TABLE ${UserEntity.table}
        |(${NineCardsSqlHelper.id} INTEGER PRIMARY KEY AUTOINCREMENT,
        |${UserEntity.email} TEXT,
        |${UserEntity.apiKey} TEXT,
        |${UserEntity.sessionToken} TEXT,
        |${UserEntity.deviceToken} TEXT,
        |${UserEntity.marketToken} TEXT,
        |${UserEntity.name} TEXT,
        |${UserEntity.avatar} TEXT,
        |${UserEntity.cover} TEXT,
        |${UserEntity.deviceName} TEXT,
        |${UserEntity.deviceCloudId} TEXT)""".stripMargin

}
