package TreeTest

import javax.sound.midi.Sequence

import org.scalatest.FunSuite

/**
  * Created by apinter on 14/07/2017.
  */
class SequenceListTest extends FunSuite {

  val strTrace = "ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15040 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15fa7 kernel32.dll+0x15c8b kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc kernel32.dll+0x15568 comctl32.dll+0x8ac2d comctl32.dll+0x8ac5f comctl32.dll+0x8ac77 comctl32.dll+0x4216 comctl32.dll+0x42d4 ntdll.dll+0x11a7 ntdll.dll+0x1cbab ntdll.dll+0x2173e ntdll.dll+0x21639 ntdll.dll+0xeac7 kernel32.dll+0x15cfc"
  val intTrace = "1 2 3 4 5 6 7 8 9 10 11 1 2 3 12 13 4 5 6 7 8 9 10 11 1 2 3 14 13 4 5 6 7 8 9 10 11 1 2 3 15 4 5 6 7 8 9 10 11 1 2 3 15"
  val intListTrace = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 12, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 14, 13, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 3, 15)
  val shortListTrace = Vector(1, 2, 3, 4, 5)
  val shortListTrace2 = Vector(5, 4, 3, 2, 1)
  val shortListTrace3 = Vector(1, 2, 3)
  val shortListTrace4 = Vector(4, 5, 6)
  val shortMap = Map(1 -> 0.1, 2 -> 0.2, 3 -> 0.3)
  val shortStringListTrace = Vector("ntdll.dll+0x2173e, ntdll.dll+0x21639, ntdll.dll+0xeac7, kernel32.dll+0x15568, comctl32.dll+0x8ac2d")
  val shortStringMap = Map("ntdll.dll+0x2173e" -> 0.1, "ntdll.dll+0x21639" -> 0.2, "ntdll.dll+0xeac7" -> 0.3)

  test("SequenceList constructor arg validation. maxSeqcount is not zero.") {
    assertThrows[IllegalArgumentException](new SequenceList[Int, Int](0))
  }
  test("SequenceList constructor arg validation. maxSeqcount is not negative.") {
    assertThrows[IllegalArgumentException](new SequenceList[Int, Int](-1))
  }
  test("SequenceList constructor arg validation. Correct error message displayed if maxSeqCount is zero.!") {
    val caught = intercept[IllegalArgumentException](new SequenceList[Int, Int](0))
    assert(caught.getMessage == "requirement failed: Max sequence count must be positive!")
  }
  test("SequenceList updateSequences fails if maxSeqSize would be exceeded") {
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    val seq2 = (shortListTrace2, 666)
    sl.updateSequences(seq1)
    assert(!sl.updateSequences(seq2))
  }
  test("SequenceList updateSequences doesn't not fail maxSeqSize check if Sequence with existing key is added multiple times.") {
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    val seq2 = (shortListTrace, 666)
    val seq3 = (shortListTrace, 666)
    val seq4 = (shortListTrace, 666)
    sl.updateSequences(seq1)
    assert(sl.updateSequences(seq2))
    assert(sl.updateSequences(seq3))
    assert(sl.updateSequences(seq4))
  }
  test("SequenceList updateSequences doesn't not fail maxSeqSize check if Sequence with existing key is added multiple times. (Different event values)") {
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    val seq2 = (shortListTrace, 777)
    val seq3 = (shortListTrace, 888)
    val seq4 = (shortListTrace, 999)
    sl.updateSequences(seq1)
    assert(sl.updateSequences(seq2))
    assert(sl.updateSequences(seq3))
    assert(sl.updateSequences(seq4))
  }
  test("SequenceList, only one Sequence exists with a given key ") {
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    val seq2 = (shortListTrace, 777)
    val seq3 = (shortListTrace, 888)
    val seq4 = (shortListTrace, 999)
    val seq5 = (shortListTrace, 666)

    assert(sl.sequences.size == 0)
    sl.updateSequences(seq1)
    assert(sl.sequences.size == 1)
    sl.updateSequences(seq2)
    assert(sl.sequences.size == 1)
    sl.updateSequences(seq3)
    assert(sl.sequences.size == 1)
    sl.updateSequences(seq4)
    assert(sl.sequences.size == 1)
    sl.updateSequences(seq5)
    assert(sl.sequences.size == 1)
  }
  test("SequenceList, getSequence returns None if sequence with key doesn't exist in list") {
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    sl.updateSequences(seq1)
    assert(sl.getSequence(shortListTrace2).isEmpty)
  }
  test("SequenceList, getSequence returns sequence if sequence with key exists in list") {
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    sl.updateSequences(seq1)
    assert(sl.getSequence(shortListTrace).get.getKey == shortListTrace)
  }
  test("SequenceList getKeys returns empty Vector if there are no sequences stored in SequenceList") {
    val sl = new SequenceList[Int, Int](1)
    assert(sl.getKeys.isEmpty)
  }
  test("SequenceList getKeys returns Vector of size 1 if there is one sequence stored in SequenceList") {
    val sl = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    sl.updateSequences(seq1)
    assert(sl.getKeys.size == 1)
  }
  test("SequenceList getKeys returns Vector of correct size if there are multiple seque0nces stored in SequenceList") {
    val sl = new SequenceList[Int, Int](4)
    val seq1 = (shortListTrace, 666)
    val seq2 = (shortListTrace, 777)
    val seq3 = (shortListTrace, 888)
    val seq4 = (shortListTrace, 999)
    val seq5 = (shortListTrace, 666)

    assert(sl.getKeys.isEmpty)
    sl.updateSequences(seq1)
    assert(sl.getKeys.size == 1)
    sl.updateSequences(seq2)
    assert(sl.getKeys.size == 1)
    sl.updateSequences(seq3)
    assert(sl.getKeys.size == 1)
    sl.updateSequences(seq4)
    assert(sl.getKeys.size == 1)
    sl.updateSequences(seq5)
    assert(sl.getKeys.size == 1)

    val seq6 = (shortListTrace2, 666)
    val seq7 = (shortListTrace3, 777)
    val seq8 = (shortListTrace4, 888)


    sl.updateSequences(seq6)
    assert(sl.getKeys.size == 2)
    sl.updateSequences(seq7)
    assert(sl.getKeys.size == 3)
    sl.updateSequences(seq8)
    assert(sl.getKeys.size == 4)

    val seq9 = (shortListTrace2, 999)
    val seq10 = (shortListTrace3, 666)
    val seq11 = (shortListTrace4, 888)

    sl.updateSequences(seq9)
    assert(sl.getKeys.size == 4)
    sl.updateSequences(seq10)
    assert(sl.getKeys.size == 4)
    sl.updateSequences(seq11)
    assert(sl.getKeys.size == 4)
  }
  test("SequenceList, getKeys returns correct keys") {
    val sl = new SequenceList[Int, Int](4)
    val seq1 = (shortListTrace, 666)

    assert(sl.getKeys.isEmpty)
    sl.updateSequences(seq1)
    assert(sl.getKeys.size == 1)
    assert(sl.getKeys.contains(shortListTrace))

    val seq2 = (shortListTrace2, 666)
    val seq3 = (shortListTrace3, 777)
    val seq4 = (shortListTrace4, 888)


    sl.updateSequences(seq2)
    assert(sl.getKeys.size == 2)
    assert(sl.getKeys.contains(shortListTrace))
    assert(sl.getKeys.contains(shortListTrace2))
    sl.updateSequences(seq3)
    assert(sl.getKeys.size == 3)
    assert(sl.getKeys.contains(shortListTrace))
    assert(sl.getKeys.contains(shortListTrace2))
    assert(sl.getKeys.contains(shortListTrace3))
    sl.updateSequences(seq4)
    assert(sl.getKeys.size == 4)
    assert(sl.getKeys.contains(shortListTrace))
    assert(sl.getKeys.contains(shortListTrace2))
    assert(sl.getKeys.contains(shortListTrace3))
    assert(sl.getKeys.contains(shortListTrace4))

    val seq6 = (shortListTrace2, 999)
    val seq7 = (shortListTrace3, 666)
    val seq8 = (shortListTrace4, 888)

    sl.updateSequences(seq6)
    assert(sl.getKeys.size == 4)
    assert(sl.getKeys.contains(shortListTrace))
    assert(sl.getKeys.contains(shortListTrace2))
    assert(sl.getKeys.contains(shortListTrace3))
    assert(sl.getKeys.contains(shortListTrace4))
    sl.updateSequences(seq7)
    assert(sl.getKeys.size == 4)
    assert(sl.getKeys.contains(shortListTrace))
    assert(sl.getKeys.contains(shortListTrace2))
    assert(sl.getKeys.contains(shortListTrace3))
    assert(sl.getKeys.contains(shortListTrace4))
    sl.updateSequences(seq8)
    assert(sl.getKeys.size == 4)
    assert(sl.getKeys.contains(shortListTrace))
    assert(sl.getKeys.contains(shortListTrace2))
    assert(sl.getKeys.contains(shortListTrace3))
    assert(sl.getKeys.contains(shortListTrace4))
  }
  test("temp") {

    var vVS: Vector[_ >: Vector[SMT[Int, Int]]] = Vector[Vector[SMT[Int, Int]]]()
    var node = new Node[Int, Int](1, 2, 3)
    var seqList = new SequenceList[Int, Int](1)
    val seq1 = (shortListTrace, 666)
    seqList.updateSequences(seq1)
    var v: Vector[_ >: SMT[Int, Int]] = Vector[SMT[Int, Int]]()

    v = v :+ node
    v = v :+ seqList

    println("\n----------------------\nv: " + v)
    vVS = vVS :+ v
    println("\n----------------------\nvVS: " + vVS)
  }
}