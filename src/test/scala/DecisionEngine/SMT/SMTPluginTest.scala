package DecisionEngine.SMT

import java.io._

import Data.{DataModel, DataWrapper, StringDataWrapper}
import Data.File.FileProcessor
import DecisionEngine.DecisionEngineConfig
import DecisionEngine.SMT.{Node, SequenceList}
import GUI.HIDS
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.scalatest.FunSuite

import scala.collection.mutable
import scala.io.Source

class SMTPluginTest extends  FunSuite {
  val isHome = true
  var testSource = ""
  var testTarget = ""
  var mapTestSource = ""
  var mapTestTarget = ""

  if (isHome) {
    mapTestSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\MapTest\\source"
    mapTestTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\MapTest\\target"
    testSource = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\"
    testTarget = "C:\\Users\\Case\\Documents\\Uni\\Project\\Datasets\\Original\\test\\main\\target"
  }
  else {
    mapTestSource = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\MapTest\\source"
    mapTestTarget = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\MapTest\\target"
    testSource = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\"
    testTarget = "C:\\Users\\apinter\\Documents\\Andras docs\\Other\\Uni\\BBK_PROJECT\\Datasets\\test\\main\\target"
  }

  val maxDepth = 4
  val maxPhi = 2
  val maxSeqCount = 1
  val smoothing = 1.0
  val prior = 1.0
  val ints = true
  val condition1 = Vector(1, 2, 3)
  val condition2 = Vector(4, 5, 6)
  val condition3 = Vector(7, 8, 9)
  val strCondition1 = Vector("one", "two", "three")
  val strCondition2 = Vector("four", "five", "six")
  val strCondition3 = Vector("seven", "eight", "nine")
  val event1 = 666
  val event2 = 777
  val event3 = 888
  val eventStr1 = "eventStr1"
  val eventStr2 = "eventStr2"
  val eventStr3 = "eventStr3"


