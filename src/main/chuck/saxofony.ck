// STK Saxofony

// patch
Saxofony sax => JCRev r => dac;
.5 => r.gain;
.05 => r.mix;

// our notes
[ 61, 63, 65, 66, 68 ] @=> int notes[];

// infinite time-loop
while( true )
{
    // set
    0.5 => sax.stiffness;
    0.3 => sax.aperture;
    0.9 => sax.noiseGain;
    0.5 => sax.blowPosition;
    5 => sax.vibratoFreq;
    0.1 => sax.vibratoGain;
    0.1 => sax.pressure;

    // factor
    Math.random2f( .75, 2 ) => float factor;

    for( int i; i < notes.size(); i++ )
    {
        
        Std.mtof(  4 + notes[i] ) => sax.freq;
        1.0 => sax.noteOn;
        500::ms => now;
    }
}

