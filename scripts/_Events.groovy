eventCompileEnd = {
    ant.copy(todir: "staging/ships") { fileset(dir: 'ships') }
}