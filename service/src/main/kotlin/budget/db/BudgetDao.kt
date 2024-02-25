package budget.db

import budget.db.jooq.Tables.BUDGET
import org.jooq.DSLContext
import org.jooq.JSONB
import java.util.*

class BudgetDao {
    fun getBudget(dsl: DSLContext, userId: UUID, name: String): String? {
        return dsl.select(BUDGET.DATA)
            .from(BUDGET)
            .where(BUDGET.USER_ID.eq(userId))
            .and(BUDGET.NAME.eq(name))
            .fetchOne()
            ?.let {
                it[BUDGET.DATA].data()
            }
    }

    fun saveBudget(dsl: DSLContext, userId: UUID, name: String, data: String) {
        dsl.insertInto(BUDGET)
            .set(BUDGET.USER_ID, userId)
            .set(BUDGET.NAME, name)
            .set(BUDGET.VERSION, 1)
            .set(BUDGET.DATA, JSONB.valueOf(data))
            .onDuplicateKeyUpdate()
            .set(BUDGET.VERSION, 1)
            .set(BUDGET.DATA, JSONB.valueOf(data))
            .execute()
    }
}
