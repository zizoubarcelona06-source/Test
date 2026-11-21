package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.ui.components.BtpVerticalBrandLockup
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavyPrimaryDark
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    companyName: String,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onLogin: (phoneOrUsername: String, password: String, rememberMe: Boolean) -> Unit,
    onLoginDemo: (phone: String, password: String, rememberMe: Boolean) -> Unit,
    onClearError: () -> Unit
) {
    val strings = LocalAppStrings.current
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val submitLogin: () -> Unit = {
        keyboardController?.hide()
        focusManager.clearFocus()
        onLogin(phone, password, rememberMe)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyPrimaryDark)
    ) {
        val isWideTablet = maxWidth >= 720.dp

        if (isWideTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel: Matches the Brand Poster in the User Reference (White canvas + BTP MANAGER logo + Bottom Navy/Orange Swoosh Wave)
                Box(
                    modifier = Modifier
                        .weight(0.95f)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFF3F6FB),
                                    Color(0xFFE7EFF9)
                                )
                            )
                        )
                ) {
                    // Bottom Navy & Orange Dynamic Swoosh Waves
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val orangeWave = Path().apply {
                            moveTo(0f, h * 0.91f)
                            quadraticBezierTo(w * 0.45f, h * 0.85f, w, h * 0.72f)
                            lineTo(w, h * 0.79f)
                            quadraticBezierTo(w * 0.48f, h * 0.90f, 0f, h * 0.95f)
                            close()
                        }
                        drawPath(orangeWave, color = AmberSecondary)

                        val navyWave = Path().apply {
                            moveTo(0f, h * 0.94f)
                            quadraticBezierTo(w * 0.50f, h * 0.89f, w, h * 0.77f)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(navyWave, color = NavyPrimary)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            OfflineSecurityBadge(text = strings.offlineBadge)
                        }

                        BtpVerticalBrandLockup(
                            subtitle = strings.loginSubtitle,
                            onDarkBackground = false,
                            emblemSize = 124.dp
                        )

                        Text(
                            text = "BTP MANAGER • $companyName",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right Panel: Full-bleed Construction Site Backdrop + White Pill Login Form
                Box(
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight()
                ) {
                    ConstructionBackdropWithWaves()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 32.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 440.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            LanguageSelectorPill(
                                currentLanguage = currentLanguage,
                                chooseLanguageDesc = strings.chooseLanguage,
                                onClick = { showLanguageDialog = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        LoginFormContent(
                            phone = phone,
                            onPhoneChange = {
                                phone = it
                                if (uiState.errorMessage != null) onClearError()
                            },
                            password = password,
                            onPasswordChange = {
                                password = it
                                if (uiState.errorMessage != null) onClearError()
                            },
                            rememberMe = rememberMe,
                            onRememberMeChange = { rememberMe = it },
                            showPassword = showPassword,
                            onToggleShowPassword = { showPassword = !showPassword },
                            uiState = uiState,
                            onSubmitLogin = submitLogin,
                            onDemoLogin = { demoPhone, demoPass ->
                                phone = demoPhone
                                password = demoPass
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onLoginDemo(demoPhone, demoPass, rememberMe)
                            },
                            modifier = Modifier.widthIn(max = 440.dp)
                        )
                    }
                }
            }
        } else {
            // Smartphone Full-Bleed Layout matching the Center Mockup Phone
            Box(modifier = Modifier.fillMaxSize()) {
                ConstructionBackdropWithWaves()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OfflineSecurityBadge(
                            text = strings.offlineBadge,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LanguageSelectorPill(
                            currentLanguage = currentLanguage,
                            chooseLanguageDesc = strings.chooseLanguage,
                            onClick = { showLanguageDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    BtpVerticalBrandLockup(
                        subtitle = companyName,
                        onDarkBackground = true,
                        emblemSize = 94.dp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    LoginFormContent(
                        phone = phone,
                        onPhoneChange = {
                            phone = it
                            if (uiState.errorMessage != null) onClearError()
                        },
                        password = password,
                        onPasswordChange = {
                            password = it
                            if (uiState.errorMessage != null) onClearError()
                        },
                        rememberMe = rememberMe,
                        onRememberMeChange = { rememberMe = it },
                        showPassword = showPassword,
                        onToggleShowPassword = { showPassword = !showPassword },
                        uiState = uiState,
                        onSubmitLogin = submitLogin,
                        onDemoLogin = { demoPhone, demoPass ->
                            phone = demoPhone
                            password = demoPass
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onLoginDemo(demoPhone, demoPass, rememberMe)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 440.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = currentLanguage,
            onSelectLanguage = onSelectLanguage,
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun ConstructionBackdropWithWaves() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_btp_site_hero),
            contentDescription = "BTP Manager Construction Site",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Smooth atmospheric gradient transitioning from warm twilight crane view into deep architectural navy
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to NavyPrimaryDark.copy(alpha = 0.42f),
                        0.32f to NavyPrimary.copy(alpha = 0.72f),
                        0.58f to NavyPrimaryDark.copy(alpha = 0.94f),
                        1.0f to Color(0xFF04142E)
                    )
                )
        )
        // Diagonal subtle orange & steel-blue geometric corner accent matching the mockup
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val bottomAccent = Path().apply {
                moveTo(w * 0.65f, h)
                lineTo(w, h * 0.87f)
                lineTo(w, h)
                close()
            }
            drawPath(bottomAccent, color = AmberSecondary.copy(alpha = 0.90f))

            val subtleWave = Path().apply {
                moveTo(0f, h * 0.88f)
                quadraticBezierTo(w * 0.45f, h * 0.96f, w * 0.78f, h * 0.86f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(subtleWave, color = NavyPrimary.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun OfflineSecurityBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavyPrimaryDark.copy(alpha = 0.78f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.OfflineBolt,
                contentDescription = null,
                tint = AmberBright,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LanguageSelectorPill(
    currentLanguage: AppLanguage,
    chooseLanguageDesc: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("login_language_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = chooseLanguageDesc,
                tint = AmberBright,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = currentLanguage.nativeName,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LoginFormContent(
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    showPassword: Boolean,
    onToggleShowPassword: () -> Unit,
    uiState: AuthUiState,
    onSubmitLogin: () -> Unit,
    onDemoLogin: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pure White Pill Phone Input Field
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = {
                Text(
                    text = "${strings.phoneLabel} (${strings.phonePlaceholder})",
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = strings.phoneLabel,
                    tint = NavyPrimary
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = uiState.errorMessage != null,
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.96f),
                errorContainerColor = Color.White,
                focusedTextColor = Color(0xFF0B1B34),
                unfocusedTextColor = Color(0xFF0B1B34),
                focusedBorderColor = AmberSecondary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                errorBorderColor = StatusDanger,
                cursorColor = AmberSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_phone_input")
        )

        // Pure White Pill Password Input Field with Visibility Toggle
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = {
                Text(
                    text = strings.passwordLabel,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = strings.passwordLabel,
                    tint = NavyPrimary
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onToggleShowPassword,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("toggle_password_visibility")
                ) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) strings.hidePassword else strings.showPassword,
                        tint = NavyPrimary.copy(alpha = 0.8f)
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmitLogin() }
            ),
            isError = uiState.errorMessage != null,
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.96f),
                errorContainerColor = Color.White,
                focusedTextColor = Color(0xFF0B1B34),
                unfocusedTextColor = Color(0xFF0B1B34),
                focusedBorderColor = AmberSecondary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                errorBorderColor = StatusDanger,
                cursorColor = AmberSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input")
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = onRememberMeChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = AmberSecondary,
                    uncheckedColor = Color.White.copy(alpha = 0.8f),
                    checkmarkColor = Color.White
                )
            )
            Text(
                text = strings.rememberMe,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )
        }

        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Surface(
                color = StatusDangerBg,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, StatusDanger.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_error_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = strings.errInvalidCredentials,
                        tint = StatusDanger,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorMessage?.let { strings.authErrorText(it) } ?: "",
                        color = StatusDanger,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Vibrant Orange Pill Login CTA Button (Matches Middle Mockup Phone)
        Button(
            onClick = onSubmitLogin,
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberSecondary,
                contentColor = Color.White,
                disabledContainerColor = OrangeDeep.copy(alpha = 0.6f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .testTag("login_submit_button")
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = strings.loginButton,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Clean Divider & Quick Demo Access Pill Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.White.copy(alpha = 0.28f)
            )
            Text(
                text = strings.quickDemoTitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.White.copy(alpha = 0.28f)
            )
        }

        // Pure White Pill Primary Demo Login Button (Mirrors the bottom white pill CTA of the mockup)
        Button(
            onClick = { onDemoLogin("0550000001", "123456") },
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = NavyPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("demo_login_admin_button")
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = AmberSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.demoAdminButton,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        OutlinedButton(
            onClick = { onDemoLogin("0550000002", "123456") },
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.25.dp, Color.White.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.08f),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("demo_login_engineer_button")
        ) {
            Icon(
                imageVector = Icons.Default.Engineering,
                contentDescription = null,
                tint = AmberBright,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.demoEngineerButton,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = strings.demoCredentialsHint,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.72f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
