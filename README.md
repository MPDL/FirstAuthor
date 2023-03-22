# AppAllAuthors
Aus einer exportierten Liste mit PuRe Datensätzen im json-Format wird eine Liste im csv-Format generiert mit ID, Titel, Genre, Date und Type, CONE-ID, Name des Autors von nur den Datensätzen, die als Creator einen Autor (Person) mit CONE-ID von einem bestimmten Institut haben.

# AppFirstAuthor
Aus einer exportierten Liste mit PuRe Datensätzen im json-Format wird eine Liste im csv-Format generiert mit ID, Titel und Name des 1. Autors von nur den Datensätzen, die als 1. Autor eine Person von einem bestimmten Institut haben.

# AppFirstLastAuthor
Aus einer exportierten Liste mit PuRe Datensätzen im json-Format wird eine Liste im csv-Format generiert mit ID, Titel, Genre, Date und Type, CONE-ID, Name des Autors von nur den Datensätzen, die als ersten oder letzten Creator einen Autor (Person) mit CONE-ID von einem bestimmten Institut haben.

# Hinweise zum Laden in Eclipse
File - Open Projects from File System

# Hinweise zum Export
File - Export - Runnable Jar file (vorher einmal als Java Application laufen lassen -> erstellt für Export notwendige Launch Konfiguration)

# Hinweise zum Laden des Ergebnisses in Excel
Leeres Spreadsheet anlegen - Daten - Aus Text - generierte Datei auswählen (out.csv) - Importieren - Dateiursprung 65001: Unicode (UTF-8) - Fertig stellen


