    def salvo = [xSSD:0,ySSD:0,arcs:['5','0','1']]
    def magazine = [capacity:8]
    
    x = 30
    y = 0
    def ttf = 0 
    def size = 15
    
    if (salvo) ttf = size * 0.70
    
    circle(cx: x + size, cy: y + size, radius: size * 0.70, fill: 'white')
    triangle(x: x + size*0.65 ,y: y + size * 0.75 + ttf, width:size*0.70,height:size, fill:'black')
    
    group(id:'magazine') {
        rect(x:x,y:y,id:'mbox',width:size * magazine.capacity / 2 + size * 0.30, height:size * 2)
        (0..magazine.capacity / 2 - 1).each {
            triangle(x: (it*size) + (x + (size * 0.15)), y: y + size * 2 - (size * 0.15), width:size, height:size * 1.70,fill:'white')
            transformations {
                translate(x:0,y:size) 
            } 
        }    
    }
    
    if (salvo.arcs) {
    salvo.arcs.each {seg ->
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