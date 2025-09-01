package com.calikot.mysavingquest.db.client

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object connection {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://xyzcompany.supabase.co",
        supabaseKey = "your_public_anon_key"
    ) {
        install(Postgrest)
    }
}