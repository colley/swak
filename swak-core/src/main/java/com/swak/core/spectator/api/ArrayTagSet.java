
package com.swak.core.spectator.api;

import com.swak.core.spectator.impl.Preconditions;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * An immutable set of tags sorted by the tag key.
 */
final class ArrayTagSet implements TagList {

  /** Empty tag set. */
  static final ArrayTagSet EMPTY = new ArrayTagSet(new String[0]);

  /** Create a new tag set. */
  static ArrayTagSet create(String... tags) {
    return EMPTY.addAll(tags);
  }

  /** Create a new tag set. */
  static ArrayTagSet create(Tag... tags) {
    return EMPTY.addAll(tags);
  }

  /** Create a new tag set. */
  static ArrayTagSet create(Iterable<Tag> tags) {
    return (tags instanceof ArrayTagSet) ? (ArrayTagSet) tags : EMPTY.addAll(tags);
  }

  /** Create a new tag set. */
  static ArrayTagSet create(Map<String, String> tags) {
    return EMPTY.addAll(tags);
  }

  /**
   * This method can be used to get better performance for some critical use-cases, but also
   * has increased risk. If there is any doubt, then use {@link #create(Tag...)} instead.
   *
   * <p>Create a new tag set based on the provided array. The provided array will be used
   * so it should not be modified after. Caller must ensure that:</p>
   *
   * <ul>
   *   <li>Length of tags array is even.</li>
   *   <li>There are no null values for the first length entries in the array.</li>
   * </ul>
   */
  static ArrayTagSet unsafeCreate(String[] tags, int length) {
    insertionSort(tags, length);
    int len = dedupInPlace(tags, length);
    return new ArrayTagSet(tags, len);
  }

  private final String[] tags;
  private final int length;

  private int cachedHashCode;

  private ArrayTagSet(String[] tags) {
    this(tags, tags.length);
  }

  private ArrayTagSet(String[] tags, int length) {
    if (tags.length % 2 != 0) {
      throw new IllegalArgumentException("length of tags array must be even");
    }
    if (length > tags.length) {
      throw new IllegalArgumentException("length cannot be larger than tags array");
    }
    this.tags = tags;
    this.length = length;
    this.cachedHashCode = 0;
  }

  /** Check if this set is empty. */
  boolean isEmpty() {
    return length == 0;
  }

  /** Add a new tag to the set. */
  ArrayTagSet add(String k, String v) {
    Preconditions.checkNotNull(k, "key");
    Preconditions.checkNotNull(v, "value");
    if (length == 0) {
      return new ArrayTagSet(new String[] {k, v});
    }

    int idx = 0;
    int cmp = -1;
    while (idx < length) {
      cmp = tags[idx].compareTo(k);
      if (cmp >= 0) {
        break;
      }
      idx += 2;
    }

    // Update an existing tag
    if (cmp == 0) {
      if (tags[idx + 1].equals(v)) {
        return this;
      }
      String[] newTags = Arrays.copyOf(tags, length);
      newTags[idx + 1] = v;
      return new ArrayTagSet(newTags);
    }

    String[] newTags = new String[length + 2];
    newTags[idx] = k;
    newTags[idx + 1] = v;

    System.arraycopy(tags, 0, newTags, 0, idx);
    System.arraycopy(tags, idx, newTags, idx + 2, length - idx);
    return new ArrayTagSet(newTags);
  }

  /** Add a new tag to the set. */
  ArrayTagSet add(Tag tag) {
    return add(tag.key(), tag.value());
  }

  /** Add a collection of tags to the set. */
  ArrayTagSet addAll(Iterable<Tag> ts) {
    if (ts instanceof Collection) {
      Collection<Tag> data = (Collection<Tag>) ts;
      if (data.isEmpty()) {
        return this;
      } else {
        String[] newTags = new String[2 * data.size()];
        int i = 0;
        for (Tag t : data) {
          newTags[i++] = t.key();
          newTags[i++] = t.value();
        }
        checkForNullValues(newTags);
        return addAll(newTags, newTags.length);
      }
    } else if (ts instanceof TagList) {
      if (ts instanceof ArrayTagSet) {
        return addAll((ArrayTagSet) ts);
      }
     return addAll((TagList) ts);
    } else {
      List<Tag> data = new ArrayList<>();
      for (Tag t : ts) {
        data.add(t);
      }
      return addAll(data);
    }
  }

  /** Add a collection of tags to the set. */
  ArrayTagSet addAll(Map<String, String> ts) {
    if (ts.isEmpty()) {
      return this;
    } else if (ts instanceof ConcurrentMap) {
      // Special case ConcurrentMaps to avoid propagating errors if there is a bug in the caller
      // and the map is mutated while being added:
      // https://github.com/Netflix/spectator/issues/733
      List<Tag> data = new ArrayList<>(ts.size());
      for (Map.Entry<String, String> entry : ts.entrySet()) {
        data.add(new BasicTag(entry.getKey(), entry.getValue()));
      }
      return addAll(data);
    } else {
      String[] newTags = new String[2 * ts.size()];
      int i = 0;
      for (Map.Entry<String, String> entry : ts.entrySet()) {
        newTags[i++] = Objects.requireNonNull(entry.getKey(), "tag keys cannot be null");
        newTags[i++] = Objects.requireNonNull(entry.getValue(), "tag values cannot be null");
      }

      boolean sorted = ts instanceof SortedMap && isNaturallyOrdered((SortedMap<String, String>) ts);
      return addAll(newTags, newTags.length, sorted, true);
    }
  }

