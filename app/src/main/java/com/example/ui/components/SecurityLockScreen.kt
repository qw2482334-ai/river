package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SecurityLockScreen(
    isSetupMode: Boolean = false,
    onUnlockAttempt: (String) -> Boolean,
    onSetupComplete: (String) -> Unit = {}
) {
    var pin by remember { mutableStateOf("") }
    var setupFirstPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    
    val title = if (isSetupMode) {
        if (setupFirstPin.isEmpty()) "设置新的数字密码" else "再次输入确认密码"
    } else {
        "请输入安全密码"
    }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(200)
            if (isSetupMode) {
                if (setupFirstPin.isEmpty()) {
                    setupFirstPin = pin
                    pin = ""
                } else {
                    if (pin == setupFirstPin) {
                        onSetupComplete(pin)
                    } else {
                        errorMsg = "两次密码不一致，请重试"
                        setupFirstPin = ""
                        pin = ""
                    }
                }
            } else {
                val success = onUnlockAttempt(pin)
                if (!success) {
                    errorMsg = "密码错误，请重试"
                    pin = ""
                }
            }
        } else {
            errorMsg = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Lock",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.height(20.dp))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN Indicators
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in 0 until 4) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < pin.length) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        // Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )
        
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else if (key == "DEL") {
                        IconButton(
                            onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(Icons.Default.Backspace, contentDescription = "Delete", modifier = Modifier.size(32.dp))
                        }
                    } else {
                        TextButton(
                            onClick = { if (pin.length < 4) pin += key },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape
                        ) {
                            Text(text = key, fontSize = 28.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
