package com.pamelapp.memorias.domain.model

enum class DeleteMode {
  TRASH,
  PERMANENT;
  
  val isTrash: Boolean
    get() = this == TRASH
  
  val isPermanent: Boolean
    get() = this == PERMANENT
}