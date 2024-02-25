package budget.db

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import javax.sql.DataSource

class Database(private val dataSource: DataSource) {
    fun <T> transaction(block: (DSLContext) -> T): T {
        val dsl = DSL.using(dataSource, SQLDialect.POSTGRES)
        return dsl.transactionResult { cfg -> block(cfg.dsl()) }
    }
}
