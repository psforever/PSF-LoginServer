// Copyright (c) 2026 PSForever
package net.psforever.packet;

import scodec.bits.BitVector;
import scodec.bits.ByteVector;

/**
 * Flattens a scodec {@link BitVector} into a flat byte array via a tree walk.
 *
 * Greatly reduces temporary allocations done by SCodec in the name of immutability,
 * lowering GC pressure.
 * Must be in Java because SCodec keeps it's fields private.
 */
public final class FastBits {
    private FastBits() {}

    /** The same bits as {@code v.compact()} */
    public static BitVector compact(BitVector v) {
        byte[] bytes = toByteArray(v);
        return bytes == null ? v.compact() : BitVector.view(bytes, v.size());
    }

    /** The same bytes as {@code v.toByteVector()} */
    public static ByteVector toByteVector(BitVector v) {
        byte[] bytes = toByteArray(v);
        return bytes == null ? v.toByteVector() : ByteVector.view(bytes);
    }

    static byte[] toByteArray(BitVector v) {
        long size = v.size();
        if (size > (long) Integer.MAX_VALUE * 8) return null;
        byte[] out = new byte[(int) ((size + 7) >>> 3)];
        long end = write(v, out, 0L);
        return end == size ? out : null;
    }

    /** SCodec tree visitor */
    private static long write(BitVector v, byte[] out, long pos) {
        while (pos >= 0) {
            if (v instanceof BitVector.Append) {
                BitVector.Append a = (BitVector.Append) v;
                pos = write(a.left(), out, pos);
                v = a.right(); // iterate down the right spine instead of recursing
            } else if (v instanceof BitVector.Bytes) {
                BitVector.Bytes b = (BitVector.Bytes) v;
                return copyBits(b.underlying(), 0L, b.size(), out, pos);
            } else if (v instanceof BitVector.Drop) {
                BitVector.Drop d = (BitVector.Drop) v;
                return copyBits(d.underlying().underlying(), d.m(), d.size(), out, pos);
            } else if (v instanceof BitVector.Chunks) {
                v = ((BitVector.Chunks) v).chunks();
            } else if (v instanceof BitVector.Suspend) {
                v = ((BitVector.Suspend) v).underlying();
            } else {
                return -1;
            }
        }
        return -1;
    }

    // Yeah unaligned bit copies aren't pretty
    private static long copyBits(ByteVector src, long from, long n, byte[] out, long pos) {
        if (n == 0) return pos;
        if ((from & 7) == 0 && (pos & 7) == 0) {
            int whole = (int) (n >>> 3);
            if (whole > 0) src.copyToArray(out, (int) (pos >>> 3), from >>> 3, whole);
            long done = (long) whole << 3;
            if (done < n) {
                int rest = (int) (n - done);
                int b = src.apply((from + done) >>> 3) & 0xff;
                out[(int) ((pos + done) >>> 3)] = (byte) (b & (0xff << (8 - rest)));
            }
            return pos + n;
        }
        long left = n;
        while (left > 0) {
            int take = (int) Math.min(8, left);
            int bit = (int) (from & 7);
            int hi = src.apply(from >>> 3) & 0xff;
            int lo = bit + take > 8 ? src.apply((from >>> 3) + 1) & 0xff : 0;
            int value = (((hi << 8) | lo) >>> (16 - bit - take)) & ((1 << take) - 1);
            int at = (int) (pos >>> 3);
            int off = (int) (pos & 7);
            int shifted = value << (16 - off - take);
            out[at] |= (byte) (shifted >>> 8);
            if (off + take > 8) out[at + 1] |= (byte) shifted;
            from += take;
            pos += take;
            left -= take;
        }
        return pos;
    }
}