  /** Add a collection of tags to the set. */
  ArrayTagSet addAll(String[] ts) {
    if (ts.length % 2 != 0) {
      throw new IllegalArgumentException("array length must be even, (length=" + ts.length + ")");
    }
    checkForNullValues(ts);
    String[] copy = Arrays.copyOf(ts, ts.length);
    return addAll(copy, copy.length);
  }

  private ArrayTagSet addAll(String[] ts, int tsLength) {
    return addAll(ts, tsLength, false, false);
  }

  /** Add a collection of tags to the set. */
  private ArrayTagSet addAll(String[] ts, int tsLength, boolean sorted, boolean distinct) {
    if (tsLength == 0) {
      return this;
    } else if (length == 0) {
      if (!sorted) {
        insertionSort(ts, tsLength);
      }
      int len = tsLength;
      if (!distinct) {
        len = dedup(ts, 0, ts, 0, tsLength);
      }
      return new ArrayTagSet(ts, len);
    } else {
      String[] newTags = new String[length + tsLength];
      if (!sorted) {
        insertionSort(ts, tsLength);
      }
      int newLength = merge(newTags, tags, length, ts, tsLength);
      return new ArrayTagSet(newTags, newLength);
    }
  }

  private ArrayTagSet addAll(ArrayTagSet ts) {
    if (ts == this || ts.isEmpty()) {
      return this;
    }
    if (isEmpty()) {
      return ts;
    }
    String[] newTags = new String[length + ts.length];
    int newLength = merge(newTags, tags, length, ts.tags, ts.length);
    return new ArrayTagSet(newTags, newLength);
  }

  private ArrayTagSet addAll(TagList ts) {
    int size = ts.size();

    if (size == 0) {
      return this;
    }
    String[] newTags = new String[size * 2];
    int j = 0;
    for (int i = 0; i < size; i++) {
      newTags[j++] = Objects.requireNonNull(ts.getKey(i), "tag keys cannot be null");
      newTags[j++] = Objects.requireNonNull(ts.getValue(i), "tag values cannot be null");
    }
    return addAll(newTags, newTags.length);
  }

  /** Add a collection of tags to the set. */
  ArrayTagSet addAll(Tag[] ts) {
    return addAll(ts, ts.length);
  }

  /** Add a collection of tags to the set. */
  ArrayTagSet addAll(Tag[] ts, int tsLength) {
    if (tsLength == 0) {
      return this;
    } else {
      String[] newTags = toStringArray(ts, tsLength);
      checkForNullValues(newTags);
      return addAll(newTags, newTags.length);
    }
  }

  private String[] toStringArray(Tag[] ts, int length) {
    String[] strs = new String[length * 2];
    for (int i = 0; i < length; ++i) {
      strs[2 * i] = ts[i].key();
      strs[2 * i + 1] = ts[i].value();
    }
    return strs;
  }

  private void checkForNullValues(String[] ts) {
    for (String s : ts) {
      if (s == null) {
        throw new NullPointerException("tag keys and values cannot be null");
      }
    }
  }

  /**
   * Sort a string array that consists of tag key/value pairs by key. The array will be
   * sorted in-place. Tag lists are supposed to be fairly small, typically less than 20
   * tags. With the small size a simple insertion sort works well.
   */
  private static void insertionSort(String[] ts, int length) {
    if (length == 4) {
      // Two key/value pairs, swap if needed
      if (ts[0].compareTo(ts[2]) > 0) {
        // Swap key
        String tmp = ts[0];
        ts[0] = ts[2];
        ts[2] = tmp;

        // Swap value
        tmp = ts[1];
        ts[1] = ts[3];
        ts[3] = tmp;
      }
    } else if (length > 4) {
      // One entry is already sorted. Two entries handled above, for larger arrays
      // use insertion sort.
      for (int i = 2; i < length; i += 2) {
        String k = ts[i];
        String v = ts[i + 1];
        int j = i - 2;
        for (; j >= 0 && ts[j].compareTo(k) > 0; j -= 2) {
          ts[j + 2] = ts[j];
          ts[j + 3] = ts[j + 1];
        }
        ts[j + 2] = k;
        ts[j + 3] = v;
      }
    }
  }

