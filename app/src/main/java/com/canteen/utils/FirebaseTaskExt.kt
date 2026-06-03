package com.canteen.utils

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

suspend fun <T> Task<T>.awaitResult(): Result<T> =
    runCatching { await() }
