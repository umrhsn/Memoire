package com.umrhsn.mmoire.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

/**
 * A custom contract that allows picking multiple visual media with a dynamic limit.
 */
class PickMultipleVisualMediaWithLimit :
    ActivityResultContract<PickMultipleVisualMediaWithLimit.Request, List<Uri>>() {

    data class Request(
        val visualMediaRequest: PickVisualMediaRequest,
        val maxItems: Int
    )

    override fun createIntent(context: Context, input: Request): Intent {
        return ActivityResultContracts.PickMultipleVisualMedia(input.maxItems)
            .createIntent(context, input.visualMediaRequest)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            ActivityResultContracts.PickMultipleVisualMedia().parseResult(resultCode, intent)
        } else {
            emptyList()
        }
    }
}
