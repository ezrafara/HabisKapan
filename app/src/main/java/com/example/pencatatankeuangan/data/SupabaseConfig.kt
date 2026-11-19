
package com.example.pencatatankeuangan.data

object SupabaseConfig {

    // URL project dari Supabase Dashboard
    val url: String = "https://nnrqvmypemgudquiqooh.supabase.co"

    // Gunakan Publishable Key dari Supabase
    // Walaupun nama variabel anonKey, isinya boleh Publishable Key.
    val anonKey: String = "sb_publishable_vylX1IV73HL6pO20gq52_w_2zsph2Bc"

    // Diisi setelah login Supabase Auth berhasil
    @Volatile
    var accessToken: String? = null

    // ID user yang sedang login
    @Volatile
    var userId: String? = null

    val isConfigured: Boolean
        get() = !url.contains("YOUR-PROJECT") &&
                !anonKey.startsWith("YOUR-")
}
