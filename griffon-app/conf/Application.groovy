application {
    title = 'Spacedock'
    startupGroups = ['spacedock']

    // Should Griffon exit when no Griffon created frames are showing?
    autoShutdown = true

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
mvcGroups {
    // MVC Group for "spacedock"
    'spacedock' {
        model      = 'spacedock.SpacedockModel'
        controller = 'spacedock.SpacedockController'
        view       = 'spacedock.SpacedockView'
    }

}
