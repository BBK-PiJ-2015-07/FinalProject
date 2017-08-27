package DecisionEngine.SMT

import java.awt.event.{ItemEvent, ItemListener}
import java.awt.{Toolkit, _}
import javax.swing.{JFormattedTextField, _}
import javax.swing.text._

import Data.DataModel
import DecisionEngine.DecisionEngineGUI


class SMTGUI extends DecisionEngineGUI {

  override type T = SMTPlugin
  override var pluginInstance: Option[SMTPlugin] = Some(new SMTPlugin)

  private final val maxDepthStr = "Max Depth:"
  private final val maxDepthToolTipStr = "Maximum Depth (sliding window size) of the Sparse Markov Tree. Must be a positive integer!"
  private final val maxPhiStr = "Max Phi:"
  private final val maxPhiToolTipStr = "Maximum Phi: the maximum number of wildcards in the conditioning sequences used for generating the Sparse Markov Tree. Must be a non-negative integer!"
  private final val maxSeqCntStr = "Max sequence count:"
  private final val maxSeqCntToolTipStr = "Maximum Sequence Count: the maximum number of sequence-trails stored in the leaf nodes of the Sparse Markov Tree. Must be a positive integer!"
  private final val smoothingStr = "Smoothing:"
  private final val smoothingToolTipStr = "Smoothing value for calculating probabilities of unseen sequences. Must be a non-negative decimal!"
  private final val priorStr = "Prior weight:"
  private final val priorToolTipStr = "The prior probability of the Sparse Markov Tree mixture. Must be a positive decimal!"
  private final val thresholdStr = "Threshold:"
  private final val thresholdToolTipStr = "The probability threshold for sequence classification. Probabilities below the threshold are classified as anomalous. Must be a non-negative decimal!"
  private final val toleranceStr = "Tolerance (%):"
  private final val toleranceToolTipStr = "If the percentage of subsequences in the analysed system call trace exceed the tolerance, the whole trace is classified as anomalous. Must be a decimal between 0-100!"
  private final val isIntStr = "Integer traces"
  private final val isIntToolTipStr = "Integer traces: check if the analysed system call traces have been pre-processed using \"system call-to-integer\" mapping."

  private final val maxDepthLabel = new JLabel(maxDepthStr)
  private final val maxPhiLabel = new JLabel(maxPhiStr)
  private final val maxSeqCntLabel = new JLabel(maxSeqCntStr)
  private final val smoothingLabel = new JLabel(smoothingStr)
  private final val priorLabel = new JLabel(priorStr)
  private final val thresholdLabel = new JLabel(thresholdStr)
  private final val toleranceLabel = new JLabel(toleranceStr)
  private final val isIntLabel = new JLabel(isIntStr)

  private final val maxDepthField = new JFormattedTextField
  private final val maxPhiField = new JFormattedTextField
  private final val maxSeqCntField = new JFormattedTextField
  private final val smoothingField = new JFormattedTextField
  private final val priorField = new JFormattedTextField
  private final val thresholdField = new JFormattedTextField
  private final val toleranceField = new JFormattedTextField

  private final val isIntCheckBox = new JCheckBox("Integer traces")


  private var isInts: Boolean = false


  override def setPluginInstance(plugin: SMTPlugin): Unit = {
    super.setPluginInstance(plugin)
  }

  override def getGUIComponent: Option[JPanel] = {
    pluginInstance match {
      case None => None
      case Some(smt) => {
        if (smt.isConfigured) {
          val config = smt.getConfiguration.get.getSettings.get.asInstanceOf[SMTSettings]
          //TODO - INITIALISE TEXT FIELDS HERE - MAKE SURE UPDATES ARE REFLECTED
        } else {

        }

        val panel: JPanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
        test(panel)
        Some(panel)
        /* panel.add(new JLabel("Maximum Tree Depth"))
        addIntTextField(panel, 3)*/


        //maxDepth: Int, maxPhi: Int, maxSeqCount: Int, private val _smoothing: Double, private val _prior: Double, threshold: Double, tolerance: Double (percentage), isInt: Boolean


      }
    }
  }


