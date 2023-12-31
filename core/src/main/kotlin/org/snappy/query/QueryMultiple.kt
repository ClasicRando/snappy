package org.snappy.query

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.snappy.command.sqlCommand
import org.snappy.result.MultiResult
import org.snappy.statement.StatementType
import java.sql.Connection

/**
 * Execute a query against this [Connection], returning a reader for multiple results.
 *
 * @param sql query or procedure name to execute
 * @param parameters values to add to the [java.sql.PreparedStatement], default is no parameters
 * @param statementType query variant as [StatementType.Text] (default) or
 * [StatementType.StoredProcedure]
 * @param timeout query timeout in seconds, default is unlimited time
 *
 * @exception java.sql.SQLException underlining database operation fails
 * @exception IllegalStateException the connection is closed
 * @see java.sql.Statement.execute
 * @see java.sql.Statement.getResultSet
 * @see java.sql.Statement.getMoreResults
 */
fun Connection.queryMultiple(
    sql: String,
    parameters: List<Any> = emptyList(),
    statementType: StatementType = StatementType.Text,
    timeout: UInt? = null,
): MultiResult = sqlCommand(sql, statementType, timeout)
    .bindMany(parameters)
    .queryMultiple(this)

/**
 * Execute a query against this [Connection], returning a reader for multiple results. Suspends a
 * call to [queryMultiple] within the context of [Dispatchers.IO].
 *
 * @param sql query or procedure name to execute
 * @param parameters values to add to the [java.sql.PreparedStatement], default is no parameters
 * @param statementType query variant as [StatementType.Text] (default) or
 * [StatementType.StoredProcedure]
 * @param timeout query timeout in seconds, default is unlimited time
 *
 * @exception java.sql.SQLException underlining database operation fails
 * @exception IllegalStateException the connection is closed
 * @see java.sql.Statement.execute
 * @see java.sql.Statement.getResultSet
 * @see java.sql.Statement.getMoreResults
 * @see withContext
 * @see Dispatchers.IO
 */
suspend inline fun Connection.queryMultipleSuspend(
    sql: String,
    parameters: List<Any> = emptyList(),
    statementType: StatementType = StatementType.Text,
    timeout: UInt? = null,
): MultiResult = sqlCommand(sql, statementType, timeout)
    .bindMany(parameters)
    .queryMultipleSuspend(this)
