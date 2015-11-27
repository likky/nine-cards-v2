package com.fortysevendeg.ninecardslauncher.api.reads

import com.fortysevendeg.ninecardslauncher.api.model._
import UserConfigImplicits._
import play.api.libs.json._

object SharedCollectionImplicits {

  implicit val sharedCollectionPackageReads = Json.reads[SharedCollectionPackage]
  implicit val assetThumbResponseReads = Json.reads[AssetThumbResponse]
  implicit val assetResponseReads = Json.reads[AssetResponse]
  implicit val sharedCollectionReads = Json.reads[SharedCollection]
  implicit val sharedCollectionListReads = Json.reads[SharedCollectionList]
  implicit val sharedCollectionSubscriptionReads = Json.reads[SharedCollectionSubscription]

  implicit val sharedCollectionPackageWrites = Json.writes[SharedCollectionPackage]
  implicit val assetThumbResponseWrites = Json.writes[AssetThumbResponse]
  implicit val assetResponseWrites = Json.writes[AssetResponse]
  implicit val sharedCollectionWrites = Json.writes[SharedCollection]

}
