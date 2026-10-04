package com.example.data.api

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    const val SUPABASE_URL = "https://osbkhcivmhsgpccxxwcv.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_6oYQAaGae7EAnKbr2dkyPg_2CB32Vt8"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
