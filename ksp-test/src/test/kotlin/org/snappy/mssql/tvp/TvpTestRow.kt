package org.snappy.mssql.tvp

import org.snappy.copy.ToObjectRow
import org.snappy.ksp.symbols.ObjectRow
import org.snappy.ksp.symbols.Rename
import org.snappy.ksp.symbols.RowParser
import org.snappy.mssql.DateTime
import org.snappy.mssql.SmallDateTime
import org.snappy.mssql.ksp.symbol.TableType
import org.snappy.mssql.toDateTime
import org.snappy.mssql.toSmallDateTime
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*
import kotlin.random.Random

@TableType("tvp_test")
@RowParser
@ObjectRow
data class TvpTestRow(
    @Rename("bool_field")
    val boolField: Boolean,
    @Rename("smallint_field")
    val smallintField: Short,
    @Rename("int_field")
    val intField: Int,
    @Rename("bigint_field")
    val bigintField: Long,
    @Rename("real_field")
    val realField: Float,
    @Rename("double_field")
    val doubleField: Double,
    @Rename("text_field")
    val textField: String,
    @Rename("numeric_field")
    val numericField: BigDecimal,
    @Rename("date_field")
    val dateField: LocalDate,
    @Rename("datetime_field")
    val datetimeField: DateTime,
    @Rename("smalldatetime_field")
    val smallDateTimeField: SmallDateTime,
    @Rename("datetimeoffset_field")
    val dateTimeOffsetField: microsoft.sql.DateTimeOffset,
    @Rename("time_field")
    val timeField: LocalTime,
) : ToObjectRow {
    override fun toObjectRow(): List<Any?> {
        return listOf(
            boolField,
            smallintField,
            intField,
            bigintField,
            realField,
            doubleField,
            textField,
            numericField,
            dateField,
            datetimeField,
            smallDateTimeField.bulkCopyString(),
            dateTimeOffsetField,
            timeField
        )
    }

    companion object {
        fun random(): TvpTestRow {
            val random = Random(System.currentTimeMillis())
            val timestamp = LocalDateTime.ofEpochSecond(
                random.nextLong(0, 2524554080),
                0,
                ZoneOffset.UTC
            ).atOffset(ZoneOffset.ofHours(random.nextInt(-12, 12)))
            return TvpTestRow(
                random.nextBoolean(),
                random.nextInt(Short.MAX_VALUE.toInt()).toShort(),
                random.nextInt(),
                random.nextLong(),
                random.nextFloat(),
                random.nextDouble(),
                UUID.randomUUID().toString(),
                BigDecimal(random.nextDouble() * 10_000).setScale(5, RoundingMode.FLOOR),
                LocalDate.ofEpochDay(random.nextLong(0, 30000)),
                LocalDateTime.ofEpochSecond(random.nextLong(0, 2524554080), 0, ZoneOffset.UTC)
                    .toDateTime(),
                LocalDateTime.ofEpochSecond(random.nextLong(0, 2524554080), 0, ZoneOffset.UTC)
                    .toSmallDateTime(),
                microsoft.sql.DateTimeOffset.valueOf(
                    Timestamp.valueOf(timestamp.toLocalDateTime()),
                    timestamp.offset.totalSeconds / 60,
                ),
                LocalTime.ofSecondOfDay(random.nextLong(0, 86400)),
            )
        }
    }
}
