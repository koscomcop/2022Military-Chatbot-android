package com.example.militaryaibot

class WeatherConst {

    // CODES
    val CODES: HashMap<String, String> = mutableMapOf<String, String>(
            "POP" to "-", //강수확률
            "PTY" to "-", //강수형태
            "PCP" to "-", //1시간 강수량
            "REH" to "-", //습도
            "SNO" to "-", //1시간 신적설
            "SKY" to "-", //하늘상태
            "TMP" to "-", //1시간 기온
            "TMN" to "-", //일 최저기온
            "TMX" to "-", //일 최고기온
            "UUU" to "-", //풍속(동서성분)
            "VVV" to "-", //풍속(남북성분)
            "WAV" to "-", //파고
            "VEC" to "-", //풍향
            "WSD" to "-", //풍속
    ) as HashMap<String, String>

    // CODE DETAIL
    var sky_detail: Map<String, String> = mapOf<String, String>(
        "1" to "맑음",
        "3" to "구름많음",
        "4" to "흐림",
    )
    var pty_detail: Map<String, String> = mapOf<String, String>(
        "0" to "없음",
        "1" to "비",
        "2" to "비/눈",
        "3" to "눈",
        "4" to "소나기",
    )
    var pcp_detail: Map<String, String> = mapOf<String, String>(
        "1" to "없음",
        "30" to "비",
        "50" to "비/눈",
        "3" to "눈",
        "4" to "소나기",
    )

    // ERRORS
    var NORMAL_SERVICE = "00"
    var APPLICATION_ERROR = "01"
    var DB_ERROR = "02"
    var NODATA_ERROR = "03"
    var HTTP_ERROR = "04"
    var SERVICETIME_OUT = "05"
    var INVALID_REQUEST_PARAMETER_ERROR = "10"
    var NO_MANDATORY_REQUEST_PARAMETERS_ERROR = "11"
    var NO_OPENAPI_SERVICE_ERROR = "12"
    var SERVICE_ACCESS_DENIED_ERROR = "20"
    var TEMPORARILY_DISABLE_THE_SERVICEKEY_ERROR = "21"
    var LIMITED_NUMBER_OF_SERVICE_REQUEST_EXCEEDS_ERROR = "22"
    var SERVICE_KEY_IS_NOT_REGISTERED_ERROR = "30"
    var DEADLINE_HAS_EXPIRED_ERROR = "31"
    var UNREGISTERED_IP_ERROR = "32"
    var UNSIGNED_CALL_ERROR = "33"
    var UNKNOWN_ERROR = "99"
}