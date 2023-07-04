package at.gv.brz.brvc.model.data

import com.squareup.moshi.Json

enum class BusinessRuleCertificateType(val value: String) {
    @Json(name = "vaccination")
    VACCINATION("vaccination"),
    @Json(name = "recovery")
    RECOVERY("recovery"),
    @Json(name = "test")
    TEST("test"),
    @Json(name = "vaccination_exemption")
    VACCINATION_EXEMPTION("vaccination_exemption"),
}