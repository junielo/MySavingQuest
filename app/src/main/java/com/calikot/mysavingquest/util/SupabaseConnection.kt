package com.calikot.mysavingquest.util

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object Connections {
    val supabase = createSupabaseClient(
        supabaseUrl = "Project URL",
        supabaseKey = "Anon key"
    ) {
        install(Postgrest)
    }
}