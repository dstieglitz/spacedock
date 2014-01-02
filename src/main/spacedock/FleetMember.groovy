package spacedock

/**
 * Created by IntelliJ IDEA.
 * User: dstieglitz
 * Date: Nov 20, 2009
 * Time: 4:00:53 PM
 * To change this template use File | Settings | File Templates.
 */
class FleetMember {

  def shipName
  def shipSpec
  def id

  public String toString() {
    def msg = "${shipName}"
    return msg
  }

}
