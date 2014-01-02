package spacedock

import javax.swing.JOptionPane
import java.io.File
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.tree.TreePath
import java.awt.event.MouseEvent
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

import net.miginfocom.swing.MigLayout

def renderer = new ShipRenderer2()
def currentShip
def fleetRenderer = new FleetRenderer1()

def render(renderer, ship, panel = shipDetailPanel) {
    if (ship) {
        edt {
            panel.removeAll()
            model.canvas = renderer.ship(ship)
            panel.add(model.canvas, 'align center')
            panel.background = Color.white
            panel.setBorder(BorderFactory.createLineBorder(Color.black))
            panel.validate()
        }
    }
}

def parseShip = {shipBuilderScript ->
    def script = "return new spacedock.ShipBuilder(registry).${shipBuilderScript}".toString()
    return model.shell.evaluate(script)
}

def renderSelected = {shipBuilderScript, panel = shipDetailPanel ->
    currentShip = parseShip(shipBuilderScript)
    render(renderer, currentShip, panel)
}

def addToFleet = {shipSpec ->
    if (shipSpec.trim() != '') {
        def name = JOptionPane.showInputDialog("Ship Name")
        if (!name || name.trim().equals('')) name = 'New Ship'
        def fm = new FleetMember(shipName: name, shipSpec: shipSpec)
        doLater {
            model.fleet.ships.addElement(fm)
        }
    }
}

actions {
    action(id: 'setBrowserRootPath',
            name: 'Browse From...',
            accelerator: shortcut('B'),
            closure: {
                fileChooser(dialogTitle: "Browse From...",
                        id: "browseFromDialog",
                        fileSelectionMode: JFileChooser.DIRECTORIES_ONLY)//,
                //the file filter must show also directories, in order to be able to look into them
                //fileFilter: [getDescription: {-> "*.xml,*.groovy"}, accept: {file -> file ==~ /.*?\.xml|.*?\.groovy/ || file.isDirectory() }] as FileFilter) {
                //}
                doOutside {
                    if (browseFromDialog.showOpenDialog() == JFileChooser.APPROVE_OPTION) {
                        controller.setBrowserRootPath(browseFromDialog.selectedFile.absolutePath, shipTree)
                    }
                }
            })

    action(id: 'loadShip',
            name: 'Load Ship...',
            accelerator: shortcut('L'),
            closure: {
                fileChooser(dialogTitle: "Load Ship...",
                        id: "saveShipDialog",
                        fileSelectionMode: JFileChooser.FILES_ONLY,
                        //the file filter must show also directories, in order to be able to look into them
                        fileFilter: [getDescription: {-> "*.ship"}, accept: {file -> file ==~ /.*?\.ship/ || file.isDirectory() }] as FileFilter) {
                }
                doOutside {
                    shipSpec.text = ''
                    def buf = new StringBuffer()
                    if (saveShipDialog.showOpenDialog() == JFileChooser.APPROVE_OPTION) {
                        saveShipDialog.selectedFile.eachLine { line ->
                            buf.append("${line}\n")
                        }
                        shipSpec.text = buf.toString()
                    }
                }
            })

    action(id: 'saveShip',
            name: 'Save Ship',
            accelerator: shortcut('V'),
            closure: {
                fileChooser(dialogTitle: "Save Ship...",
                        id: "saveShipDialog",
                        fileSelectionMode: JFileChooser.FILES_ONLY,
                        //the file filter must show also directories, in order to be able to look into them
                        fileFilter: [getDescription: {-> "*.ship"}, accept: {file -> file ==~ /.*?\.ship/ || file.isDirectory() }] as FileFilter) {
                }
                doOutside {
                    if (saveShipDialog.showOpenDialog() == JFileChooser.APPROVE_OPTION) {
                        saveShipDialog.selectedFile.write(shipSpec.text)
                    }
                }
            })

    action(id: 'saveFleet',
            name: 'Save Fleet PDF',
            accelerator: shortcut('S'),
            closure: {
                fileChooser(dialogTitle: "Save Fleet...",
                        id: "saveFleetDialog",
                        fileSelectionMode: JFileChooser.FILES_ONLY,
                        //the file filter must show also directories, in order to be able to look into them
                        fileFilter: [getDescription: {-> "*.pdf"}, accept: {file -> file ==~ /.*?\.pdf/ || file.isDirectory() }] as FileFilter) {
                }
                doOutside {
                    if (saveFleetDialog.showOpenDialog() == JFileChooser.APPROVE_OPTION) {
                        fleetRenderer.model = model
                        fleetRenderer.renderFleet(model.fleet, saveFleetDialog.selectedFile.absolutePath)
                    }
                }
            })
}

