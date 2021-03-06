package org.katis.capnproto.runtime

object StructReader {
  trait Factory {
    type Reader

    def Reader: (SegmentReader,
                        Int,
                        Int,
                        Int,
                        Short,
                        Int) => Reader
  }
}

class StructReader(private[runtime] val _segment: SegmentReader = SegmentReader.EMPTY,
                   private[runtime] val _dataOffset: Int = 0,
                   private[runtime] val _pointers: Int = 0,
                   private[runtime] val _dataSize: Int = 0,
                   private[runtime] val _pointerCount: Short = 1,
                   private[runtime] val _nestingLimit: Int = 0x80000000) {

  protected def _getBooleanField(offset: Int): Boolean = {
    if (offset < this._dataSize) {
      val b = this._segment.buffer.get(this._dataOffset + offset / 8)
      (b & (1 << (offset % 8))) != 0
    } else {
      false
    }
  }

  protected def _getBooleanField(offset: Int, mask: Boolean): Boolean = this._getBooleanField(offset) ^ mask

  protected def _getByteField(offset: Int): Byte = {
    if ((offset + 1) * 8 <= this._dataSize) {
      this._segment.buffer.get(this._dataOffset + offset)
    } else {
      0
    }
  }

  protected def _getByteField(offset: Int, mask: Byte): Byte = {
    (this._getByteField(offset) ^ mask).toByte
  }

  protected def _getShortField(offset: Int): Short = {
    if ((offset + 1) * 16 <= this._dataSize) {
      this._segment.buffer.getShort(this._dataOffset + offset * 2)
    } else {
      0
    }
  }

  protected def _getShortField(offset: Int, mask: Short): Short = {
    (this._getShortField(offset) ^ mask).toShort
  }

  protected def _getIntField(offset: Int): Int = {
    if ((offset + 1) * 32 <= this._dataSize) {
      this._segment.buffer.getInt(this._dataOffset + offset * 4)
    } else {
      0
    }
  }

  protected def _getIntField(offset: Int, mask: Int): Int = this._getIntField(offset) ^ mask

  protected def _getLongField(offset: Int): Long = {
    if ((offset + 1) * 64 <= this._dataSize) {
      this._segment.buffer.getLong(this._dataOffset + offset * 8)
    } else {
      0
    }
  }

  protected def _getLongField(offset: Int, mask: Long): Long = this._getLongField(offset) ^ mask

  protected def _getFloatField(offset: Int): Float = {
    if ((offset + 1) * 32 <= this._dataSize) {
      this._segment.buffer.getFloat(this._dataOffset + offset * 4)
    } else {
      0
    }
  }

  protected def _getFloatField(offset: Int, mask: Int): Float = {
    if ((offset + 1) * 32 <= this._dataSize) {
      java.lang.Float.intBitsToFloat(this._segment.buffer.getInt(this._dataOffset + offset * 4) ^ mask)
    } else {
      java.lang.Float.intBitsToFloat(mask)
    }
  }

  protected def _getDoubleField(offset: Int): Double = {
    if ((offset + 1) * 64 <= this._dataSize) {
      this._segment.buffer.getDouble(this._dataOffset + offset * 8)
    } else {
      0
    }
  }

  protected def _getDoubleField(offset: Int, mask: Long): Double = {
    if ((offset + 1) * 64 <= this._dataSize) {
      java.lang.Double.longBitsToDouble(this._segment.buffer.getLong(this._dataOffset + offset * 8) ^
        mask)
    } else {
      java.lang.Double.longBitsToDouble(mask)
    }
  }

  protected def _pointerFieldIsNull(ptrIndex: Int): Boolean = {
    this._segment.buffer.getLong((this._pointers + ptrIndex) * Constants.BYTES_PER_WORD) == 0
  }

  protected def _getPointerField[T <: PointerFamily : FromPointer](ptrIndex: Int): T#Reader = {
    val factory = implicitly[FromPointer[T]]
    if (ptrIndex < this._pointerCount) {
      factory.fromPointerReader(this._segment, this._pointers + ptrIndex, this._nestingLimit)
    } else {
      factory.fromPointerReader(SegmentReader.EMPTY, 0, this._nestingLimit)
    }
  }

  protected def _getPointerField[T <: PointerFamily : FromPointerRefDefault](ptrIndex: Int,
                                 defaultSegment: SegmentReader,
                                 defaultOffset: Int): T#Reader = {
    val factory = implicitly[FromPointerRefDefault[T]]
    if (ptrIndex < this._pointerCount) {
      factory.fromPointerReaderRefDefault(this._segment, this._pointers + ptrIndex, defaultSegment, defaultOffset,
        this._nestingLimit)
    } else {
      factory.fromPointerReaderRefDefault(SegmentReader.EMPTY, 0, defaultSegment, defaultOffset, this._nestingLimit)
    }
  }

  protected def _getPointerField[T <: PointerFamily : FromPointerBlobDefault](
                                                               ptrIndex: Int,
                                    defaultBuffer: java.nio.ByteBuffer,
                                    defaultOffset: Int,
                                    defaultSize: Int): T#Reader = {
    val factory = implicitly[FromPointerBlobDefault[T]]
    if (ptrIndex < this._pointerCount) {
      factory.fromPointerReaderBlobDefault(this._segment, this._pointers + ptrIndex, defaultBuffer, defaultOffset,
        defaultSize)
    } else {
      factory.fromPointerReaderBlobDefault(SegmentReader.EMPTY, 0, defaultBuffer, defaultOffset, defaultSize)
    }
  }
}
