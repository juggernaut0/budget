package budget.db

import org.jooq.DSLContext
import org.jooq.impl.DSL
import javax.sql.DataSource

class Database(private val dataSource: DataSource) {
    fun <T> transaction(block: (DSLContext) -> T): T {
        val connection = dataSource.connection
        val dsl = DSL.using(connection)
        return dsl.transactionResult { cfg -> block(cfg.dsl()) }
    }
}