package budget

import auth.javalin.AuthenticatedRole
import auth.javalin.ValidatedToken
import budget.api.*
import budget.db.BudgetDao
import budget.db.Database
import io.javalin.Javalin
import kotlinx.serialization.json.Json
import multiplatform.javalin.handleApi
import java.util.UUID

class ApiHandler(private val db: Database, private val dao: BudgetDao) {
    private val json = Json { ignoreUnknownKeys = true }

    fun registerRoutes(app: Javalin) {
        app
            .handleApi(getBudget, AuthenticatedRole) {
                val userId = (auth as ValidatedToken).userId
                val rawData = db.transaction { dsl -> dao.getBudget(dsl, userId, params.name) }
                if (rawData != null) {
                    json.decodeFromString(Budget.serializer(), rawData)
                } else {
                    Budget(emptyList(), emptyList(), Settings(0, 0, 1.0))
                }
            }
            .handleApi(updateBudget, AuthenticatedRole) {
                val userId = (auth as ValidatedToken).userId
                val rawData = json.encodeToString(Budget.serializer(), it)
                db.transaction { dsl -> dao.saveBudget(dsl, userId, params.name, rawData) }
            }
    }
}
