package org.snappy.extensions

import org.snappy.NullFieldName
import org.snappy.OutParameterOutsideProcedure
import org.snappy.SnappyRow
import org.snappy.SqlParameter
import org.snappy.StatementType
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Extract all column names from a [ResultSet]. Can fail if a column name is null
 *
 * @exception NullFieldName should never happen
 */
val ResultSet.columNames: List<String> get() = (1..this.metaData.columnCount)
    .map { index -> this.metaData.getColumnName(index) ?: throw NullFieldName() }

/**
 * Map the current state of [ResultSet] to a [SnappyRow] using the [columns] provided. Note: names
 * specified in [columns] must match the [ResultSet] so the parameter value should be fetched using
 * [ResultSet.columNames].
 */
@PublishedApi
internal fun ResultSet.toSnappyRow(columns: List<String>): SnappyRow {
    val data = columns.associateWith { name -> this.getObject(name) }
    return SnappyRow(data)
}

/**
 * Map a [List] of objects into a [List] of [SqlParameter]. Any item within the [List] that is
 * already a [SqlParameter] will remain untouched, while other values are converted to a
 * [SqlParameter.In] instance.
 */
internal fun List<Any?>.toSqlParameterList(): List<SqlParameter> {
    if (isEmpty()) return emptyList()
    return this.map { it as? SqlParameter ?: SqlParameter.In(it) }
}

/**
 * Set [parameter] within a [PreparedStatement].
 *
 * This function will fail if the:
 * - [parameter] is [SqlParameter.Out] but the statement is not a [CallableStatement]
 * - no parameter within the statement corresponds to the supplied parameter (e.g. not enough
 * placeholders)
 * - [parameter] cannot be encoded to into the [PreparedStatement]
 *
 * @exception java.sql.SQLException
 * @exception OutParameterOutsideProcedure
 */
internal fun PreparedStatement.setParameter(parameterIndex: Int, parameter: SqlParameter) {
    when (parameter) {
        is SqlParameter.Out -> {
            if (this !is CallableStatement) {
                throw OutParameterOutsideProcedure()
            }
            this.registerOutParameter(parameterIndex, parameter.sqlType)
        }
        is SqlParameter.In -> parameter.value.encode(this, parameterIndex)
    }
}

/**
 * Create a new [PreparedStatement] using the provided details. If the [statementType] is
 * [StatementType.StoredProcedure] the [sql] query is intended to be a procedure name that is
 * transformed into a JDBC stored procedure call.
 *
 * If anything within the function throws an exception the statement will always be closed before
 * the exception is rethrown.
 *
 * @exception java.sql.SQLException underlining database operation fails
 * @exception IllegalStateException statement is null before returning (should never happen)
 */
internal fun Connection.getStatement(
    sql: String,
    parameters: List<Any?>,
    statementType: StatementType,
    timeout: UInt?,
): PreparedStatement {
    check(!isClosed) { "Cannot query a closed connection" }
    var statement: PreparedStatement? = null
    try {
        statement = when (statementType) {
            StatementType.StoredProcedure -> prepareCall(
                "{call $sql(${"?,".repeat(parameters.size).trim(',')})"
            )
            StatementType.Text -> prepareStatement(sql)
        }
        statement?.let {
            timeout?.let {
                statement.queryTimeout = it.toInt()
            }
            for ((i, parameter) in parameters.toSqlParameterList().withIndex()) {
                statement.setParameter(i + 1, parameter)
            }
        }
        return statement
            ?: throw IllegalStateException("Statement cannot be null when exiting function")
    } catch (t: Throwable) {
        statement?.close()
        throw t
    }
}

/**
 * Chunk a [Sequence] into a subset of [Sequence]s of the desired [size]. If the [Sequence] does not
 * fit evenly into chunks of the desired size, the trailing [Sequence] will be smaller.
 */
fun <T> Sequence<T>.chunkedIter(size: Int): Sequence<Sequence<T>> = sequence {
    val iterator = this@chunkedIter.iterator()
    while (iterator.hasNext()) {
        var itemNumber = 0
        val sequence = generateSequence {
            if (!iterator.hasNext() || itemNumber == size) {
                return@generateSequence null
            }
            itemNumber++
            iterator.next()
        }
        yield(sequence)
    }
}
