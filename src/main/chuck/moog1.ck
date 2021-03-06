// STK ModalBar

// patch
Moog moog => dac;

// scale
[0, 2, 4, 5, 5, 4] @=> int scale[];

// infinite time loop
while( true )
{
    // ding!
    0.5 => moog.filterQ;
    0.4 => moog.filterSweepRate;
    5 => moog.lfoSpeed;
    0.2 => moog.lfoDepth;
    1.0 => moog.volume;
    
    for(0 => int i; i < scale.cap(); i++) {
       44 + scale[i] => Std.mtof => moog.freq;
       1.0 => moog.noteOn;
       1.0::second => now;
       1.0 => moog.noteOff;
    }
    
    // advance time
    0.5::second => now;
}
