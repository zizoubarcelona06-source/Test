package com.example.core.localization

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    AR("ar", "العربية", true),
    FR("fr", "Français", false),
    EN("en", "English", false),
    ES("es", "Español", false),
    IT("it", "Italiano", false),
    DE("de", "Deutsch", false),
    RU("ru", "Русский", false),
    TR("tr", "Türkçe", false),
    ZH("zh", "中文", false);

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: AR
    }
}

interface AppStrings {
    // Common
    val languageLabel: String
    val chooseLanguage: String
    val close: String
    val save: String
    val cancel: String
    val edit: String
    val delete: String
    val deleteForever: String
    val addNew: String
    val all: String
    val viewAll: String
    val total: String
    val hide: String
    val yesRestore: String

    // Navigation
    val navDashboard: String
    val navProjects: String
    val navResources: String
    val navFinance: String
    val navReports: String

    // Auth & Login
    val offlineBadge: String
    val secureLoginTitle: String
    val loginSubtitle: String
    val phoneLabel: String
    val phonePlaceholder: String
    val passwordLabel: String
    val passwordPlaceholder: String
    val showPassword: String
    val hidePassword: String
    val rememberMe: String
    val loginButton: String
    val quickDemoTitle: String
    val demoAdminButton: String
    val demoEngineerButton: String
    val demoCredentialsHint: String
    val errEmptyPhone: String
    val errInvalidPhone: String
    val errEmptyPassword: String
    val errShortPassword: String
    val errInvalidCredentials: String
    val errAccountDisabled: String
    val errGenericAuth: String
    val errUserExists: String
    val errCreateUserFields: String
    val errDisableActiveUser: String
    val msgUserCreated: String
    val msgPasswordUpdated: String
    val msgUserStatusUpdated: String
    val msgCompanySaved: String
    val msgDemoRestored: String

    // Dashboard
    val welcomePrefix: String
    val lockAndLogoutDesc: String
    val netOperatingBalance: String
    val localSelfContained: String
    val collectedRevenue: String
    val totalExpenses: String
    val pendingReceivables: String
    val activeProjects: String
    val ofTotalProjects: String
    val sitesAndWorkshops: String
    val activeSitesSub: String
    val presentWorkforce: String
    val activeEquipmentLabel: String
    val activeAlerts: String
    val stockAndInvoicesSub: String
    val instantAlertsTitle: String
    val systemModulesTitle: String
    val ongoingProjectsTitle: String
    val lowStockAlertPrefix: String
    val availableQtyFmt: String
    val overdueInvoicePrefix: String
    val duePrefix: String
    val maintenanceEqPrefix: String
    val nextDatePrefix: String
    val safetyAlertPrefix: String
    val safetyCheckFallback: String

    // Shortcuts
    val modProjects: String
    val modProjectsSub: String
    val modSites: String
    val modSitesSub: String
    val modSchedule: String
    val modScheduleSub: String
    val modEmployees: String
    val modEmployeesSub: String
    val modEquipment: String
    val modEquipmentSub: String
    val modInventory: String
    val modInventorySub: String
    val modExpenses: String
    val modExpensesSub: String
    val modInvoices: String
    val modInvoicesSub: String
    val modPartners: String
    val modPartnersSub: String
    val modReports: String
    val modReportsSub: String
    val modSecurity: String
    val modSecuritySub: String

    // Projects Hub
    val projectsHubTitle: String
    val tabProjects: String
    val tabSites: String
    val tabSchedules: String
    val searchProjectsHint: String
    val searchSitesHint: String
    val searchTasksHint: String
    val emptyProjectsTitle: String
    val emptyProjectsSub: String
    val addProject: String
    val emptySitesTitle: String
    val emptySitesSub: String
    val addSite: String
    val emptyTasksTitle: String
    val emptyTasksSub: String
    val addTask: String
    val budgetLabel: String
    val spentLabel: String
    val deliveryLabel: String
    val supervisorLabel: String
    val workersCountLabel: String
    val teamLabel: String
    val dueDateLabel: String
    val deleteProjectTitle: String
    val deleteSiteTitle: String
    val deleteTaskTitle: String
    val confirmDeletePrefix: String

