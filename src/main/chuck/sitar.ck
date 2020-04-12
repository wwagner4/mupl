// patch
Sitar sit => PRCRev r => dac;
0.1 => r.mix;

// time loop
while( true )
{
    // freq
    Math.random2( 0, 11 ) => float winner;
    Std.mtof( 30 + Math.random2(0,3) * 12 + winner ) => float f;
    f => sit.freq;
    
    // pluck!
    1.0 => sit.noteOn;
    0.8::second => now;
}