application(title: 'Spacedock',
        size: [1024, 700],
        //pack:true,
        //location:[50,50],
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                imageIcon('/griffon-icon-32x32.png').image,
                imageIcon('/griffon-icon-16x16.png').image]
) {

    menuBar() {
        menu(text: 'File') {
            menuItem(setBrowserRootPath)
        }
    }

    panel(layout: new MigLayout('fill')) {
        tabbedPane(constraints: 'grow') {

            /*
            panel(name: 'Registry', layout: new MigLayout('fill')) {
              splitPane(dividerLocation: 512, constraints: 'span,grow') {
                panel(layout: new MigLayout('fill')) {
                  scrollPane(constraints: 'span,grow') {
                    tree(id: 'registryTree', model: model.registry)
                  }
                }
                panel(id: 'registryDetailPanel', layout: new MigLayout('fill, wrap 1')) {
                  label("Select a registry node to see detail")
                }
              }
            }
            */

            panel(name: 'Ship Browser', layout: new MigLayout('fill'), constraints: 'grow') {
                splitPane(dividerLocation: 512, constraints: 'grow') {
                    panel(layout: new MigLayout('fill, ins 0'), constraints: 'grow') {
                        busyComponent(id: 'b1', constraints: 'grow') {
                            busyModel(description: 'Rendering selected ship')
                            panel(layout: new MigLayout('fill, ins 0'), constraints: 'grow') {
                                scrollPane(constraints: 'grow') {
                                    tree(id: 'shipTree', model: model.ships)
                                }
                            }
                        }
                    }
                    panel(id: 'shipDetailContainer', layout: new MigLayout('fill')) {
                        panel(id: 'shipDetailPanel', layout: new MigLayout('fill'), constraints: 'align center') {
                            label("Select a ship to view its SSD")
                        }
                    }
                }
            }

            panel(name: 'Ship Editor', layout: new MigLayout('fill'), constraints: 'grow') {
                splitPane(dividerLocation: 480, constraints: 'grow') {

                    busyComponent(id: 'b2', constraints: 'grow') {
                        busyModel(description: 'Rendering selected ship')
                        panel(layout: new MigLayout('fill')) {
                            panel(constraints: 'grow', layout: new MigLayout('fill'), border: BorderFactory.createTitledBorder('Ship Specification')) {
                                scrollPane(constraints: 'grow') {
                                    editorPane(id: 'shipSpec')
                                }
                            }
                            panel(constraints: 'dock south') {
                                button(action: loadShip)
                                button('Build Ship', actionPerformed: {

                                    b2.setBusy(true)
                                    doOutside {
                                        try {
                                            renderSelected(shipSpec.text, rightPanel)
                                            b2.setBusy(false)
                                        } catch (Throwable t) {
                                            t.printStackTrace()
                                            JOptionPane.showMessageDialog(null, t.message, "alert", JOptionPane.ERROR_MESSAGE)
                                        } finally {
                                            b2.setBusy(false)
                                        }
                                    }

                                })
                                button('Clear Specification', actionPerformed: {evt ->
                                    shipSpec.text = ''
                                })
                                button(action: saveShip)
                            }
                        }
                    }

                    panel(layout: new MigLayout('fill'), constraints: 'grow') {
                        panel(id: 'rightPanel', layout: new MigLayout('fill'), constraints: 'grow') {
                            panel(id: 'resultPanel', constraints: 'align center')
                        }
                        panel(id: 'viewControls', constraints: 'dock south') {
                            label('vgap')
                            textField(id: 'vgapTF', text: renderer.vgap, columns: 5).addFocusListener({evt ->
                                try {
                                    val = Integer.parseInt(vgapTF.text)
                                    renderer.vgap = val
                                    render(renderer, currentShip, rightPanel)
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null, "Invalid vgap: ${vgapTF.text}", "alert", JOptionPane.ERROR_MESSAGE)
                                    edt {
                                        vgapTF.text = renderer.vgap
                                    }
                                }
                            } as java.awt.event.FocusListener)

                            label('size')
                            textField(id: 'sizeTF', text: renderer.size, columns: 5).addFocusListener({evt ->
                                try {
                                    val = Integer.parseInt(sizeTF.text)
                                    renderer.size = val
                                    render(renderer, currentShip, rightPanel)
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null, "Invalid vgap: ${vgapTF.text}", "alert", JOptionPane.ERROR_MESSAGE)
                                    edt {
                                        sizeTF.text = renderer.size
                                    }
                                }
                            } as java.awt.event.FocusListener)
                            label('Zoom')
                            slider(id: 'zoomSlider', minimum: 0, maximum: 300, value: 100)
                            zoomSlider.addChangeListener({evt ->
                                //println evt.source.value
                                renderer.zoom = evt.source.value / 100
                                render(renderer, currentShip, rightPanel)
                            } as javax.swing.event.ChangeListener)
                        }
                    }

                }
            }

            panel(name: 'Fleet Editor', layout: new MigLayout('fill')) {
                splitPane(dividerLocation: 400, constraints: 'grow') {
                    busyComponent(id: 'b3', constraints: 'grow') {
                        busyModel(description: 'Rendering selected ship')
                        panel(layout: new MigLayout('fill')) {
                            panel(border: BorderFactory.createTitledBorder('Fleet Info'), constraints: 'grow, dock north') {
                                label('Name')
                                textArea(columns: 20)
                            }
                            panel(layout: new MigLayout('fill'), border: BorderFactory.createTitledBorder('Ships'), constraints: 'grow') {
                                scrollPane(constraints: 'span, grow') {
                                    list(id: 'shipList', model: model.fleet.ships)
                                }
                                panel(constraints: 'dock south', layout: new MigLayout('fill, wrap 2')) {
                                    button("Load Fleet...", enabled: true, constraints: 'grow')
                                    button("Add Ship from File...", constraints: 'grow', enabled: false)
                                    button("Add Ship from Ship Editor", enabled: true, actionPerformed: {evt ->
                                        addToFleet(shipSpec.text)
                                    }, constraints: 'grow')
                                    button("Remove Ship", enabled: true, constraints: 'grow', actionPerformed: {evt ->
                                        def selectedShip = shipList.getSelectedValue()
                                        if (selectedShip) {
                                            def result = JOptionPane.showConfirmDialog(current, "Are you sure you want to remove this ship?", "Confirm Delete", JOptionPane.YES_NO_OPTION)
                                            if (result == JOptionPane.YES_OPTION) shipList.model.removeElement(selectedShip)
                                            doLater {
                                                fleetShipDetailPanel.removeAll()
                                                fleetShipDetailPanel.validate()
                                            }
                                        }
                                    })
                                    button("Save Fleet", enabled: true, constraints: 'grow')
                                    button(action: saveFleet)
                                }
                            }
                        }
                    }
                    panel(id: 'fleetEditorRightPanel', layout: new MigLayout('fill'), constraints: 'grow') {
                        tabbedPane(constraints: 'grow') {
                            panel(name: 'Status Display', layout: new MigLayout('fill'), constraints: 'grow') {
                                panel(id: 'fleetShipDetailPanel', layout: new MigLayout('fill'), border: BorderFactory.createTitledBorder('Ship'), constraints: 'grow') {
                                    label("Select a ship to view its SSD", constraints: 'align center')
                                }
                            }
                            panel(name: 'Ship Editor', layout: new MigLayout('fill'), constraints: 'grow') {
                                panel(constraints: 'grow', layout: new MigLayout('fill'), border: BorderFactory.createTitledBorder('Ship Specification')) {
                                    scrollPane(constraints: 'grow') {
                                        editorPane(id: 'fleetShipSpec', constraints: 'grow')
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
registryTree.addTreeSelectionListener({evt ->
  def node = evt.path[evt.path.size() - 1]
  //println node
  //println node.class.name
  if (node.userObject instanceof Map) {
    registryDetailPanel.removeAll()
    registryDetailPanel.add(panel(layout: new MigLayout('fill, wrap 2')) {
      node.userObject.each {nvpair ->
        label(nvpair.key.toString())
        label(nvpair.value.toString())
      }
    })
  }
  registryDetailPanel.revalidate()
} as javax.swing.event.TreeSelectionListener)
  */

/*
shipTree.mouseListeners.each {
  shipTree.removeMouseListener(it)
}

shipTree.addTreeSelectionListener({evt ->

  def node = evt.path[evt.path.size() - 1]
  if (!node.directory) {
    b1.setBusy(true)
    doOutside {
      try {
        def shipBuilderScript = new spacedock.FTJavaShipParser().parseShip(node.absolutePath)
        renderSelected(shipBuilderScript)
        b1.setBusy(false)
      } catch (Throwable t) {
        t.printStackTrace()
        JOptionPane.showMessageDialog(null, t.message, "alert", JOptionPane.ERROR_MESSAGE)
      } finally {
        b1.setBusy(false)
      }
    }
  }
} as javax.swing.event.TreeSelectionListener)
   */
shipTree.addMouseListener({e ->
    try {
        def menu
        def pt = e.locationOnScreen
        def tl = e.source.locationOnScreen
        def path = e.source.getClosestPathForLocation((pt.x - tl.x) as int, (pt.y - tl.y) as int);
        int stCount = path.getPathCount();
        def selectedNode = path.getPathComponent(stCount - 1);

        if (e.source == shipTree && e.id == MouseEvent.MOUSE_PRESSED && e.button == MouseEvent.BUTTON1) {
            if (!selectedNode.directory) {
                b1.setBusy(true)
                doOutside {
                    try {
                        def shipBuilderScript = new FTJavaShipParser().parseShip(selectedNode.absolutePath)
                        renderSelected(shipBuilderScript)
                        b1.setBusy(false)
                    } catch (Throwable t) {
                        t.printStackTrace()
                        JOptionPane.showMessageDialog(null, t.message, "alert", JOptionPane.ERROR_MESSAGE)
                    } finally {
                        b1.setBusy(false)
                    }
                }
            }
        }

        else if (e.source == shipTree && e.id == MouseEvent.MOUSE_PRESSED && e.button == MouseEvent.BUTTON3) {
            /*
            * Find the position where the event occurred relative to the tree position
            * I put it all inside a JTree extended class defined by me, that is why 'this'
            * refers to the JTree
            *  then find the path to the node on which the click happened with getClossestPathForLocation
            * has an unsolved problem which is that any click under the last node and inside the JTree window
            * will fire the event on the last node.
            */
            shipTree.setSelectionPath(path)
            menu = popupMenu() {
                menuItem("Copy to ship editor", actionPerformed: {evt ->
                    def shipBuilderScript = new FTJavaShipParser().parseShip(selectedNode.absolutePath)
                    shipSpec.text = shipBuilderScript
                })
                menuItem("Add to current fleet", actionPerformed: {evt ->
                    def shipBuilderScript = new FTJavaShipParser().parseShip(selectedNode.absolutePath)
                    addToFleet(shipBuilderScript)
                })
            }

            menu.show(e.source, (pt.x - tl.x) as int, (pt.y - tl.y) as int)
        }

        else if (e.source == shipTree && e.id == MouseEvent.MOUSE_RELEASED && e.button == MouseEvent.BUTTON3) {
            if (menu) menu.dispose()
        }
    } catch (Throwable t) {
        println t.message
    }
} as java.awt.event.MouseListener)


shipList.addListSelectionListener({evt ->
    b3.setBusy(true)
    doOutside {
        try {
            if (shipList.selectedValue) {
                def shipBuilderScript = shipList.selectedValue.shipSpec
                renderSelected(shipBuilderScript, fleetShipDetailPanel)
                fleetShipSpec.text = shipBuilderScript
            }
        } catch (Throwable t) {
            t.printStackTrace()
            JOptionPane.showMessageDialog(null, t.message, "alert", JOptionPane.ERROR_MESSAGE)
        } finally {
            b3.setBusy(false)
        }
    }
} as javax.swing.event.ListSelectionListener)