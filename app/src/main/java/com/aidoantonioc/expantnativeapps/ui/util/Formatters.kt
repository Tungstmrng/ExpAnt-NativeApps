package com.aidoantonioc.expantnativeapps.ui.util

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
        .apply { maximumFractionDigits = 0 }
        .format(amount)