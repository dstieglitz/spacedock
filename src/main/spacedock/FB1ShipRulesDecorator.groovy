package spacedock
/**
 *
 * This class wraps a ship object and does validation, rule-based calculation, etc.
 *
 * Created by IntelliJ IDEA.
 * User: dstieglitz
 * Date: Nov 14, 2009
 * Time: 8:43:15 AM
 * To change this template use File | Settings | File Templates.
 */
class FB1ShipRulesDecorator {

  private def underlyingShip

  def eval = {script, binding ->
    GroovyShell shell = new GroovyShell(binding);
    return shell.evaluate(script)
  }

  public FB1ShipRulesDecorator(ship) {
    this.underlyingShip = ship
    validate()
  }

  public void validate() {
    if (!underlyingShip.mass) throw new RuntimeException("Must specify ship's mass")
    if (!underlyingShip.hull) throw new RuntimeException("Must specify ship's hull type")
  }

  private int getInteger(val) {
    try {
      return Integer.parseInt(val)
    } catch (Throwable t) {
      throw new RuntimeException("Invalid integer value: ${t.message}")
    }
  }

  public int getDamagePoints() {
    if (underlyingShip.hull=='Custom') return Integer.parseInt(underlyingShip.damagePoints)
    def mass = getInteger(underlyingShip.mass)
    def hullFactor
    switch (underlyingShip.hull.toLowerCase()) {
      case 'fragile': hullFactor = 0.10; break;
      case 'weak': hullFactor = 0.20; break;
      case 'average': hullFactor = 0.30; break;
      case 'strong': hullFactor = 0.40; break;
      case 'super': hullFactor = 0.50; break;
      default: hullFactor = 0.0; break;
    }
    def dp = Math.round(mass*hullFactor)
    //println "DAMAGE POINTS=${dp}"
    return dp
  }

//  public int getMass() {
//    def tmf = 0
//    underlyingShip.components.each {comp ->
//      if (comp.getMass) {
//        def b = new Binding();
//        b.setVariable(comp.name, comp)
//        tmf += eval(comp.getMass, b)
//      }
//    }
//
//    return tmf
//  }

  public int getCrewSize() {
    def mass = getInteger(underlyingShip.mass)
    def massCrewRatio = 20
    //if (underlyingShip.type=='Civilian') {
    // def massCrewRatio = 50
    //}
    def cs
    if (mass < massCrewRatio) cs = 1
    else cs = Math.ceil(mass/massCrewRatio)
    //}
    //println "CREW SIZE=${cs}"
    return cs
  }

  // this is the number of damage points in between crew casualty boxes
  // (marked with a star on the SSD)

  public int getCasualtyFactor() {
    def cf = Math.ceil(this.damagePoints/this.crewSize)
    //println "CASUALTY FACTOR=${cf}"
    return cf
  }

//  def invokeMethod(String name, args) {
//    println "INVOKE ${name}"
//    try {
//      return this.invokeMethod(name, args)
//    } catch (Throwable t) {
//      try {
//        return underlyingShip.invokeMethod(name, args)
//      } catch (Throwable s) {
//        throw t
//      }
//    }
//  }

  def getProperty(String name) {
    //println "GET ${name}"
    if (this.metaClass.properties.find {p -> p.name == name }) return this.metaClass.properties.find {p -> p.name == name}.getProperty(this)
    else return underlyingShip[name]
  }

}
