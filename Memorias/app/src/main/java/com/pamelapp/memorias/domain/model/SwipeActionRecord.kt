package com.pamelapp.memorias.domain.model

data class SwipeActionRecord(
  val type: SwipeActionType,
  val photo: Photo,
  val indexBeforeAction: Int,
  val wasFavoriteBeforeAction: Boolean
)