    // Project/Site/Task Dialogs
    val newProjectTitle: String
    val editProjectTitle: String
    val projectCode: String
    val projectName: String
    val clientName: String
    val locationLabel: String
    val estimatedBudget: String
    val spentAmountLabel: String
    val projectStatus: String
    val progressLabel: String
    val startDate: String
    val endDate: String
    val projectDesc: String
    val saveProject: String
    val newSiteTitle: String
    val editSiteTitle: String
    val siteName: String
    val linkedProject: String
    val addressLabel: String
    val supervisorName: String
    val workersCount: String
    val safetyStatus: String
    val siteStatus: String
    val dailyFieldNote: String
    val saveSite: String
    val newTaskTitle: String
    val editTaskTitle: String
    val taskTitle: String
    val projectLabel: String
    val siteLabel: String
    val assignedTeam: String
    val priorityLabel: String
    val saveTask: String

    // Resources Hub
    val resourcesHubTitle: String
    val tabEmployees: String
    val tabEquipment: String
    val tabMaterials: String
    val searchEmployeesHint: String
    val searchEquipmentHint: String
    val searchMaterialsHint: String
    val lowStockFilterChip: String
    val lowStockBadge: String
    val emptyEmployeesTitle: String
    val emptyEmployeesSub: String
    val addEmployee: String
    val emptyEquipmentTitle: String
    val emptyEquipmentSub: String
    val addEquipment: String
    val emptyMaterialsTitle: String
    val emptyMaterialsSub: String
    val addMaterial: String
    val dailyWageHint: String
    val nextMaintenanceLabel: String
    val hourlyCostLabel: String
    val deleteEmployeeTitle: String
    val deleteEquipmentTitle: String
    val deleteMaterialTitle: String

    // Resource Dialogs
    val newEmployeeTitle: String
    val editEmployeeTitle: String
    val fullNameLabel: String
    val roleLabel: String
    val nationalIdLabel: String
    val dailyWageLabel: String
    val assignedSiteLabel: String
    val attendanceStatusLabel: String
    val hireDateLabel: String
    val newEquipmentTitle: String
    val editEquipmentTitle: String
    val equipmentNameLabel: String
    val serialCodeLabel: String
    val categoryLabel: String
    val operationalStatusLabel: String
    val newMaterialTitle: String
    val editMaterialTitle: String
    val materialNameLabel: String
    val skuLabel: String
    val unitLabel: String
    val currentQtyLabel: String
    val minThresholdLabel: String
    val unitPriceLabel: String
    val supplierLabel: String
    val warehouseLabel: String

    // Finance Hub
    val financeHubTitle: String
    val collectedLabel: String
    val expensesLabel: String
    val supplierPayablesLabel: String
    val tabExpenses: String
    val tabInvoices: String
    val tabPartners: String
    val searchExpensesHint: String
    val searchInvoicesHint: String
    val searchPartnersHint: String
    val clientsLabel: String
    val suppliersLabel: String
    val emptyExpensesTitle: String
    val emptyExpensesSub: String
    val addExpense: String
    val emptyInvoicesTitle: String
    val emptyInvoicesSub: String
    val addInvoice: String
    val emptyClientsTitle: String
    val emptyClientsSub: String
    val addClient: String
    val emptySuppliersTitle: String
    val emptySuppliersSub: String
    val addSupplier: String
    val totalAmountLabel: String
    val paidAmountLabel: String
    val paymentBtn: String
    val phonePrefix: String
    val addressPrefix: String
    val supplierBalanceLabel: String
    val deleteExpenseTitle: String
    val deleteInvoiceTitle: String
    val deleteClientTitle: String
    val deleteSupplierTitle: String

