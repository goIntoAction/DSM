package wang.zengye.dsm.data.model.dashboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DesktopInitdataDto(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: DesktopInitdataData?
)

@JsonClass(generateAdapter = true)
data class DesktopInitdataData(
    @Json(name = "AppPrivilege") val appPrivilege: Map<String, Boolean>?
) {
    /**
     * 获取有权限的应用 ID 列表
     */
    fun getEnabledAppIds(): List<String> {
        return appPrivilege?.filterValues { it }?.keys?.toList() ?: emptyList()
    }
}
