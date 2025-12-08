package com.rcsi.wellby.toolkitTab.toDoList
// to do item model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "todo_items")
data class ToDoItemEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String,
    var date: Date,
    var isComplete: Boolean
)
