package mupl

import mupl.GainVal.GainVal

object GainVal extends Enumeration {
  type GainVal = Value
  val LL, L, M, H, HH = Value
}

sealed trait Chunk

case class Melo(name: String,
                sounds: List[Sound]
               ) extends Chunk

case class Variable(name: String,
                    chunk: Chunk
                   )

case class Symbol(name: String) extends Chunk

case class Sequence(chunks: List[Chunk]
                   ) extends Chunk

case class Parallel(sequences: List[Sequence]) extends Chunk

case class Sound(dur: Int,
                 gain: Option[GainVal],
                 pitch: Option[Int],
                )

