package spacedock
/**
 * Created by IntelliJ IDEA.
 * User: dstieglitz
 * Date: Nov 12, 2009
 * Time: 2:54:28 PM
 * To change this template use File | Settings | File Templates.
 */


import groovy.swing.SwingBuilder

import java.awt.*

import groovy.swing.j2d.GraphicsBuilder
import groovy.swing.j2d.GraphicsPanel

/**
 * Renders according to Fleet Book 1 rules for ships 
 */
class ShipRenderer2 {

  def gb = new GraphicsBuilder()
  def sb = new SwingBuilder()
  def zoom = 1.0
  def size = 15
  def vgap = 20 // gap between components in bottom panel (damage profile and core systems)
  def unitBorder = size * 0.26 as int

  private static int getInteger(var) {
    //println var
    try {
      return Integer.parseInt(var)
    } catch (Throwable t) {
      //println "Invalid coordinate: ${t.message}"
      return 0
    }
  }

  private static int largestOf(intList) {
    def val = 0
    intList.each {
      if (it > val) val = it
    }
    return val
  }

  def ship = {ship ->
    ship = new FB1ShipRulesDecorator(ship)
    //println "TMF: ${decorator.getTotalMassFactor()}"

    def minx = null
    def miny = null
    def maxx = 0
    def maxy = 0

    ship.components.each {
      //println "${it.name}: x=${it.xSSD},y=${it.ySSD}"
      if (it.xSSD) {
        def val = getInteger(it.xSSD)
        if (getInteger(it.xSSD) > maxx) maxx = val
        if (!minx || val < minx) minx = val
      }
      if (it.ySSD) {
        def val = getInteger(it.ySSD)
        if (val > maxy) maxy = val
        if (!miny || val < miny) miny = val
      }
    }

    //println "MAXX=${maxx}"
    //println "MAXY=${maxy}"

    def x = 0
    def y = 0

    if (!minx) minx = size * 6
    if (!miny) miny = 0

    //def hgap = size * 2 + size

    def damageProfile = ship.components.find {c -> c.name.endsWith('damageProfile')}
    def totalDamagePoints = ship.damagePoints
    def rowDamagePoints = Math.floor(totalDamagePoints / 4)
    def remainder = totalDamagePoints % 4
    //println damageProfile
    def armor = damageProfile ? getInteger(damageProfile.totalArmor) : 0
    //println armor
    def unitSize = size * 0.30

    def units = rowDamagePoints + remainder
    def rows = armor > 0 ? 5 : 4
    def damageProfileWidth = unitSize * 2 * units + unitBorder * (units - 1) as int
    def damageProfileHeight = unitSize * 2 * rows + unitBorder * (rows - 1) as int

    def canvasWidth = Math.round(largestOf([(maxx + minx + (size * 2)), damageProfileWidth + minx, size * 6]) * zoom) as int
    def canvasHeight = Math.round(largestOf([maxy + (vgap * 2), maxy + (size * 3) + damageProfileHeight + (vgap * 2)]) * zoom) as int// core systems are drawn at (size*2) scale

    def panel = sb.panel(new GraphicsPanel(), minimumSize: [canvasWidth + 1, canvasHeight + 1])
    def go

    gb.with {
      go = group() {
        // debug
        //rect(x: 0, y: 0, width: canvasWidth, height: canvasHeight)

        // clear the default black background
        rect(x: 0, y: 0, width: canvasWidth + 1, height: canvasHeight + 1, fill: 'white', borderColor: 'white')

        font(size: size)
        text(x: 0, y: 0, text: "${ship.name} class ${ship.shipClass}", f: 'black', bc: 'none')
        font(size: 10)
        text(x: 0, y: size, text: "TMF ${ship.mass} / NPV ${ship.pointValue}", f: 'black', bc: 'none')

        ship.components.findAll {c -> c.name.endsWith('beam') }.each {beam ->
          x = ShipRenderer2.getInteger(beam.xSSD)
          y = ShipRenderer2.getInteger(beam.ySSD)

          if (beam.arcs) {
            subtract(fill: java.awt.Color.black) {
              rays(cx: x + size, cy: y + size, rays: 6, radius: size, rounded: true, extent: 0.999999)
              circle(cx: x + size, cy: y + size, radius: size * 0.70)
            }

            beam.arcs.split(',').each {seg ->
              intersect(fill: java.awt.Color.white) {
                subtract() {
                  circle(cx: x + size, cy: y + size, radius: size)
                  circle(cx: x + size, cy: y + size, radius: size * 0.70)
                }
                arc(x: x, y: y, close: 'pie', width: size * 2, height: size * 2, extent: 60, start: 60)
                transformations {
                  rotate(angle: ShipRenderer2.getInteger(seg) * 60, x: x + size, y: y + size)
                }
              }
            }
          } else {
            subtract(fill: java.awt.Color.white) {
              rays(cx: x + size, cy: y + size, rays: 6, radius: size, rounded: true, extent: 0.999999)
              circle(cx: x + size, cy: y + size, radius: size * 0.70)
            }
          }

          font(size: size)
          text(x: (x + size) - (size / 2) + (size * 0.18), y: (y + size) - (size / 2) + (size * 0.1), text: beam.rating, fill: 'black')
        }

        ship.components.findAll {c -> c.name.endsWith('fireCon') }.each {fireCon ->
          x = ShipRenderer2.getInteger(fireCon.xSSD)
          y = ShipRenderer2.getInteger(fireCon.ySSD)

          rect(x: x, y: y, width: size, height: size)
          circle(cx: x + (size / 2), cy: y + (size / 2), radius: size * 0.25, fill: java.awt.Color.black)
        }

        ship.components.findAll {c -> c.name.endsWith('screenGenerator') }.each {screenGenerator ->
          x = ShipRenderer2.getInteger(screenGenerator.xSSD)
          y = ShipRenderer2.getInteger(screenGenerator.ySSD)

          group(borderWidth: 2) {
            circle(cx: x + size, cy: y + size, radius: size * 0.30, fill: java.awt.Color.black)
            arc(x: x, y: y + (size * 0.11), close: 'open', width: size * 2, height: size * 2, extent: 70, start: 55)
            arc(x: x + (size / 2), y: y + (size * 0.41), close: 'open', width: size, height: size, extent: 90, start: 45)
          }
        }

        ship.components.findAll {c -> c.name.endsWith('pulseTorpedo') }.each {pulseTorpedo ->
          x = ShipRenderer2.getInteger(pulseTorpedo.xSSD)
          y = ShipRenderer2.getInteger(pulseTorpedo.ySSD)
          def ttf = 0

          if (pulseTorpedo.arcs) ttf = size * 0.80

          rect(x: x + (size * 0.75), y: y + ttf, width: (size * 0.50), height: size, fill: 'black')
          rect(x: x + (size * 0.75), y: y + size + ttf, width: (size * 0.50), height: size, fill: 'white')

          if (pulseTorpedo.arcs) {
            pulseTorpedo.arcs.split(',').each {seg ->
              intersect(fill: java.awt.Color.white) {
                subtract() {
                  circle(cx: x + size, cy: y + size + ttf, radius: size * 3)
                  circle(cx: x + size, cy: y + size + ttf, radius: size * 1.3)
                }
                arc(x: x, y: y, close: 'pie', width: size * 2, height: size * 2, extent: 60, start: 60)
                transformations {
                  rotate(angle: ShipRenderer2.getInteger(seg) * 60, x: x + size, y: y + size)
                }
              }
            }
          }
        }

        def magazinesById = [:]

        ship.components.findAll {c -> c.name.endsWith('salvoMissileMagazine') }.each {salvo ->
          x = ShipRenderer2.getInteger(salvo.xSSD)
          y = ShipRenderer2.getInteger(salvo.ySSD)
          def cap = ShipRenderer2.getInteger(salvo.capacity)
          def magazineWidth = size * cap / 2 + size * 0.30
          magazinesById.put(salvo.id, salvo)

          group() {
            def mbox = rect(x: x, y: y, width: magazineWidth, height: size * 2)
            (0..cap / 2 - 1).each {
              triangle(x: (it * size) + (x + (size * 0.15)), y: y + size * 2 - (size * 0.15), width: size, height: size * 1.70, fill: 'white')
            }
          }
        }


        ship.components.findAll {c -> c.name.endsWith('salvoMissileLauncher') }.each {salvo ->
          x = ShipRenderer2.getInteger(salvo.xSSD)
          y = ShipRenderer2.getInteger(salvo.ySSD)
          def magazine = magazinesById[salvo.smmId]

          if (magazine) {
            def cap = ShipRenderer2.getInteger(magazine.capacity)
            def magazineWidth = size * cap / 2 + size * 0.30 as int
            def x2 = ShipRenderer2.getInteger(magazine.xSSD) + (magazineWidth / 2)
            line(x1: x + size, y1: y + size * 0.70 * 2, x2: x2, y2: magazine.ySSD, fill: Color.gray, borderWidth: 2)
          }

          def ttd = 0
          if (salvo.arcs) ttf = size * 0.70

          circle(cx: x + size, cy: y + size, radius: size * 0.70, fill: 'white')
          triangle(x: x + size * 0.65, y: y + size * 0.75 + ttf, width: size * 0.70, height: size, fill: 'black')

          if (salvo.arcs) {
            salvo.arcs.split(',').each {seg ->
              intersect(fill: java.awt.Color.white) {
                subtract() {
                  circle(cx: x + size, cy: y + size + ttf, radius: size * 3)
                  circle(cx: x + size, cy: y + size + ttf, radius: size * 1.35)
                }
                arc(x: x, y: y, close: 'pie', width: size * 2, height: size * 2, extent: 60, start: 60)
                transformations {
                  rotate(angle: Integer.parseInt(seg) * 60, x: x + size, y: y + size)
                }
              }
            }
          }
        }

        ship.components.findAll {c -> c.name.endsWith('fighterBay') }.each {fighterBay ->
          x = ShipRenderer2.getInteger(fighterBay.xSSD)
          y = ShipRenderer2.getInteger(fighterBay.ySSD)

          triangle(x: x, y: y, width: size, height: (size * 2), fill: 'white')
        }



        ship.components.findAll {c -> c.name.endsWith('pointDefence') }.each {pointDefence ->
          x = ShipRenderer2.getInteger(pointDefence.xSSD)
          y = ShipRenderer2.getInteger(pointDefence.ySSD)

          circle(cx: x + size, cy: y + size, radius: (size / 2))
          arc(x: x + (size / 2), y: y + (size / 2), close: 'pie', width: size, height: size, extent: 90, start: 45, fill: java.awt.Color.black)
          arc(x: x + (size / 2), y: y + (size / 2), close: 'pie', width: size, height: size, extent: 90, start: 225, fill: java.awt.Color.black)
        }

        //y = maxy + ((canvasHeight - maxy) / 2) - (damageProfileHeight / 2)
        y = maxy + size + vgap
        x+=1

        if (damageProfile) {
          x = ShipRenderer2.getInteger(damageProfile.xSSD)
          if (ShipRenderer2.getInteger(damageProfile.ySSD) > 0) y = ShipRenderer2.getInteger(damageProfile.ySSD)
          //else y = y + (size * 2)

          // debug
          //rect(x: 0, y: y, width: damageProfileWidth, height: damageProfileHeight, borderColor: 'white')
          def counter = 1
          group() {
            if (armor > 0) {
              (0..armor - 1).each {idx ->
                circle(cx: x + (idx * unitBorder) + unitSize + (idx * unitSize * 2), cy: y + unitSize, radius: unitSize)
              }
              y += (unitSize * 2) + unitBorder
            }
            (0..3).each {row ->
              def damagePointsThisRow = rowDamagePoints + ((remainder-- > 0) ? 1 : 0)
              (0..damagePointsThisRow - 1).each {col ->
                def box = rect(x: x + (col * unitBorder) + (col * unitSize * 2), y: y + (row * unitBorder) + (row * unitSize * 2), width: unitSize * 2, height: unitSize * 2)
                if (ship.casualtyFactor > 0 && ((counter % ship.casualtyFactor == 0) || counter == ship.damagePoints)) {
                  star(cx: box.x + unitSize, cy: box.y + unitSize, or: unitSize * 0.70, ir: unitSize * 0.40, fill: Color.black)
                }
                counter++
              }
//              if (row == 3 && remainder > 0) {
//                (rowDamagePoints..(rowDamagePoints + remainder - 1)).each {col ->
//                  def box = rect(x: x + (col * unitBorder) + (col * unitSize * 2), y: y + (row * unitBorder) + (row * unitSize * 2), width: unitSize * 2, height: unitSize * 2)
//                  if (counter++ % 6 == 0 || counter==col) {
//                    star(cx: box.x + unitSize, cy: box.y + unitSize, or: unitSize * 0.70, ir: unitSize * 0.40, fill: Color.black)
//                  }
//                }
//              }
            }
          }
        }

        // draw ship core systems at twice size factor
        def coreSystemsSize = size * 2
        y = canvasHeight - coreSystemsSize

        // debug
        //rect(x: 0, y: y, width: canvasWidth, height: coreSystemsSize, borderColor: 'green')

        // debug
        //rect(x: 0, y: y, width: coreSystemsSize*6, height: coreSystemsSize, borderColor: 'white')

        def ftl = ship.components.find {c -> c.name.endsWith('ftlDrive')}
        if (ftl) {
          //x = spacedock.ShipRenderer2.getInteger(ftl.xSSD)
          //y = spacedock.ShipRenderer2.getInteger(ftl.ySSD)
          x = 0
          rect(x: x, y: y, width: coreSystemsSize, height: coreSystemsSize)
          quadCurve(x1: x, y1: y + (coreSystemsSize / 2), ctrlx: x + (coreSystemsSize / 4), ctrly: y + 0, x2: x + (coreSystemsSize / 2), y2: y + (coreSystemsSize / 2))
          quadCurve(x1: x + (coreSystemsSize / 2), y1: y + (coreSystemsSize / 2), ctrlx: x + (coreSystemsSize * 0.75), ctrly: y + coreSystemsSize, x2: x + coreSystemsSize, y2: y + (coreSystemsSize / 2))
        }

        def spaceDrive = ship.components.find {c -> c.name.endsWith('spaceDrive')}
        if (spaceDrive) {
          x = coreSystemsSize + ((canvasWidth - coreSystemsSize - coreSystemsSize * 3) / 2) - (coreSystemsSize / 2)

          polygon(points: [x + (coreSystemsSize / 2), y, x, y + (coreSystemsSize * 0.35), x, y + coreSystemsSize, x + coreSystemsSize, y + coreSystemsSize, x + coreSystemsSize, y + (coreSystemsSize * 0.35), x + (coreSystemsSize / 2), y])
          font(size: coreSystemsSize * 0.70)
          text(x: x + coreSystemsSize * 0.30, y: y + coreSystemsSize * 0.30, text: spaceDrive.initialThrust, fill: Color.black)
        }

        def coreSystems = ship.components.find {c -> c.name.endsWith('coreSystems')}
        if (coreSystems) {
          def borderx = 0.08
          def bordery = 0.05
          x = canvasWidth - (coreSystemsSize * 3)

          group() {
            rect(width: coreSystemsSize * 3, height: coreSystemsSize)
            group() {
              def box = rect(width: coreSystemsSize, height: coreSystemsSize, fill: 'black')
              circle(cx: coreSystemsSize * 0.50, cy: coreSystemsSize * 0.35, radius: coreSystemsSize * 0.20, borderColor: 'white', borderWidth: 2)
              line(x1: coreSystemsSize * 0.15, y1: coreSystemsSize * 0.55, x2: coreSystemsSize * 0.85, y2: coreSystemsSize * 0.55, borderColor: 'white', borderWidth: 2)
              line(x1: coreSystemsSize * 0.15, y1: coreSystemsSize * 0.70, x2: coreSystemsSize * 0.85, y2: coreSystemsSize * 0.70, borderColor: 'white', borderWidth: 2)
              line(x1: coreSystemsSize * 0.15, y1: coreSystemsSize * 0.85, x2: coreSystemsSize * 0.85, y2: coreSystemsSize * 0.85, borderColor: 'white', borderWidth: 2)
              transformations {
                scale(x: 0.90, y: 0.90)
                translate(x: coreSystemsSize * borderx, y: coreSystemsSize * bordery)
              }
            }
            group() {
              def box = rect(width: coreSystemsSize, height: coreSystemsSize, fill: 'black')
              circle(cx: coreSystemsSize * 0.50, cy: coreSystemsSize * 0.50, radius: coreSystemsSize * 0.40, borderColor: 'white', borderWidth: 2)
              circle(cx: coreSystemsSize * 0.50, cy: coreSystemsSize * 0.30, radius: coreSystemsSize * 0.08, fill: 'white')
              line(x1: coreSystemsSize * 0.30, y1: coreSystemsSize * 0.45, x2: coreSystemsSize * 0.70, y2: coreSystemsSize * 0.45, borderColor: 'white', borderWidth: 2)
              line(x1: coreSystemsSize * 0.50, y1: coreSystemsSize * 0.45, x2: coreSystemsSize * 0.50, y2: coreSystemsSize * 0.75, borderColor: 'white', borderWidth: 2)
              transformations {
                scale(x: 0.90, y: 0.90)
                translate(x: coreSystemsSize + (2 * coreSystemsSize * borderx), y: coreSystemsSize * bordery)
              }
            }
            group() {
              def box = rect(width: coreSystemsSize, height: coreSystemsSize, fill: 'black')
              circle(cx: coreSystemsSize * 0.50, cy: coreSystemsSize * 0.50, radius: coreSystemsSize * 0.08, fill: 'white')
              circle(cx: coreSystemsSize * 0.50, cy: coreSystemsSize * 0.50, radius: coreSystemsSize * 0.30, borderColor: 'white') {
                transformations {
                  translate(x: -coreSystemsSize * 0.40)
                  skew(x: 0.80)
                }
              }
              group() {
                circle(cx: coreSystemsSize * 0.50, cy: coreSystemsSize * 0.50, radius: coreSystemsSize * 0.30, borderColor: 'white') {
                  transformations {
                    translate(x: -coreSystemsSize * 0.40)
                    skew(x: 0.80)
                  }
                }
                transformations {
                  rotate(angle: 100, x: coreSystemsSize * 0.50, y: coreSystemsSize * 0.50)
                }
              }
              group() {
                circle(cx: coreSystemsSize * 0.50, cy: coreSystemsSize * 0.50, radius: coreSystemsSize * 0.30, borderColor: 'white') {
                  transformations {
                    translate(x: -coreSystemsSize * 0.40)
                    skew(x: 0.80)
                  }
                }
                transformations {
                  rotate(angle: 230, x: coreSystemsSize * 0.50, y: coreSystemsSize * 0.50)
                }
              }
              transformations {
                scale(x: 0.90, y: 0.90)
                translate(x: 2 * coreSystemsSize + (3 * coreSystemsSize * borderx), y: coreSystemsSize * bordery)
              }
            }
            transformations {
              translate(x: x, y: y)
            }
          }
        }

        //
        transformations {
          scale(x: zoom, y: zoom)
        }
      } // top group
    } // gb.with

    panel.graphicsOperation = go
    return panel
  }
}
