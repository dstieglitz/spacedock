
root {
    'groovy.swing.SwingBuilder' {
        controller = ['Threading']
        view = '*'
    }
}

jx {
    'groovy.swing.SwingXBuilder' {
        view = '*'
    }
}

root.'griffon.builder.gfx.GfxBuilder'.view = '*'
