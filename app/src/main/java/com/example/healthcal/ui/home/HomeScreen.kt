package com.example.healthcal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healthcal.data.model.FoodLog

private val Page = androidx.compose.ui.graphics.Color(0xFFFAF9FC)
private val Violet = androidx.compose.ui.graphics.Color(0xFF7C3AED)
private val Lilac = androidx.compose.ui.graphics.Color(0xFFF3E8FF)
private val Ink = androidx.compose.ui.graphics.Color(0xFF1F1B2D)
private val Muted = androidx.compose.ui.graphics.Color(0xFF8A8496)

@Composable
fun HomeScreen(email: String?, onSignOut: () -> Unit, viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    val tabs = listOf("Beranda", "Makanan", "Minum", "Aktivitas", "Artikel")
    LaunchedEffect(state.saved) {
        if (state.saved) {
            showAdd = false
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        containerColor = Page,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Text(listOf("⌂", "◉", "◒", "↗", "▤")[index], style = MaterialTheme.typography.titleLarge) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Violet, selectedTextColor = Violet, indicatorColor = Lilac, unselectedIconColor = Muted, unselectedTextColor = Muted)
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == 0 || tab == 1) FloatingActionButton(onClick = { viewModel.clearFeedback(); showAdd = true }, containerColor = Violet, contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(20.dp)) { Text("+", style = MaterialTheme.typography.headlineMedium) }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(if (tab == 1) "Catatan makanan" else if (tab == 0) "NutriTrack" else tabs[tab], style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
                        Text(if (tab == 1) "Catat dan pantau asupanmu" else "Halo${email?.substringBefore('@')?.let { ", $it" } ?: ""} 👋", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = onSignOut) { Text("Keluar", color = Violet) }
                }
            }
            if (tab == 0 || tab == 1) {
                if (tab == 0) {
                    item {
                        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Violet), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(22.dp)) {
                                Text("KALORI HARI INI", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .78f))
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("${state.calories}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold)
                                    Text("  /  2.000 kkal", modifier = Modifier.padding(bottom = 6.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f))
                                }
                                Spacer(Modifier.height(12.dp))
                                LinearProgressIndicator(progress = { (state.calories / 2000f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp), color = MaterialTheme.colorScheme.onPrimary, trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .25f))
                                Spacer(Modifier.height(8.dp))
                                Text("${(2000 - state.calories).coerceAtLeast(0)} kkal tersisa dari target harian", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .88f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard("Air minum", "—", "Tracker belum terhubung", Modifier.weight(1f), "💧")
                            MetricCard("Aktivitas", "—", "Tracker belum terhubung", Modifier.weight(1f), "🏃")
                        }
                    }
                    item { SectionTitle("Makanan hari ini", onClick = { tab = 1 }) }
                } else {
                    item { SectionTitle("Asupan hari ini", onClick = { showAdd = true }) }
                }
                when {
                    state.loading -> item { Box(Modifier.fillMaxWidth().padding(28.dp), Alignment.Center) { CircularProgressIndicator(color = Violet) } }
                    state.error != null -> item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Column(Modifier.padding(16.dp)) { Text(state.error ?: "", color = MaterialTheme.colorScheme.onErrorContainer); TextButton(onClick = viewModel::refresh) { Text("Coba lagi") } }
                        }
                    }
                    state.foods.isEmpty() -> item { EmptyFoodCard(onAdd = { showAdd = true }) }
                    else -> items(state.foods, key = { it.id }) { food -> FoodRow(food) }
                }
            } else {
                item { EmptyFeatureCard(tabs[tab]) }
            }
        }
    }
    if (showAdd) AddFoodDialog(
        saving = state.saving,
        error = state.error,
        onDismiss = { showAdd = false; viewModel.clearFeedback() },
        onSave = viewModel::addFood
    )
}

@Composable
private fun MetricCard(title: String, value: String, note: String, modifier: Modifier, icon: String) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(title, color = Muted, style = MaterialTheme.typography.labelMedium)
            Text(value, color = Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(note, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionTitle(title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onClick) { Text("Lihat semua", color = Violet) }
    }
}

@Composable
private fun EmptyFoodCard(onAdd: () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(52.dp).background(Lilac, CircleShape), Alignment.Center) { Text("🍽", style = MaterialTheme.typography.titleLarge) }
            Spacer(Modifier.height(12.dp)); Text("Belum ada makanan", color = Ink, fontWeight = FontWeight.SemiBold)
            Text("Mulai catat asupanmu hari ini", color = Muted, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onAdd) { Text("Tambah makanan", color = Violet) }
        }
    }
}

@Composable
private fun FoodRow(food: FoodLog) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(food.foodName, color = Ink, fontWeight = FontWeight.SemiBold)
                Text("${food.kategori}  ·  ${food.portion ?: 1.0} porsi", color = Muted, style = MaterialTheme.typography.bodySmall)
                val macros = listOfNotNull(food.protein?.let { "P ${it.toInt()}g" }, food.karbohidrat?.let { "K ${it.toInt()}g" }, food.lemak?.let { "L ${it.toInt()}g" }).joinToString("   ")
                if (macros.isNotBlank()) Text(macros, color = Violet, style = MaterialTheme.typography.labelSmall)
            }
            Text("${food.kalori.toInt()} kkal", color = Violet, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyFeatureCard(name: String) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Segera hadir", color = Violet, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Fitur $name sedang disiapkan.", color = Muted)
        }
    }
}

@Composable
private fun AddFoodDialog(saving: Boolean, error: String?, onDismiss: () -> Unit, onSave: (String, String, Double, String, Double?, Double?, Double?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var meal by remember { mutableStateOf("Sarapan") }
    var calories by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("1") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val caloriesValue = calories.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Tambah makanan", color = Ink, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.foundation.rememberScrollState().let { scroll -> Column(Modifier.weight(1f, fill = false).padding(end = 2.dp).verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Field("Nama makanan", name, { name = it })
                    Text("Waktu makan", color = Muted, style = MaterialTheme.typography.labelMedium)
                    Row(Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Sarapan", "Makan Siang", "Makan Malam", "Camilan").forEach { option ->
                        FilterChip(selected = meal == option, onClick = { meal = option }, label = { Text(option, maxLines = 1) })
                    } }
                    Field("Kalori (kkal)", calories, { calories = it }, numeric = true)
                    Field("Jumlah porsi", portion, { portion = it }, numeric = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field("Karbo (g)", carbs, { carbs = it }, numeric = true, modifier = Modifier.weight(1f))
                        Field("Protein (g)", protein, { protein = it }, numeric = true, modifier = Modifier.weight(1f))
                        Field("Lemak (g)", fat, { fat = it }, numeric = true, modifier = Modifier.weight(1f))
                    }
                    if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name.trim(), meal, caloriesValue ?: 0.0, portion, carbs.toDoubleOrNull(), protein.toDoubleOrNull(), fat.toDoubleOrNull()) }, enabled = !saving && name.isNotBlank() && caloriesValue != null && caloriesValue >= 0) {
                if (saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Batal") } },
        shape = RoundedCornerShape(28.dp), containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, numeric: Boolean = false, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(value, onChange, modifier = modifier, label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text))
}
