package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.repository.AxioRepository
import com.example.util.SmsExpenseParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Automatically captures incoming SMS messages sent by Indian Banks / UPI.
 * Parses transaction amount, merchant, and bank account in real-time,
 * and inserts it directly into the local AppDatabase.
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val fullBodyBuilder = StringBuilder()
            var sender = ""

            for (sms in messages) {
                if (sms != null) {
                    if (sender.isEmpty()) {
                        sender = sms.displayOriginatingAddress ?: sms.originatingAddress ?: ""
                    }
                    fullBodyBuilder.append(sms.displayMessageBody ?: sms.messageBody ?: "")
                }
            }

            val fullSms = fullBodyBuilder.toString()
            if (fullSms.isBlank()) return

            Log.d("SmsBroadcastReceiver", "Incoming SMS from $sender: $fullSms")

            val parsedResult = SmsExpenseParser.parseSms(fullSms, sender)
            if (parsedResult != null) {
                Log.d("SmsBroadcastReceiver", "Auto-parsed SMS successfully: ₹${parsedResult.amount} at ${parsedResult.merchantOrSender}")
                val db = AppDatabase.getDatabase(context.applicationContext)
                val repository = AxioRepository(db)

                // Insert in background coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repository.processParsedSms(parsedResult)
                    } catch (e: Exception) {
                        Log.e("SmsBroadcastReceiver", "Failed to save parsed SMS transaction", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsBroadcastReceiver", "Error processing received SMS", e)
        }
    }
}
