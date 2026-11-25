# REVIV - Emergency Response & Disaster Coordination System

Console-only Java project (50 classes) with MySQL + JDBC. Citizens raise SOS requests, dispatchers assign rescue
teams, and the system tracks resources, shelters, hospitals, volunteers, donations and alerts.
Rescue operations, alerts, escalation and stock monitoring run in background threads.

## Setup (IntelliJ IDEA)
1. Install MySQL and make sure it is running.
2. Open `src/com/reviv/database/DBConnection.java` and set `USER` and `PASSWORD` to your MySQL login.
3. Download MySQL Connector/J, rename it `mysql-connector-j.jar`, put it in `lib/`.
4. IntelliJ: right-click `src` > Mark Directory as > **Sources Root**.
5. File > Project Structure > Libraries > **+** > Java > select `lib/mysql-connector-j.jar`.
6. Run `com.reviv.Main`. The app creates `reviv_db`, the tables and the sample data by itself
   (or run `sql/schema.sql` yourself in MySQL Workbench).

> If you ran an older version of this project before, run `DROP DATABASE reviv_db;` once so the new tables are created.

## Compile and run from terminal (project root)
Windows (cmd):
```
dir /s /b src\*.java > sources.txt
javac -cp lib\mysql-connector-j.jar -d out @sources.txt
java -cp "out;lib\mysql-connector-j.jar" com.reviv.Main
```
Linux / macOS:
```
javac -cp lib/mysql-connector-j.jar -d out $(find src -name "*.java")
java -cp "out:lib/mysql-connector-j.jar" com.reviv.Main
```

## Demo logins
| Role | Username | Password |
|------|----------|----------|
| ADMIN | admin, admin2, admin3 | admin123 |
| DISPATCHER | dispatcher, dispatcher2, dispatcher3 | dispatcher123 |
| CITIZEN | citizen, citizen2, citizen3 | citizen123 |
| VOLUNTEER | volunteer, volunteer2, volunteer3 | volunteer123 |

Passwords are plain text (hashing is outside the syllabus).
Unlock a locked account: `UPDATE users SET failed_attempts = 0, locked = FALSE WHERE username = 'citizen2';`

## Features by role
- **Main menu:** 1 Login, 2 Register (citizen/volunteer), 0 Exit. Account locks after 3 wrong passwords.
- **Citizen:** raise SOS, view my SOS status, view active alerts.
- **Volunteer:** update availability, view active alerts, view shelters.
- **Dispatcher:** 1 pending queue, 2 process next SOS (choose team, deduct resources), 3 teams, 4 start rescue (RescueTask threads),
  5 broadcast alert (AlertThread), 6 hospitals, 7 shelters, 8 allot patient, 9 assign evacuees.
- **Admin:** options 1-9 above, plus 10 add stock, 11 view stock, 12 add team, 13 team availability, 14 add donation,
  15 view donations, 16-18 reports (zone / team workload / resource usage), 19 start and 20 stop monitoring threads, 21 view users.

SOS status chain (invalid jumps rejected): REPORTED -> ASSIGNED -> IN_PROGRESS -> RESOLVED.
Each assignment uses 2 Food Packets and 1 Medical Kit.

## Structure (flow: menu -> service -> DAO -> MySQL)
```
Reviv/
  lib/mysql-connector-j.jar
  sql/schema.sql
  src/com/reviv/
    Main.java
    exception/ (6)  model/ (8)  user/ (3)  database/ (11)
    service/ (10)   threads/ (4)  util/ (2)  menu/ (5)
```

## 5-minute demo flow
1. **Citizen** - Main `1`, `citizen` / `citizen123`.
   Option `1`: `SOS101`, `Zone A`, `House flooded`. Option `1` again: `SOS102`, `Zone B`, `Person trapped`.
   Option `1` again: `SOS101` -> **DuplicateSOSException**. Option `1`: `SOS103`, `Zone A`, `Need food` (leave it pending). Option `2` to see status. `0` logout.
2. **Dispatcher** - Main `1`, `dispatcher` / `dispatcher123`.
   `1` queue. `2` then team `3` -> **TeamBusyException** (Gamma is busy). `2` then team `1` (SOS101 ASSIGNED). `2` then team `2` (SOS102 ASSIGNED).
   `3` teams. `4` -> **two RescueTask threads run together**, menu waits (join). `3` teams are free again.
   `5`: `Zone A`, `Evacuate to high ground` -> AlertThread prints 3 lines. `8`: hospital `1`. `9`: shelter `1`, `5` evacuees; again shelter `3`, `99` -> **ResourceUnavailableException**. Type `abc` at a menu -> **InvalidInputException**. `0` logout.
3. **Volunteer** - `volunteer` / `volunteer123`: `1` then `2`; `2` alerts; `3` shelters; `0`.
4. **Admin** - `admin` / `admin123`: `11` stock (used counts changed), `19` start monitors (LOW STOCK WARNING for Tent;
   after about 10 seconds ESCALATION WARNING for SOS103), `20` stop, `16`, `17`, `18` reports, `14` donation, `15`, `21` users, `0`.
5. **Lockout** - Main `1`, `citizen2` with a wrong password 3 times -> **AuthenticationException**, account locked.

## Testing checklist
- [ ] App starts, tables and sample data appear in MySQL
- [ ] Register new citizen and new volunteer, then login
- [ ] Wrong password 3 times locks the account
- [ ] Wrong menu option, text instead of number, empty text and negative numbers never crash the app
- [ ] Duplicate SOS ID rejected; SOS survives restart (queue reloads from DB)
- [ ] Busy team rejected; stock too low rejected; full shelter rejected; hospital with no beds shows message
- [ ] Two rescue threads finish and teams become available again in DB
- [ ] AlertThread prints 3 lines; monitors start and stop cleanly
- [ ] Reports show the right counts

## Syllabus topic -> where used
| Topic | Files |
|-------|-------|
| try / catch / finally | every DAO (finally closes resources), `MainMenu.login`, `AdminMenu.show`, all menus (catch) |
| throw / throws | services and DAOs (`throws`), `SOSService`, `ShelterService`, `UserService` (`throw`) |
| Custom exceptions | `exception/` (6 classes extend Exception) |
| ArrayList | all DAO getAll methods, `TeamService`, `ShelterService`, `HospitalService`, `VolunteerService` |
| LinkedList | `SOSService` pending queue (addLast / removeFirst), `AlertService` last 10 alerts |
| HashSet | `ZoneService` (zones), `SOSService` (duplicate ids), `VolunteerService` (skills) |
| HashMap | `ResourceService` (name -> stock), `UserService` (failed logins), `ReportService` (counts), `EscalationThread` (ages) |
| Thread class | `AlertThread`, `EscalationThread` |
| Runnable | `RescueTask`, `ResourceMonitorTask` |
| Multithreading | `DispatcherMenu.startRescue` (start + join), `AdminMenu.startMonitors/stopMonitors` (interrupt) |
| JDBC | `database/` only: Connection, DriverManager, Statement, PreparedStatement, ResultSet, SQLException |
