package com.fortysevendeg.ninecardslauncher.modules.appsmanager

import com.fortysevendeg.ninecardslauncher.commons.Service

trait AppManagerServices {
  def getApps: Service[GetAppsRequest, GetAppsResponse]
  def createBirmapForNoPackagesInstalled: Service[IntentsRequest, PackagesResponse]
  def getCategorizedApps: Service[GetCategorizedAppsRequest, GetCategorizedAppsResponse]
  def getAppsByCategory: Service[GetAppsByCategoryRequest, GetAppsByCategoryResponse]
  def categorizeApps: Service[CategorizeAppsRequest, CategorizeAppsResponse]
}

trait AppManagerServicesComponent {
  val appManagerServices: AppManagerServices
}