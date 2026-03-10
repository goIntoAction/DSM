package wang.zengye.dsm.data.model.control_panel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 用户详情响应
 */
@JsonClass(generateAdapter = true)
data class UserDetailDto(
    @Json(name = "data") val data: UserDetailDataDto?
)

@JsonClass(generateAdapter = true)
data class UserDetailDataDto(
    @Json(name = "users") val users: List<UserDataDto>?
)

@JsonClass(generateAdapter = true)
data class UserDataDto(
    @Json(name = "name") val name: String?,
    @Json(name = "uid") val uid: Int?,
    @Json(name = "additional") val additional: UserAdditional?
)

@JsonClass(generateAdapter = true)
data class UserAdditional(
    @Json(name = "description") val description: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "expired") val expired: String?,
    @Json(name = "cannot_chg_passwd") val cannotChgPasswd: Boolean?,
    @Json(name = "passwd_never_expire") val passwdNeverExpire: Boolean?
)

/**
 * 用户群组响应
 */
@JsonClass(generateAdapter = true)
data class UserGroupsDto(
    @Json(name = "data") val data: UserGroupsDataDto?
)

@JsonClass(generateAdapter = true)
data class UserGroupsDataDto(
    @Json(name = "groups") val groups: List<UserGroupDataDto>?
)

@JsonClass(generateAdapter = true)
data class UserGroupDataDto(
    @Json(name = "name") val name: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "is_member") val isMember: Boolean?
)
