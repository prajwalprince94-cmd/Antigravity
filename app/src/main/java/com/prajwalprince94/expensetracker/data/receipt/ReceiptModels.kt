package com.prajwalprince94.expensetracker.data.receipt

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * An individual itemized line extracted from a receipt.
 */
data class ReceiptLineItem(
    val name: String,
    val amountPaise: Long
) {
    val formattedAmount: String
        get() = "₹%,.2f".format(Locale.US, amountPaise / 100.0)
}

/**
 * Structured output of the Smart Receipt OCR & Pattern Extraction Engine.
 */
data class ParsedReceipt(
    val id: String = "rcpt_${UUID.randomUUID()}",
    val merchantName: String,
    val totalAmountPaise: Long,
    val formattedDate: String = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
    val suggestedCategory: String = "Food & Dining",
    val suggestedAccount: String = "Paytm / UPI",
    val lineItems: List<ReceiptLineItem> = emptyList(),
    val rawText: String = "",
    val confidence: Float = 0.95f
) {
    val formattedTotalAmount: String
        get() = "₹%,.2f".format(Locale.US, totalAmountPaise / 100.0)
}

/**
 * Curated physical receipt presets for one-tap test scanning and offline demo.
 */
data class ReceiptPreset(
    val name: String,
    val merchant: String,
    val category: String,
    val iconKey: String,
    val rawText: String
) {
    companion object {
        fun defaultPresets(): List<ReceiptPreset> = listOf(
            ReceiptPreset(
                name = "DMart Supermarket",
                merchant = "D-Mart Supermarts",
                category = "Groceries",
                iconKey = "ShoppingCart",
                rawText = """
                    DMART READY
                    Avenue Supermarts Ltd.
                    Store #402, Mumbai
                    Date: 28/10/2026   Time: 18:42
                    --------------------------------
                    1. Amul Taza Milk 1L            68.00
                    2. Quaker Rolled Oats 1kg      185.00
                    3. Borges Extra Olive Oil      650.00
                    4. Tata Sampann Toor Dal 1kg   195.00
                    5. Surf Excel Matic Top Load   420.00
                    6. Cadbury Dark Milk Chocolate  95.00
                    --------------------------------
                    Subtotal:                   1,613.00
                    CGST @ 2.5%:                   40.32
                    SGST @ 2.5%:                   40.32
                    Round Off:                     -0.64
                    Grand Total:                ₹1,693.00
                    Paid via UPI: HDFC Bank
                    Thank you for shopping at DMart!
                """.trimIndent()
            ),
            ReceiptPreset(
                name = "Starbucks Coffee",
                merchant = "Starbucks Coffee",
                category = "Food & Dining",
                iconKey = "Coffee",
                rawText = """
                    STARBUCKS COFFEE
                    Tata Starbucks Pvt Ltd
                    Bandra West, Mumbai
                    Tax Invoice No: SBX-9481
                    Date: 29-Oct-2026  14:15
                    --------------------------------
                    1x Grande Caramel Macchiato    380.00
                    1x Triple Chocolate Cookie     220.00
                    --------------------------------
                    Sub Total:                     600.00
                    CGST @ 2.5%:                    15.00
                    SGST @ 2.5%:                    15.00
                    Total Payable:                 ₹630.00
                    Payment Mode: Paytm QR Code
                    Enjoy your coffee!
                """.trimIndent()
            ),
            ReceiptPreset(
                name = "Shell Fuel Slip",
                merchant = "Shell Fuel Station",
                category = "Transportation",
                iconKey = "LocalGasStation",
                rawText = """
                    SHELL INDIA MARKETS PVT LTD
                    Pump Station #12, Highway Express
                    Date: 27/10/2026  09:30
                    --------------------------------
                    Product: Shell V-Power Petrol
                    Rate: ₹108.40 / Ltr
                    Volume: 19.37 Ltr
                    --------------------------------
                    Net Amount:                  ₹2,100.00
                    Paid via ICICI Credit Card
                    Drive Safe with Shell!
                """.trimIndent()
            ),
            ReceiptPreset(
                name = "Apollo Pharmacy",
                merchant = "Apollo Pharmacy",
                category = "Healthcare",
                iconKey = "FitnessCenter",
                rawText = """
                    APOLLO PHARMACY
                    Apollo Hospitals Enterprise Ltd
                    Date: 26/10/2026
                    --------------------------------
                    1. Becosules Z Capsules 20s    120.00
                    2. Dolo 650mg Paracetamol 15s   32.50
                    3. Hansaplast Bandages 10s      45.00
                    4. Cetaphil Gentle Cleanser    380.00
                    Discount:                      -37.50
                    --------------------------------
                    Net Payable Amount:           ₹540.00
                    Payment: Google Pay UPI
                    Get Well Soon!
                """.trimIndent()
            ),
            ReceiptPreset(
                name = "Reliance Digital",
                merchant = "Reliance Digital Gadgets",
                category = "Shopping",
                iconKey = "PhoneAndroid",
                rawText = """
                    RELIANCE DIGITAL
                    Reliance Retail Limited
                    Date: 25-Oct-2026
                    --------------------------------
                    1. Anker 30W USB-C GaN Charger 999.00
                    2. Braided Type-C Fast Cable   399.00
                    --------------------------------
                    Taxable Value:                1,184.75
                    GST Total @ 18%:                213.25
                    Invoice Total:               ₹1,398.00
                    Paid via SBI Debit Card
                """.trimIndent()
            )
        )
    }
}
