package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.Pembelian
import com.example.pencatatankeuangan.model.PembelianRequest

class PurchaseRepository {

    private val daftarPembelian = mutableListOf<Pembelian>()

    private var nextId = 1L

    // READ: mengambil semua pembelian
    suspend fun getAll(): List<Pembelian> {
        return daftarPembelian.toList()
    }

    // READ: mengambil detail berdasarkan ID
    suspend fun getById(id: Long): Pembelian? {
        return daftarPembelian.find {
            it.id == id
        }
    }

    // CREATE: menambahkan pembelian
    suspend fun add(request: PembelianRequest) {
        val pembelian = Pembelian(
            id = nextId++,
            namaProduk = request.namaProduk,
            merek = request.merek,
            kategori = request.kategori,
            toko = request.toko,
            tanggal = request.tanggal,
            jumlah = request.jumlah,
            hargaPerItem = request.hargaPerItem,
            imageUrl = request.imageUrl
        )

        daftarPembelian.add(pembelian)
    }

    // UPDATE: mengubah pembelian
    suspend fun update(
        id: Long,
        request: PembelianRequest
    ) {
        val index = daftarPembelian.indexOfFirst {
            it.id == id
        }

        if (index != -1) {
            daftarPembelian[index] = Pembelian(
                id = id,
                namaProduk = request.namaProduk,
                merek = request.merek,
                kategori = request.kategori,
                toko = request.toko,
                tanggal = request.tanggal,
                jumlah = request.jumlah,
                hargaPerItem = request.hargaPerItem,
                imageUrl = request.imageUrl
            )
        }
    }

    // DELETE: menghapus pembelian
    suspend fun delete(id: Long) {
        daftarPembelian.removeAll {
            it.id == id
        }
    }
}
