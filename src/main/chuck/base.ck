
1.1 => float globalSpeedFact;
0.1 => float globalGainFact;

class Sound {
    fun void play() {
        <<<"play must be overwritten">>>;
    }
}


class SK extends Sound {
    StifKarp @inst;
    ADSR @adsr;
    55 => int midi;
    2 => int duration;
    1.0 => float gainFact;
    
    fun void play() {
        adsr.set( 10::ms, 8::ms, 1, 500::ms );
        globalGainFact * gainFact => inst.noteOn;
        Std.mtof(midi) => inst.freq;
        adsr.keyOn();
        globalSpeedFact / duration => float t;
        t::second => now;
        adsr.keyOff();
    }
}

class Buf extends Sound {
    SndBuf @inst;
    2 => int duration;
    1.0 => float gainFact;
    
    fun void play() {
        0 => inst.pos;
        globalSpeedFact / duration => float t;
        t::second => now;
    }
}

class PA extends Sound {
    2 => int duration;
    
    fun void play() {
        globalSpeedFact / duration => float t;
        t::second => now;
    }
}

class NOP extends Sound {
    fun void play() {
    }
}

class Melody {

    fun void play() {
        sounds().cap() => int sc;
        for(0 => int i; i < sc; i++) {
            sounds()[i % sc] @=> Sound s;
            s.play();
        }
    }

    fun Sound[] sounds() {
        <<<"sounds must be overwritten>">>>;
        return null;
    }

    fun PA pa(int duration) {
        PA pa;
        duration => pa.duration;
        return pa;
    }

    fun NOP nop() {
        NOP nop;
        return nop;
    }

    fun Sound pl(int midi, int duration, float gainFact) {
        <<<"pl(3) must be overwritten>">>>;
        return null;
    }
    fun Sound pl(int duration, float gainFact) {
        return pl(55, duration, gainFact);
    }
    fun Sound plD(int duration) {
        return pl(55, duration, 1.0);
    }
    fun Sound pl(int midi, int duration) {
        return pl(midi, duration, 1.0);
    }
    fun Sound plL(int midi, int duration) {
        return pl(midi, duration, 0.3);
    }
    fun Sound pl(int midi) {
        return pl(midi, 1, 1.0);
    }
}

class SKMelody extends Melody {

    StifKarp _inst => ADSR _adsr => dac;

    fun Sound pl(int midi, int duration, float gainFact) {
        SK sound;
        _inst @=> sound.inst;
        _adsr @=> sound.adsr;    
        midi => sound.midi;
        duration => sound.duration;
        gainFact => sound.gainFact;
        return sound;
    }

}

class BufMelody extends Melody {

    SndBuf _inst => dac;
    "special:" + name() => _inst.read;

    fun string name() {
        <<<"name must be defined">>>;
        return null;
    }

    fun Sound pl(int midi, int duration, float gainFact) {
        Buf sound;
        _inst @=> sound.inst;
        duration => sound.duration;
        gainFact => sound.gainFact;
        return sound;
    }

}

class BufGlotAhhMelody extends BufMelody {

    fun string name() {
        return "glot_ahh";
    }
}


class SKMelody01 extends SKMelody {

    fun Sound[] sounds() {
        return [
            nop(), plL(55, 1),pl(58, 2),pl(56, 2),plL(59, 1),plL(58, 2),plL(58, 2),
            nop(), plL(55, 1),pl(58, 2),pl(56, 2),plL(59, 1),plL(58, 1)
        ];
    }

}
class SKMelody02 extends SKMelody {

    fun Sound[] sounds() {
        return [
            pl(39, 1),pa(1), pl(44, 1), pa(1),
            pl(39, 1),pa(1), plL(44, 2), plL(39, 2), pa(1) 
        ];
    }

}

class GlotMelody01 extends BufGlotAhhMelody {

    fun Sound[] sounds() {
        return [
            plD(2),plD(2),plD(2),plD(2),plD(2),plD(2),plD(2),plD(4),plD(4), 
            plD(2),plD(2),plD(2),plD(4),plD(4),plD(2),plD(2),plD(2),plD(2) 
        ];
    }

}
class SilentMelody extends Melody {

    fun Sound[] sounds() {
        return [
            nop(), pa(1),pa(1), pa(1), pa(1),
            pa(1),pa(1), pa(1), pa(1)
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