    // Finance Dialogs
    val newExpenseTitle: String
    val editExpenseTitle: String
    val expenseDescLabel: String
    val amountLabel: String
    val paymentMethodLabel: String
    val dateLabel: String
    val notesLabel: String
    val newInvoiceTitle: String
    val editInvoiceTitle: String
    val invoiceNumberLabel: String
    val invoiceTypeLabel: String
    val partyNameLabel: String
    val statusLabel: String
    val issueDateLabel: String
    val saveInvoice: String
    val recordPaymentTitle: String
    val remainingBalanceLabel: String
    val newPaymentAmountLabel: String
    val confirmPayment: String
    val newClientTitle: String
    val editClientTitle: String
    val clientEntityNameLabel: String
    val companyOrgLabel: String
    val emailLabel: String
    val taxIdLabel: String
    val saveClient: String
    val newSupplierTitle: String
    val editSupplierTitle: String
    val supplierNameLabel: String
    val supplyFieldLabel: String
    val balanceOwedLabel: String
    val saveSupplier: String

    // Reports & Security
    val reportsHubTitle: String
    val tabReports: String
    val tabSecurity: String
    val avgProjectProgress: String
    val registeredProjectsSub: String
    val currentInventoryValue: String
    val lowStockCountSub: String
    val dailyPayrollCost: String
    val presentWorkersSub: String
    val equipmentReadiness: String
    val completedTasksSub: String
    val expensesByCategoryTitle: String
    val budgetVsSpentTitle: String
    val noProjectsChartData: String
    val exportShareReport: String
    val shareReportChooser: String
    val activeStatusBadge: String
    val changePasswordBtn: String
    val lockExitBtn: String
    val companyThemeSettingsTitle: String
    val companyCurrencySummaryFmt: String
    val editCompanyCurrencyBtn: String
    val appearanceLabel: String
    val themeAuto: String
    val themeLight: String
    val themeDark: String
    val userAccountsTitle: String
    val newAccountBtn: String
    val resetDemoDataBtn: String
    val createUserTitle: String
    val usernameLatinLabel: String
    val rolePermissionLabel: String
    val passwordMin4Label: String
    val createBtn: String
    val updatePasswordTitle: String
    val newPasswordLabel: String
    val updateBtn: String
    val companySettingsTitle: String
    val companyNameLabel: String
    val defaultCurrencyLabel: String
    val resetDemoTitle: String
    val resetDemoMessage: String

    // Canonical Term Translations (DB keys -> localized display)
    val terms: Map<String, String>

    fun translateTerm(canonical: String): String = terms[canonical] ?: canonical

    fun authErrorText(key: String): String = when (key) {
        "ERR_EMPTY_PHONE" -> errEmptyPhone
        "ERR_INVALID_PHONE" -> errInvalidPhone
        "ERR_EMPTY_PASSWORD" -> errEmptyPassword
        "ERR_SHORT_PASSWORD" -> errShortPassword
        "ERR_INVALID_CREDENTIALS" -> errInvalidCredentials
        "ERR_ACCOUNT_DISABLED" -> errAccountDisabled
        "ERR_GENERIC_AUTH" -> errGenericAuth
        "ERR_USER_EXISTS" -> errUserExists
        "ERR_CREATE_USER_FIELDS" -> errCreateUserFields
        "ERR_DISABLE_ACTIVE_USER" -> errDisableActiveUser
        "MSG_USER_CREATED" -> msgUserCreated
        "MSG_PASSWORD_UPDATED" -> msgPasswordUpdated
        "MSG_USER_STATUS_UPDATED" -> msgUserStatusUpdated
        "MSG_COMPANY_SAVED" -> msgCompanySaved
        "MSG_DEMO_RESTORED" -> msgDemoRestored
        else -> key
    }
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { ArabicStrings }

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.AR -> ArabicStrings
    AppLanguage.FR -> FrenchStrings
    AppLanguage.EN -> EnglishStrings
    AppLanguage.ES -> SpanishStrings
    AppLanguage.IT -> ItalianStrings
    AppLanguage.DE -> GermanStrings
    AppLanguage.RU -> RussianStrings
    AppLanguage.TR -> TurkishStrings
    AppLanguage.ZH -> ChineseStrings
}
