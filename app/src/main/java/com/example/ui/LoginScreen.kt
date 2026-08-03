package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (Long) -> Unit,
    authViewModel: AuthViewModel
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    
    var isSmsMode by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "智能财富管家",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "专业级多账户金融管理体系",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { isSmsMode = false; isRegisterMode = false }) {
                            Text("密码登录", fontWeight = if (!isSmsMode && !isRegisterMode) FontWeight.Bold else FontWeight.Normal)
                        }
                        TextButton(onClick = { isSmsMode = true; isRegisterMode = false }) {
                            Text("短信验证码", fontWeight = if (isSmsMode) FontWeight.Bold else FontWeight.Normal)
                        }
                        TextButton(onClick = { isRegisterMode = true; isSmsMode = false }) {
                            Text("注册账户", fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("手机号") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSmsMode) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = smsCode,
                                onValueChange = { smsCode = it },
                                label = { Text("验证码") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (phone.length == 11) {
                                        countdown = 60
                                        scope.launch {
                                            while(countdown > 0) {
                                                delay(1000)
                                                countdown--
                                            }
                                        }
                                        errorMessage = "验证码已发送 (模拟验证码: 123456)"
                                    } else {
                                        errorMessage = "请输入11位手机号"
                                    }
                                },
                                enabled = countdown == 0,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (countdown > 0) "${countdown}s" else "获取")
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("密码") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (phone.isBlank()) {
                                errorMessage = "请输入手机号"
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                try {
                                    if (isRegisterMode) {
                                        if (password.isBlank()) {
                                            errorMessage = "密码不能为空"
                                        } else {
                                            val id = authViewModel.register(phone, password)
                                            if (id != null) {
                                                onLoginSuccess(id)
                                            } else {
                                                errorMessage = "注册失败，手机号可能已被注册"
                                            }
                                        }
                                    } else if (isSmsMode) {
                                        if (smsCode == "123456") {
                                            val id = authViewModel.loginOrRegisterSms(phone)
                                            onLoginSuccess(id)
                                        } else {
                                            errorMessage = "验证码错误 (输入 123456)"
                                        }
                                    } else {
                                        val id = authViewModel.login(phone, password)
                                        if (id != null) {
                                            onLoginSuccess(id)
                                        } else {
                                            errorMessage = "账号或密码错误"
                                        }
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isRegisterMode) "立即注册" else "安全登录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
