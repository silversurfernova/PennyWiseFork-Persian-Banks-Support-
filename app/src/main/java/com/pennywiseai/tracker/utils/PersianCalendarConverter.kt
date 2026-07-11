package com.pennywiseai.tracker.utils

/**
 * Gregorian <-> Jalali (Persian / Solar Hijri) calendar conversion.
 *
 * Pure integer-arithmetic port of the widely-used jalaali-js algorithm
 * (Borkowski's algorithm for the Jalali calendar's 33-year leap-year rule),
 * accurate for every Jalali year covered by [BREAKS] — roughly 1 AP to
 * beyond year 3000 AP, far more than the app will ever need to display.
 *
 * Display-only: nothing that stores or filters dates goes through this —
 * transactions, backups, and DB columns all stay Gregorian.
 */
object PersianCalendarConverter {

    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756,
        818, 1111, 1181, 1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394,
        2456, 3178
    )

    private class JalCalResult(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCalResult {
        val bl = BREAKS.size
        val gy = jy + 621
        var leapJ = -14
        var jp = BREAKS[0]
        var jump = 0
        var i = 1
        while (i < bl) {
            val jm = BREAKS[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += (jump / 33) * 8 + (jump % 33) / 4
            jp = jm
            i += 1
        }
        var n = jy - jp

        leapJ += (n / 33) * 8 + ((n % 33) + 3) / 4
        if (jump % 33 == 4 && jump - n == 4) {
            leapJ += 1
        }

        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG

        if (jump - n < 6) {
            n = n - jump + ((jump + 4) / 33) * 33
        }
        var leap = ((n + 1) % 33 - 1) % 4
        if (leap == -1) leap = 4

        return JalCalResult(leap, gy, march)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = (gy + (gm - 8) / 6 + 100100) * 1461 / 4 +
            (153 * ((gm + 9) % 12) + 2) / 5 +
            gd - 34840408
        d = d - (gy + 100100 + (gm - 8) / 6) / 100 * 3 / 4 + 752
        return d
    }

    private fun d2g(jdn: Int): Triple<Int, Int, Int> {
        var j = 4 * jdn + 139361631
        j += (4 * jdn + 183187720) / 146097 * 3 / 4 * 4 - 3908
        val i = (j % 1461) / 4 * 5 + 308
        val gd = (i % 153) / 5 + 1
        val gm = (i / 153) % 12 + 1
        val gy = j / 1461 - 100100 + (8 - gm) / 6
        return Triple(gy, gm, gd)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - (jm / 7) * (jm - 7) + jd - 1
    }

    private fun d2j(jdn: Int): Triple<Int, Int, Int> {
        val gy = d2g(jdn).first
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(r.gy, 3, r.march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) {
                return Triple(jy, 1 + k / 31, k % 31 + 1)
            }
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        return Triple(jy, 7 + k / 30, k % 30 + 1)
    }

    /** Converts a Gregorian date to Jalali. Returns (year, month[1-12], day). */
    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> = d2j(g2d(gy, gm, gd))

    /** Converts a Jalali date to Gregorian. Returns (year, month[1-12], day). */
    fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> = d2g(j2d(jy, jm, jd))
}
