package spacedock
/**
 * Created by IntelliJ IDEA.
 * User: dstieglitz
 * Date: Nov 13, 2009
 * Time: 8:11:10 AM
 * To change this template use File | Settings | File Templates.
 */
class FTJavaShipParser {

  def nodeNameMap = ['BeamBattery': 'beam', 'PointDefense': 'pointDefence',
          'FireControl': 'fireCon', 'FTLDrive': 'ftlDrive', 'ClassName': 'ClassName',
          'MainDrive': 'spaceDrive', 'Armor': 'damageProfile', 'Mass': 'Mass', 'Hull': 'Hull',
          'ShipClass': 'ShipClass', 'Screen': 'screenGenerator', 'PointValue': 'PointValue',
          'PulseTorpedo': 'pulseTorpedo', 'FighterBay': 'fighterBay',
          'SalvoMissileLauncher': 'salvoMissileLauncher', 'SalvoMissileMagazine': 'salvoMissileMagazine']

  def arcStringMap = ['F': '0', 'FS': '1', 'AS': '2', 'A': '3', 'AP': '4', 'FP': '5', 'All arcs': null]

  def ship = [components: [[name: 'coreSystems']]]

  def weakTypeCheck = true

  public String toShipBuilderScript() {
    def shipBuilderScript = new StringBuffer()

    shipBuilderScript.append("ship(")
    def attrs = ship.findAll {c -> c.key != 'components'}.keySet() as List
    for (int i = 0; i < attrs.size(); i++) {
      def key = attrs[i]
      def val = ship[key]
      shipBuilderScript.append("${key}:'$val'")
      if (i < attrs.size() - 1) shipBuilderScript.append(",")
      ship.remove(key)
    }
    shipBuilderScript.append(") {\n")

    ship.components.each {map ->
      shipBuilderScript.append("${map.name}(")
      map.remove('name')
      def keys = map.keySet() as List
      for (int i = 0; i < keys.size(); i++) {
        def key = keys[i]
        def val = map[key]
        shipBuilderScript.append("${key}:'$val'")
        if (i < keys.size() - 1) shipBuilderScript.append(",")
      }
      shipBuilderScript.append(")\n")
    }
    shipBuilderScript.append("}")
    return shipBuilderScript.toString()
  }

  def parseShip = {fileName ->
    def shipXml = new XmlSlurper().parse(fileName)
    processChildren(shipXml.depthFirst().collect {it})
    //println ship
    return toShipBuilderScript()
  }

  def processArcs = {arcString ->
    def result = ''
    arcString = arcString[1..arcString.length() - 2]
    def arr = arcString.split('/')
    for (int i = 0; i < arr.size(); i++) {
      result += arcStringMap[arr[i]]
      if (i < arr.size() - 1) result += ','
    }

    return (result == 'null') ? null : result
  }

  def processPointValue = {node ->
    ship.pointValue = node.text()
  }

  def processMass = {node ->
    //println "mass is ${node.text()}"
    ship.mass = node.text()
  }

  def processHull = {node ->
    //println "hull is ${node.@type.text()}"
    ship.hull = node.@type.text()
    if (ship.hull == 'Custom') {
      if (!node.@totalHullBoxes) throw new RuntimeException("A custom hull type must specify damage points")
      else ship.damagePoints = node.@totalHullBoxes.text()
    }
  }

  def processClassName = {node ->
    ship.put('name', node.text())
  }

  def processShipClass = {node ->
    ship.put('shipClass', node.text())
  }

  def processChildren = {children ->
    children.each {
      def newName = nodeNameMap[it.name()]
      if (newName) {
        try {
          this."process${newName}"(it)
        } catch (MissingMethodException e) {
          //println e.message
          def comp = [name: newName]
          def attrs = it.attributes().keySet() as List
          for (int i = 0; i < attrs.size(); i++) {
            def key = attrs[i]
            def val
            if (key == 'arcs') val = processArcs(it.attributes()[key])
            else val = it.attributes()[key]
            if (val) comp.put(key, val)
          }
          ship.components.add(comp)
        }
      } else {
        def msg = "I don't know anything about a(n) ${it.name()}"
        if (weakTypeCheck) println msg
        else throw new RuntimeException(msg)
      }
    }
  }

}
