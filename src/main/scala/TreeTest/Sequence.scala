package TreeTest

/**
  * Created by Case on 02/07/2017.
  */
/*class Sequence (val sequence: List[Int], var predictions: Map[Int, Double]){

  override def toString: String = "\n--------\nSequence\n-sequence:\n    " + sequence + "\n-predictions:\n    " + predictions + "\n-------"

  def getProbability(input:Int): Option[Double] = predictions.get(input)
}*/

class Sequence[A] (val sequence: List[A], var predictions: Map[A, Double]){

  override def toString: String = "\n--------\nSequence\n-sequence:\n    " + sequence + "\n-predictions:\n    " + predictions + "\n-------"

  def getProbability(input: A): Option[Double] = predictions.get(input)
}



/* //With String
class Sequence (val sequence: String, var predictions: Map[String, Double]){

}*/
