package org.snappy.postgresql.notify

import org.postgresql.PGConnection
import org.snappy.execute.execute
import org.snappy.execute.executeSuspend
import org.snappy.postgresql.listen.validateChannelName
import java.sql.Connection

fun <C> C.notify(channelName: String, message: String? = null)
where
    C : PGConnection,
    C : Connection
{
    validateChannelName(channelName)
    this.execute("NOTIFY $channelName, '${message?.replace("'", "''")}'")
}

suspend fun <C> C.notifySuspend(channelName: String, message: String? = null)
where
    C : PGConnection,
    C : Connection
{
    validateChannelName(channelName)
    this.executeSuspend("NOTIFY $channelName, '${message?.replace("'", "''")}'")
}