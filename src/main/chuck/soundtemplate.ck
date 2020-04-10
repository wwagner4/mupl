10 => int alldur;
1.0 => float globalSpeedFact;
1.0 => float globalGainFact;


class Sound {
    fun void play() {
        <<<"play must be overwritten">>>;
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
class SilentMelody extends Melody {
}


class Bwg extends Sound {
    Gain @g;
    BandedWG @inst;
    // must contain the three parameters
    55 => int midi;
    2 => int duration;
    1.0 => float gainFact;
    
    
    fun void play() {
        // set parameters for sound objects
        0.5 => inst.bowPressure; 
        Std.mtof( midi ) => inst.freq;
        5.0 * gainFact * globalGainFact => g.gain;
        // calculate time in seconds
        globalSpeedFact / duration => float t;
        
        // sound on and off
        1.0 => inst.noteOn;
        t::second => now;
        //0.0 => inst.noteOff;
    }
}

class BwgMelody extends Melody {
    
    // define sound queue
    Gain _g;
    BandedWG _inst => _g => dac;
    
    fun Sound pl(int duration, float gainFact, int midi) {
        // create a new sound
        Bwg sound;
        // set objects from melody to sound
        _inst @=> sound.inst;
        _g @=> sound.g;
        // set the three paranmeters
        midi => sound.midi;
        duration => sound.duration;
        gainFact => sound.gainFact;
        
        return sound;
    }
}

class m1Melody01 extends BwgMelody {
    
    fun Sound[] sounds() {
        return [
            nop(), pl(4, 1.0, 44),pl(4, 1.0, 46),pl(4, 1.0, 46),pl(2, 0.7, 44)
        ];
    }
    
}

m1Melody01 m1;
m1.play();
