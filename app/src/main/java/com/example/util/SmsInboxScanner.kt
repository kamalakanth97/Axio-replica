package com.example.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log

data class SmsSyncReport(
    val totalScanned: Int,
    val totalParsed: Int,
    val newTransactionsAdded: Int,
    val totalExpenseParsed: Double,
    val totalIncomeParsed: Double
)

object SmsInboxScanner {

    private const val TAG = "SmsInboxScanner"

    /**
     * Reads SMS from Android's SMS inbox content provider.
     * Searches messages received in the last `daysBack` days.
     */
    fun readInboxSms(
        context: Context,
        daysBack: Int = 90,
        limit: Int = 500
    ): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        val contentResolver = context.contentResolver
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI

        val timeCutoff = System.currentTimeMillis() - (daysBack.toLong() * 24 * 60 * 60 * 1000L)
        val selection = "${Telephony.Sms.DATE} >= ?"
        val selectionArgs = arrayOf(timeCutoff.toString())
        val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit"

        try {
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)

                while (it.moveToNext()) {
                    val address = if (addressIdx != -1) it.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    if (body.isNotBlank()) {
                        result.add(Pair(address, body))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Missing READ_SMS permission", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS inbox", e)
        }

        return result
    }
}