  /**
   * Merge and dedup any entries in {@code ts} that have the same key. The last entry
   * with a given key will get selected. Each list must be sorted by key before processing.
   */
  static int merge(String[] dst, String[] srcA, int lengthA, String[] srcB, int lengthB) {
    int i = 0;
    int ai = 0;
    int bi = 0;

    while (ai < lengthA && bi < lengthB) {
      final String ak = srcA[ai];
      final String av = srcA[ai + 1];
      String bk = srcB[bi];
      String bv = srcB[bi + 1];
      int cmp = ak.compareTo(bk);
      if (cmp < 0) {
        // srcA should already have been deduped as it comes from the tag list
        dst[i++] = ak;
        dst[i++] = av;
        ai += 2;
      } else if (cmp > 0) {
        // Choose last value for a given key if there are duplicates. It is possible srcB
        // will contain duplicates if the user supplied a type like a list.
        int j = bi + 2;
        for (; j < lengthB && bk.equals(srcB[j]); j += 2) {
          bv = srcB[j + 1];
        }
        dst[i++] = bk;
        dst[i++] = bv;
        bi = j;
      } else {
        // Newer tags should override, use source B if there are duplicate keys.
        // If source B has duplicates, then use the last value for the given key.
        int j = bi + 2;
        for (; j < lengthB && ak.equals(srcB[j]); j += 2) {
          bk = srcB[j];
          bv = srcB[j + 1];
        }
        dst[i++] = bk;
        dst[i++] = bv;
        bi = j;
        ai += 2; // Ignore
      }
    }

    if (ai < lengthA) {
      System.arraycopy(srcA, ai, dst, i, lengthA - ai);
      i += lengthA - ai;
    } else if (bi < lengthB) {
      i = dedup(srcB, bi, dst, i, lengthB - bi);
    }

    return i;
  }

  /**
   * Dedup any entries in {@code ts} that have the same key. The last entry with a given
   * key will get selected. Input data must already be sorted by the tag key. Returns the
   * length of the overall deduped array.
   */
  private static int dedup(String[] src, int ss, String[] dst, int ds, int len) {
    if (len == 0) {
      return ds;
    } else {
      String k = src[ss];
      dst[ds] = k;
      dst[ds + 1] = src[ss + 1];
      int j = ds;
      final int e = ss + len;
      for (int i = ss + 2; i < e; i += 2) {
        if (k.equals(src[i])) {
          dst[j] = src[i];
          dst[j + 1] = src[i + 1];
        } else {
          j += 2; // Not deduping, skip over previous entry
          k = src[i];
          dst[j] = k;
          dst[j + 1] = src[i + 1];
        }
      }
      return j + 2;
    }
  }

  /**
   * Dedup any entries in {@code data} that have the same key. The last entry with a given
   * key will get selected. Input data must already be sorted by the tag key. Returns the
   * length of the overall deduped array.
   */
  private static int dedupInPlace(String[] data, int len) {
    if (len == 0) {
      return 0;
    } else {
      String k = data[0];
      int j = 0;
      for (int i = 2; i < len; i += 2) {
        if (k.equals(data[i])) {
          data[j] = data[i];
          data[j + 1] = data[i + 1];
        } else {
          j += 2; // Not deduping, skip over previous entry
          k = data[i];
          if (i > j) {
            data[j] = k;
            data[j + 1] = data[i + 1];
          }
        }
      }
      return j + 2;
    }
  }

  /** Return the key at the specified position. */
  @Override public String getKey(int i) {
    return tags[i * 2];
  }

  /** Return the value at the specified position. */
  @Override public String getValue(int i) {
    return tags[i * 2 + 1];
  }

  /** Return the current size of this tag set. */
  @Override public int size() {
    return length / 2;
  }

  @Override public ArrayTagSet filter(BiPredicate<String, String> predicate) {
    final int n = size();
    String[] result = new String[2 * n];
    int pos = 0;
    for (int i = 0; i < n; ++i) {
      final String k = getKey(i);
      final String v = getValue(i);
      if (predicate.test(k, v)) {
        result[pos++] = k;
        result[pos++] = v;
      }
    }
    return new ArrayTagSet(result, pos);
  }

  @Override public ArrayTagSet filterByKey(Predicate<String> predicate) {
    return filter((k, v) -> predicate.test(k));
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArrayTagSet other = (ArrayTagSet) o;
    if (length != other.length) return false;

    for (int i = 0; i < length; ++i) {
      if (!tags[i].equals(other.tags[i])) return false;
    }
    return true;
  }

  @Override public int hashCode() {
    int hc = cachedHashCode;
    if (hc == 0) {
      hc = 1;
      for (int i = 0; i < length; ++i) {
        hc = 31 * hc + tags[i].hashCode();
      }
      cachedHashCode = hc;
    }
    return hc;
  }

  @Override public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; i += 2) {
      builder.append(':').append(tags[i]).append('=').append(tags[i + 1]);
    }
    return builder.toString();
  }

  @Override
  public Spliterator<Tag> spliterator() {
    return Spliterators.spliterator(iterator(), size(),
            (Spliterator.ORDERED | Spliterator.SORTED | Spliterator.NONNULL
                    | Spliterator.DISTINCT | Spliterator.IMMUTABLE));
  }

  private static boolean isNaturallyOrdered(SortedMap<?, ?> map) {
    return map.comparator() == null || Comparator.naturalOrder().equals(map.comparator());
  }
}
