package io.github.asrayacreate.twa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** The widget's calendar must agree with the web app's पात्रो (values checked against index.html). */
public class NepaliCalTest {
    @Test public void firstDayOfTheTable() {
        NepaliCal.Bs b = NepaliCal.adToBs(1943, 4, 14);
        assertEquals(2000, b.y); assertEquals(1, b.m); assertEquals(1, b.d);
        assertNull(NepaliCal.adToBs(1943, 4, 13));
    }

    @Test public void ghatasthapana2083() {
        NepaliCal.Bs b = NepaliCal.adToBs(2026, 10, 11);
        assertEquals(2083, b.y); assertEquals(6, b.m); assertEquals(25, b.d); assertEquals(0, b.wd);
        assertEquals(0, NepaliCal.tithiOfDay(2026, 10, 11));                 /* Shukla Pratipada */
        assertEquals("घटस्थापना", NepaliCal.festivals(b).get(0)[1]);
    }

    @Test public void laxmiPuja2083() {
        NepaliCal.Bs b = NepaliCal.adToBs(2026, 11, 8);
        assertEquals(7, b.m); assertEquals(22, b.d);
        assertEquals("लक्ष्मी पूजा (तिहार)", NepaliCal.festivals(b).get(0)[1]);
    }

    @Test public void purnimaBoundaries2026() {
        assertEquals(13, NepaliCal.tithiOfDay(2026, 2, 1));                  /* Purnima began 05:52 IST = 06:07 NPT, just after 06:00 */
        assertEquals(15, NepaliCal.tithiOfDay(2026, 6, 30));                 /* Purnima ended 05:26 IST = 05:41 NPT */
    }

    @Test public void nepaliDigits() {
        assertEquals("२०८३", NepaliCal.npd(2083));
        assertEquals("शुक्ल प्रतिपदा", NepaliCal.tithiName(0, false));
        assertEquals("Aunsi", NepaliCal.tithiName(29, true));
    }
}
