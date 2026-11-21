package com.example.ui.screens.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.data.local.entity.BtpConstants
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.SupplierEntity
import com.example.ui.components.BtpBrandEmblem
import com.example.ui.components.BtpSegmentedTabs
import com.example.ui.components.BtpUnifiedModuleIcon
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ItemActionsRow
import com.example.ui.components.SearchAndFilterRow
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavyPrimaryDark
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.viewmodel.FinanceUiState

@Composable
fun FinanceHubScreen(
    uiState: FinanceUiState,
    projectNames: List<String>,
    selectedSubTab: Int,
    onSubTabChange: (Int) -> Unit,
    currency: String,
    onSearchChange: (String) -> Unit,
    onExpenseCategoryChange: (String) -> Unit,
    onSaveExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onSaveInvoice: (InvoiceEntity) -> Unit,
    onRecordPayment: (InvoiceEntity, Double) -> Unit,
    onDeleteInvoice: (InvoiceEntity) -> Unit,
    onSaveClient: (ClientEntity) -> Unit,
    onDeleteClient: (ClientEntity) -> Unit,
    onSaveSupplier: (SupplierEntity) -> Unit,
    onDeleteSupplier: (SupplierEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    var partnerTypeTab by remember { mutableIntStateOf(0) } // 0 = Clients, 1 = Suppliers

    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var deletingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    var editingInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var payingInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }
    var deletingInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }

    var editingClient by remember { mutableStateOf<ClientEntity?>(null) }
    var showClientDialog by remember { mutableStateOf(false) }
    var deletingClient by remember { mutableStateOf<ClientEntity?>(null) }

    var editingSupplier by remember { mutableStateOf<SupplierEntity?>(null) }
    var showSupplierDialog by remember { mutableStateOf(false) }
    var deletingSupplier by remember { mutableStateOf<SupplierEntity?>(null) }

    val activeCount = when (selectedSubTab) {
        0 -> uiState.expenses.size
        1 -> uiState.invoices.size
        else -> if (partnerTypeTab == 0) uiState.clients.size else uiState.suppliers.size
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedSubTab) {
                        0 -> {
                            editingExpense = null
                            showExpenseDialog = true
                        }
                        1 -> {
                            editingInvoice = null
                            showInvoiceDialog = true
                        }
                        else -> {
                            if (partnerTypeTab == 0) {
                                editingClient = null
                                showClientDialog = true
                            } else {
                                editingSupplier = null
                                showSupplierDialog = true
                            }
                        }
                    }
                },
                containerColor = AmberSecondary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("finance_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.addNew)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.financeHubTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = AmberSecondary.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = "$activeCount",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = AmberSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Executive Navy Financial Summary Strip
                Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.35f)),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(NavyPrimaryDark, NavyPrimary)))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(AmberSecondary)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.collectedLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = Formatters.formatMoney(uiState.totalCollectedFromClients, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(Color.White.copy(alpha = 0.16f))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.expensesLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = Formatters.formatMoney(uiState.totalExpenses, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(Color.White.copy(alpha = 0.16f))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.supplierPayablesLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = Formatters.formatMoney(uiState.totalSupplierPayables, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            BtpSegmentedTabs(
                tabs = listOf(strings.tabExpenses, strings.tabInvoices, strings.tabPartners),
                selectedIndex = selectedSubTab,
                onSelect = onSubTabChange
            )

            SearchAndFilterRow(
                query = uiState.searchQuery,
                onQueryChange = onSearchChange,
                placeholder = when (selectedSubTab) {
                    0 -> strings.searchExpensesHint
                    1 -> strings.searchInvoicesHint
                    else -> strings.searchPartnersHint
                },
                filters = if (selectedSubTab == 0) listOf("الكل") + BtpConstants.expenseCategories else emptyList(),
                selectedFilter = uiState.expenseCategoryFilter,
                onFilterSelect = onExpenseCategoryChange
            )

            if (selectedSubTab == 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = partnerTypeTab == 0,
                        onClick = { partnerTypeTab = 0 },
                        label = { Text("${strings.clientsLabel} (${uiState.clients.size})") },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberSecondary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = partnerTypeTab == 1,
                        onClick = { partnerTypeTab = 1 },
                        label = { Text("${strings.suppliersLabel} (${uiState.suppliers.size})") },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberSecondary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val twoColumns = maxWidth >= 680.dp

                when (selectedSubTab) {
                    0 -> {
                        if (uiState.expenses.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Outlined.AccountBalanceWallet,
                                title = strings.emptyExpensesTitle,
                                subtitle = strings.emptyExpensesSub,
                                actionLabel = strings.addExpense,
                                onAction = {
                                    editingExpense = null
                                    showExpenseDialog = true
                                }
                            )
                        } else if (twoColumns) {
                            val rows = uiState.expenses.chunked(2)
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { exp ->
                                            ExpenseCard(
                                                expense = exp,
                                                currency = currency,
                                                onEdit = {
                                                    editingExpense = exp
                                                    showExpenseDialog = true
                                                },
                                                onDelete = { deletingExpense = exp },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.expenses, key = { it.id }) { exp ->
                                    ExpenseCard(
                                        expense = exp,
                                        currency = currency,
                                        onEdit = {
                                            editingExpense = exp
                                            showExpenseDialog = true
                                        },
                                        onDelete = { deletingExpense = exp }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (uiState.invoices.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                                title = strings.emptyInvoicesTitle,
                                subtitle = strings.emptyInvoicesSub,
                                actionLabel = strings.addInvoice,
                                onAction = {
                                    editingInvoice = null
                                    showInvoiceDialog = true
                                }
                            )
                        } else if (twoColumns) {
                            val rows = uiState.invoices.chunked(2)
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { inv ->
                                            InvoiceCard(
                                                invoice = inv,
                                                currency = currency,
                                                onRecordPayment = { payingInvoice = inv },
                                                onEdit = {
                                                    editingInvoice = inv
                                                    showInvoiceDialog = true
                                                },
                                                onDelete = { deletingInvoice = inv },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.invoices, key = { it.id }) { inv ->
                                    InvoiceCard(
                                        invoice = inv,
                                        currency = currency,
                                        onRecordPayment = { payingInvoice = inv },
                                        onEdit = {
                                            editingInvoice = inv
                                            showInvoiceDialog = true
                                        },
                                        onDelete = { deletingInvoice = inv }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        if (partnerTypeTab == 0) {
                            if (uiState.clients.isEmpty()) {
                                EmptyStateView(
                                    icon = Icons.Outlined.Storefront,
                                    title = strings.emptyClientsTitle,
                                    subtitle = strings.emptyClientsSub,
                                    actionLabel = strings.addClient,
                                    onAction = {
                                        editingClient = null
                                        showClientDialog = true
                                    }
                                )
                            } else if (twoColumns) {
                                val rows = uiState.clients.chunked(2)
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 84.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(rows) { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            rowItems.forEach { client ->
                                                ClientCard(
                                                    client = client,
                                                    onEdit = {
                                                        editingClient = client
                                                        showClientDialog = true
                                                    },
                                                    onDelete = { deletingClient = client },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 84.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(uiState.clients, key = { it.id }) { client ->
                                        ClientCard(
                                            client = client,
                                            onEdit = {
                                                editingClient = client
                                                showClientDialog = true
                                            },
                                            onDelete = { deletingClient = client }
                                        )
                                    }
                                }
                            }
                        } else {
                            if (uiState.suppliers.isEmpty()) {
                                EmptyStateView(
                                    icon = Icons.Outlined.LocalShipping,
                                    title = strings.emptySuppliersTitle,
                                    subtitle = strings.emptySuppliersSub,
                                    actionLabel = strings.addSupplier,
                                    onAction = {
                                        editingSupplier = null
                                        showSupplierDialog = true
                                    }
                                )
                            } else if (twoColumns) {
                                val rows = uiState.suppliers.chunked(2)
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 84.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(rows) { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            rowItems.forEach { supplier ->
                                                SupplierCard(
                                                    supplier = supplier,
                                                    currency = currency,
                                                    onEdit = {
                                                        editingSupplier = supplier
                                                        showSupplierDialog = true
                                                    },
                                                    onDelete = { deletingSupplier = supplier },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 84.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(uiState.suppliers, key = { it.id }) { supplier ->
                                        SupplierCard(
                                            supplier = supplier,
                                            currency = currency,
                                            onEdit = {
                                                editingSupplier = supplier
                                                showSupplierDialog = true
                                            },
                                            onDelete = { deletingSupplier = supplier }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    if (showExpenseDialog) {
        ExpenseFormDialog(
            initial = editingExpense,
            projectNames = projectNames,
            onSave = {
                onSaveExpense(it)
                showExpenseDialog = false
            },
            onDismiss = { showExpenseDialog = false }
        )
    }

    if (showInvoiceDialog) {
        InvoiceFormDialog(
            initial = editingInvoice,
            projectNames = projectNames,
            onSave = {
                onSaveInvoice(it)
                showInvoiceDialog = false
            },
            onDismiss = { showInvoiceDialog = false }
        )
    }

    payingInvoice?.let { inv ->
        RecordPaymentDialog(
            invoice = inv,
            currency = currency,
            onConfirmPayment = { amt ->
                onRecordPayment(inv, amt)
                payingInvoice = null
            },
            onDismiss = { payingInvoice = null }
        )
    }

    if (showClientDialog) {
        ClientFormDialog(
            initial = editingClient,
            onSave = {
                onSaveClient(it)
                showClientDialog = false
            },
            onDismiss = { showClientDialog = false }
        )
    }

    if (showSupplierDialog) {
        SupplierFormDialog(
            initial = editingSupplier,
            onSave = {
                onSaveSupplier(it)
                showSupplierDialog = false
            },
            onDismiss = { showSupplierDialog = false }
        )
    }

    deletingExpense?.let { e ->
        ConfirmDeleteDialog(
            title = strings.deleteExpenseTitle,
            message = "${strings.confirmDeletePrefix} \"${e.title}\"؟",
            onConfirm = {
                onDeleteExpense(e)
                deletingExpense = null
            },
            onDismiss = { deletingExpense = null }
        )
    }

    deletingInvoice?.let { inv ->
        ConfirmDeleteDialog(
            title = strings.deleteInvoiceTitle,
            message = "${strings.confirmDeletePrefix} ${inv.invoiceNumber}؟",
            onConfirm = {
                onDeleteInvoice(inv)
                deletingInvoice = null
            },
            onDismiss = { deletingInvoice = null }
        )
    }

    deletingClient?.let { c ->
        ConfirmDeleteDialog(
            title = strings.deleteClientTitle,
            message = "${strings.confirmDeletePrefix} \"${c.name}\"؟",
            onConfirm = {
                onDeleteClient(c)
                deletingClient = null
            },
            onDismiss = { deletingClient = null }
        )
    }

    deletingSupplier?.let { s ->
        ConfirmDeleteDialog(
            title = strings.deleteSupplierTitle,
            message = "${strings.confirmDeletePrefix} \"${s.name}\"؟",
            onConfirm = {
                onDeleteSupplier(s)
                deletingSupplier = null
            },
            onDismiss = { deletingSupplier = null }
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: ExpenseEntity,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BtpUnifiedModuleIcon(
                        icon = Icons.Outlined.Payments,
                        isAccentOrange = false,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${expense.projectName} • ${expense.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Formatters.formatMoney(expense.amount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StatusDanger
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(label = expense.category)
                    StatusChip(label = expense.paymentMethod)
                }
                ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun InvoiceCard(
    invoice: InvoiceEntity,
    currency: String,
    onRecordPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val ratio = if (invoice.totalAmount > 0) (invoice.paidAmount / invoice.totalAmount).toFloat().coerceIn(0f, 1f) else 0f
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BtpUnifiedModuleIcon(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        isAccentOrange = true,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${invoice.invoiceNumber} • ${strings.translateTerm(invoice.type)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = invoice.partyName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = invoice.projectName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip(label = invoice.status)
            }

            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = StatusSuccess,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${strings.totalAmountLabel} ${Formatters.formatMoney(invoice.totalAmount, currency)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${strings.paidAmountLabel} ${Formatters.formatMoney(invoice.paidAmount, currency)} • ${strings.dueDateLabel} ${invoice.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (invoice.remainingAmount > 0) {
                        OutlinedButton(
                            onClick = onRecordPayment,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.65f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = AmberSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.paymentBtn, style = MaterialTheme.typography.labelMedium, color = AmberSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                    ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
                }
            }
        }
    }
}

@Composable
private fun ClientCard(
    client: ClientEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BtpUnifiedModuleIcon(
                        icon = Icons.Outlined.BusinessCenter,
                        isAccentOrange = false,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(client.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(client.companyName, style = MaterialTheme.typography.bodySmall, color = AmberSecondary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
            }
            Text("${strings.phonePrefix} ${client.phone} • ${client.email}", style = MaterialTheme.typography.bodySmall)
            Text("${strings.addressPrefix} ${client.address} • NIF: ${client.taxId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SupplierCard(
    supplier: SupplierEntity,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BtpUnifiedModuleIcon(
                        icon = Icons.Outlined.LocalShipping,
                        isAccentOrange = true,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(supplier.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        StatusChip(label = supplier.category)
                    }
                }
                ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
            }
            Text("${strings.phonePrefix} ${supplier.phone} • ${supplier.address}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${strings.supplierBalanceLabel} ${Formatters.formatMoney(supplier.balanceOwed, currency)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
