package net.rsprox.proxy.server.prot

internal object LoginServerProtId {
    /**
     * TFU responses
     */
    const val OK = 2
    const val INVALID_USERNAME_OR_PASSWORD = 3
    const val BANNED = 4
    const val DUPLICATE = 5
    const val CLIENT_OUT_OF_DATE = 6
    const val SERVER_FULL = 7
    const val LOGINSERVER_OFFLINE = 8
    const val IP_LIMIT = 9
    const val FORCE_PASSWORD_CHANGE = 11
    const val NEED_MEMBERS_ACCOUNT = 12
    const val INVALID_SAVE = 13
    const val UPDATE_IN_PROGRESS = 14
    const val RECONNECT_OK = 15
    const val TOO_MANY_ATTEMPTS = 16
    const val LOCKED = 18
    const val HOP_BLOCKED = 21
    const val INVALID_LOGIN_PACKET = 22
    const val LOGINSERVER_LOAD_ERROR = 24
    const val UNKNOWN_REPLY_FROM_LOGINSERVER = 25
    const val IP_BLOCKED = 26
    const val DISALLOWED_BY_SCRIPT = 29
    const val NEGATIVE_CREDIT = 32
    const val INVALID_SINGLE_SIGNON = 35
    const val NO_REPLY_FROM_SINGLE_SIGNON = 36
    const val PROFILE_BEING_EDITED = 37
    const val NO_BETA_ACCESS = 38
    const val INSTANCE_INVALID = 39
    const val INSTANCE_NOT_SPECIFIED = 40
    const val INSTANCE_FULL = 41
    const val IN_QUEUE = 42
    const val ALREADY_IN_QUEUE = 43
    const val BILLING_TIMEOUT = 44
    const val NOT_AGREED_TO_NDA = 45
    const val EMAIL_NOT_VALIDATED = 47
    const val CONNECT_FAIL = 50

    /**
     * Responses from the OSRS client.
     * Namings here are guessed.
     */
    const val SUCCESSFUL = 0
    const val BAD_SESSION_ID = 10
    const val IN_MEMBERS_AREA = 17
    const val CLOSED_BETA_INVITED_ONLY = 19
    const val INVALID_LOGINSERVER = 20
    const val LOGINSERVER_NO_REPLY = 23
    const val SERVICE_UNAVAILABLE = 27
    const val DISPLAYNAME_REQUIRED = 31
    const val PRIVACY_POLICY = 55
    const val AUTHENTICATOR = 56
    const val INVALID_AUTHENTICATOR_CODE = 57
    const val UPDATE_DOB = 61
    const val TIMEOUT = 62
    const val KICK = 63
    const val RETRY = 64
    const val LOGIN_FAIL_1 = 65
    const val LOGIN_FAIL_2 = 67
    const val OUT_OF_DATE_RELOAD = 68
    const val PROOF_OF_WORK = 69
    const val DOB_ERROR = 71
    const val WEBSITE_DOB = 72
    const val DOB_REVIEW = 73
    const val CLOSED_BETA = 74
}
