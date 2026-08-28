package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TransactionType
import com.example.util.RupeeFormatter
import com.example.util.SmsExpenseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("axio - Expense Tracker", appName)
  }

  @Test
  fun `test rupee formatting in INR`() {
    val formatted = RupeeFormatter.formatRupees(123450.00)
    assertTrue(formatted.contains("₹") || formatted.contains("INR"))
  }

  @Test
  fun `test SMS parsing for Indian Bank SMS`() {
    val sms = "Rs. 450.00 debited from A/c XX4092 on 26-AUG-26 by UPI to SWIGGY. Info: UPI/32810928/Swiggy. Bal: Rs. 1,62,800.00"
    val result = SmsExpenseParser.parseSms(sms)
    assertNotNull(result)
    assertEquals(450.00, result!!.amount, 0.01)
    assertEquals(TransactionType.EXPENSE, result.type)
  }

  @Test
  fun `test UPI intent URI builder`() {
    val details = com.example.util.UpiPaymentDetails(
      vpa = "swiggy@icici",
      name = "Swiggy India",
      amount = 450.0,
      note = "Dinner"
    )
    val uri = com.example.util.UpiIntentHelper.buildUpiUri(details)
    val uriString = uri.toString()
    assertTrue(uriString.startsWith("upi://pay"))
    assertTrue(uriString.contains("pa=swiggy%40icici") || uriString.contains("pa=swiggy@icici"))
    assertTrue(uriString.contains("am=450.00"))
    assertTrue(uriString.contains("cu=INR"))
  }
}
