package net.psforever.persistence

/** A PSF-HTTP API sign-in session; see the V019 migration for what each column means. */
case class ApiSession(
    tokenDigest: String,
    accountId: Int,
    createdAtMs: Long,
    lastSeenAtMs: Long,
    credentialFingerprint: String,
    gmAtIssue: Boolean,
    mfa: Boolean
)

/** An account's authenticator-app secret, base32-encoded. */
case class AccountTotp(
    accountId: Int,
    secret: String,
    enabled: Boolean,
    lastUsedStep: Long
)
