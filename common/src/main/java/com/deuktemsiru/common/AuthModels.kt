package com.deuktemsiru.common

data class ApiResponse<T>(val code: Int, val message: String, val data: T?)
data class MemberSummary(val memberId: Long, val nickname: String, val role: String)
data class LoginData(val accessToken: String, val refreshToken: String, val member: MemberSummary)
data class TokenData(val accessToken: String)
