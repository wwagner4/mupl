// patch
BeeThree sit => PRCRev r => dac;
0.2 => r.mix;

// time loop
while( true )
{
    // freq
    Math.random2( 0, 11 ) => float winner;
    Std.mtof( 30 + Math.random2(0,3) * 12 + winner ) => float f;
    f => sit.freq;
    
    // pluck!
    0.1 => sit.noteOn;
    0.8::second => now;
    0.0 => sit.noteOff;
    0.0::second => now;
}