  test("SMTPlugin - configuration works") {

    val settings = new SMTSettings(maxDepth, maxPhi, maxSeqCount, smoothing, prior, ints, 0.0, 0.0)
    val config: DecisionEngineConfig = new SMTConfig
    config.asInstanceOf[SMTConfig].storeSettings(settings)
    assert(config.getSettings.get == settings)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.maxDepth == maxDepth)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.maxPhi == maxPhi)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.maxSeqCount == maxSeqCount)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.smoothing == smoothing)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.prior == prior)
    assert(config.asInstanceOf[SMTConfig].getSettings.get.isIntTrace == ints)

    val plugin = new SMTPlugin(new SMTGUI)
    plugin.configure(config)

    val returnedModel = plugin.getModel
    assert(returnedModel isDefined)
    val model = returnedModel.get
    val root = model.retrieve.get.asInstanceOf[Node[Int, Int]]

    assert(root.maxDepth == maxDepth)

    assert(root.maxPhi == maxPhi)
    assert(root.maxSeqCount == maxSeqCount)
    assert(root.smoothing == smoothing)
    assert(root.prior == prior)
  }
  test("SMTPlugin - configure returns false if no settings are provided"){
    val emptyConfig = new SMTConfig
    val plugin = new SMTPlugin(new SMTGUI)
    assert(!plugin.configure(emptyConfig))
  }
  test("SMTPlugin - learn returns trained model - root loaded") {

    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    val model = new DataModel
    model.store(n1)
    assert(model.retrieve.get.isInstanceOf[Node[Int, Int]])

    val plugin = new SMTPlugin(new SMTGUI)
    assert(plugin.loadModel(model, true))
    val returnedModel1 = plugin.getModel.get.retrieve.get
    assert(returnedModel1.isInstanceOf[Node[Int, Int]])
    val retNode1 = returnedModel1.asInstanceOf[Node[Int, Int]]

    var r3 = retNode1.predict(condition1, event1)
    var r4 = retNode1.predict(condition1, event2)
    assert(r3._1 == 4.0 && r1._2 == 2.0)
    assert(r4._1 == 2.0 && r2._2 == 2.0)
    assert(retNode1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    val dw = new StringDataWrapper
    //storing condition1, event2
    dw.store("filename", "1 2 3 777")
    plugin.learn(Vector(dw), None, ints)
    val returnedModel2 = plugin.getModel.get.retrieve.get
    val retNode2 = returnedModel2.asInstanceOf[Node[Int, Int]]

    val r5 = retNode2.predict(condition1, event1)
    val r6 = retNode2.predict(condition1, event2)
    var r7 = retNode2.predict(condition1, event3)
    assert(retNode2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
    assert(r5._1 == 2.0 && r5._2 == 2.0)
    assert(r6._1 == 2.0 && r6._2 == 2.0)
    assert(r7._1 == 1.0 && r7._2 == 2.0)

    //storing condition2, event1
    dw.store("filename2", "4    5 6 666")

    plugin.learn(Vector(dw), None, ints)
    val returnedModel3 = plugin.getModel.get.retrieve.get
    val retNode3 = returnedModel3.asInstanceOf[Node[Int, Int]]

    var r8 = retNode3.predict(condition1, event1)
    var r9 = retNode3.predict(condition1, event2)
    var r10 = retNode3.predict(condition1, event3)
    var r11 = retNode3.predict(condition2, event1)
    var r12 = retNode3.predict(condition2, event2)
    var r13 = retNode3.predict(condition2, event3)

    assert(retNode3.getChildren(0)(0).asInstanceOf[Node[Int, Int]].getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1.drop(1)).get.getWeight == 2.0 / 9)
    assert(r8._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r8._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r9._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r9._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r10._1 == 1.0 / 9 + 1.0 / 9 + 1.0 / 6 + 1.0 / 3 && r10._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r11._1 == 4.0 / 9 + 4.0 / 9 + 4.0 / 6 + 4.0 / 3 && r11._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r12._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r12._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r13._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r13._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)

    val n2 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val n2Wrapper = new DataModel
    n2Wrapper.store(n2)
    val dw2 = new StringDataWrapper
    //store condition1, event1
    dw2.store("filename3", "1 2 3 666")

    //DataModel stored as root in DecisionEngine should not change. Only the new tree passed in should be trained.
    val newDM = plugin.learn(Vector(dw2), Some(n2Wrapper), ints)
    val n2Trained = newDM.get.retrieve.get.asInstanceOf[Node[Int, Int]]
    assert(n2 == n2Trained)

    var newR1 = n2Trained.predict(condition1, event1)
    var newR2 = n2Trained.predict(condition1, event2)
    assert(newR1._1 == 4.0 && newR1._2 == 2.0)
    assert(newR2._1 == 2.0 && newR2._2 == 2.0)
    assert(n2Trained.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    //Root has not changed after training new model passed in to learn
    val returnedModel4 = plugin.getModel.get.retrieve.get
    val retNode4 = returnedModel3.asInstanceOf[Node[Int, Int]]

    r8 = retNode4.predict(condition1, event1)
    r9 = retNode4.predict(condition1, event2)
    r10 = retNode4.predict(condition1, event3)
    r11 = retNode4.predict(condition2, event1)
    r12 = retNode4.predict(condition2, event2)
    r13 = retNode4.predict(condition2, event3)

    assert(retNode4.getChildren(0)(0).asInstanceOf[Node[Int, Int]].getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1.drop(1)).get.getWeight == 2.0 / 9)
    assert(r8._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r8._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r9._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r9._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r10._1 == 1.0 / 9 + 1.0 / 9 + 1.0 / 6 + 1.0 / 3 && r10._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r11._1 == 4.0 / 9 + 4.0 / 9 + 4.0 / 6 + 4.0 / 3 && r11._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r12._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r12._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r13._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r13._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
  }
  test("SMTPlugin - learn returns trained model - root loaded - STRING traces") {
    val ints2 = false
    val n1 = Node[String, String](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(strCondition1, eventStr1)
    var r1 = n1.predict(strCondition1, eventStr1)
    var r2 = n1.predict(strCondition1, eventStr2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[String, String]].getSequence(strCondition1).get.getWeight == 2.0 / 3)

    val model = new DataModel
    model.store(n1)
    assert(model.retrieve.get.isInstanceOf[Node[String, String]])

    val plugin = new SMTPlugin(new SMTGUI)
    assert(plugin.loadModel(model, true))
    val returnedModel1 = plugin.getModel.get.retrieve.get
    assert(returnedModel1.isInstanceOf[Node[String, String]])
    val retNode1 = returnedModel1.asInstanceOf[Node[String, String]]

    var r3 = retNode1.predict(strCondition1, eventStr1)
    var r4 = retNode1.predict(strCondition1, eventStr2)
    assert(r3._1 == 4.0 && r1._2 == 2.0)
    assert(r4._1 == 2.0 && r2._2 == 2.0)
    assert(retNode1.getChildren(0)(0).asInstanceOf[SequenceList[String, String]].getSequence(strCondition1).get.getWeight == 2.0 / 3)

    val dw = new StringDataWrapper
    //storing strCondition1, eventStr2
    dw.store("filename1", "one two three eventStr2")
    plugin.learn(Vector(dw), None, ints2)
    val returnedModel2 = plugin.getModel.get.retrieve.get
    val retNode2 = returnedModel2.asInstanceOf[Node[String, String]]

    val r5 = retNode2.predict(strCondition1, eventStr1)
    val r6 = retNode2.predict(strCondition1, eventStr2)
    var r7 = retNode2.predict(strCondition1, eventStr3)
    assert(retNode2.getChildren(0)(0).asInstanceOf[SequenceList[String, String]].getSequence(strCondition1).get.getWeight == 2.0 / 3)
    assert(r5._1 == 2.0 && r5._2 == 2.0)
    assert(r6._1 == 2.0 && r6._2 == 2.0)
    assert(r7._1 == 1.0 && r7._2 == 2.0)

    //storing condition2, eventStr1
    dw.store("filename2", "four five six eventStr1")

    plugin.learn(Vector(dw), None, ints2)
    val returnedModel3 = plugin.getModel.get.retrieve.get
    val retNode3 = returnedModel3.asInstanceOf[Node[String, String]]

    var r8 = retNode3.predict(strCondition1, eventStr1)
    var r9 = retNode3.predict(strCondition1, eventStr2)
    var r10 = retNode3.predict(strCondition1, eventStr3)
    var r11 = retNode3.predict(strCondition2, eventStr1)
    var r12 = retNode3.predict(strCondition2, eventStr2)
    var r13 = retNode3.predict(strCondition2, eventStr3)

    assert(retNode3.getChildren(0)(0).asInstanceOf[Node[String, String]].getChildren(0)(0).asInstanceOf[SequenceList[String, String]].getSequence(strCondition1.drop(1)).get.getWeight == 2.0 / 9)
    assert(r8._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r8._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r9._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r9._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r10._1 == 1.0 / 9 + 1.0 / 9 + 1.0 / 6 + 1.0 / 3 && r10._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r11._1 == 4.0 / 9 + 4.0 / 9 + 4.0 / 6 + 4.0 / 3 && r11._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r12._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r12._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r13._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r13._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)

    val n2 = Node[String, String](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val n2Wrapper = new DataModel
    n2Wrapper.store(n2)
    val dw2 = new StringDataWrapper
    //store strCondition1, eventStr1
    dw2.store("filename3", "one two three eventStr1")

    //DataModel stored as root in DecisionEngine should not change. Only the new tree passed in should be trained.
    val newDM = plugin.learn(Vector(dw2), Some(n2Wrapper), ints2)
    val n2Trained = newDM.get.retrieve.get.asInstanceOf[Node[String, String]]
    assert(n2 == n2Trained)

    var newR1 = n2Trained.predict(strCondition1, eventStr1)
    var newR2 = n2Trained.predict(strCondition1, eventStr2)
    assert(newR1._1 == 4.0 && newR1._2 == 2.0)
    assert(newR2._1 == 2.0 && newR2._2 == 2.0)
    assert(n2Trained.getChildren(0)(0).asInstanceOf[SequenceList[String, String]].getSequence(strCondition1).get.getWeight == 2.0 / 3)

    //Root has not changed after training new model passed in to learn
    val returnedModel4 = plugin.getModel.get.retrieve.get
    val retNode4 = returnedModel3.asInstanceOf[Node[String, String]]

    r8 = retNode4.predict(strCondition1, eventStr1)
    r9 = retNode4.predict(strCondition1, eventStr2)
    r10 = retNode4.predict(strCondition1, eventStr3)
    r11 = retNode4.predict(strCondition2, eventStr1)
    r12 = retNode4.predict(strCondition2, eventStr2)
    r13 = retNode4.predict(strCondition2, eventStr3)

    assert(retNode4.getChildren(0)(0).asInstanceOf[Node[String, String]].getChildren(0)(0).asInstanceOf[SequenceList[String, String]].getSequence(strCondition1.drop(1)).get.getWeight == 2.0 / 9)
    assert(r8._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r8._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r9._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r9._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r10._1 == 1.0 / 9 + 1.0 / 9 + 1.0 / 6 + 1.0 / 3 && r10._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r11._1 == 4.0 / 9 + 4.0 / 9 + 4.0 / 6 + 4.0 / 3 && r11._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r12._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r12._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
    assert(r13._1 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3 && r13._2 == 2.0 / 9 + 2.0 / 9 + 2.0 / 6 + 2.0 / 3)
  }


  test("SMTPlugin - learn returns model if data is empty") {
    //Creating + loading root
    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    val model = new DataModel
    model.store(n1)
    val plugin = new SMTPlugin(new SMTGUI)
    assert(plugin.loadModel(model, true))
    val rootM = plugin.getModel.get.retrieve.get
    assert(rootM.isInstanceOf[Node[Int, Int]])
    val root = rootM.asInstanceOf[Node[Int, Int]]

    var r3 = root.predict(condition1, event1)
    var r4 = root.predict(condition1, event2)
    assert(r3._1 == 4.0 && r1._2 == 2.0)
    assert(r4._1 == 2.0 && r2._2 == 2.0)
    assert(root.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    //Passing in new model to learn - root should not change
    val n2 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    val childrenCountBefore = n2.getChildren.size
    val dm2 = new DataModel
    dm2.store(n2)

    //No data passed in
    val nonTrainedModel = plugin.learn(Vector(), Some(dm2), ints).get.retrieve.get.asInstanceOf[Node[Int, Int]]
    val root2 = plugin.getModel.get.retrieve.get.asInstanceOf[Node[Int, Int]]
    assert(n2 == nonTrainedModel)
    assert(nonTrainedModel.getChildren.size == childrenCountBefore)
    assert(nonTrainedModel.getChildren.size != root2.getChildren.size)

    //Non-trained model predictions different from root's
    val r5 = nonTrainedModel.predict(condition1, event1)
    val r6 = nonTrainedModel.predict(condition1, event2)

    assert(r5._1 == 0.0 && r5._2 == 0.0)
    assert(r6._1 == 0.0 && r6._2 == 0.0)
    assert(nonTrainedModel.getChildren.size == 0)

    //root hasn't changed
    r3 = root2.predict(condition1, event1)
    r4 = root2.predict(condition1, event2)
    assert(r3._1 == 4.0 && r1._2 == 2.0)
    assert(r4._1 == 2.0 && r2._2 == 2.0)
    assert(root2.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
  }
  test("SMTPlugin - learn returns None if no model is set as root or passed as param") {
    val plugin = new SMTPlugin(new SMTGUI)
    assert(plugin.getModel().isEmpty)
    val dw = new StringDataWrapper
    dw.store("filename", "1 2 3 666")
    assert(plugin.learn(Vector(dw), None, ints).isEmpty)
  }
  test("SMTPlugin - passing empty DataWrapper to learn returns DataModel") {
    //Creating + loading root
    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    val model = new DataModel
    model.store(n1)
    val plugin = new SMTPlugin(new SMTGUI)
    val emptyWrapper = new StringDataWrapper
    val returnedModel = plugin.learn(Vector(emptyWrapper), Some(model), ints).get.retrieve.get.asInstanceOf[Node[Int, Int]]

    var r3 = returnedModel.predict(condition1, event1)
    var r4 = returnedModel.predict(condition1, event2)
    assert(r3._1 == 4.0 && r1._2 == 2.0)
    assert(r4._1 == 2.0 && r2._2 == 2.0)
    assert(returnedModel.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
  }
  test("SMTPlugin - Empty string in StringWrapper - passed DataModel returned") {
    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)


    val dm = new DataModel
    dm.store(n1)
    val wrapper = new StringDataWrapper
    wrapper.store("filename", "")
    val plugin = new SMTPlugin(new SMTGUI)
    val returnedModel = plugin.learn(Vector(wrapper), Some(dm), ints).get.retrieve.get.asInstanceOf[Node[Int, Int]]

    r1 = returnedModel.predict(condition1, event1)
    r2 = returnedModel.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(returnedModel.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
  }
  test("SMTPlugin - StringWrapper contains non-numeric calls") {
    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)


    val dm = new DataModel
    dm.store(n1)
    val wrapper = new StringDataWrapper
    wrapper.store("filename", "1 2 3 nonNumeric")
    val plugin = new SMTPlugin(new SMTGUI)
    plugin.setIntRoot(true)
    val returnedModel = plugin.learn(Vector(wrapper), Some(dm), ints).get.retrieve.get.asInstanceOf[Node[Int, Int]]

    r1 = returnedModel.predict(condition1, event1)
    r2 = returnedModel.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(returnedModel.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
  }
  test("SMTPlugin - Multiple StringWrappers - one with non-numeric traces ignored in learn") {
    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    val dm = new DataModel
    dm.store(n1)
    val wrapper = new StringDataWrapper
    wrapper.store("filename", "1 2 3 nonNumeric")
    val wrapper2 = new StringDataWrapper
    wrapper2.store("filename2", "1 2 3 777")
    val plugin = new SMTPlugin(new SMTGUI)
    plugin.setIntRoot(true)
    val returnedModel = plugin.learn(Vector(wrapper, wrapper2), Some(dm), ints).get.retrieve.get.asInstanceOf[Node[Int, Int]]

    val retNode = returnedModel.asInstanceOf[Node[Int, Int]]

    val r5 = retNode.predict(condition1, event1)
    val r6 = retNode.predict(condition1, event2)
    var r7 = retNode.predict(condition1, event3)
    assert(retNode.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
    assert(r5._1 == 2.0 && r5._2 == 2.0)
    assert(r6._1 == 2.0 && r6._2 == 2.0)
    assert(r7._1 == 1.0 && r7._2 == 2.0)
  }
  test("SMTPlugin learn ignores short traces") {

    val n1 = Node[Int, Int](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(condition1, event1)
    var r1 = n1.predict(condition1, event1)
    var r2 = n1.predict(condition1, event2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)

    val dm = new DataModel
    dm.store(n1)
    val shortWrapper = new StringDataWrapper
    shortWrapper.store("filename", "1")
    val wrapper2 = new StringDataWrapper
    wrapper2.store("filename", "1 2 3 777")
    val plugin = new SMTPlugin(new SMTGUI)
    //TODO - SETINTROOT WILL BE DELETED OR MADE PRIVATE => TEST FROM UI
    plugin.setIntRoot(true)
    val returnedModel = plugin.learn(Vector(shortWrapper, wrapper2), Some(dm), ints).get.retrieve.get.asInstanceOf[Node[Int, Int]]

    val retNode = returnedModel.asInstanceOf[Node[Int, Int]]

    val r5 = retNode.predict(condition1, event1)
    val r6 = retNode.predict(condition1, event2)
    var r7 = retNode.predict(condition1, event3)
    assert(retNode.getChildren(0)(0).asInstanceOf[SequenceList[Int, Int]].getSequence(condition1).get.getWeight == 2.0 / 3)
    assert(r5._1 == 2.0 && r5._2 == 2.0)
    assert(r6._1 == 2.0 && r6._2 == 2.0)
    assert(r7._1 == 1.0 && r7._2 == 2.0)
  }
  test("SMTPlugin learn ignores short traces - STRING") {
    val ints2 = false
    val n1 = Node[String, String](maxDepth, maxPhi, maxSeqCount, smoothing, prior)
    n1.learn(strCondition1, eventStr1)
    var r1 = n1.predict(strCondition1, eventStr1)
    var r2 = n1.predict(strCondition1, eventStr2)
    assert(r1._1 == 4.0 && r1._2 == 2.0)
    assert(r2._1 == 2.0 && r2._2 == 2.0)
    assert(n1.getChildren(0)(0).asInstanceOf[SequenceList[String, String]].getSequence(strCondition1).get.getWeight == 2.0 / 3)

    val dm = new DataModel
    dm.store(n1)
    val shortWrapper = new StringDataWrapper
    shortWrapper.store("filename", "shortStr")
    val wrapper2 = new StringDataWrapper
    wrapper2.store("filename", "one two three eventStr2")
    val plugin = new SMTPlugin(new SMTGUI)
    //TODO - SETINTROOT WILL BE DELETED OR MADE PRIVATE => TEST FROM UI
    plugin.setIntRoot(ints2)
    val returnedModel = plugin.learn(Vector(shortWrapper, wrapper2), Some(dm), ints2).get.retrieve.get.asInstanceOf[Node[String, String]]
    val retNode = returnedModel.asInstanceOf[Node[String, String]]
    val r5 = retNode.predict(strCondition1, eventStr1)
    val r6 = retNode.predict(strCondition1, eventStr2)
    var r7 = retNode.predict(strCondition1, eventStr3)
    assert(retNode.getChildren(0)(0).asInstanceOf[SequenceList[String, String]].getSequence(strCondition1).get.getWeight == 2.0 / 3)
    assert(r5._1 == 2.0 && r5._2 == 2.0)
    assert(r6._1 == 2.0 && r6._2 == 2.0)
    assert(r7._1 == 1.0 && r7._2 == 2.0)
  }
  test("SMTPlugin isTrained works") {
    val settings = new SMTSettings(maxDepth, maxPhi, maxSeqCount, smoothing, prior, ints, 1.0, 0.0)
    val config = new SMTConfig
    config.storeSettings(settings)
    val plugin = new SMTPlugin(new SMTGUI)
    plugin.configure(config)

    assert(plugin.getModel.get.retrieve.get.asInstanceOf[Node[Int, Int]].getChildren.size == 0)
    assert(!plugin.isTrained)
    val wrapper = new StringDataWrapper
    wrapper.store("filename", "1 2 3 4")
    assert(plugin.classify(Vector(wrapper), None, ints).get.asInstanceOf[SMTReport].anomalyPercentage.get == 100.0)
    assert(plugin.learn(Vector(wrapper), None, ints).get.retrieve.get.asInstanceOf[Node[Int, Int]].getChildren.nonEmpty)
    assert(plugin.isTrained)
  }
}
