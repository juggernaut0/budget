package budget

class Config(
    val port: Int,
    val auth: AuthConfig,
    val db: DbConfig,
)

class AuthConfig(
    val url: String,
    val mock: Boolean,
)

class DbConfig(
    val url: String,
    val user: String,
    val password: String,
)
