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

class Bwg extends Sound {
    Gain @g;
    BandedWG @inst;
    // must contain the three parameters
    55 => int midi;
    2 => int duration;
    1.0 => float gainFact;
    
    
    fun void play() {
        0.5 => inst.bowPressure; 
        Std.mtof( midi ) => inst.freq;
        20.0 * gainFact * globalGainFact => g.gain;
        globalSpeedFact / duration => float t;
        
        1.0 => inst.noteOn;
        t::second => now;
        //0.0 => inst.noteOff;
    }
}

class BwgMelody extends Melody {
    
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
