package spacedock
/**
 * Created by IntelliJ IDEA.
 * User: dstieglitz
 * Date: Nov 12, 2009
 * Time: 2:51:00 PM
 * To change this template use File | Settings | File Templates.
 */


import groovy.swing.SwingBuilder

import java.awt.*

import groovy.swing.j2d.GraphicsBuilder
import groovy.swing.j2d.GraphicsPanel
import net.miginfocom.swing.MigLayout

class ShipRenderer1 {

  def gb = new GraphicsBuilder()
  def sb = new SwingBuilder()

  def ship = {ship ->
    println "drawing ship ${ship}"
    def zoom = 40
    int canvasSize = zoom + 1
    def x = 0
    def y = 0

    def beamPanel
    def fireConPanel
    def pointDefencePanel
    def screenGeneratorPanel
    def damageProfilePanel
    def coreSystemsPanel

    def shipPanel = sb.panel(id: 'shipPanel', layout: new MigLayout('wrap 1'), constraints: 'shrink') {
      beamPanel = sb.panel(id: 'beamPanel', layout: new MigLayout('wrap 2'), constraints: 'shrink, gap 0')
      fireConPanel = sb.panel(id: 'fireConPanel', layout: new MigLayout('wrap 2'), constraints: 'shrink, gap 0')
      pointDefencePanel = sb.panel(id: 'pointDefencePanel', layout: new MigLayout('wrap 2'), constraints: 'shrink, gap 0')
      screenGeneratorPanel = sb.panel(id: 'screenGeneratorPanel', layout: new MigLayout('wrap 1'), constraints: 'shrink, gap 0')
      damageProfilePanel = sb.panel(id: 'damageProfilePanel', layout: new MigLayout('wrap 1'), constraints: 'shrink, gap 0')
      coreSystemsPanel = sb.panel(id: 'coreSystemsPanel', layout: new MigLayout('wrap 3'), constraints: 'shrink, gap 0')
    }

    ship.components.findAll {comp -> comp.name.endsWith('beam') }.each {nvpair ->
      def beam = nvpair
      def beamCanvas = sb.panel(new GraphicsPanel(), preferredSize: [canvasSize, canvasSize], minimumSize: [canvasSize, canvasSize])
      def size = zoom / 2

      beamCanvas.graphicsOperation = gb.with {
        group() {
          subtract {
            rays(cx: x + size, cy: y + size, rays: 6, radius: size, rounded: true, extent: 0.999999)
            circle(cx: x + size, cy: y + size, radius: size * 0.70)
          }

          beam.arcs.each {seg ->
            intersect(fill: java.awt.Color.black) {
              subtract() {
                circle(cx: x + size, cy: y + size, radius: size)
                circle(cx: x + size, cy: y + size, radius: size * 0.70)
              }
              arc(x: x, y: y, close: 'pie', width: size * 2, height: size * 2, extent: 60, start: 60)
              transformations {
                rotate(angle: seg * 60, x: x + size, y: y + size)
              }
            }
          }

          font(size: size)
          text(x: (x + size) - (size / 2) + (size * 0.18), y: (y + size) - (size / 2) + (size * 0.1), text: beam.classId, fill: 'black')
        }
      }
      beamPanel.add(beamCanvas)
    }

    ship.components.findAll {comp -> comp.name.endsWith('fireCon') }.each {nvpair ->
      def thePanel = fireConPanel
      def component = nvpair
      def componentCanvas = sb.panel(new GraphicsPanel(), preferredSize: [canvasSize, canvasSize], minimumSize: [canvasSize, canvasSize])
      def size = zoom / 2

      componentCanvas.graphicsOperation = gb.with {
        group() {
          rect(x: x, y: y, width: size, height: size)
          circle(cx: x + size / 2, cy: y + size / 2, radius: size * 0.70, fill: java.awt.Color.black) {
            transformations {
              scale(x: 0.4, y: 0.4)
              translate(x: size * 0.70, y: size * 0.70)
            }
          }
        }
      }
      thePanel.add(componentCanvas)
    }

    ship.components.findAll {comp -> comp.name.endsWith('pointDefence') }.each {nvpair ->
      def thePanel = pointDefencePanel
      def component = nvpair
      def componentCanvas = sb.panel(new GraphicsPanel(), preferredSize: [canvasSize, canvasSize], minimumSize: [canvasSize, canvasSize])
      def size = zoom / 4

      componentCanvas.graphicsOperation = gb.with {
        group() {
          circle(cx: x + size, cy: y + size, radius: size)
          arc(x: x, y: y, close: 'pie', width: size * 2, height: size * 2, extent: 90, start: 45, fill: java.awt.Color.black)
          arc(x: x, y: y, close: 'pie', width: size * 2, height: size * 2, extent: 90, start: 225, fill: java.awt.Color.black)
        }
      }
      thePanel.add(componentCanvas)
    }

    ship.components.findAll {comp -> comp.name.endsWith('screenGenerator') }.each {nvpair ->
      def thePanel = screenGeneratorPanel
      def component = nvpair
      def componentCanvas = sb.panel(new GraphicsPanel(), preferredSize: [canvasSize, canvasSize], minimumSize: [canvasSize, canvasSize])
      def size = zoom / 2

      componentCanvas.graphicsOperation = gb.with {
        group(borderWidth: 2) {
          
        }
      }
      thePanel.add(componentCanvas, 'span, grow')
    }

    ship.components.findAll {comp -> comp.name.endsWith('damageProfile') }.each {nvpair ->
      def thePanel = damageProfilePanel
      def component = nvpair
      def unitBorder = 3
      def maxw = component.armor
      def armc = component.armor > 0 ? 1 : 0
      component.components.each { if (it.points > component.armor) maxw = it.points }

      def size = zoom * 0.75
      def unitSize = size / 4
      def pwidth = maxw * (unitSize * 2) + maxw * unitBorder as int
      def pheight = (component.components.size() + armc) * (unitSize * 2) + maxw * unitBorder as int
      def componentCanvas = sb.panel(new GraphicsPanel(), preferredSize: [pwidth, pheight], minimumSize: [pwidth, pheight])

      componentCanvas.graphicsOperation = gb.with {
        group() {
          (0..component.armor - 1).each {idx ->
            circle(cx: (idx * unitBorder) + unitSize + (idx * unitSize * 2), cy: unitSize, radius: unitSize)
          }
          component.components.eachWithIndex {threshold, tidx ->
            def indices
            if (threshold.damageControl.indexOf(',') < 0) indices = [threshold.damageControl]
            else indices = threshold.damageControl.split(',')
            (0..threshold.points - 1).each {idx ->
              def box = rect(x: (idx * unitBorder) + (idx * unitSize * 2), y: unitBorder + (unitSize * 2) + (tidx * unitBorder) + (tidx * unitSize * 2), width: unitSize * 2, height: unitSize * 2)
              if ((idx + 1).toString() in indices) {
                star(cx: box.x + unitSize, cy: box.y + unitSize, or: unitSize * 0.70, ir: unitSize * 0.40, fill: Color.black)
              }
            }
          }
        }
      }
      thePanel.add(componentCanvas)
    }

    ship.components.findAll {comp -> comp.name.endsWith('ftlDrive') }.each {nvpair ->
      def thePanel = coreSystemsPanel
      def component = nvpair
      def componentCanvas = sb.panel(new GraphicsPanel(), preferredSize: [canvasSize, canvasSize], minimumSize: [canvasSize, canvasSize])
      def size = zoom

      componentCanvas.graphicsOperation = gb.with {
        group() {
          rect(width: size, height: size)
          quadCurve(x1: x, y1: (size / 2), ctrlx: (size / 4), ctrly: 0, x2: (size / 2), y2: (size / 2))
          quadCurve(x1: (size / 2), y1: (size / 2), ctrlx: (size * 0.75), ctrly: size, x2: size, y2: (size / 2))
        }
      }
      thePanel.add(componentCanvas)
    }

    ship.components.findAll {comp -> comp.name.endsWith('spaceDrive') }.each {nvpair ->
      def thePanel = coreSystemsPanel
      def component = nvpair
      def componentCanvas = sb.panel(new GraphicsPanel(), preferredSize: [canvasSize, canvasSize], minimumSize: [canvasSize, canvasSize])
      def size = zoom

      componentCanvas.graphicsOperation = gb.with {
        group() {
          polygon(points: [(size / 2), 0, 0, (size * 0.35), 0, size, size, size, size, (size * 0.35), (size / 2), 0])
          font(size: size * 0.70)
          text(x: size * 0.30, y: size * 0.30, text: '2', fill: Color.black)
        }
      }
      thePanel.add(componentCanvas)
    }

    ship.components.findAll {comp -> comp.name.endsWith('coreSystems') }.each {nvpair ->
      def thePanel = coreSystemsPanel
      def component = nvpair
      def componentCanvas = sb.panel(new GraphicsPanel(), preferredSize: [canvasSize * 3, canvasSize], minimumSize: [canvasSize * 3, canvasSize])
      def borderx = 0.08
      def bordery = 0.05
      def size = zoom

      componentCanvas.graphicsOperation = gb.with {

      }
      thePanel.add(componentCanvas)
    }

    return shipPanel
  }
}
