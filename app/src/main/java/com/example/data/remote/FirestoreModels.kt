package com.example.data.remote

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class FsUserProfile(
    @DocumentId val id: String = "",
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: String = "مهندس موقع",
    val branchName: String = "المقر الرئيسي",
    val preferredLanguageCode: String = "ar",
    val activeCompanyId: String = "",
    val active: Boolean = true,
    val rewardPoints: Int = 120,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsCompany(
    @DocumentId val id: String = "",
    val name: String = "",
    val currencySymbol: String = "د.ج",
    val inviteCode: String = "",
    val ownerId: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsCompanyInvite(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val ownerId: String = "",
    val defaultRole: String = "مهندس موقع",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsCompanyMember(
    @DocumentId val id: String = "",
    val uid: String = "",
    val companyId: String = "",
    val fullName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: String = "مهندس موقع",
    val branchName: String = "المقر الرئيسي",
    val preferredLanguageCode: String = "ar",
    val inviteCode: String = "",
    val active: Boolean = true,
    @ServerTimestamp val joinedAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsProject(
    @DocumentId val id: String = "",
    val code: String = "",
    val name: String = "",
    val clientName: String = "",
    val location: String = "",
    val budget: Double = 0.0,
    val spentAmount: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val progressPercent: Int = 0,
    val status: String = "قيد التنفيذ",
    val description: String = "",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsSite(
    @DocumentId val id: String = "",
    val projectName: String = "",
    val name: String = "",
    val address: String = "",
    val supervisorName: String = "",
    val workerCount: Int = 0,
    val safetyStatus: String = "آمن",
    val status: String = "نشط",
    val dailyNote: String = "",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsTask(
    @DocumentId val id: String = "",
    val title: String = "",
    val projectName: String = "",
    val siteName: String = "",
    val assignedTeam: String = "",
    val startDate: String = "",
    val dueDate: String = "",
    val priority: String = "متوسطة",
    val completed: Boolean = false,
    val progress: Int = 0,
    val assignedToUid: String = "",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsEmployee(
    @DocumentId val id: String = "",
    val fullName: String = "",
    val role: String = "",
    val phone: String = "",
    val nationalId: String = "",
    val dailyWage: Double = 0.0,
    val assignedSiteName: String = "",
    val attendanceStatus: String = "حاضر",
    val hireDate: String = "",
    val active: Boolean = true,
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsEquipment(
    @DocumentId val id: String = "",
    val name: String = "",
    val code: String = "",
    val category: String = "",
    val status: String = "في الخدمة",
    val assignedSiteName: String = "",
    val nextMaintenanceDate: String = "",
    val hourlyCost: Double = 0.0,
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsEquipmentFault(
    @DocumentId val id: String = "",
    val equipmentName: String = "",
    val siteName: String = "",
    val severity: String = "عالية",
    val description: String = "",
    val reportedByUid: String = "",
    val reportedByName: String = "",
    val status: String = "مفتوح",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsMaterial(
    @DocumentId val id: String = "",
    val name: String = "",
    val sku: String = "",
    val unit: String = "",
    val quantityInStock: Double = 0.0,
    val minThreshold: Double = 0.0,
    val unitPrice: Double = 0.0,
    val supplierName: String = "",
    val warehouseLocation: String = "",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsExpense(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val projectName: String = "",
    val paymentMethod: String = "",
    val notes: String = "",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsInvoice(
    @DocumentId val id: String = "",
    val invoiceNumber: String = "",
    val type: String = "",
    val partyName: String = "",
    val projectName: String = "",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val issueDate: String = "",
    val dueDate: String = "",
    val status: String = "",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsClient(
    @DocumentId val id: String = "",
    val name: String = "",
    val companyName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val taxId: String = "",
    val notes: String = "",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsSupplier(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val balanceOwed: Double = 0.0,
    val notes: String = "",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsChatChannel(
    @DocumentId val id: String = "",
    val name: String = "",
    val channelType: String = "PROJECT",
    val projectName: String = "",
    val participantUidsCsv: String = "",
    val lastMessagePreview: String = "",
    val lastMessageAtMs: Long = 0L,
    val unreadCount: Int = 0,
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsChatMessage(
    @DocumentId val id: String = "",
    val channelCloudId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderRole: String = "",
    val content: String = "",
    val attachmentType: String = "NONE",
    val attachmentName: String = "",
    val attachmentUri: String = "",
    val timestampMs: Long = 0L,
    val read: Boolean = false,
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FsCompanyNotification(
    @DocumentId val id: String = "",
    val category: String = "PROJECTS",
    val title: String = "",
    val message: String = "",
    val targetTab: String = "DASHBOARD",
    val targetSubTab: Int = 0,
    val read: Boolean = false,
    val createdAtMs: Long = 0L,
    val audience: String = "ALL",
    val companyId: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object FirestoreMappers {
    fun withCreateTimestamps(fields: Map<String, Any?>): Map<String, Any> =
        (fields + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )).filterValues { it != null }.mapValues { it.value!! }

    fun withUpdateTimestamps(fields: Map<String, Any?>): Map<String, Any> =
        (fields + mapOf(
            "updatedAt" to FieldValue.serverTimestamp()
        )).filterValues { it != null }.mapValues { it.value!! }
}
