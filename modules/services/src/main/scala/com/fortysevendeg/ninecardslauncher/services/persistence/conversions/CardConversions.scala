package com.fortysevendeg.ninecardslauncher.services.persistence.conversions

import com.fortysevendeg.ninecardslauncher.repository.model.{Card => RepoCard, CardData => RepoCardData}
import com.fortysevendeg.ninecardslauncher.services.persistence._
import com.fortysevendeg.ninecardslauncher.services.persistence.models.Card

trait CardConversions {

  def toCard(card: RepoCard): Card = {
    Card(
      id = card.id,
      position = card.data.position,
      micros = card.data.micros,
      term = card.data.term,
      packageName = card.data.packageName,
      cardType = card.data.cardType,
      intent = card.data.intent,
      imagePath = card.data.imagePath,
      starRating = card.data.starRating,
      numDownloads = card.data.numDownloads,
      notification = card.data.notification)
  }

  def toRepositoryCard(card: Card): RepoCard =
    RepoCard(
      id = card.id,
      data = RepoCardData(
        position = card.position,
        micros = card.micros,
        term = card.term,
        packageName = card.packageName,
        cardType = card.cardType,
        intent = card.intent,
        imagePath = card.imagePath,
        starRating = card.starRating,
        numDownloads = card.numDownloads,
        notification = card.notification
      )
    )

  def toRepositoryCard(request: UpdateCardRequest): RepoCard =
    RepoCard(
      id = request.id,
      data = RepoCardData(
        position = request.position,
        micros = request.micros,
        term = request.term,
        packageName = request.packageName,
        cardType = request.cardType,
        intent = request.intent,
        imagePath = request.imagePath,
        starRating = request.starRating,
        numDownloads = request.numDownloads,
        notification = request.notification
      )
    )

  def toRepositoryCardData(card: Card): RepoCardData =
    RepoCardData(
      position = card.position,
      term = card.term,
      cardType = card.cardType,
      micros = card.micros,
      packageName = card.packageName,
      intent = card.intent,
      imagePath = card.imagePath,
      starRating = card.starRating,
      numDownloads = card.numDownloads,
      notification = card.notification)

  def toRepositoryCardData(request: AddCardRequest): RepoCardData =
    RepoCardData(
      position = request.position,
      micros = request.micros,
      term = request.term,
      packageName = request.packageName,
      cardType = request.cardType,
      intent = request.intent,
      imagePath = request.imagePath,
      starRating = request.starRating,
      numDownloads = request.numDownloads,
      notification = request.notification)
}