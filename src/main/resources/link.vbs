Set oWS = WScript.CreateObject("WScript.Shell")
sLinkFile = "%link1%"
Set oLink = oWS.CreateShortcut(sLinkFile)
    oLink.TargetPath = "%java%"
	oLink.Arguments = "-jar " & chr(34) & "%exec%" & chr(34)
    oLink.Description = "DigitalRoadmap - Simple Roadmapping Program"
 '  oLink.IconLocation = ""
oLink.Save

sLinkFile = "%link2"
Set oLink = oWS.CreateShortcut(sLinkFile)
    oLink.TargetPath = "%java%"
	oLink.Arguments = "-jar " & chr(34) & "%exec%" & chr(34)
    oLink.Description = "DigitalRoadmap - Simple Roadmapping Program"
 '  oLink.IconLocation = ""
oLink.Save
