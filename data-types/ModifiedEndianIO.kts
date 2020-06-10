@file:Suppress("NAME_SHADOWING")

import java.nio.ByteBuffer

enum class Endian {
    BIG,
    LITTLE,
    MIDDLE;

    fun getRange(modifier: Modifier, byteCount: Int) = when (this) {
        BIG -> byteCount - 1 downTo 0
        LITTLE -> 0 until byteCount
        MIDDLE -> if (modifier == Modifier.INVERSE) MID_ENDIAN_INVERSE else MID_ENDIAN_ORDER
    }

    companion object {
        private val MID_ENDIAN_ORDER = listOf(1, 0, 3, 2)
        private val MID_ENDIAN_INVERSE = MID_ENDIAN_ORDER.reversed()
    }
}

enum class Modifier {
    NONE,
    ADD,
    INVERSE,
    SUBTRACT;
}

enum class DataType(val byteCount: Int) {
    BYTE(1),
    SHORT(2),
    MEDIUM(3),
    INT(4),
    LONG(8);
}

@Throws(IllegalStateException::class)
fun check(type: DataType, modifier: Modifier, order: Endian) {
    if (order == Endian.MIDDLE) {
        check(modifier == Modifier.NONE || modifier == Modifier.INVERSE) {
            "Middle endian doesn't support variable modifier $modifier"
        }
        check(type == DataType.INT) {
            "Middle endian can only be used with an integer"
        }
    }
}

fun read(buffer: ByteBuffer, type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long {
    check(buffer.remaining() >= type.byteCount) {
        "Not enough allocated buffer remaining $type."
    }

    check(type, modifier, order)

    var longValue: Long = 0
    var read: Int
    for (index in order.getRange(modifier, type.byteCount)) {
        read = buffer.get().toInt()
        read = when (if(index == 0 && order != Endian.MIDDLE) modifier else Modifier.NONE) {
            Modifier.ADD -> read - 128 and 0xff
            Modifier.INVERSE -> -read and 0xff
            Modifier.SUBTRACT -> 128 - read and 0xff
            else -> read and 0xff shl index * 8
        }
        longValue = longValue or read.toLong()
    }
    return longValue
}

fun write(buffer: ByteBuffer, type: DataType, value: Number, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG) {
    check(type, modifier, order)

    for (index in order.getRange(modifier, type.byteCount)) {
        val modifiedValue = when (if (index == 0 && order != Endian.MIDDLE) modifier else Modifier.NONE) {
            Modifier.ADD -> value.toInt() + 128
            Modifier.INVERSE -> -value.toInt()
            Modifier.SUBTRACT -> 128 - value.toInt()
            else -> (value.toLong() shr index * 8).toInt()
        }
        buffer.put(modifiedValue.toByte())
    }
}