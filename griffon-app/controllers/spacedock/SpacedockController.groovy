package spacedock

class SpacedockController {
    // these will be injected by Griffon
    def model
    def view

    void mvcGroupInit(Map args) {
        // this method is called after model and view are injected

        def ogb = new groovy.util.ObjectGraphBuilder()
        ogb.classNameResolver = {name ->
            "javax.swing.tree.DefaultMutableTreeNode"
        }
        ogb.childPropertySetter = {parent, child, pname, cname ->
            parent.add(child)
        }

        def root = ogb.node(userObject: 'universe') {
            node(userObject: 'data') {
                node(userObject: 'ship') {
                    node(userObject: 'type') {
                        node(userObject: 'Military')
                        node(userObject: 'Merchant')
                        node(userObject: 'Civilian')
                    }
                    node(userObject: 'class') {
                        node(userObject: [className: 'Scout', abbreviation: 'SC', type: '/universe/data/ship/type/Military', massRange: '4..10'])
                        node(userObject: [className: 'Courier', abbreviation: 'SC', type: '/universe/data/ship/type/Merchant', massRange: '4..10'])
                        node(userObject: [className: 'Corvette', abbreviation: 'CT', type: '/universe/data/ship/type/Military', massRange: '8..16'])
                        node(userObject: [className: 'Frigate', abbreviation: 'FF', type: '/universe/data/ship/type/Military', massRange: '14..26'])
                        node(userObject: [className: 'Destroyer', abbreviation: 'DD', type: '/universe/data/ship/type/Military', massRange: '24..36'])
                        node(userObject: [className: 'Superdestroyer', abbreviation: 'DH', type: '/universe/data/ship/type/Military', massRange: '30..50'])
                        node(userObject: [className: 'Light Cruiser', abbreviation: 'CL', type: '/universe/data/ship/type/Military', massRange: '40..60'])
                        node(userObject: [className: 'Escort Cruiser', abbreviation: 'CE', type: '/universe/data/ship/type/Military', massRange: '50..70'])
                        node(userObject: [className: 'Heavy Cruiser', abbreviation: 'CH', type: '/universe/data/ship/type/Military', massRange: '60..90'])
                        node(userObject: [className: 'Battlecruiser', abbreviation: 'BC', type: '/universe/data/ship/type/Military', massRange: '80..110'])
                        node(userObject: [className: 'Battleship', abbreviation: 'BB', type: '/universe/data/ship/type/Military', massRange: '100..140'])
                        node(userObject: [className: 'Battledreadnought', abbreviation: 'BDN', type: '/universe/data/ship/type/Military', massRange: '120..160'])
                        node(userObject: [className: 'Dreadnought', abbreviation: 'DN', type: '/universe/data/ship/type/Military', massRange: '140..180'])
                        node(userObject: [className: 'Superdreadnought', abbreviation: 'SDN', type: '/universe/data/ship/type/Military', massRange: '160'])
                        node(userObject: [className: 'Escort Carrier', abbreviation: 'CVE', type: '/universe/data/ship/type/Military', massRange: '80..140'])
                        node(userObject: [className: 'Light Carrier', abbreviation: 'CVL', type: '/universe/data/ship/type/Military', massRange: '120..180'])
                        node(userObject: [className: 'Heavy Carrier', abbreviation: 'CVH', type: '/universe/data/ship/type/Military', massRange: '160'])
                        node(userObject: [className: 'Attack Carrier', abbreviation: 'CVA', type: '/universe/data/ship/type/Military', massRange: '150'])
                    }
                    node(userObject: 'component') {
                        node(userObject: [name: 'ship', components: []])
                        node(userObject: [name: 'beam', arcs: '', classId: '1', getMass: '''L:{
              switch (beam.classId) {               
                case '1': return 1
                case '2': return 2 + (beam.arcs.length()>5)?2:1
                case '3': return 4 + beam.arcs.length()
                case '4': return 8 + 2*beam.arcs.length()
                default: throw new RuntimeException('Unknown battery class ${beam.classId}')
              }}'''])
                        node(userObject: [name: 'fireCon', getMass: '2'])
                        node(userObject: [name: 'pointDefence'])
                        node(userObject: [name: 'damageProfile', components: []])
                        node(userObject: [name: 'threshold'])
                        node(userObject: [name: 'ftlDrive'])
                        node(userObject: [name: 'spaceDrive'])
                        node(userObject: [name: 'coreSystems'])
                        node(userObject: [name: 'commandBridge'])
                        node(userObject: [name: 'lifeSupport'])
                        node(userObject: [name: 'powerCore'])
                        node(userObject: [name: 'salvoMissleRack'])
                        node(userObject: [name: 'screenGenerator'])
                        node(userObject: [name: 'pulseTorpedo'])
                        node(userObject: [name: 'fighterBay'])
                        node(userObject: [name: 'salvoMissileLauncher'])
                        node(userObject: [name: 'salvoMissileRack'])
                        node(userObject: [name: 'salvoMissileMagazine'])
                        node(userObject: [name: 'salvo'])

                    }
                }
            }
        }

        model.registry = new javax.swing.tree.DefaultTreeModel(root)

        Binding binding = new Binding();
        binding.setVariable('registry', model.registry)
        model.shell = new GroovyShell(binding);

        model.fleet.put('ships', new javax.swing.DefaultListModel())

        setBrowserRootPath(System.getProperty('default.ships.dir'))
    }

    def setBrowserRootPath = {newPath,theTree = null ->
        def fgb = new groovy.util.ObjectGraphBuilder()
        fgb.classNameResolver = {name ->
            "javax.swing.tree.DefaultMutableTreeNode"
        }
        fgb.childPropertySetter = {parent, child, pname, cname ->
            parent.add(child)
        }
        model.browserRootPath = newPath
        def dirtree = new File(model.browserRootPath)
        def files = fgb.node(userObject: dirtree)
        model.ships = new spacedock.FileTreeModel(dirtree)
        if (theTree) {
            theTree.model = model.ships
            theTree.setRootVisible false
        }
    }

    /*
    def action = { evt = null ->
    }
    */
}