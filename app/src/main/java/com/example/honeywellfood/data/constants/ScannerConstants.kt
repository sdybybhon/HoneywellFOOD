package com.example.honeywellfood.data.constants

object ScannerConstants {
    object Time {
        const val MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24L
        const val DAYS_IN_WEEK = 7
        const val DAYS_IN_MONTH = 30

        const val SCAN_RESTART_DELAY_MS = 1500L
    }

    object Scanner {
        const val ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER"
        const val ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER"
        const val ACTION_CONTROL_SCANNER = "com.honeywell.aidc.action.ACTION_CONTROL_SCANNER"
        const val EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER"
        const val EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE"
        const val EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES"
        const val EXTRA_SCAN = "com.honeywell.aidc.extra.EXTRA_SCAN"
        const val SCANNER_TYPE = "dcs.scanner.imager"
        const val PROFILE_NAME = "DEFAULT"

        const val BARCODE_TYPE_EAN13 = "d"
        const val BARCODE_TYPE_UPCA = "c"
        const val BARCODE_TYPE_CODE128 = "j"
        const val BARCODE_TYPE_CODE39 = "b"
        const val BARCODE_TYPE_QR = "s"
        const val BARCODE_TYPE_PDF417 = "r"
        const val BARCODE_TYPE_DATAMATRIX = "w"
        const val BARCODE_TYPE_AZTEC = "z"

        const val EAN13_LENGTH_WITHOUT_CHECKSUM = 12
        const val MIN_BARCODE_LENGTH = 8
    }

    object Api {
        const val CACHE_TAG = "ScanRepository"
        const val STATUS_SUCCESS = "success"
    }

    object UI {
        const val NO_CATEGORY = "Без категории"
        const val DATE_FORMAT = "dd.MM.yyyy"
    }

    object Expiry {
        const val EXPIRED = 0
        const val LESS_THAN_7_DAYS = 7
        const val LESS_THAN_30_DAYS = 30
    }

    object LogTags {
        const val SCAN_REPOSITORY = "ScanRepository"
        const val MAIN_FRAGMENT = "MainFragment"
    }

}