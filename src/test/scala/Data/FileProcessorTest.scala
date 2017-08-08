package Data

import java.io._

import Data.File.FileProcessor
import org.apache.commons.io.FilenameUtils
import org.scalatest.FunSuite

import scala.io.Source

class FileProcessorTest extends FunSuite {
  val isHome = false
  var testSource = ""
  var testTarget = ""
  var mapTestSource = ""
  var mapTestTarget = ""

  if(isHome) {
    mapTestSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\MapTest\\source"
    mapTestTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\MapTest\\target"
    testSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\source"
    testTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\target"
  }
  else {
    mapTestSource = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\MapTest\\source"
    mapTestTarget = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\MapTest\\target"
    testSource = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\mainTest\\source"
    testTarget = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\mainTest\\target"
  }

  def recursiveListDirs(f: File): Array[File] = {
    val these = f.listFiles.filter(_.isDirectory)
    these ++ these.flatMap(recursiveListDirs)
  }

  def recursiveListFiles(f: File): Array[File] = {
    val all = f.listFiles
    val files = f.listFiles.filter(_.isFile)
    //val these = f.listFiles
    //these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
    files ++ all.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  test("FileProcessor - non-existent source") {
    val nonExistentDir = new File("C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\source\\Narnia")
    val t = new File(testTarget)
    //val t = new File(testTargetWork)
    val d = Array("\\s")
    val e = Array("GHC")

    val caught = intercept[IllegalArgumentException](new FileProcessor(nonExistentDir, t, d, e))
    assert(caught.getMessage == "requirement failed: Source directory does not exist or is not a directory!")
  }
  test("FileProcessor - non-existent target") {
    val s = new File(testSource)
    //val s = new File(testSourceWork)
    val nonExistentDir = new File("C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\source\\Narnia")
    val d = Array("\\s")
    val e = Array("GHC")

    val caught = intercept[IllegalArgumentException](new FileProcessor(s, nonExistentDir, d, e))
    assert(caught.getMessage == "requirement failed: Target directory does not exist or is not a directory!")
  }
  test("FileProcessor - Default delimiter is '\\s'"){
    val s = new File(testSource)
    val t = new File(testTarget)
    /*val s = new File(testSourceWork)
    val t = new File(testTargetWork)*/
    val fp = new FileProcessor(s, t)
    assert(fp.getDelimiters.size ==1)
    assert(fp.getDelimiters(0).equals("\\s"))
  }
  test("FileProcessor - Default extensions is empty array"){
    val s = new File(testSource)
    val t = new File(testTarget)
    /*val s = new File(testSourceWork)
    val t = new File(testTargetWork)*/
    val fp = new FileProcessor(s, t)
    assert(fp.getExtensions.isEmpty)
  }
  test("FileProcessor - revert to default delimiter if none provided in setDelimiters") {
    /*val s = new File(testSourceWork)
    val t = new File(testTargetWork)*/
    val s = new File(testSource)
    val t = new File(testTarget)
    val d = Array("\\s")
    val e = Array("GHC")
    val fp = new FileProcessor(s, t, d, e)
    fp.setDelimiters(Array())
    assert(fp.getDelimiters.length == 1)
    assert(fp.getDelimiters(0)equals("\\s"))
  }
  test("FileProcessor - revert to default (no) extenisons if none provided in setExtensions") {
    /*val s = new File(testSourceWork)
    val t = new File(testTargetWork)*/
    val s = new File(testSource)
    val t = new File(testTarget)
    val d = Array("\\s")
    val e = Array("GHC")
    val fp = new FileProcessor(s, t, d, e)
    fp.setExtensions(Array())
    assert(fp.getExtensions.isEmpty)
  }
  test("FileProcessor - preprocess returns None if source doesn't exist anymore"){
    val t = new File(testTarget)
    //val t = new File(testTargetWork)
    val tempF = new File(testSource + "\\tempSource")
    tempF.mkdir
    if(tempF.exists){
      val fp = new FileProcessor(tempF, t)
      tempF.delete()
      assert(fp.preprocess == None)
    }
  }
  test("FileProcessor - preprocess returns None if target doesn't exist anymore"){
    val s = new File(testSource)
    //val s = new File(testSourceWork)
    val tempF = new File(testTarget + "\\tempTarget")
    tempF.mkdir
    if(tempF.exists){
      val fp = new FileProcessor(s, tempF)
      tempF.delete()
      assert(fp.preprocess == None)
    }
  }
  test("FileProcessor - preprocess creates target dirs") {
    val s = new File(testSource)
    val t = new File(testTarget)
    /*val s = new File(testSourceWork)
    val t = new File(testTargetWork)*/
    val d = Array("\\s")
    val e = Array("GHC")
    val fp = new FileProcessor(s, t, d, e)
    fp.preprocess()
    val folderNames = Array("1", "2", "3", "4")
    val dirs = recursiveListDirs(t).map(f => f.getName)
    assert(dirs.length == 4)
    for (n <- folderNames) {
      assert(dirs.contains(n))
    }
  }
  test("FileProcessor - preprocess works with multiple delimiters.") {
    val s = new File(testSource)
    val t = new File(testTarget)
    /*val s = new File(testSourceWork)
    val t = new File(testTargetWork)*/
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC")
    val fp = new FileProcessor(s, t, d, e)
    assert(fp.getSource.isDefined)
    assert(fp.getTarget.isDefined)
    assert(fp.getSource.get.exists)
    assert(fp.getTarget.get.exists)
    assert(fp.getSource.get.isDirectory)
    assert(fp.getTarget.get.isDirectory)
    var sysCallMap = Map[String, Int]()
    try {
      sysCallMap = fp.preprocess.get
    }catch{
      case e: Throwable => println("MESSAGE: " + e.getCause.getMessage)
    }

    var sfStr = Array[String]()
    val s1 = Source.fromFile(recursiveListFiles(s).filter(f => f.getName == "multipleDelimiters.GHC")(0))
    try
      sfStr = s1.mkString.split(d.mkString("|"))
    finally s1.close()

    var tfStr = Array[String]()
    val s2 = Source.fromFile(recursiveListFiles(t).filter(f => f.getName == "multipleDelimiters_INT.IDS")(0))
    try
      tfStr = s2.mkString.split(d.mkString("|"))
    finally s2.close()

    for (i <- sfStr.indices) {
      assert(sysCallMap(sfStr(i)) == tfStr(i).toInt)
    }
  }
  test("FileProcessor - preprocess works with multiple extensions.") {
    val s = new File(testSource)
    val t = new File(testTarget)
    /*val s = new File(testSourceWork)
    val t = new File(testTargetWork)*/
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC", "AAA", "EEE")
    val fp = new FileProcessor(s, t, d, e)
    fp.preprocess
    val sourceFiles = recursiveListFiles(s)
    val sourceFilesToCopy = recursiveListFiles(s).filter(f => FilenameUtils.getExtension(f.getName) == "GHC" || FilenameUtils.getExtension(f.getName) == "AAA" || FilenameUtils.getExtension(f.getName) == "EEE")
    val sourceFilesToCopy2 = recursiveListFiles(s).filter(f => e.exists(f.getName.endsWith(_)))
    val targetFiles = recursiveListFiles(t)

    assert(targetFiles.nonEmpty)
    assert(targetFiles.length == sourceFilesToCopy.length)
    assert(targetFiles.length == sourceFilesToCopy2.length)

    for (f <- targetFiles) assert(sourceFiles.exists(f2 => f.getName == FilenameUtils.removeExtension(f2.getName) + "_INT.IDS"))

    assert(sourceFiles.exists(f => FilenameUtils.removeExtension(f.getName) == "file2_DontCopy"))
    assert(!targetFiles.exists(f => FilenameUtils.removeExtension(f.getName) == "file2_DontCopy"))
  }
  test("FileProcessor - preprocess returns map of correct size"){
    val s = new File(mapTestSource)
    val t = new File(mapTestTarget)
    /*val s = new File(testSourceWork)
    val t = new File(testTargetWork)*/
    val d = Array("\\s", "\\|", "\\;", "\\,", "\\_")
    val e = Array("GHC", "AAA", "EEE")
    val fp = new FileProcessor(s, t, d, e)
    val sysCallMap = fp.preprocess.get
    assert(sysCallMap.size == 5)
  }
}