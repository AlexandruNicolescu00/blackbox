package com.example.blackbox.common.iota

// TrinaryRadix defines the base of the trinary system.
const val TRINARY_RADIX = 3

// TryteRadix defines the radix used for tryte conversions.
const val TRYTE_RADIX = 27

// TryteAlphabet are letters of the alphabet and the number 9
// which directly map to decimal values of a single Tryte value.
const val TRYTE_ALPHABET = "9ABCDEFGHIJKLMNOPQRSTUVWXYZ"

// TritsPerTryte is the number of trits in one tryte
const val TRITS_PER_TRYTE =  3

const val TRITS_PER_BYTE = 6
const val TRYTES_PER_BYTE = TRITS_PER_BYTE / TRITS_PER_TRYTE

// MinTryteValue is the minimum value of a tryte value.
const val MIN_TRYTE_VALUE: Byte = -13

// MaxTryteValue is the maximum value of a tryte value.
const val MAX_TRYTE_VALUE = 13

// MinTritValue is the minimum value of a trit value.
const val MIN_TRIT_VALUE = -1

// MaxTritValue is the maximum value of a trit value.
const val MAX_TRIT_VALUE = 1

