package com.example.deuktemsiru_buyer.domain.member

fun gradeLabel(grade: String?, totalOrders: Int) =
    ecoGrades.firstOrNull { it.code == grade }?.apiLabel
        ?: ecoGrades.last { totalOrders >= it.minOrders }.label

fun nextGradeHint(grade: String?, totalOrders: Int): String {
    val apiGradeIndex = ecoGrades.indexOfFirst { it.code == grade }
    if (apiGradeIndex >= 0) {
        return ecoGrades.getOrNull(apiGradeIndex + 1)
            ?.let { "더 구하면 ${it.apiLabel}로 성장해요" }
            ?: "최고 등급이에요!"
    }
    return ecoGrades.firstOrNull { totalOrders < it.minOrders }
        ?.let { "${it.minOrders}회 주문하면 ${it.label}로 성장해요" }
        ?: "최고 등급이에요!"
}

fun gradeProgress(grade: String?, totalOrders: Int): Float {
    val apiGradeIndex = ecoGrades.indexOfFirst { it.code == grade }
    if (apiGradeIndex >= 0) return (apiGradeIndex + 1f) / ecoGrades.size
    val nextGrade = ecoGrades.firstOrNull { totalOrders < it.minOrders } ?: return 1f
    return (totalOrders.toFloat() / nextGrade.minOrders).coerceIn(0.2f, 1f)
}


private data class EcoGrade(val code: String, val minOrders: Int, val label: String, val apiLabel: String)

private val ecoGrades = listOf(
    EcoGrade("SEEDLING", 0, "새싹", "새싹 🌱"),
    EcoGrade("SPROUT", 5, "새싹+", "새싹+ 🌿"),
    EcoGrade("TREE", 15, "나무", "나무 🌳"),
    EcoGrade("FOREST", 30, "숲", "숲 🌲"),
)
