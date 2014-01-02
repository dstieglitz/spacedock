package spacedock

import com.lowagie.text.Document
import com.lowagie.text.DocumentException
import com.lowagie.text.pdf.PdfWriter

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

/**
 * Created by IntelliJ IDEA.
 * User: dstieglitz
 * Date: Nov 20, 2009
 * Time: 11:46:52 AM
 * To change this template use File | Settings | File Templates.
 */
class FleetRenderer1 {

  def columns = 3
  def shipRenderer = new ShipRenderer2()
  def model
  def scale = 100.0f
  def dpi = 300

  def canvasToImage = {comp, dpi = this.dpi ->
    println "canvasToImage dpi = ${dpi}"   
    if (comp) {
      comp.setSize(comp.minimumSize)
      try {
        int wp = comp.minimumSize.width
        int hp = comp.minimumSize.height
        float wi = wp / 72
        float hi = hp / 72
        BufferedImage image = new BufferedImage(wi * dpi as int, hi * dpi as int, BufferedImage.TYPE_INT_RGB)
        Graphics2D g2 = image.createGraphics()
        g2.scale(dpi / 72f, dpi / 72f)
        g2.setColor(java.awt.Color.green)
        g2.fillRect(0, 0, wp, hp)
        comp.paint(g2)
        g2.dispose()
        return image
      } catch (IOException e) {
        System.err.println(e);
      }
    } else {
      println "No component to save"
    }
  }

  def saveImage = {evt = null ->
    def comp = model.canvas
    def image = canvasToImage(comp)
    ImageIO.write(image, "jpeg", new File("/Users/dstieglitz/NetBeansProjects/Spacedock/test.jpeg"));
  }

  public void renderFleet(fleet, filename) {
    // step 1
    Document document = new Document();
    println fleet

    Binding binding = new Binding();
    binding.setVariable('registry', model.registry)
    GroovyShell shell = new GroovyShell(binding);

    def images = []
    fleet.ships.toArray().each {ship ->
      def script = "return new spacedock.ShipBuilder(registry).${ship.shipSpec}".toString()
      def canvas = shipRenderer.ship(shell.evaluate(script))
      images.add(canvasToImage(canvas))     
    }

    println document.pageSize.width
    println document.pageSize.height

    try {
      // step 2
      PdfWriter.getInstance(document, new FileOutputStream(filename));
      // step 3
      document.open();
      // determine how many images can fit in a row
//    def rowMax = document.pageSize.width
//    def row = 0
      def table = new com.lowagie.text.Table(columns);
      table.setPadding(5)
      table.setBorderWidth(1)
      table.setWidth(100.0f)
      /*
      images.each {image ->
        ImageIO.write(image, "jpeg", new File("/Users/dstieglitz/NetBeansProjects/Spacedock/test.jpeg"))
        println "image width is ${image.width}"        
        def desiredColWidth = document.pageSize.width / columns
        println "desired with is ${desiredColWidth}"
        if (image.width > desiredColWidth) scale = desiredColWidth / image.width * 100.0f
        println "scaling by ${scale as float}"
        //     if (image.width < (rowMax - row)) {
        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(image, null)
        img.scalePercent(scale as float)
        document.add(img)
        //     }
      }
      */
      images.each { image ->
        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(image, null)
        table.addCell(new com.lowagie.text.Cell(img))
      }
      document.add(table)
      
    } catch (DocumentException de) {
      System.err.println(de.getMessage());
    } catch (IOException ioe) {
      System.err.println(ioe.getMessage());
    }
    // step 5
    println "all done"
    document.close();
  }

}
