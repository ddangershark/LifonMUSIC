package com.lifon.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val BASE_URL = "https://cupsize-api.usvidelsvet.workers.dev"

@Composable
fun AuthScreen(onAuthSuccess: (token: String) -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val alpha = remember { Animatable(0f) }
    val slideY = remember { Animatable(30f) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch { slideY.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF1A1A1F),
                        0.5f to Color(0xFF111114),
                        1.0f to Color(0xFF080809)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp)
                .offset(y = slideY.value.dp)
                .graphicsLayer { this.alpha = alpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.15f))

            Text(
                if (isLogin) "Войти" else "Регистрация",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "LifonMUSIC",
                color = Color(0xFFE8D5FF).copy(alpha = 0.45f),
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorMsg = null },
                singleLine = true,
                placeholder = {
                    Text(
                        "Имя пользователя",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFE8D5FF).copy(alpha = 0.40f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    cursorColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.06f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMsg = null },
                singleLine = true,
                placeholder = {
                    Text(
                        "Пароль",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 14.sp
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFE8D5FF).copy(alpha = 0.40f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    cursorColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.06f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            AnimatedVisibility(visible = errorMsg != null) {
                Text(
                    errorMsg.orEmpty(),
                    color = Color(0xFFFF6B8A),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isLoading) Color.White.copy(alpha = 0.5f) else Color.White)
                    .clickable(enabled = !isLoading) {
                        val u = username.trim().lowercase()
                        val p = password

                        if (u.isBlank() || p.isBlank()) {
                            errorMsg = "Заполни все поля"
                            return@clickable
                        }

                        scope.launch {
                            isLoading = true
                            errorMsg = null

                            val result = authRequest(
                                isLogin = isLogin,
                                username = u,
                                password = p
                            )

                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> onAuthSuccess(result.token)
                                is AuthResult.Error -> errorMsg = result.message
                            }
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isLoading) "..." else if (isLogin) "Войти" else "Зарегистрироваться",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isLogin) "Нет аккаунта? " else "Уже есть аккаунт? ",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp
                )
                Text(
                    if (isLogin) "Зарегистрироваться" else "Войти",
                    color = Color(0xFFE8D5FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isLogin = !isLogin
                            errorMsg = null
                        }
                        .padding(4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
                    .clickable(enabled = !isLoading) { onAuthSuccess("guest") }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Войти как гость",
                        color = Color.White.copy(alpha = 0.55f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Вы не сможете получать статистику о своих прослушиваниях",
                        color = Color.White.copy(alpha = 0.28f),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

sealed class AuthResult {
    data class Success(val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

suspend fun authRequest(isLogin: Boolean, username: String, password: String): AuthResult {
    val endpoint = if (isLogin) "/auth/login" else "/auth/register"
    val url = "$BASE_URL$endpoint"

    return withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
            }

            val body = JSONObject()
                .put("username", username)
                .put("password", password)
                .toString()

            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            val obj = runCatching { JSONObject(text) }.getOrNull()
            val ok = obj?.optBoolean("ok", false) ?: false

            if (code in 200..299 && ok) {
                val token = obj?.optString("token", null)
                if (!token.isNullOrBlank()) return@withContext AuthResult.Success(token)
                return@withContext AuthResult.Error("Сервер не вернул token")
            }

            val msg = obj?.optString("error")?.takeIf { it.isNotBlank() }
                ?: if (code == 401) "Неверный логин или пароль"
                else "Ошибка сервера ($code)"

            AuthResult.Error(msg)
        } catch (_: Exception) {
            AuthResult.Error("Нет соединения")
        } finally {
            conn?.disconnect()
        }
    }
}