package com.pixo.ai.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

private val PixoPurple = Color(0xFF8C58FF)
private val PixoPink = Color(0xFFFF4F9A)
private val PixoDark = Color(0xFF090617)
private val PixoCard = Color(0xFF17112A)
private val PixoText = Color(0xFFF7F2FF)
private val PixoMuted = Color(0xFFA69CB7)

@Composable
fun PixoApp(vm: PixoViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var introSeen by remember { mutableStateOf(false) }
    var showAuth by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PixoDark
    ) {
        when {
            !introSeen -> WelcomeScreen(
                onGetStarted = { introSeen = true; showAuth = true }
            )

            !state.session.loggedIn && !state.session.guest -> {
                AuthScreen(
                    onLogin = vm::login,
                    onSignUp = vm::signUp,
                    onGuest = vm::continueAsGuest,
                    onBack = { introSeen = false }
                )
            }

            else -> HomeScreen(vm, state)
        }
    }
}

@Composable
private fun WelcomeScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF7664FF), Color(0xFFDB6A9E), Color(0xFFFFB26D))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(48.dp))
                    .background(Color.White.copy(alpha = .12f))
                    .border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(48.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(84.dp)
                )
            }

            Spacer(Modifier.height(30.dp))
            Text(
                "Pixo",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                "Create. Edit. Imagine.",
                color = Color.White.copy(alpha = .9f),
                fontSize = 17.sp
            )

            Spacer(Modifier.height(42.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF120A2E))
            ) {
                Text("Get started", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onGuest: () -> Unit,
    onBack: () -> Unit
) {
    var signUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).statusBarsPadding().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, null, tint = PixoText)
            }
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(34.dp))
        Text("Pixo", fontSize = 42.sp, fontWeight = FontWeight.Black, color = PixoText)
        Text(
            if (signUp) "Create your account" else "Welcome back",
            color = PixoMuted,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(36.dp))

        PixoTextField(email, { email = it }, "Email")
        Spacer(Modifier.height(14.dp))
        PixoTextField(
            password,
            { password = it },
            "Password",
            password = true
        )

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                if (signUp) onSignUp(email, password) else onLogin(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(17.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PixoPurple)
        ) {
            Text(if (signUp) "Create account" else "Sign in", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onGuest,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(17.dp)
        ) {
            Text("Continue as guest", color = PixoText)
        }

        Spacer(Modifier.height(22.dp))

        TextButton(onClick = { signUp = !signUp }) {
            Text(
                if (signUp) "Already have an account? Sign in"
                else "Don't have an account? Sign up",
                color = PixoPink
            )
        }

        Spacer(Modifier.height(25.dp))
        Text(
            "Google / Apple / Facebook authentication can be connected here later.",
            color = PixoMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PixoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PixoPurple,
            unfocusedBorderColor = Color(0xFF3B304E),
            focusedLabelColor = PixoPurple,
            unfocusedLabelColor = PixoMuted,
            focusedTextColor = PixoText,
            unfocusedTextColor = PixoText,
            cursorColor = PixoPurple
        )
    )
}

