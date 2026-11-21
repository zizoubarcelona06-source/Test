package com.example.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.data.local.entity.BtpConstants
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.SupplierEntity
import com.example.ui.components.BtpDropdownField
import com.example.ui.components.BtpInputField

@Composable
fun ExpenseFormDialog(
    initial: ExpenseEntity?,
    projectNames: List<String>,
    onSave: (ExpenseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: BtpConstants.expenseCategories.first()) }
    var amount by remember { mutableStateOf(initial?.amount?.toLong()?.toString() ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: Formatters.todayDate()) }
    var projectName by remember { mutableStateOf(initial?.projectName ?: projectNames.firstOrNull().orEmpty()) }
    var paymentMethod by remember { mutableStateOf(initial?.paymentMethod ?: BtpConstants.paymentMethods.first()) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newExpenseTitle else strings.editExpenseTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BtpInputField(value = title, onValueChange = { title = it }, label = strings.expenseDescLabel)
                BtpDropdownField(
                    label = strings.categoryLabel,
                    selectedValue = category,
                    options = BtpConstants.expenseCategories,
                    onOptionSelected = { category = it }
                )
                BtpInputField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = strings.amountLabel,
                    keyboardType = KeyboardType.Decimal
                )
                if (projectNames.isNotEmpty()) {
                    BtpDropdownField(
                        label = strings.projectLabel,
                        selectedValue = projectName,
                        options = projectNames,
                        onOptionSelected = { projectName = it }
                    )
                } else {
                    BtpInputField(value = projectName, onValueChange = { projectName = it }, label = strings.projectLabel)
                }
                BtpDropdownField(
                    label = strings.paymentMethodLabel,
                    selectedValue = paymentMethod,
                    options = BtpConstants.paymentMethods,
                    onOptionSelected = { paymentMethod = it }
                )
                BtpInputField(value = date, onValueChange = { date = it }, label = strings.dateLabel)
                BtpInputField(value = notes, onValueChange = { notes = it }, label = strings.notesLabel, singleLine = false)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (title.isNotBlank() && amt != null && amt > 0) {
                        onSave(
                            ExpenseEntity(
                                id = initial?.id ?: 0L,
                                title = title.trim(),
                                category = category,
                                amount = amt,
                                date = date,
                                projectName = projectName.ifBlank { "-" },
                                paymentMethod = paymentMethod,
                                notes = notes.trim()
                            )
                        )
                    }
                }
            ) { Text(strings.save) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun InvoiceFormDialog(
    initial: InvoiceEntity?,
    projectNames: List<String>,
    onSave: (InvoiceEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var invoiceNumber by remember { mutableStateOf(initial?.invoiceNumber ?: "INV-2026-${(200..999).random()}") }
    var type by remember { mutableStateOf(initial?.type ?: BtpConstants.invoiceTypes.first()) }
    var partyName by remember { mutableStateOf(initial?.partyName ?: "") }
    var projectName by remember { mutableStateOf(initial?.projectName ?: projectNames.firstOrNull().orEmpty()) }
    var totalAmount by remember { mutableStateOf(initial?.totalAmount?.toLong()?.toString() ?: "") }
    var paidAmount by remember { mutableStateOf(initial?.paidAmount?.toLong()?.toString() ?: "0") }
    var issueDate by remember { mutableStateOf(initial?.issueDate ?: Formatters.todayDate()) }
    var dueDate by remember { mutableStateOf(initial?.dueDate ?: Formatters.todayDate()) }
    var status by remember { mutableStateOf(initial?.status ?: "غير مدفوعة") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newInvoiceTitle else strings.editInvoiceTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BtpInputField(value = invoiceNumber, onValueChange = { invoiceNumber = it }, label = strings.invoiceNumberLabel)
                BtpDropdownField(
                    label = strings.invoiceTypeLabel,
                    selectedValue = type,
                    options = BtpConstants.invoiceTypes,
                    onOptionSelected = { type = it }
                )
                BtpInputField(value = partyName, onValueChange = { partyName = it }, label = strings.partyNameLabel)
                if (projectNames.isNotEmpty()) {
                    BtpDropdownField(
                        label = strings.projectLabel,
                        selectedValue = projectName,
                        options = projectNames,
                        onOptionSelected = { projectName = it }
                    )
                } else {
                    BtpInputField(value = projectName, onValueChange = { projectName = it }, label = strings.projectLabel)
                }
                BtpInputField(value = totalAmount, onValueChange = { totalAmount = it }, label = strings.amountLabel, keyboardType = KeyboardType.Decimal)
                BtpInputField(value = paidAmount, onValueChange = { paidAmount = it }, label = strings.paidAmountLabel, keyboardType = KeyboardType.Decimal)
                BtpDropdownField(
                    label = strings.statusLabel,
                    selectedValue = status,
                    options = BtpConstants.invoiceStatuses,
                    onOptionSelected = { status = it }
                )
                BtpInputField(value = issueDate, onValueChange = { issueDate = it }, label = strings.issueDateLabel)
                BtpInputField(value = dueDate, onValueChange = { dueDate = it }, label = strings.dueDateLabel)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = totalAmount.toDoubleOrNull() ?: 0.0
                    val paid = (paidAmount.toDoubleOrNull() ?: 0.0).coerceIn(0.0, total)
                    if (partyName.isNotBlank() && total > 0) {
                        val derivedStatus = when {
                            paid >= total -> "مدفوعة"
                            status == "متأخرة" -> "متأخرة"
                            paid > 0.0 -> "مدفوعة جزئياً"
                            else -> "غير مدفوعة"
                        }
                        onSave(
                            InvoiceEntity(
                                id = initial?.id ?: 0L,
                                invoiceNumber = invoiceNumber.ifBlank { "INV-01" },
                                type = type,
                                partyName = partyName.trim(),
                                projectName = projectName.ifBlank { "-" },
                                totalAmount = total,
                                paidAmount = paid,
                                issueDate = issueDate,
                                dueDate = dueDate,
                                status = derivedStatus
                            )
                        )
                    }
                }
            ) { Text(strings.saveInvoice) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    invoice: InvoiceEntity,
    currency: String,
    onConfirmPayment: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var paymentText by remember { mutableStateOf(invoice.remainingAmount.toLong().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.recordPaymentTitle, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${invoice.invoiceNumber} (${invoice.partyName})",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${strings.remainingBalanceLabel} ${Formatters.formatMoney(invoice.remainingAmount, currency)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                BtpInputField(
                    value = paymentText,
                    onValueChange = { paymentText = it },
                    label = strings.newPaymentAmountLabel,
                    keyboardType = KeyboardType.Decimal
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = paymentText.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        onConfirmPayment(amt)
                    }
                }
            ) { Text(strings.confirmPayment) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun ClientFormDialog(
    initial: ClientEntity?,
    onSave: (ClientEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var company by remember { mutableStateOf(initial?.companyName ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var taxId by remember { mutableStateOf(initial?.taxId ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newClientTitle else strings.editClientTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BtpInputField(value = name, onValueChange = { name = it }, label = strings.clientEntityNameLabel)
                BtpInputField(value = company, onValueChange = { company = it }, label = strings.companyOrgLabel)
                BtpInputField(value = phone, onValueChange = { phone = it }, label = strings.phoneLabel, keyboardType = KeyboardType.Phone)
                BtpInputField(value = email, onValueChange = { email = it }, label = strings.emailLabel, keyboardType = KeyboardType.Email)
                BtpInputField(value = address, onValueChange = { address = it }, label = strings.addressLabel)
                BtpInputField(value = taxId, onValueChange = { taxId = it }, label = strings.taxIdLabel)
                BtpInputField(value = notes, onValueChange = { notes = it }, label = strings.notesLabel, singleLine = false)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            ClientEntity(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                companyName = company.ifBlank { name.trim() },
                                phone = phone.ifBlank { "-" },
                                email = email.ifBlank { "-" },
                                address = address.ifBlank { "-" },
                                taxId = taxId.ifBlank { "-" },
                                notes = notes.trim()
                            )
                        )
                    }
                }
            ) { Text(strings.saveClient) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun SupplierFormDialog(
    initial: SupplierEntity?,
    onSave: (SupplierEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: BtpConstants.supplierCategories.first()) }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var balance by remember { mutableStateOf(initial?.balanceOwed?.toLong()?.toString() ?: "0") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newSupplierTitle else strings.editSupplierTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BtpInputField(value = name, onValueChange = { name = it }, label = strings.supplierNameLabel)
                BtpDropdownField(
                    label = strings.supplyFieldLabel,
                    selectedValue = category,
                    options = BtpConstants.supplierCategories,
                    onOptionSelected = { category = it }
                )
                BtpInputField(value = phone, onValueChange = { phone = it }, label = strings.phoneLabel, keyboardType = KeyboardType.Phone)
                BtpInputField(value = email, onValueChange = { email = it }, label = strings.emailLabel, keyboardType = KeyboardType.Email)
                BtpInputField(value = address, onValueChange = { address = it }, label = strings.addressLabel)
                BtpInputField(value = balance, onValueChange = { balance = it }, label = strings.balanceOwedLabel, keyboardType = KeyboardType.Decimal)
                BtpInputField(value = notes, onValueChange = { notes = it }, label = strings.notesLabel, singleLine = false)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            SupplierEntity(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                category = category,
                                phone = phone.ifBlank { "-" },
                                email = email.ifBlank { "-" },
                                address = address.ifBlank { "-" },
                                balanceOwed = balance.toDoubleOrNull() ?: 0.0,
                                notes = notes.trim()
                            )
                        )
                    }
                }
            ) { Text(strings.saveSupplier) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
