
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

    fun Sound pl(int duration, float gainFact, int midi) {
        <<<"pl(3) must be overwritten>">>>;
        return null;
    }
}

class SKMelody extends Melody {

    StifKarp _inst => ADSR _adsr => dac;

    fun Sound pl(int duration, float gainFact, int midi) {
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

    fun Sound pl(int duration, float gainFact, int midi) {
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
