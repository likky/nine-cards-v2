package com.fortysevendeg.ninecardslauncher.api

import com.fortysevendeg.ninecardslauncher.api.services.SharedCollectionsServiceClient
import com.fortysevendeg.rest.client.ServiceClient

trait NineCardsServiceClient
  extends ServiceClient
  with SharedCollectionsServiceClient