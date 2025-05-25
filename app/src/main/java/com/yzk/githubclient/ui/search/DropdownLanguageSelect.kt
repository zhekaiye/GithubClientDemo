package com.yzk.githubclient.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


/**
 * @description dropdown selection for language
 *
 * @author: yezhekai.256
 * @date: 5/25/25
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSingleSelect(onLanguageSelected: (String?) -> Unit) {
    // 定义选项列表
    val languages = listOf(
        "All",
        "Java",
        "Kotlin",
        "Python",
        "JavaScript",
        "TypeScript",
        "Go",
        "Rust",
        "Swift",
        "C",
        "C++",
        "C#"
    )

    // 状态管理
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("All") }

    // 定义边框样式
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val borderWidth = 1.dp
    val menuShape = RoundedCornerShape(8.dp)

    // 触发按钮
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
//        modifier = Modifier.background(Color.Transparent)
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
//                .background(Color.Transparent)
                .fillMaxWidth()
                .border( // 输入框边框
                    width = borderWidth,
                    color = borderColor,
                    shape = menuShape
                ),
            readOnly = true,
            value = selectedText,
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
//                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,  // 隐藏下划线
                unfocusedIndicatorColor = Color.Transparent
            ),
        )
        // 下拉菜单
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.Transparent)
                .border( // 下拉菜单容器边框
                    width = borderWidth,
                    color = borderColor,
                    shape = menuShape
                )
        ) {
            languages.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
//                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        selectedText = item
                        expanded = false
                        onLanguageSelected(item)
                    },
//                    colors = MenuItemDefaults.itemColors(
//                        containerColor = Color.Transparent
//                    ),
                    modifier = Modifier.background(Color.Transparent)
                )
            }
        }
    }
}