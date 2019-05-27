package at.favre.app.blurbenchmark.util

/**
 * Created by PatrickF on 24.05.2014.
 */
/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * `StringJoiner` is used to construct a sequence of characters separated
 * by a delimiter and optionally starting with a supplied prefix
 * and ending with a supplied suffix.
 *
 *
 * Prior to adding something to the `StringJoiner`, its
 * `sj.toString()` method will, by default, return `prefix + suffix`.
 * However, if the `setEmptyValue` method is called, the `emptyValue`
 * supplied will be returned instead. This can be used, for example, when
 * creating a string using set notation to indicate an empty set, i.e.
 * `"{}"`, where the `prefix` is `"{"`, the
 * `suffix` is `"}"` and nothing has been added to the
 * `StringJoiner`.
 *
 * @apiNote
 *
 *The String `"[George:Sally:Fred]"` may be constructed as follows:
 *
 *
 * <pre> `StringJoiner sj = new StringJoiner(":", "[", "]");
 * sj.add("George").add("Sally").add("Fred");
 * String desiredString = sj.toString();
`</pre> *
 *
 *
 * A `StringJoiner` may be employed to create formatted output from a
 * [java.util.stream.Stream] using
 * [java.util.stream.Collectors.joining]. For example:
 *
 *
 * <pre> `List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
 * String commaSeparatedNumbers = numbers.stream()
 * .map(i -> i.toString())
 * .collect(Collectors.joining(", "));
`</pre> *
 * @see java.util.stream.Collectors.joining
 * @see java.util.stream.Collectors.joining
 * @since 1.8
 */
class StringJoiner
/**
 * Constructs a `StringJoiner` with no characters in it using copies
 * of the supplied `prefix`, `delimiter` and `suffix`.
 * If no characters are added to the `StringJoiner` and methods
 * accessing the string value of it are invoked, it will return the
 * `prefix + suffix` (or properties thereof) in the result, unless
 * `setEmptyValue` has first been called.
 *
 * @param delimiter the sequence of characters to be used between each
 * element added to the `StringJoiner`
 * @param prefix    the sequence of characters to be used at the beginning
 * @param suffix    the sequence of characters to be used at the end
 * @throws NullPointerException if `prefix`, `delimiter`, or
 * `suffix` is `null`
 */
@JvmOverloads constructor(delimiter: CharSequence,
                          prefix: CharSequence = "",
                          suffix: CharSequence = "") {
    private val prefix: String = prefix.toString()
    private val delimiter: String = delimiter.toString()
    private val suffix: String = suffix.toString()

    /*
     * StringBuilder value -- at any time, the characters constructed from the
     * prefix, the added element separated by the delimiter, but without the
     * suffix, so that we can more easily add elements without having to jigger
     * the suffix each time.
     */
    private var value: StringBuilder? = null

    /*
     * By default, the string consisting of prefix+suffix, returned by
     * toString(), or properties of value, when no elements have yet been added,
     * i.e. when it is empty.  This may be overridden by the user to be some
     * other value including the empty String.
     */
    private val emptyValue: String = this.prefix + this.suffix

    /**
     * Sets the sequence of characters to be used when determining the string
     * representation of this `StringJoiner` and no elements have been
     * added yet, that is, when it is empty.  A copy of the `emptyValue`
     * parameter is made for this purpose. Note that once an add method has been
     * called, the `StringJoiner` is no longer considered empty, even if
     * the element(s) added correspond to the empty `String`.
     *
     * @param emptyValue the characters to return as the value of an empty
     * `StringJoiner`
     * @return this `StringJoiner` itself so the calls may be chained
     * @throws NullPointerException when the `emptyValue` parameter is
     * `null`
     */
    fun setEmptyValue(emptyValue: CharSequence): StringJoiner {
        return this
    }

    /**
     * Returns the current value, consisting of the `prefix`, the values
     * added so far separated by the `delimiter`, and the `suffix`,
     * unless no elements have been added in which case, the
     * `prefix + suffix` or the `emptyValue` characters are returned
     *
     * @return the string representation of this `StringJoiner`
     */
    override fun toString(): String {
        return if (value == null) {
            emptyValue
        } else {
            if (suffix == "") {
                value!!.toString()
            } else {
                val initialLength = value!!.length
                val result = value!!.append(suffix).toString()
                // reset value to pre-append initialLength
                value!!.setLength(initialLength)
                result
            }
        }
    }

    /**
     * Adds a copy of the given `CharSequence` value as the next
     * element of the `StringJoiner` value. If `newElement` is
     * `null`, then `"null"` is added.
     *
     * @param newElement The element to add
     * @return a reference to this `StringJoiner`
     */
    fun add(newElement: CharSequence): StringJoiner {
        prepareBuilder().append(newElement)
        return this
    }

    /**
     * Adds the contents of the given `StringJoiner` without prefix and
     * suffix as the next element if it is non-empty. If the given `StringJoiner` is empty, the call has no effect.
     *
     *
     *
     * A `StringJoiner` is empty if [add()][.add]
     * has never been called, and if `merge()` has never been called
     * with a non-empty `StringJoiner` argument.
     *
     *
     *
     * If the other `StringJoiner` is using a different delimiter,
     * then elements from the other `StringJoiner` are concatenated with
     * that delimiter and the result is appended to this `StringJoiner`
     * as a single element.
     *
     * @param other The `StringJoiner` whose contents should be merged
     * into this one
     * @return This `StringJoiner`
     * @throws NullPointerException if the other `StringJoiner` is null
     */
    fun merge(other: StringJoiner): StringJoiner {
        if (other.value != null) {
            val length = other.value!!.length
            // lock the length so that we can seize the data to be appended
            // before initiate copying to avoid interference, especially when
            // merge 'this'
            val builder = prepareBuilder()
            builder.append(other.value, other.prefix.length, length)
        }
        return this
    }

    private fun prepareBuilder(): StringBuilder {
        if (value != null) {
            value!!.append(delimiter)
        } else {
            value = StringBuilder().append(prefix)
        }
        return value!!
    }

    /**
     * Returns the length of the `String` representation
     * of this `StringJoiner`. Note that if
     * no add methods have been called, then the length of the `String`
     * representation (either `prefix + suffix` or `emptyValue`)
     * will be returned. The value should be equivalent to
     * `toString().length()`.
     *
     * @return the length of the current value of `StringJoiner`
     */
    fun length(): Int {
        // Remember that we never actually append the suffix unless we return
        // the full (present) value or some sub-string or length of it, so that
        // we can add on more if we need to.
        return if (value != null)
            value!!.length + suffix.length
        else
            emptyValue.length
    }
}
