package com.example.blackbox.common.iota

interface ICurl {
    /**
     * Absorbs the specified trits.
     *
     * @param trits  The trits.
     * @param offset The offset to start from.
     * @param length The length.
     * @return The ICurl instance (used for method chaining).
     */
    fun absorb(trits: Trits, offset: Int, length: Int): ICurl

    /**
     * Absorbs the specified trits.
     *
     * @param trits The trits.
     * @return The ICurl instance (used for method chaining).
     */
    fun absorb(trits: Trits): ICurl;

    /**
     * Squeezes the specified trits.
     *
     * @param trits  The trits.
     * @param offset The offset to start from.
     * @param length The length.
     * @return The squeezed trits.
     */
    fun squeeze(trits: Trits, offset: Int, length: Int): Trits

    /**
     * Squeezes the specified trits.
     *
     * @param trits The trits.
     * @return The squeezed trits.
     */
    fun squeeze(trits: Trits): Trits

    /**
     * Transforms this instance.
     *
     * @return The ICurl instance (used for method chaining).
     */
    fun transform(): ICurl;

    /**
     * Resets this state.
     *
     * @return The ICurl instance (used for method chaining).
     */
    fun reset(pairMode: Boolean = false): ICurl;

    /**
     * Clones this instance.
     *
     * @return A new instance.
     */
    fun clone(): ICurl
}