package com.example.lab08

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colores parecidos a Microsoft To Do
private val AzulToDo = Color(0xFF2564CF)
private val AzulOscuro = Color(0xFF1A4DA6)

// La pantalla solo recibe el estado y avisa los eventos (UDF)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val state by viewModel.uiState.collectAsState()
    TaskContent(
        state = state,
        onAdd = viewModel::addTask,
        onToggle = viewModel::toggleTaskCompletion,
        onEdit = viewModel::startEditing,
        onDelete = viewModel::deleteTask,
        onDeleteAll = viewModel::deleteAllTasks,
        onSaveEdit = viewModel::saveEdit,
        onCancelEdit = viewModel::cancelEditing
    )
}

@Composable
fun TaskContent(
    state: TaskUiState,
    onAdd: (String) -> Unit,
    onToggle: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onDeleteAll: () -> Unit,
    onSaveEdit: (String) -> Unit,
    onCancelEdit: () -> Unit
) {
    var mostrarCompletadas by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AzulToDo, AzulOscuro)))
            .systemBarsPadding()
            .imePadding()
    ) {
        Encabezado(total = state.pendientes.size, onDeleteAll = onDeleteAll)

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (state.pendientes.isEmpty() && state.completadas.isEmpty()) {
                item {
                    Text(
                        "No tienes tareas pendientes. ¡Agrega una abajo!",
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            items(state.pendientes, key = { it.id }) { task ->
                TareaItem(task, state.tareaEditando?.id == task.id, onToggle, onEdit, onDelete, onSaveEdit, onCancelEdit)
            }
            if (state.completadas.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable { mostrarCompletadas = !mostrarCompletadas }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (mostrarCompletadas) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                            contentDescription = null, tint = Color.White
                        )
                        Text(
                            "Completadas  ${state.completadas.size}",
                            color = Color.White, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (mostrarCompletadas) {
                    items(state.completadas, key = { it.id }) { task ->
                        TareaItem(task, state.tareaEditando?.id == task.id, onToggle, onEdit, onDelete, onSaveEdit, onCancelEdit)
                    }
                }
            }
        }

        BarraAgregar(onAdd = onAdd)
    }

}

@Composable
private fun Encabezado(total: Int, onDeleteAll: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val fecha = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-PE")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Mis tareas", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(fecha, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp)
            Text("$total pendientes", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = Color.White)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Eliminar todas las tareas") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = { menu = false; onDeleteAll() }
                )
            }
        }
    }
}

@Composable
private fun TareaItem(
    task: Task,
    editando: Boolean,
    onToggle: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onSaveEdit: (String) -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable(enabled = !editando) { onEdit(task) }
    ) {
        if (editando) {
            // Ejercicio 1 - Editar: la fila se vuelve un campo de texto
            var texto by remember(task.id) { mutableStateOf(task.description) }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Editar tarea") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onSaveEdit(texto) }, enabled = texto.isNotBlank()) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar cambios", tint = AzulToDo)
                }
                IconButton(onClick = onCancelEdit) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar edición", tint = Color(0xFF605E5C))
                }
            }
            return@Card
        }
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circulito para marcar como completada
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .then(
                        if (task.isCompleted) Modifier.background(AzulToDo)
                        else Modifier.border(2.dp, Color(0xFF8A8886), CircleShape)
                    )
                    .clickable { onToggle(task) }
                    .semantics {
                        contentDescription = if (task.isCompleted) "Marcar como pendiente" else "Marcar como completada"
                    },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = task.description,
                fontSize = 16.sp,
                color = if (task.isCompleted) Color(0xFF8A8886) else Color(0xFF201F1E),
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp)
            )
            // Ejercicio 1 - Eliminar una sola tarea
            IconButton(onClick = { onDelete(task) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea", tint = Color(0xFFA4262C))
            }
        }
    }
}

@Composable
private fun BarraAgregar(onAdd: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E3F7A))
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onAdd(texto); texto = "" }) {
            Icon(Icons.Default.Add, contentDescription = "Agregar tarea", tint = Color.White)
        }
        TextField(
            value = texto,
            onValueChange = { texto = it },
            placeholder = { Text("Agregar una tarea", color = Color.White.copy(alpha = 0.8f)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
