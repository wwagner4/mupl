// patch
HevyMetl sit => PRCRev r => dac;
0.2 => r.mix;

// time loop
while( true )
{
    // freq
    Math.random2( 0, 11 ) => float winner;
    Std.mtof( 30 + Math.random2(0,3) * 12 + winner ) => float f;
    f => sit.freq;
    
    0.1 => sit.noteOn;
    0.5::second => now;
    0.2 => sit.noteOff;
    0.5::second => now;
}