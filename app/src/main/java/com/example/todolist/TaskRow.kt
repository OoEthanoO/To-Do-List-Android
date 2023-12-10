package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskRow(task: Task, onDelete: () -> Unit, onCompleteToggle: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Checkbox(
            checked = task.isComplete,
            onCheckedChange = { onCompleteToggle() }
        )
        if (task.priority != "None") {
            Icon(
                Icons.Default.Warning,
                contentDescription = "",
                tint = when (task.priority) {
                    "Low" -> Color.Green
                    "Medium" -> Color(0xFFffe000)
                    else -> Color.Red
                },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(textDecoration = if (task.isComplete) TextDecoration.LineThrough else TextDecoration.None)) {
                        append(task.title)
                    }
                },
                color =
                if (task.isComplete) {
                    Color.Gray
                } else {
                    when (task.priority) {
                        "Low" -> {
                            Color.Green
                        }
                        "Medium" -> {
                            Color(0xFFffe000)
                        }
                        "High" -> {
                            Color.Red
                        }
                        else -> {
                            Color.Black
                        }
                    }
                }
            )

            if (task.haveDueDate) {
                val date = task.dueDate.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
                val formattedDate = date?.format(formatter)
                if (formattedDate != null) {
                    Text(
                        text = formattedDate,
                        color =
                        if (task.isComplete) {
                            Color.Gray
                        } else {
                            when (task.priority) {
                                "Low" -> {
                                    Color.Green
                                }

                                "Medium" -> {
                                    Color(0xFFffe000)
                                }

                                "High" -> {
                                    Color.Red
                                }

                                else -> {
                                    Color.Black
                                }
                            }
                        }
                    )
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
        }
    }
}