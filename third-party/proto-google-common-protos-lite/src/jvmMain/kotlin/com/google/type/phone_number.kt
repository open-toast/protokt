@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/type/phone_number.proto
package com.google.type

import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.sizeof
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit

/**
 * An object representing a phone number, suitable as an API wire format.
 *
 *  This representation:
 *
 *   - should not be used for locale-specific formatting of a phone number, such    as "+1 (650)
 * 253-0000 ext. 123"
 *
 *   - is not designed for efficient storage  - may not be suitable for dialing - specialized
 * libraries (see references)    should be used to parse the number for that purpose
 *
 *  To do something meaningful with this number, such as format it for various use-cases, convert it
 * to an `i18n.phonenumbers.PhoneNumber` object first.
 *
 *  For instance, in Java this would be:
 *
 *     com.google.type.PhoneNumber wireProto =
 * com.google.type.PhoneNumber.newBuilder().build();
 * com.google.i18n.phonenumbers.Phonenumber.PhoneNumber phoneNumber =
 * PhoneNumberUtil.getInstance().parse(wireProto.getE164Number(), "ZZ");    if
 * (!wireProto.getExtension().isEmpty()) {      phoneNumber.setExtension(wireProto.getExtension());
 * }
 *
 *   Reference(s):   - https://github.com/google/libphonenumber
 */