  //TODO - DELETE TEST
  def test(panel: JPanel): Unit = {
    val frame = new JFrame("HIDS")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(800, 500)

    panel.add(maxDepthLabel)
    addNonNegNumTextField(panel, maxDepthField, maxDepthStr, maxDepthToolTipStr, 3, isPositive = true, isDouble = false, isPercent = false)
    panel.add(maxPhiLabel)
    addNonNegNumTextField(panel, maxPhiField, maxPhiStr, maxPhiToolTipStr, 3, isPositive = false, isDouble = false, isPercent = false)
    panel.add(maxSeqCntLabel)
    addNonNegNumTextField(panel, maxSeqCntField, maxSeqCntStr, maxSeqCntToolTipStr, 3, isPositive = true, isDouble = false, isPercent = false)
    panel.add(smoothingLabel)
    addNonNegNumTextField(panel, smoothingField, smoothingStr, smoothingToolTipStr, 3, isPositive = false, isDouble = true, isPercent = false)
    panel.add(priorLabel)
    addNonNegNumTextField(panel, priorField, priorStr, priorToolTipStr, 3, isPositive = true, isDouble = true, isPercent = false)
    panel.add(thresholdLabel)
    addNonNegNumTextField(panel, thresholdField, thresholdStr, thresholdToolTipStr, 5, isPositive = false, isDouble = true, isPercent = false)
    panel.add(toleranceLabel)
    addNonNegNumTextField(panel, toleranceField, toleranceStr, toleranceToolTipStr, 5, isPositive = false, isDouble = true, isPercent = true)

    isIntCheckBox.addItemListener(new ItemListener {
      override def itemStateChanged(e: ItemEvent): Unit = {
        val isIntsBox = e.getSource.asInstanceOf[JCheckBox]
        isInts = isIntsBox.isSelected
        println("intbox state: " + isInts)
      }
    })
    panel.add(isIntCheckBox)





    val cp = frame.getContentPane
    cp.setLayout(new FlowLayout(FlowLayout.LEFT))
    cp.add(panel)
    frame.pack()
    frame.setVisible(true)


  }

  private def setPluginRoot(model: DataModel): Boolean = {
    pluginInstance match {
      case None => false
      case Some(plugin) => plugin.loadModel(model)
    }
  }

  private def isConfigured = ???
  def reset() = ???

  private def addNonNegNumTextField(panel: JPanel, field: JFormattedTextField, fieldName: String, tooltipStr: String, col: Int, isPositive: Boolean, isDouble: Boolean, isPercent: Boolean) = {
    field.setColumns(col)
    field.setName(fieldName)
    field.createToolTip()
    field.setToolTipText(tooltipStr)
    val doc = field.getDocument.asInstanceOf[PlainDocument]
    doc.setDocumentFilter(new NonNegIntFilter(isPositive, isDouble, isPercent))
    panel.add(field)
  }

  private class NonNegIntFilter(isPositive: Boolean, isDouble: Boolean, isPercent: Boolean) extends DocumentFilter {

    @throws[BadLocationException]
    override def insertString(fb: DocumentFilter.FilterBypass, offset: Int, string: String, attr: AttributeSet): Unit = {
      println("in insertString")
      val doc = fb.getDocument
      val sb = new StringBuilder
      sb.append(doc.getText(0, doc.getLength))
      sb.insert(offset, string)
      if (test(sb.toString, isDouble, isPercent)) super.insertString(fb, offset, string, attr)
      else {
        // warn the user and don't allow the insert
        println("insertString test failed. removing new char")
        Toolkit.getDefaultToolkit.beep
      }
    }

    private def test(text: String, isDouble: Boolean, isPercent: Boolean) = try {
      println("in test")

      if (isDouble) {
        val input = text.toDouble
        if (isPercent) {
          (isPositive && input > 0.0 && input <= 100.0) || (!isPositive && input >= 0.0 && input <= 100.0)
        } else {
          (isPositive && input > 0.0) || (!isPositive && input >= 0.0)
        }
      } else {
        val input = text.toInt
        (isPositive && input > 0) || (!isPositive && input >= 0)
      }
    } catch {
      case e: Throwable =>
        println("EXCEPTION: " + e.getMessage)
        false
    }

    @throws[BadLocationException]
    override def replace(fb: DocumentFilter.FilterBypass, offset: Int, length: Int, text: String, attrs: AttributeSet): Unit = {
      println("in replace")
      val doc = fb.getDocument
      val sb = new StringBuilder
      sb.append(doc.getText(0, doc.getLength))
      sb.replace(offset, offset + length, text)
      if (test(sb.toString, isDouble, isPercent)) super.replace(fb, offset, length, text, attrs)
      else {
        Toolkit.getDefaultToolkit.beep
      }
    }

    @throws[BadLocationException]
    override def remove(fb: DocumentFilter.FilterBypass, offset: Int, length: Int): Unit = {
      println("in remove")
      val doc = fb.getDocument
      val sb = new StringBuilder
      sb.append(doc.getText(0, doc.getLength))
      sb.delete(offset, offset + length)
      if (sb.toString().length() == 0) super.replace(fb, offset, length, "", null)
      else if (test(sb.toString, isDouble, isPercent)) super.remove(fb, offset, length)
      else {
        Toolkit.getDefaultToolkit.beep
      }
    }
  }

}