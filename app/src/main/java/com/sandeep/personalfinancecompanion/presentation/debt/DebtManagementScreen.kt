package com.sandeep.personalfinancecompanion.presentation.debt

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.R
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntryType
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPerson
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPersonSummary
import com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed
import com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtManagementScreen(
    innerPadding: PaddingValues,
    onPersonClick: (String) -> Unit,
    viewModel: DebtManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEntryDialog by remember { mutableStateOf<EntryDialogState?>(null) }
    var showEditPersonDialog by remember { mutableStateOf<UdhaarPerson?>(null) }
    var pickedContact by remember { mutableStateOf<PickedContact?>(null) }

    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pickedContact = result.data?.data?.let { readPickedPhone(context, it) }
        }
    }

    fun openContactPicker() {
        val intent = Intent(
            Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        )
        contactLauncher.launch(intent)
    }

    showEntryDialog?.let { dialogState ->
        UdhaarEntryDialog(
            state = dialogState,
            currency = uiState.selectedCurrency,
            pickedContact = pickedContact,
            onContactConsumed = { pickedContact = null },
            onPickContact = { openContactPicker() },
            onDismiss = {
                pickedContact = null
                showEntryDialog = null
            },
            onSave = { personId, name, phone, amount, type, note, date ->
                viewModel.saveEntry(personId, name, phone, amount, type, note, date)
                pickedContact = null
                showEntryDialog = null
            }
        )
    }

    showEditPersonDialog?.let { person ->
        EditPersonDialog(
            person = person,
            pickedContact = pickedContact,
            onContactConsumed = { pickedContact = null },
            onPickContact = { openContactPicker() },
            onDismiss = {
                pickedContact = null
                showEditPersonDialog = null
            },
            onSave = { name, phone ->
                viewModel.updatePerson(person, name, phone)
                pickedContact = null
                showEditPersonDialog = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 8.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DebtSummaryCard(
                    title = stringResource(R.string.label_you_will_get),
                    amount = uiState.overview.totalReceivable,
                    currency = uiState.selectedCurrency,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                DebtSummaryCard(
                    title = stringResource(R.string.label_you_owe),
                    amount = uiState.overview.totalPayable,
                    currency = uiState.selectedCurrency,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    showEntryDialog = EntryDialogState(
                        initialType = UdhaarEntryType.GIVEN,
                        title = context.getString(R.string.title_add_udhaar_entry)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_add_udhaar_entry))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.label_people_ledgers),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.peerSummaries.isEmpty()) {
                DebtEmptyState(
                    onAdd = {
                        showEntryDialog = EntryDialogState(
                            initialType = UdhaarEntryType.GIVEN,
                            title = context.getString(R.string.title_add_udhaar_entry)
                        )
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = innerPadding.calculateBottomPadding() + 100.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.peerSummaries, key = { it.person.id }) { summary ->
                        PeerDebtCard(
                            summary = summary,
                            currency = uiState.selectedCurrency,
                            onCall = { dialPhone(context, summary.person.phoneNumber) },
                            onEditPerson = { showEditPersonDialog = summary.person },
                            onClick = { onPersonClick(summary.person.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtSummaryCard(
    title: String,
    amount: Double,
    currency: Currency,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.formatAmount(amount, currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun PeerDebtCard(
    summary: UdhaarPersonSummary,
    currency: Currency,
    onCall: () -> Unit,
    onEditPerson: () -> Unit,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val phone = summary.person.phoneNumber

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    summary.person.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    summary.person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (summary.netAmount == 0.0) {
                        stringResource(R.string.label_settled)
                    } else if (summary.isOwedToYou) {
                        stringResource(R.string.label_net_owed_to_you)
                    } else {
                        stringResource(R.string.label_net_you_owe)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = CurrencyFormatter.formatAmount(summary.netAmount, currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    summary.netAmount == 0.0 -> colorScheme.onSurfaceVariant
                    summary.isOwedToYou -> IncomeGreen
                    else -> ExpenseRed
                }
            )

            if (!phone.isNullOrBlank()) {
                IconButton(onClick = onCall) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = stringResource(R.string.cd_call_person),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(onClick = onEditPerson) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.btn_edit_person),
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UdhaarEntryDialog(
    state: EntryDialogState,
    currency: Currency,
    pickedContact: PickedContact?,
    onContactConsumed: () -> Unit,
    onPickContact: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String?, String, String?, Double, UdhaarEntryType, String, Long) -> Unit
) {
    var name by remember(state.personId) { mutableStateOf(state.personName) }
    var phone by remember(state.personId) { mutableStateOf(state.phoneNumber.orEmpty()) }
    var amount by remember(state.personId, state.initialType) { mutableStateOf("") }
    var type by remember(state.personId, state.initialType) { mutableStateOf(state.initialType) }
    var note by remember(state.personId, state.initialType) { mutableStateOf("") }
    var selectedDateTime by remember(state.personId, state.initialType) { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pickedContact) {
        pickedContact?.let {
            name = it.name.ifBlank { name }
            phone = it.phoneNumber
            onContactConsumed()
        }
    }

    val errorInvalidAmount = stringResource(R.string.error_invalid_amount)
    val errorAmountTooLarge = stringResource(R.string.error_amount_too_large)
    val errorPersonRequired = stringResource(R.string.error_person_required)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.label_person_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.label_phone_optional)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onPickContact) {
                        Icon(
                            Icons.Default.Contacts,
                            contentDescription = stringResource(R.string.cd_pick_contact)
                        )
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.length <= 13) {
                            amount = filtered
                            error = null
                        }
                    },
                    label = { Text(stringResource(R.string.label_amount, currency.symbol)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!state.isRepayment) {
                    UdhaarTypeChips(selected = type, onSelected = { type = it })
                }
                DateTimeSelector(
                    dateTime = selectedDateTime,
                    onDateTimeChanged = { selectedDateTime = it }
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.label_note)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amount.toDoubleOrNull()
                    when {
                        name.isBlank() -> error = errorPersonRequired
                        parsed == null || parsed <= 0.0 -> error = errorInvalidAmount
                        parsed > 1000000000.0 -> error = errorAmountTooLarge
                        else -> onSave(state.personId, name, phone, parsed, type, note, selectedDateTime)
                    }
                }
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun EditPersonDialog(
    person: UdhaarPerson,
    pickedContact: PickedContact?,
    onContactConsumed: () -> Unit,
    onPickContact: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var name by remember(person.id) { mutableStateOf(person.name) }
    var phone by remember(person.id) { mutableStateOf(person.phoneNumber.orEmpty()) }

    LaunchedEffect(pickedContact) {
        pickedContact?.let {
            name = it.name.ifBlank { name }
            phone = it.phoneNumber
            onContactConsumed()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.btn_edit_person)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_person_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.label_phone_optional)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onPickContact) {
                        Icon(
                            Icons.Default.Contacts,
                            contentDescription = stringResource(R.string.cd_pick_contact)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, phone) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
private fun UdhaarTypeChips(
    selected: UdhaarEntryType,
    onSelected: (UdhaarEntryType) -> Unit
) {
    val options = listOf(
        UdhaarEntryType.GIVEN to stringResource(R.string.label_udhar_diya),
        UdhaarEntryType.TAKEN to stringResource(R.string.label_udhar_liya)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (type, label) ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelected(type) },
                label = { Text(label, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
private fun DateTimeSelector(
    dateTime: Long,
    onDateTimeChanged: (Long) -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    OutlinedButton(
        onClick = {
            val calendar = Calendar.getInstance().apply { timeInMillis = dateTime }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selected = Calendar.getInstance().apply {
                        timeInMillis = dateTime
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            selected.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            selected.set(Calendar.MINUTE, minute)
                            selected.set(Calendar.SECOND, 0)
                            selected.set(Calendar.MILLISECOND, 0)
                            onDateTimeChanged(selected.timeInMillis)
                        },
                        selected.get(Calendar.HOUR_OF_DAY),
                        selected.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(stringResource(R.string.label_entry_date_time, formatter.format(Date(dateTime))))
    }
}

@Composable
fun DebtEmptyState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Handshake,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.msg_no_debts_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            stringResource(R.string.msg_no_debts_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAdd, shape = RoundedCornerShape(12.dp)) {
            Text(stringResource(R.string.btn_add_udhaar_entry))
        }
    }
}

data class EntryDialogState(
    val personId: String? = null,
    val personName: String = "",
    val phoneNumber: String? = null,
    val initialType: UdhaarEntryType,
    val title: String,
    val isRepayment: Boolean = false
)

data class PaymentDialogState(
    val summary: UdhaarPersonSummary,
    val entryType: UdhaarEntryType
) {
    val person: UdhaarPerson = summary.person
    val outstandingAmount: Double = summary.netAmount
    val isOwedToYou: Boolean = summary.isOwedToYou
}

data class PickedContact(
    val name: String,
    val phoneNumber: String
)

private fun readPickedPhone(context: Context, uri: Uri): PickedContact? {
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val name = if (nameIndex >= 0) cursor.getString(nameIndex).orEmpty() else ""
        val number = if (numberIndex >= 0) cursor.getString(numberIndex).orEmpty() else ""
        PickedContact(name = name, phoneNumber = number)
    }
}

private fun dialPhone(context: Context, phoneNumber: String?) {
    val cleaned = phoneNumber?.takeIf { it.isNotBlank() } ?: return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(cleaned)}"))
    context.startActivity(intent)
}