@Deprecated("use v1")
@KtGeneratedMessage("google.type.PhoneNumber")
class PhoneNumber private constructor(
    /**
     * Required.  Either a regular number, or a short code.  New fields may be added to the oneof
     * below in the future, so clients should ignore phone numbers for which none of the fields they
     * coded against are set.
     */
    val kind: Kind?,
    /**
     * The phone number's extension. The extension is not standardized in ITU recommendations,
     * except for being defined as a series of numbers with a maximum length of 40 digits. Other than
     * digits, some other dialing characters such as ',' (indicating a wait) or '#' may be stored here.
     *
     *
     *  Note that no regions currently use extensions with short codes, so this field is normally
     * only set in conjunction with an E.164 number. It is held separately from the E.164 number to
     * allow for short code extensions in the future.
     */
    val extension: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        when (kind) {
            is PhoneNumber.Kind.E164Number -> {
                result += sizeof(Tag(1)) + sizeof(kind.e164Number)}
            is PhoneNumber.Kind.ShortCode -> {
                result += sizeof(Tag(2)) + sizeof(kind.shortCode)}
            null -> Unit
        }
        if (extension.isNotEmpty()) {
            result += sizeof(Tag(3)) + sizeof(extension)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        when (kind) {
            is PhoneNumber.Kind.E164Number -> {
                serializer.write(Tag(10)).write(kind.e164Number)
            }
            is PhoneNumber.Kind.ShortCode -> {
                serializer.write(Tag(18)).write(kind.shortCode)
            }
            null -> Unit
        }
        if (extension.isNotEmpty()) {
            serializer.write(Tag(26)).write(extension)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is PhoneNumber &&
        other.kind == kind &&
        other.extension == extension &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + kind.hashCode()
        result = 31 * result + extension.hashCode()
        return result
    }

    override fun toString(): String = "PhoneNumber(" +
        "kind=$kind, " +
        "extension=$extension" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: PhoneNumberDsl.() -> Unit): PhoneNumber = PhoneNumber.Deserializer {
        kind = this@PhoneNumber.kind
        extension = this@PhoneNumber.extension
        unknownFields = this@PhoneNumber.unknownFields
        dsl()
    }

    sealed class Kind {
        /**
         * The phone number, represented as a leading plus sign ('+'), followed by a phone number
         * that uses a relaxed ITU E.164 format consisting of the country calling code (1 to 3 digits)
         * and the subscriber number, with no additional spaces or formatting, e.g.:  - correct:
         * "+15552220123"  - incorrect: "+1 (555) 222-01234 x123".
         *
         *  The ITU E.164 format limits the latter to 12 digits, but in practice not all countries
         * respect that, so we relax that restriction here. National-only numbers are not allowed.
         *
         *  References:  - https://www.itu.int/rec/T-REC-E.164-201011-I  -
         * https://en.wikipedia.org/wiki/E.164.  -
         * https://en.wikipedia.org/wiki/List_of_country_calling_codes
         */
        data class E164Number(
            val e164Number: String,
        ) : Kind()

        /**
         * A short code.
         *
         *  Reference(s):  - https://en.wikipedia.org/wiki/Short_code
         */
        data class ShortCode(
            val shortCode: PhoneNumber.ShortCode,
        ) : Kind()
    }

    class PhoneNumberDsl {
        var kind: Kind? = null

        var extension: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): PhoneNumber = PhoneNumber(kind,
        extension,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<PhoneNumber>,
            (PhoneNumberDsl.() -> Unit) -> PhoneNumber {
        override fun deserialize(deserializer: KtMessageDeserializer): PhoneNumber {
            var kind : Kind? = null
            var extension = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return PhoneNumber(kind,
                            extension,
                            UnknownFieldSet.from(unknownFields))
                    10 -> kind = Kind.E164Number(deserializer.readString())
                    18 -> kind =
                            Kind.ShortCode(deserializer.readMessage(com.google.type.PhoneNumber.ShortCode))
                    26 -> extension = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: PhoneNumberDsl.() -> Unit): PhoneNumber =
                PhoneNumberDsl().apply(dsl).build()
    }

    /**
     * An object representing a short code, which is a phone number that is typically much shorter
     * than regular phone numbers and can be used to address messages in MMS and SMS systems, as well
     * as for abbreviated dialing (e.g. "Text 611 to see how many minutes you have remaining on your
     * plan.").
     *
     *  Short codes are restricted to a region and are not internationally dialable, which means the
     * same short code can exist in different regions, with different usage and pricing, even if those
     * regions share the same country calling code (e.g. US and CA).
     */
    @KtGeneratedMessage("google.type.ShortCode")
    class ShortCode private constructor(
        /**
         * Required. The BCP-47 region code of the location where calls to this short code can be
         * made, such as "US" and "BB".
         *
         *  Reference(s):  - http://www.unicode.org/reports/tr35/#unicode_region_subtag
         */
        val regionCode: String,
        /**
         * Required. The short code digits, without a leading plus ('+') or country calling code,
         * e.g. "611".
         */
        val number: String,
        val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
    ) : KtMessage {
        override val messageSize: Int by lazy { messageSize() }

        private fun messageSize(): Int {
            var result = 0
            if (regionCode.isNotEmpty()) {
                result += sizeof(Tag(1)) + sizeof(regionCode)
            }
            if (number.isNotEmpty()) {
                result += sizeof(Tag(2)) + sizeof(number)
            }
            result += unknownFields.size()
            return result
        }

        override fun serialize(serializer: KtMessageSerializer) {
            if (regionCode.isNotEmpty()) {
                serializer.write(Tag(10)).write(regionCode)
            }
            if (number.isNotEmpty()) {
                serializer.write(Tag(18)).write(number)
            }
            serializer.writeUnknown(unknownFields)
        }

        override fun equals(other: Any?): Boolean = other is ShortCode &&
            other.regionCode == regionCode &&
            other.number == number &&
            other.unknownFields == unknownFields

        override fun hashCode(): Int {
            var result = unknownFields.hashCode()
            result = 31 * result + regionCode.hashCode()
            result = 31 * result + number.hashCode()
            return result
        }

        override fun toString(): String = "ShortCode(" +
            "regionCode=$regionCode, " +
            "number=$number" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

        fun copy(dsl: ShortCodeDsl.() -> Unit): ShortCode = ShortCode.Deserializer {
            regionCode = this@ShortCode.regionCode
            number = this@ShortCode.number
            unknownFields = this@ShortCode.unknownFields
            dsl()
        }

        class ShortCodeDsl {
            var regionCode: String = ""

            var number: String = ""

            var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

            fun build(): ShortCode = ShortCode(regionCode,
            number,
             unknownFields)
        }

        companion object Deserializer : KtDeserializer<ShortCode>,
                (ShortCodeDsl.() -> Unit) -> ShortCode {
            override fun deserialize(deserializer: KtMessageDeserializer): ShortCode {
                var regionCode = ""
                var number = ""
                var unknownFields: UnknownFieldSet.Builder? = null
                while (true) {
                    when(deserializer.readTag()) {
                        0 -> return ShortCode(regionCode,
                                number,
                                UnknownFieldSet.from(unknownFields))
                        10 -> regionCode = deserializer.readString()
                        18 -> number = deserializer.readString()
                        else -> unknownFields = (unknownFields ?:
                                UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown())
                                }
                    }
                }
            }

            override fun invoke(dsl: ShortCodeDsl.() -> Unit): ShortCode =
                    ShortCodeDsl().apply(dsl).build()
        }
    }
}
