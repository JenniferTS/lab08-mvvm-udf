package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Todo lo que la pantalla necesita para dibujarse (el estado baja a la UI)
data class TaskUiState(
    val pendientes: List<Task> = emptyList(),
    val completadas: List<Task> = emptyList(),
    val tareaEditando: Task? = null
)

class TaskViewModel(private val dao: TaskDao) : ViewModel() {

    private val tareaEditando = MutableStateFlow<Task?>(null)

    // La lista viene de Room como Flow, asi que ya no hay que recargarla a mano
    val uiState: StateFlow<TaskUiState> =
        combine(dao.observeTasks(), tareaEditando) { tareas, editando ->
            TaskUiState(
                pendientes = tareas.filter { !it.isCompleted },
                completadas = tareas.filter { it.isCompleted },
                tareaEditando = editando
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskUiState())

    // Eventos que suben desde la UI

    fun addTask(description: String) {
        if (description.isBlank()) return
        viewModelScope.launch { dao.insertTask(Task(description = description.trim())) }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch { dao.updateTask(task.copy(isCompleted = !task.isCompleted)) }
    }

    // Ejercicio 1 - Editar tareas
    fun startEditing(task: Task) { tareaEditando.value = task }

    fun cancelEditing() { tareaEditando.value = null }

    fun saveEdit(newDescription: String) {
        val task = tareaEditando.value ?: return
        if (newDescription.isBlank()) return
        viewModelScope.launch { dao.updateTask(task.copy(description = newDescription.trim())) }
        tareaEditando.value = null
    }

    // Ejercicio 1 - Eliminar una tarea
    fun deleteTask(task: Task) {
        viewModelScope.launch { dao.deleteTask(task) }
    }

    fun deleteAllTasks() {
        viewModelScope.launch { dao.deleteAllTasks() }
    }

    companion object {
        // Para que el ViewModel sobreviva a las recomposiciones y giros de pantalla
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                TaskViewModel(TaskDatabase.getDatabase(app).taskDao())
            }
        }
    }
}
