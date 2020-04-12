class SK extends Sound {
    StifKarp @inst;
    ADSR @adsr;
    55 => int midi;
    2 => int duration;
    1.0 => float gainFact;
    
    fun void play() {
        adsr.set( 10::ms, 8::ms, 1, 500::ms );
        0.1 * globalGainFact * gainFact => inst.noteOn;
        Std.mtof(midi) => inst.freq;
        adsr.keyOn();
        globalSpeedFact / duration => float t;
        t::second => now;
        adsr.keyOff();
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
class GlotAhhMelody extends BufMelody {
    fun string name() {
        return "glot_ahh";
    }
}

// FM HevyMetl 
class HM extends Sound {
    PRCRev @rev;
    HevyMetl @inst; 

    // must contain the three parameters
    55 => int midi;
    2 => int duration;
    1.0 => float gainFact;
    
    
    fun void play() {
        // set parameters for sound objects
        Std.mtof( midi ) => inst.freq;
        0.6 * gainFact * globalGainFact => float gain;
        // calculate time in seconds
        globalSpeedFact / duration => float t;
        0.8 * t => float t1;
        0.2 * t => float t2;
        
        // sound on and off
        gain => inst.noteOn;
        t1::second => now;
        0.0 => inst.noteOff;
        t2::second => now;
    }
}

class HMMelody extends Melody {
    
    // define sound queue
    HevyMetl _inst => PRCRev _rev => dac;
    0.05 => _rev.mix;    
    
    fun Sound pl(int duration, float gainFact, int midi) {
        // create a new sound
        HM sound;
        // set objects from melody to sound
        _inst @=> sound.inst;
        _rev @=> sound.rev;
        // set the three paranmeters
        midi => sound.midi;
        duration => sound.duration;
        gainFact => sound.gainFact;
        return sound;
    }
}

// Moog 2
class M2 extends Sound {
    Moog @inst;

    // must contain the three parameters
    55 => int midi;
    2 => int duration;
    1.0 => float gainFact;
    
    
    fun void play() {
        // set parameters for sound objects
        0.7 => inst.filterQ;
        0.6 => inst.filterSweepRate;
        9 => inst.lfoSpeed;
        0.1 => inst.lfoDepth;
        1.0 => inst.volume;

        
        Std.mtof( midi ) => inst.freq;
        0.6 * gainFact * globalGainFact => float gain;
        // calculate time in seconds
        globalSpeedFact / duration => float t;
        
        // sound on and off
        gain => inst.noteOn;
        t::second => now;
        gain => inst.noteOff;
    }
}

class M2Melody extends Melody {
    
    // define sound queue
    Moog _inst => dac;
    
    fun Sound pl(int duration, float gainFact, int midi) {
        // create a new sound
        M2 sound;
        // set objects from melody to sound
        _inst @=> sound.inst;
        // set the three paranmeters
        midi => sound.midi;
        duration => sound.duration;
        gainFact => sound.gainFact;
        
        return sound;
    }
}