@Composable
private fun HomeScreen(vm: PixoViewModel, state: PixoUiState) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(1) }
    var showEditor by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = decodeBitmap(context, uri)
            if (bitmap != null) {
                vm.setImage(bitmap)
                showEditor = true
            } else {
                Toast.makeText(context, "Unable to open image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showEditor && state.selectedBitmap != null) {
        EditorScreen(
            state = state,
            vm = vm,
            onBack = { showEditor = false },
            onPickImage = { picker.launch("image/*") },
            onSave = {
                val bitmap = state.editedBitmap ?: state.selectedBitmap
                if (bitmap != null) saveBitmap(context, bitmap)
            }
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pixo", fontSize = 27.sp, fontWeight = FontWeight.Black, color = PixoText)
            Spacer(Modifier.weight(1f))
            Surface(shape = CircleShape, color = PixoCard) {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Person, null, tint = PixoText)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            SearchBar()
            Spacer(Modifier.height(18.dp))

            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip("Text to image", true)
                ModeChip("Image edit", false)
                ModeChip("Auto enhance", false)
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = PixoCard
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Describe what you want to create or edit",
                        color = PixoMuted,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(34.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { picker.launch("image/*") },
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Outlined.CloudUpload, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Upload")
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { picker.launch("image/*") },
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PixoPink)
                        ) {
                            Text("Edit image")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Examples", fontWeight = FontWeight.Bold, color = PixoText, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            val examples = listOf(
                "Portrait edit" to Color(0xFF6B4D75),
                "Product scene" to Color(0xFF4B5C74),
                "Remove background" to Color(0xFF78594C),
                "Change style" to Color(0xFF68506B),
                "Creative scene" to Color(0xFF455A7A),
                "Enhance photo" to Color(0xFF715C48)
            )

            examples.chunked(3).forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { (title, color) ->
                        ExampleCard(title, color, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "Signed in as " + if (state.session.guest) "Guest" else state.session.email,
                color = PixoMuted,
                fontSize = 12.sp
            )
            TextButton(onClick = vm::logout) {
                Text("Sign out", color = PixoPink)
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SearchBar() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(15.dp),
        color = PixoCard
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 13.dp)) {
            Icon(Icons.Outlined.Search, null, tint = PixoMuted)
            Spacer(Modifier.width(8.dp))
            Text("Search", color = PixoMuted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ModeChip(text: String, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PixoPurple.copy(alpha = .18f) else PixoCard,
        modifier = Modifier.border(
            1.dp,
            if (selected) PixoPurple else Color(0xFF302741),
            RoundedCornerShape(12.dp)
        )
    ) {
        Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AutoAwesome, null, tint = if (selected) PixoPurple else PixoMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = PixoText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ExampleCard(title: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)).background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Style, null, tint = Color.White.copy(alpha = .75f), modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(title, color = PixoMuted, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun EditorScreen(
    state: PixoUiState,
    vm: PixoViewModel,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onSave: () -> Unit
) {
    val display = state.editedBitmap ?: state.selectedBitmap!!

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, null, tint = PixoText)
            }
            Text("AI Image Edit", color = PixoText, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onPickImage) {
                Icon(Icons.Outlined.AddPhotoAlternate, null, tint = PixoText)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(PixoCard),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = display.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Fit
                )

                if (state.loading) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = .65f)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(22.dp),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text("What should Pixo change?", color = PixoText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(9.dp))

            OutlinedTextField(
                value = state.prompt,
                onValueChange = vm::setPrompt,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                placeholder = {
                    Text(
                        "Example: remove the background and put the person in a purple studio",
                        color = PixoMuted
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixoPurple,
                    unfocusedBorderColor = Color(0xFF3B304E),
                    focusedTextColor = PixoText,
                    unfocusedTextColor = PixoText,
                    cursorColor = PixoPurple
                )
            )

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip("Remove background", vm)
                SuggestionChip("Make it cinematic", vm)
                SuggestionChip("Change sky", vm)
                SuggestionChip("Add flowers", vm)
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = vm::editImage,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrushColor(PixoPurple, PixoPink)
                )
            ) {
                Icon(Icons.Outlined.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.loading) "Generating..." else "Generate AI edit", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(17.dp)
            ) {
                Icon(Icons.Outlined.Download, null)
                Spacer(Modifier.width(8.dp))
                Text("Save to gallery", color = PixoText)
            }

            state.message?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = PixoMuted, fontSize = 13.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SuggestionChip(text: String, vm: PixoViewModel) {
    Surface(
        modifier = Modifier.clickable { vm.setPrompt(text) },
        shape = RoundedCornerShape(14.dp),
        color = PixoCard
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), color = PixoText, fontSize = 12.sp)
    }
}

private fun BrushColor(start: Color, end: Color): Color = start

private fun decodeBitmap(context: Context, uri: android.net.Uri): Bitmap? {
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    }
}

private fun saveBitmap(context: Context, bitmap: Bitmap) {
    if (Build.VERSION.SDK_INT <= 28 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
    ) {
        if (context is Activity) {
            androidx.core.app.ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1001
            )
            Toast.makeText(context, "Allow storage permission, then tap Save again.", Toast.LENGTH_LONG).show()
        }
        return
    }

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "Pixo_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= 29) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Pixo")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    if (uri == null) {
        Toast.makeText(context, "Could not save image", Toast.LENGTH_SHORT).show()
        return
    }

    runCatching {
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
        }
        if (Build.VERSION.SDK_INT >= 29) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
    }.onFailure {
        resolver.delete(uri, null, null)
        Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
    }
}
