# MindTrace

MindTrace ist eine native Android-App zur lokalen persönlichen Dokumentation von Stimmung und Medikamentenerinnerungen. Sie ist kein Medizinprodukt und ersetzt keine medizinische Beratung, Diagnose oder Behandlung.

## Architektur und Technologien

- Kotlin, Jetpack Compose, Material 3 und Navigation Compose
- Single-Activity-Architektur mit MVVM-naher Struktur
- Room über SQLite für ausschließlich lokale Speicherung
- Repository-Pattern, ViewModel, Coroutines und Flow
- AlarmManager, PendingIntent, BroadcastReceiver und NotificationCompat für tägliche Erinnerungen

## Projektstruktur

- `data/local`: Room-Datenbank, Entities, DAOs und Converter
- `data/repository`: Datenzugriff und Koordination mit Alarmplanung
- `alarm`: testbare Zeitberechnung und Android-Alarmplanung
- `notification`: NotificationChannel und Benachrichtigungen
- `receiver`: Alarm-, Aktions- und Reschedule-Receiver
- `ui`: Navigation, Screens, Einstellungen, Komponenten, Theme und ViewModels

## Start in Android Studio

1. Projektordner `MindTraceApp` in Android Studio öffnen.
2. Gradle Sync ausführen.
3. Ein Gerät oder einen Emulator mit Android 8.0 oder neuer auswählen.
4. App-Modul `app` starten.

Die produktive Datenbank startet leer. Beispieldaten werden nur in Previews und Tests verwendet.

## Berechtigungen

MindTrace fordert auf modernen Android-Versionen Benachrichtigungen an (`POST_NOTIFICATIONS`). In den Einstellungen können Sprache, Darstellung, Benachrichtigungen, Ton und Vibration lokal angepasst werden. Für möglichst genaue Erinnerungen wird `SCHEDULE_EXACT_ALARM` berücksichtigt. Wenn exakte Alarme nicht erlaubt sind, speichert die App Erinnerungen weiterhin und verwendet eine ungenauere Alarmplanung als Fallback.

`RECEIVE_BOOT_COMPLETED` wird genutzt, um aktive Erinnerungen nach einem Geräteneustart erneut zu planen. Änderungen an Datum, Uhrzeit oder Zeitzone werden ebenfalls über Receiver aufgegriffen.

## Alarmplanung

Für jede aktive Erinnerung wird nur der nächste lokale Zeitpunkt geplant. Wenn ein Alarm ausgelöst wurde, wird die Benachrichtigung angezeigt und der nächste tägliche Termin neu berechnet. Die Berechnung arbeitet mit `java.time`, lokaler Zeitzone und berücksichtigt dadurch Sommer- und Winterzeit.

Beim Deaktivieren oder Löschen einer Erinnerung wird der zugehörige Alarm entfernt. Mehrere Erinnerungen nutzen eindeutige Request-Codes und Notification-IDs.

## Benachrichtigungen testen

- Auf Android 13+ Benachrichtigungsberechtigung erlauben.
- Falls exakte Alarme blockiert sind, in den Systemeinstellungen für MindTrace zulassen.
- Eine Erinnerung wenige Minuten in der Zukunft anlegen.
- Die Notification öffnen oder die Aktion `Eingenommen` antippen.
- In der App wird der heutige Einnahmestatus über Room und Flow aktualisiert.

## Datenschutz

Die App besitzt keine Internetberechtigung, nutzt keine Cloud, kein Tracking, keine Werbung und schreibt keine Medikamentennamen oder Stimmungsnotizen in Logs.

## Tests

Unit-Tests prüfen die Alarm-Zeitberechnung einschließlich Tageswechsel und Zeitumstellung sowie die Validierung von Stimmungswerten. Instrumentation-Tests prüfen Room-DAOs, Speichern/Laden und das Verhindern doppelter Einnahmeeinträge.

Ausführen:

```powershell
.\gradlew test
.\gradlew connectedAndroidTest
```

In dieser Codex-Shell waren weder ein Gradle-Kommando noch ein Android SDK-Pfad verfügbar. Die finale Build- und Testausführung muss deshalb in Android Studio oder in einer Umgebung mit installiertem Android SDK und Gradle Wrapper erfolgen.

## Bekannte Einschränkungen

Herstellerspezifische Energiesparfunktionen können Alarme und Benachrichtigungen verzögern oder unterdrücken. In solchen Fällen müssen Nutzer MindTrace gegebenenfalls von Akku-Optimierungen ausnehmen.
