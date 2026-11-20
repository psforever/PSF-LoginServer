// Copyright (c) 2026 PSForever
package net.psforever.packet;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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

    /*
     * ByteVector.AtArray and ByteVector.Buffer are private to Java, though public classes to the JVM,
     * so their accessors are reached through method handles. Constant handles inline like calls.
     * Unset (left Object) if this scodec version lacks them: those vectors then take the slow path.
     */
    private static final Class<?> AT_ARRAY = classOrObject("scodec.bits.ByteVector$AtArray");
    private static final MethodHandle AT_ARRAY_ARR = accessor(AT_ARRAY, "arr", byte[].class);
    private static final Class<?> AT_BYTE_BUFFER = classOrObject("scodec.bits.ByteVector$AtByteBuffer");
    private static final MethodHandle AT_BYTE_BUFFER_BUF = accessor(AT_BYTE_BUFFER, "buf", java.nio.ByteBuffer.class);
    private static final Class<?> BUFFER = classOrObject("scodec.bits.ByteVector$Buffer");
    private static final MethodHandle BUFFER_HD = accessor(BUFFER, "hd", ByteVector.class);
    private static final MethodHandle BUFFER_LAST = accessor(BUFFER, "lastChunk", byte[].class);

    private static Class<?> classOrObject(String name) {
        try {
            return Class.forName(name, false, ByteVector.class.getClassLoader());
        } catch (ReflectiveOperationException e) {
            return Object.class;
        }
    }

    /** (Object) -> type, calling the named accessor; or, if absent, one that throws */
    private static MethodHandle accessor(Class<?> cls, String name, Class<?> type) {
        MethodType erased = MethodType.methodType(type, Object.class);
        try {
            return MethodHandles.publicLookup().findVirtual(cls, name, MethodType.methodType(type)).asType(erased);
        } catch (ReflectiveOperationException e) {
            MethodHandle fail = MethodHandles.throwException(type, IllegalStateException.class)
                .bindTo(new IllegalStateException(cls.getName() + "." + name));
            return MethodHandles.dropArguments(fail, 0, Object.class);
        }
    }

    private static long copyBits(ByteVector src, long from, long n, byte[] out, long pos) {
        if (n == 0) return pos;
        return copyTree(src, 0L, from, from + n, out, pos);
    }

    /**
     * SCodec tree visitor for the bytes under a BitVector. ByteVector.copyToArray and apply would do,
     * but copyToArray allocates a closure, refs, and list cells per call, and apply walks from the root
     * per byte. Copies bits [from, end) of the vector, which starts at bit base of the whole, to pos.
     */
    private static long copyTree(ByteVector v, long base, long from, long end, byte[] out, long pos) {
        try {
            while (pos >= 0) {
                long vEnd = base + (v.size() << 3);
                if (vEnd <= from || base >= end) return pos; // nothing of the range here
                long lo = Math.max(from, base); // the range's part of v, relative to the whole
                long n = Math.min(end, vEnd) - lo;
                if (v instanceof ByteVector.Append) {
                    ByteVector.Append a = (ByteVector.Append) v;
                    pos = copyTree(a.left(), base, from, end, out, pos);
                    base += a.left().size() << 3;
                    v = a.right(); // iterate down the right spine instead of recursing
                } else if (v instanceof ByteVector.Chunk) {
                    ByteVector.View view = ((ByteVector.Chunk) v).bytes();
                    Object at = view.at();
                    if (at.getClass() == AT_ARRAY) {
                        byte[] arr = (byte[]) AT_ARRAY_ARR.invokeExact(at);
                        return copyArrayBits(arr, (view.offset() << 3) + lo - base, n, out, pos);
                    }
                    if (at.getClass() == AT_BYTE_BUFFER) { // as BitVector.fromInt and fromLong make
                        java.nio.ByteBuffer buf = (java.nio.ByteBuffer) AT_BYTE_BUFFER_BUF.invokeExact(at);
                        if (buf.hasArray()) {
                            long start = (long) buf.arrayOffset() + view.offset();
                            return copyArrayBits(buf.array(), (start << 3) + lo - base, n, out, pos);
                        }
                    }
                    // a direct or read-only ByteBuffer: copy out the bytes the range covers, then the bits
                    long first = (lo - base) >>> 3;
                    byte[] tmp = new byte[(int) ((((lo - base) + n + 7) >>> 3) - first)];
                    view.copyToArray(tmp, 0, first, tmp.length);
                    return copyArrayBits(tmp, (lo - base) & 7, n, out, pos);
                } else if (v instanceof ByteVector.Chunks) {
                    v = ((ByteVector.Chunks) v).chunks();
                } else if (v.getClass() == BUFFER) {
                    // hd, then the first lastSize bytes of lastChunk
                    ByteVector hd = (ByteVector) BUFFER_HD.invokeExact((Object) v);
                    pos = copyTree(hd, base, from, end, out, pos);
                    base += hd.size() << 3;
                    lo = Math.max(from, base);
                    n = Math.min(end, vEnd) - lo;
                    if (pos < 0 || n <= 0) return pos;
                    return copyArrayBits((byte[]) BUFFER_LAST.invokeExact((Object) v), lo - base, n, out, pos);
                } else {
                    return -1;
                }
            }
            return -1;
        } catch (Throwable t) {
            return -1; // an accessor this scodec version lacks: the caller falls back to scodec
        }
    }

    // Yeah unaligned bit copies aren't pretty
    private static long copyArrayBits(byte[] src, long from, long n, byte[] out, long pos) {
        if ((from & 7) == 0 && (pos & 7) == 0) {
            int whole = (int) (n >>> 3);
            if (whole > 0) System.arraycopy(src, (int) (from >>> 3), out, (int) (pos >>> 3), whole);
            long done = (long) whole << 3;
            if (done < n) {
                int rest = (int) (n - done);
                int b = src[(int) ((from + done) >>> 3)] & 0xff;
                out[(int) ((pos + done) >>> 3)] = (byte) (b & (0xff << (8 - rest)));
            }
            return pos + n;
        }
        long left = n;
        while (left > 0) {
            int take = (int) Math.min(8, left);
            int bit = (int) (from & 7);
            int hi = src[(int) (from >>> 3)] & 0xff;
            int lo = bit + take > 8 ? src[(int) (from >>> 3) + 1] & 0xff : 0;
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
