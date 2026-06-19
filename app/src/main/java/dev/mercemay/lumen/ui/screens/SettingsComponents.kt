package dev.mercemay.lumen.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, { onValueChange(it.filter(Char::isDigit).ifEmpty { "0" }) }, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
}

@Composable
fun DecimalField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, { text -> onValueChange(text.filter { it.isDigit() || it == '.' }) }, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun <T : Enum<T>> EnumRow(label: String, entries: List<T>, selected: T, onSelected: (T) -> Unit) {
    SingleChoiceRow(label, entries.map { it.name }, selected.name) { value ->
        entries.firstOrNull { it.name == value }?.let(onSelected)
    }
}

@Composable
fun SingleChoiceRow(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label)
        options.forEach { option ->
            if (option == selected) {
                Button(onClick = { onSelected(option) }, modifier = Modifier.fillMaxWidth()) { Text(option) }
            } else {
                OutlinedButton(onClick = { onSelected(option) }, modifier = Modifier.fillMaxWidth()) { Text(option) }
            }
        }
    }
}

@Composable
fun MultiChoiceRow(label: String, options: List<String>, selected: Set<String>, onSelected: (Set<String>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label)
        options.forEach { option ->
            val checked = option in selected
            val onClick = {
                onSelected(if (checked) selected - option else selected + option)
            }
            if (checked) {
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(option) }
            } else {
                OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(option) }
            }
        }
    }
}
