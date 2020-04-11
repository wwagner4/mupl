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
