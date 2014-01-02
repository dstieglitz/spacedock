#Spacedock
Ship SSD builder for Full Thrust and related games. Full Thrust is brought to you by [Ground Zero Games](http://shop.groundzerogames.co.uk/). Spacedock is a [Griffon](http://griffon.codehaus.org) application. 

Spacedock allows you to build ships using a domain-specific language (a "Blueprint") that renders the ship using customizable icons and features.

#Features
* Create ship SSDs from text-based blueprints (custom DSL or XML)
* Create fleets of ships
* Print ship and fleet SSDs



#Spacedock Blueprints

An example blueprint is shown below:

```
ship(name:'Warsaw',shipClass:'Destroyer',mass:'28',pointValue:'93',hull:'Average') {
coreSystems()
spaceDrive(initialThrust:'4',type:'Standard')
ftlDrive()
damageProfile(totalArmor:'3')
fireCon(xSSD:'107',ySSD:'170')
beam(arcs:'4,5,0',xSSD:'50',ySSD:'70',rating:'2')
beam(arcs:'0,1,2',xSSD:'150',ySSD:'70',rating:'2')
beam(xSSD:'40',ySSD:'110',rating:'1')
beam(xSSD:'160',ySSD:'110',rating:'1')
pointDefence(xSSD:'100',ySSD:'140')
}
```

Alternatively, you can use an XML-based blueprint:

```
<?xml version="1.0" standalone="yes" ?>
<Ship>
    <Name>Kamchutka</Name>
    <Race>Eurasian Solar Union</Race>
    <ClassAbbrev>BB</ClassAbbrev>
    <ClassName>Kamchutka</ClassName>
    <ShipClass>Battleship</ShipClass>
    <CrewQuality>First Rate</CrewQuality>
    <Mass>125</Mass>
    <PointValue>421</PointValue>
    <MainDrive type="Standard" initialThrust="4" active="false" />
    <FTLDrive/>
    <Armor totalArmor="6"/>
    <Hull value="125" type="Custom" class="Military" stealth="0" totalHullBoxes="38"/>
    <Electronics>
        <FireControl xSSD="75" ySSD="170"/>
        <FireControl xSSD="116" ySSD="168"/>
        <FireControl xSSD="157" ySSD="168"/>
    </Electronics>
    <Defenses>
        <Screen xSSD="116" ySSD="135"/>
    </Defenses>
    <Holds>
    </Holds>
    <Weapons>
        <BeamBattery  xSSD="33" ySSD="28" rating="3" arcs="(FP/F/FS)"/>
        <BeamBattery  xSSD="197" ySSD="29" rating="3" arcs="(FP/F/FS)"/>
        <BeamBattery  xSSD="85" ySSD="92" rating="2" arcs="(FP/F/FS)"/>
        <BeamBattery  xSSD="146" ySSD="92" rating="2" arcs="(FP/F/FS)"/>
        <BeamBattery  xSSD="30" ySSD="80" rating="2" arcs="(AP/FP/F)"/>
        <BeamBattery  xSSD="206" ySSD="78" rating="2" arcs="(F/FS/AS)"/>
        <BeamBattery  xSSD="41" ySSD="122" rating="1" arcs="(All arcs)"/>
        <BeamBattery  xSSD="195" ySSD="122" rating="1" arcs="(All arcs)"/>
        <PointDefense xSSD="80" ySSD="138"/>
        <PointDefense xSSD="150" ySSD="137"/>
        <PointDefense xSSD="191" ySSD="157"/>
        <PointDefense xSSD="43" ySSD="157"/>
        <PulseTorpedo  xSSD="115" ySSD="42" arcs="(F)"/>
        <BeamBattery  xSSD="73" ySSD="55" rating="2" arcs="(AP/FP/F)"/>
        <BeamBattery  xSSD="154" ySSD="53" rating="2" arcs="(F/FS/AS)"/>
    </Weapons>
</Ship>
```