package com.example.core.security

data class RolePermissions(
    val canViewFinance: Boolean,
    val canAddExpense: Boolean,
    val canAddProject: Boolean,
    val canAddEmployee: Boolean,
    val canAddEquipment: Boolean,
    val canReportFault: Boolean,
    val canCreateTask: Boolean,
    val canSendMessage: Boolean,
    val canPostAnnouncement: Boolean,
    val canManageUsers: Boolean,
    val allowedNotificationCategories: Set<String>
)

object RolePermissionPolicy {

    val allNotificationCategories = listOf(
        "CHAT",
        "ATTENDANCE",
        "LEAVE",
        "REWARDS",
        "EQUIPMENT",
        "INVENTORY",
        "APPROVALS",
        "PROJECTS",
        "FINANCE",
        "TASKS",
        "ADMIN_SECURITY"
    )

    fun permissionsFor(role: String?): RolePermissions {
        return when (role?.trim()) {
            "مدير عام" -> RolePermissions(
                canViewFinance = true,
                canAddExpense = true,
                canAddProject = true,
                canAddEmployee = true,
                canAddEquipment = true,
                canReportFault = true,
                canCreateTask = true,
                canSendMessage = true,
                canPostAnnouncement = true,
                canManageUsers = true,
                allowedNotificationCategories = allNotificationCategories.toSet()
            )
            "مدير مشاريع" -> RolePermissions(
                canViewFinance = false,
                canAddExpense = false,
                canAddProject = true,
                canAddEmployee = true,
                canAddEquipment = true,
                canReportFault = true,
                canCreateTask = true,
                canSendMessage = true,
                canPostAnnouncement = true,
                canManageUsers = false,
                allowedNotificationCategories = setOf(
                    "CHAT", "ATTENDANCE", "LEAVE", "REWARDS", "EQUIPMENT", "INVENTORY", "APPROVALS", "PROJECTS", "TASKS"
                )
            )
            "محاسب مالي" -> RolePermissions(
                canViewFinance = true,
                canAddExpense = true,
                canAddProject = false,
                canAddEmployee = false,
                canAddEquipment = false,
                canReportFault = false,
                canCreateTask = true,
                canSendMessage = true,
                canPostAnnouncement = false,
                canManageUsers = false,
                allowedNotificationCategories = setOf(
                    "CHAT", "REWARDS", "APPROVALS", "PROJECTS", "FINANCE", "TASKS"
                )
            )
            else -> RolePermissions( // مهندس موقع / مسؤول ميداني
                canViewFinance = false,
                canAddExpense = false,
                canAddProject = false,
                canAddEmployee = false,
                canAddEquipment = true,
                canReportFault = true,
                canCreateTask = true,
                canSendMessage = true,
                canPostAnnouncement = false,
                canManageUsers = false,
                allowedNotificationCategories = setOf(
                    "CHAT", "ATTENDANCE", "LEAVE", "REWARDS", "EQUIPMENT", "INVENTORY", "PROJECTS", "TASKS"
                )
            )
        }
    }
}
