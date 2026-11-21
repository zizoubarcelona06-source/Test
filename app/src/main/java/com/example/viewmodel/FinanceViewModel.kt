package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.repository.BtpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinanceUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val invoices: List<InvoiceEntity> = emptyList(),
    val clients: List<ClientEntity> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val totalExpenses: Double = 0.0,
    val totalInvoicedToClients: Double = 0.0,
    val totalCollectedFromClients: Double = 0.0,
    val totalSupplierPayables: Double = 0.0,
    val searchQuery: String = "",
    val expenseCategoryFilter: String = "الكل"
)

class FinanceViewModel(private val repository: BtpRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val expenseCategory = MutableStateFlow("الكل")

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.expenses,
        repository.invoices,
        repository.clients,
        repository.suppliers,
        searchQuery,
        expenseCategory
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val expenses = values[0] as List<ExpenseEntity>
        @Suppress("UNCHECKED_CAST")
        val invoices = values[1] as List<InvoiceEntity>
        @Suppress("UNCHECKED_CAST")
        val clients = values[2] as List<ClientEntity>
        @Suppress("UNCHECKED_CAST")
        val suppliers = values[3] as List<SupplierEntity>
        val q = (values[4] as String).trim().lowercase()
        val cat = values[5] as String

        val clientInv = invoices.filter { it.type == "فاتورة عميل" }
        val supplierInv = invoices.filter { it.type == "فاتورة مورد" }

        FinanceUiState(
            expenses = expenses.filter {
                (cat == "الكل" || it.category == cat) &&
                    (q.isEmpty() || it.title.lowercase().contains(q) || it.projectName.lowercase().contains(q))
            },
            invoices = invoices.filter {
                q.isEmpty() || it.invoiceNumber.lowercase().contains(q) || it.partyName.lowercase().contains(q) || it.projectName.lowercase().contains(q)
            },
            clients = clients.filter {
                q.isEmpty() || it.name.lowercase().contains(q) || it.companyName.lowercase().contains(q)
            },
            suppliers = suppliers.filter {
                q.isEmpty() || it.name.lowercase().contains(q) || it.category.lowercase().contains(q)
            },
            totalExpenses = expenses.sumOf { it.amount },
            totalInvoicedToClients = clientInv.sumOf { it.totalAmount },
            totalCollectedFromClients = clientInv.sumOf { it.paidAmount },
            totalSupplierPayables = supplierInv.sumOf { it.remainingAmount },
            searchQuery = values[4] as String,
            expenseCategoryFilter = cat
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinanceUiState())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setExpenseCategoryFilter(category: String) {
        expenseCategory.value = category
    }

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.saveExpense(expense) }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun saveInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch { repository.saveInvoice(invoice) }
    }

    fun recordPayment(invoice: InvoiceEntity, amount: Double) {
        viewModelScope.launch { repository.recordInvoicePayment(invoice, amount) }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch { repository.deleteInvoice(invoice) }
    }

    fun saveClient(client: ClientEntity) {
        viewModelScope.launch { repository.saveClient(client) }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch { repository.deleteClient(client) }
    }

    fun saveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch { repository.saveSupplier(supplier) }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch { repository.deleteSupplier(supplier) }
    }
}
