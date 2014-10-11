package monocle.bench

import monocle.SimplePrism
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import scalaz.Maybe

@State(Scope.Benchmark)
class PrismBench {

  sealed trait ADT
  case class I(i: Int)    extends ADT
  case class S(s: String) extends ADT
  case class R(r: ADT)    extends ADT

  def getIMaybe(adt: ADT): Maybe[Int]    = adt match { case I(i) => Maybe.just(i); case _ => Maybe.empty }
  def getSMaybe(adt: ADT): Maybe[String] = adt match { case S(s) => Maybe.just(s); case _ => Maybe.empty }
  def getRMaybe(adt: ADT): Maybe[ADT]    = adt match { case R(r) => Maybe.just(r); case _ => Maybe.empty }

  def mkI(i: Int)   : ADT = I(i)
  def mkS(s: String): ADT = S(s)
  def mkR(r: ADT)   : ADT = R(r)

  val _i = SimplePrism(getIMaybe)(mkI)
  val _s = SimplePrism(getSMaybe)(mkS)
  val _r = SimplePrism(getRMaybe)(mkR)

  val intADT      = mkI(5)
  val stringADT   = mkS("Yop")
  val nestedValue = mkR(mkR(mkI(5)))

  @Benchmark def stdSuccessGetOption() = getIMaybe(intADT)
  @Benchmark def prismSuccessGetOption() = _i.getMaybe(intADT)
  
  @Benchmark def stdFailureGetOption() = getIMaybe(stringADT)
  @Benchmark def prismFailureGetOption() = _i.getMaybe(stringADT)

  @Benchmark def nestedstdGetOption() = for {
    r2 <- getRMaybe(nestedValue)
    r1 <- getRMaybe(r2)
    i  <- getIMaybe(r1)
  } yield i
  @Benchmark def nestedPrismGetOption() = (_r composePrism _r composePrism _i).getMaybe(nestedValue)

  @Benchmark def stdReverseGet()  = mkI(5)
  @Benchmark def prismReverseGet() = _i.reverseGet(5)


}