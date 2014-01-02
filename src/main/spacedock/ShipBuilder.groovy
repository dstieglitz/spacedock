package spacedock
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dstieglitz
 */
class ShipBuilder extends groovy.util.BuilderSupport {

  def componentPath = '/universe/data/ship/component'
  def ship = [:]
  def model
  def strictTypeChecking = true

  public ShipBuilder(model) {
    super()
    this.model = model
  }

  public ShipBuilder(model, componentPath) {
    super(model)
    this.componentPath = componentPath
  }

  protected void setParent(Object parent, Object child) {
    //println "setParent(${parent},${child})"
    if (parent instanceof Map && parent.containsKey('components')) parent.components.add(child)
    //println parent
  }

  // standard deep copy implementation
  // does not work with Groovy maps

  def deepcopy(orig) {
    def bos = new ByteArrayOutputStream()
    def oos = new ObjectOutputStream(bos)
    oos.writeObject(orig); oos.flush()
    def bin = new ByteArrayInputStream(bos.toByteArray())
    def ois = new ObjectInputStream(bin)
    return ois.readObject()
  }

  def find(current = null, path) {
    def result
    if (path == null) return null
    if (path.length() == 0) return null
    if (path.indexOf('/') < 0) return null
    if (!current) current = model.root
    if (current.isLeaf()) {
      path = path.replaceAll('\\/', '').trim()
      //println "leaf found, checking '${current.userObject}' against '${path}'"
      if (current.userObject.equals(path)) return current.userObject
      else if (current.userObject instanceof Map && current.userObject.name && current.userObject.name.equals(path)) {
        //println "RETURNING ${current.userObject} (1)"
        return current.userObject
      }
      else return null
    }

    //println "find(${path})"
    //println "Searching ${current.userObject}"

    def secondSlash = path.indexOf('/', 1)
    //println "second slash at ${secondSlash}"

    def pathElement = path[0..secondSlash]
    pathElement = pathElement.replaceAll('\\/', '')
    //println "pathElement is ${pathElement}"

    def remainingPath = path[secondSlash..path.length() - 1]
    //println "remainingPath is ${remainingPath}"

    def idx = 0

    while (idx < current.childCount) {
//      def child = model.getChild(current, idx)
//      result = find(child, pathElement)
//      println "GOT ${result} (1)"
//      if (!result) result = find(child, remainingPath)
//      println "GOT ${result} (2)"
//      if (result) break
//      idx++

      def child = model.getChild(current, idx)
      result = result = find(child, remainingPath)
      if (result) break
      idx++
    }

    return result
  }

  protected Object createNode(Object name) {
    def node = find(name)
    if (!node) throw new RuntimeException("Invalid registry reference ${name}")
    return deepcopy(node)
    //println "found ${node}"
    //return node
  }

  protected Object createNode(Object name, Object value) {
    //println "createNode(${name},${value})"
    //a node without parameters, but with closure
    return createNode(name)
  }


  protected Object createNode(Object name, Map attributes) {
    // a Node without closure but with parameters
    //println "createNode(${name},${attributes})"
    def node = createNode(name)
    if (attributes) {
      attributes.each {nvpair ->
        //println nvpair.key
        //println nvpair.value
        //println node
        node."${nvpair.key}" = nvpair.value
      }
    }
    return node
  }


  protected Object createNode(Object name, Map attributes, Object value) {
    //a node with closure and parameters
    //println "createNode(${name},${attributes},${value})"
    return createNode(name, attributes)
  }


  protected Object getName(String methodName) {
    //println "getName(${methodName})"
    return componentPath + "/${methodName}"
  }

}

