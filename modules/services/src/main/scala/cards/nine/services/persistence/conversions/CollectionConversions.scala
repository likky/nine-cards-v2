package cards.nine.services.persistence.conversions

import cards.nine.models.{CollectionData, Collection}
import cards.nine.repository.model.{Card => RepositoryCard, Collection => RepositoryCollection, CollectionData => RepositoryCollectionData, Moment => RepositoryMoment}
import cards.nine.services.persistence._

trait CollectionConversions
  extends CardConversions
  with MomentConversions {

  def toCollection(collection: RepositoryCollection): Collection =
    Collection(
      id = collection.id,
      position = collection.data.position,
      name = collection.data.name,
      collectionType = collection.data.collectionType,
      icon = collection.data.icon,
      themedColorIndex = collection.data.themedColorIndex,
      appsCategory = collection.data.appsCategory,
      originalSharedCollectionId = collection.data.originalSharedCollectionId,
      sharedCollectionId = collection.data.sharedCollectionId,
      sharedCollectionSubscribed = collection.data.sharedCollectionSubscribed getOrElse false,
      moment = None
    )

  def toCollection(collection: RepositoryCollection, cards: Seq[RepositoryCard], moment: Option[RepositoryMoment]): Collection =
    Collection(
      id = collection.id,
      position = collection.data.position,
      name = collection.data.name,
      collectionType = collection.data.collectionType,
      icon = collection.data.icon,
      themedColorIndex = collection.data.themedColorIndex,
      appsCategory = collection.data.appsCategory,
      originalSharedCollectionId = collection.data.originalSharedCollectionId,
      sharedCollectionId = collection.data.sharedCollectionId,
      sharedCollectionSubscribed = collection.data.sharedCollectionSubscribed getOrElse false,
      cards = cards map toCard,
      moment = moment map toMoment
    )

  def toRepositoryCollection(collection: Collection): RepositoryCollection =
    RepositoryCollection(
      id = collection.id,
      data = RepositoryCollectionData(
        position = collection.position,
        name = collection.name,
        collectionType = collection.collectionType,
        icon = collection.icon,
        themedColorIndex = collection.themedColorIndex,
        appsCategory = collection.appsCategory,
        originalSharedCollectionId = collection.originalSharedCollectionId,
        sharedCollectionId = collection.sharedCollectionId,
        sharedCollectionSubscribed = Option(collection.sharedCollectionSubscribed)
      )
    )

  def toRepositoryCollectionData(collection: CollectionData): RepositoryCollectionData =
    RepositoryCollectionData(
      position = collection.position,
      name = collection.name,
      collectionType = collection.collectionType,
      icon = collection.icon,
      themedColorIndex = collection.themedColorIndex,
      appsCategory = collection.appsCategory,
      originalSharedCollectionId = collection.originalSharedCollectionId,
      sharedCollectionId = collection.sharedCollectionId,
      sharedCollectionSubscribed = Option(collection.sharedCollectionSubscribed))
}
