    def pulseTorpedo = [xSSD:0,ySSD:0,arcs:['5','0','1']]
    
    x = 301
    y = 110
    def ttf = 0
    def size = 15
    
    if (pulseTorpedo.arcs) ttf = size * 0.80
    
    rect(x: x + (size * 0.75), y: y + ttf, width: (size * 0.50), height: size, fill: 'black')
    rect(x: x + (size * 0.75), y: y + size + ttf, width: (size * 0.50), height: size, fill: 'white')
    
    if (pulseTorpedo.arcs) {
    pulseTorpedo.arcs.each {seg ->
      intersect(fill: java.awt.Color.white) {
        subtract() {
          circle(cx: x + size, cy: y + size + ttf, radius: size * 3)
          circle(cx: x + size, cy: y + size + ttf, radius: size * 1.3)
        }
        arc(x: x, y: y, close: 'pie', width: size * 2, height: size * 2, extent: 60, start: 60)
        transformations {
          //scale(x:0.80,y:0.70)
         // translate(x:x+(size*0.25),y:y+(ttf*0.50))
          rotate(angle: Integer.parseInt(seg) * 60, x: x + size, y: y + size)
        }
      }
    }
}