package com.example.photogallery

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

private val Ink = Color(0xFF171923)
private val Muted = Color(0xFF85899A)
private val Accent = Color(0xFF6750A4)
private val Canvas = Color(0xFFF7F6FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GalleryApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryApp() {
    var photos by rememberSaveable { mutableStateOf(listOf<String>()) }
    var favorites by rememberSaveable { mutableStateOf(listOf<String>()) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var openedPhoto by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(100)
    ) { picked ->
        val newUris = picked.map { it.toString() }
        photos = (photos + newUris).distinct()
    }

    val visiblePhotos = if (selectedTab == 1) photos.filter { it in favorites } else photos

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Accent,
            background = Canvas,
            surface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Canvas,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Моя галерея", fontWeight = FontWeight.Bold, color = Ink)
                            Text(
                                if (selectedTab == 1) "Избранное" else "${photos.size} фото",
                                fontSize = 12.sp,
                                color = Muted
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить фото", tint = Accent)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Canvas)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Collections, contentDescription = null) },
                        label = { Text("Все фото") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text("Избранное") }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    containerColor = Accent,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Add, contentDescription = "Добавить фото") }
            }
        ) { padding ->
            if (visiblePhotos.isEmpty()) {
                EmptyGallery(
                    isFavorites = selectedTab == 1,
                    modifier = Modifier.padding(padding),
                    onAdd = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = 3.dp, end = 3.dp, top = 6.dp, bottom = 96.dp),
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    items(visiblePhotos, key = { it }) { uri ->
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE8E5EF))
                                .clickable { openedPhoto = uri }
                        ) {
                            AsyncImage(
                                model = Uri.parse(uri),
                                contentDescription = "Фотография",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (uri in favorites) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "В избранном",
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(7.dp).size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        openedPhoto?.let { uri ->
            Dialog(onDismissRequest = { openedPhoto = null }) {
                Surface(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.86f),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF101014)
                ) {
                    Box {
                        AsyncImage(
                            model = Uri.parse(uri),
                            contentDescription = "Просмотр фотографии",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { openedPhoto = null },
                            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                        }
                        IconButton(
                            onClick = {
                                favorites = if (uri in favorites) favorites - uri else favorites + uri
                            },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(
                                if (uri in favorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Избранное",
                                tint = if (uri in favorites) Color(0xFFFF6B83) else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGallery(isFavorites: Boolean, modifier: Modifier = Modifier, onAdd: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (isFavorites) Icons.Default.FavoriteBorder else Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = Color(0xFFB6AEC9),
            modifier = Modifier.size(68.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (isFavorites) "Пока нет избранного" else "В галерее пока пусто",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isFavorites) "Отмечайте сердечком фотографии, которые хотите сохранить здесь"
            else "Добавьте фотографии с устройства, чтобы увидеть их здесь",
            color = Muted,
            fontSize = 14.sp
        )
        if (!isFavorites) {
            Spacer(Modifier.height(22.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Выбрать фотографии")
            }
        }
    }
}
