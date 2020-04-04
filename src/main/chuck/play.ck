class m1Melody extends SKMelody {

    fun Sound[] sounds() {
        return [
            nop(), pl(1, 0.3, 55),pl(2, 1.0, 58),pl(2, 1.0, 56),pl(1, 0.3, 59),pl(2, 0.3, 58),pl(2, 0.3, 58),
            nop(), pl(1, 0.3, 55),pl(2, 1.0, 58),pl(2, 1.0, 56),pl(1, 0.3, 59),pl(1, 0.3, 58)
        ];
    }

}
class m2Melody extends SKMelody {

    fun Sound[] sounds() {
        return [
            pl(1, 1.0, 39), pa(1), pl(1, 1.0, 44), pa(1),
            pl(1, 1.0, 39), pa(1), pl(2, 0.3, 44), pl(2, 0.3, 39), pa(1) 
        ];
    }

}

class p1Melody extends BufGlotAhhMelody {

    fun Sound[] sounds() {
        return [
            pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(4, 1.0, 55), pl(4, 1.0, 55), 
            pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(4, 1.0, 55), pl(4, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55), pl(2, 1.0, 55) 
        ];
    }

}

fun void m1() {
    m1Melody m;
    m.play();
}

fun void m2() {
    m2Melody m;
    m.play();
}

fun void p1() {
    p1Melody m;
    m.play();
}

fun void par01() {
    m1();
    m1();
}
fun void par02() {
    m2();
    m2();
}

fun void par03() {
    p1();
    p1();
}

fun void par() {
    spork ~ par01();
    spork ~ par02();
    spork ~ par03();
}

20 => int alldur;

if (me.args() != 1) {
    <<<"one argument must be defined" >>>;
} else {
   me.arg(0) => string arg;
   if (arg == "m1") {m1();alldur::second => now;}
   else if (arg == "m2") {m2();alldur::second => now;}
   else if (arg == "p1") {p1();alldur::second => now;}
   else if (arg == "par") {par();alldur::second => now;}
   else {<<<"unknown argument", arg>>>;}
}




