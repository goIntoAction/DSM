package wang.zengye.dsm.data.model.control_panel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 证书响应
 */
@JsonClass(generateAdapter = true)
data class CertificatesDto(
    @Json(name = "data") val data: CertificatesDataDto?
)

@JsonClass(generateAdapter = true)
data class CertificatesDataDto(
    @Json(name = "certificates") val certificates: List<CertificateDataDto>?
)

@JsonClass(generateAdapter = true)
data class CertificateDataDto(
    @Json(name = "id") val id: String?,
    @Json(name = "desc") val desc: String?,
    @Json(name = "issuer") val issuer: CertificateIssuer?,
    @Json(name = "subject") val subject: CertificateSubject?,
    @Json(name = "valid_from") val validFrom: String?,
    @Json(name = "valid_till") val validTill: String?,
    @Json(name = "is_default") val isDefault: Boolean?,
    @Json(name = "services") val services: List<CertificateService>?
)

@JsonClass(generateAdapter = true)
data class CertificateIssuer(
    @Json(name = "common_name") val commonName: String?
)

@JsonClass(generateAdapter = true)
data class CertificateSubject(
    @Json(name = "common_name") val commonName: String?
)

@JsonClass(generateAdapter = true)
data class CertificateService(
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "display_name_i18n") val displayNameI18n: String?
)
