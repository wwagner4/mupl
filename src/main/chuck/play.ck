class SKMelody01 extends SKMelody {

    fun Sound[] sounds() {
        return [
            nop(), pl(1, 0.3, 55),pl(2, 1.0, 58),pl(2, 1.0, 56),pl(1, 0.3, 59),pl(2, 0.3, 58),pl(2, 0.3, 58),
            nop(), pl(1, 0.3, 55),pl(2, 1.0, 58),pl(2, 1.0, 56),pl(1, 0.3, 59),pl(1, 0.3, 58)
        ];
    }

}
class SKMelody02 extends SKMelody {

    fun Sound[] sounds() {
        return [
            pl(1, 1.0, 39), pa(1), pl(1, 1.0, 44), pa(1),
            pl(1, 1.0, 39), pa(1), pl(2, 0.3, 44), pl(2, 0.3, 39), pa(1) 
        ];
    }

}

class GlotMelody01 extends BufGlotAhhMelody {

    fun Sound[] sounds() {
        return [
            pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(4, 1.0, 55), pl(4, 1.0, 55), 
            pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(4, 1.0, 55), pl(4, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55) 
        ];
    }

}

class SilentMelody extends SKMelody {

    fun Sound[] sounds() {
        return [
            nop(), pa(1), pa(1), pa(1), pa(1), pa(1), pa(1), pa(1), pa(1)
        ];
    }

}

fun void m1() {
    SKMelody01 m;
    m.play();
    m.play();
    1::second => now;
}

fun void m2() {
    SKMelody02 m;
    m.play();
    m.play();
    1::second => now;
}

fun void m3() {
    GlotMelody01 m;
    m.play();
    m.play();
    1::second => now;
}

spork ~ m1();
spork ~ m2();
m3();
1::second => now;


