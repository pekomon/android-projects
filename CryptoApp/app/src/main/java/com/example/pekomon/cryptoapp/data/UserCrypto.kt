package com.example.pekomon.cryptoapp.data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class UserCrypto(
    val cryptoId: String,
    val amount: Double,
    val purchasePrice: Double,
    @Serializable(with = LocalDateTimeSerializer::class)
    val purchaseDateTime: LocalDateTime,
    val transactions: List<Transaction> = listOf()
)

@Serializable
data class Transaction(
    val type: TransactionType,
    val amount: Double,
    val price: Double,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateTime: LocalDateTime
)

@Serializable
enum class TransactionType {
    BUY, SELL
}

// LocalDateTime serializer
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString())
    }
} 