package budget

import auth.javalin.MockAuthHandler
import auth.javalin.TokenAuthProvider
import budget.db.BudgetDao
import budget.db.Database
import budget.db.DbMigration
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.config4k.extract
import io.javalin.Javalin
import multiplatform.api.ZeroDepApiClient
import multiplatform.javalin.AuthenticationPlugin
import org.slf4j.LoggerFactory
import javax.sql.DataSource

fun main() {
    val logger = LoggerFactory.getLogger("budget.Main")
    val config = ConfigFactory.load().extract<Config>()

    DbMigration.runMigrations(config.db.url, config.db.user, config.db.password)

    val handler = ApiHandler(Database(dataSource(config)), BudgetDao())

    val app = Javalin.create { javalinConfig ->
        javalinConfig.requestLogger.http { ctx, dur ->
            logger.info("${ctx.method()} ${ctx.path()} ${ctx.status()} ${dur}ms")
        }
        javalinConfig.useVirtualThreads = true
        javalinConfig.staticFiles.add { staticFiles ->
            staticFiles.directory = "/static"
            staticFiles.hostedPath = "/budget"
        }
        javalinConfig.registerPlugin(AuthenticationPlugin {
            register(TokenAuthProvider(ZeroDepApiClient(baseUrl = config.auth.url)))
        })
    }
    handler.registerRoutes(app)
    if (config.auth.mock) {
        MockAuthHandler().registerRoutes(app)
    }
    app.start(config.port)
}

private fun dataSource(config: Config): DataSource {
    val hikariConfig = HikariConfig().apply {
        dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"

        addDataSourceProperty("user", config.db.user)
        addDataSourceProperty("password", config.db.password)
        addDataSourceProperty("url", config.db.url)
    }
    return HikariDataSource(hikariConfig)
}
