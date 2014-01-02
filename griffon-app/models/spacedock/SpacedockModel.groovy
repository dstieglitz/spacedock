package spacedock

import groovy.beans.Bindable

class SpacedockModel {
    // @Bindable String propName
    @Bindable Object registry
    @Bindable Object ships
    @Bindable Object canvas
    @Bindable Object fleet = [name:'My Fleet']
    @Bindable GroovyShell shell
    @Bindable String browserRootPath = System.getProperty('default.ships.dir')
    @Bindable Object shipTree
}