<?php
header("Content-Type: application/json; charset=UTF-8");

$host = "localhost";
$user = "root";
$pass = "letmein";
$dbname = "COMP1786";

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed: " . $conn->connect_error]);
    exit();
}

$jsonInput = file_get_contents("php://input");
$eventsData = json_decode($jsonInput, true);

if (!empty($eventsData) && is_array($eventsData)) {
    $successCount = 0;

    $stmtEvent = $conn->prepare("INSERT INTO events
        (local_event_id, name, date, location, organiser, type, status, description, contact_email, contact_phone)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            name = VALUES(name),
            date = VALUES(date),
            location = VALUES(location),
            organiser = VALUES(organiser),
            type = VALUES(type),
            status = VALUES(status),
            description = VALUES(description),
            contact_email = VALUES(contact_email),
            contact_phone = VALUES(contact_phone)");

    $stmtSession = $conn->prepare("INSERT INTO sessions
        (event_id, local_session_id, session_name, session_time, session_speaker, session_room, session_type, session_description)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            session_name = VALUES(session_name),
            session_time = VALUES(session_time),
            session_speaker = VALUES(session_speaker),
            session_room = VALUES(session_room),
            session_type = VALUES(session_type),
            session_description = VALUES(session_description)");

    foreach ($eventsData as $event) {
        $localId      = $event['id'];
        $name         = $event['name'];
        $date         = $event['date'];
        $location     = $event['location'];
        $organiser    = $event['organiser'];
        $type         = $event['type'];
        $status       = $event['status'];
        $description  = $event['description'] ?? '';
        $email        = $event['contactEmail'] ?? '';
        $phone        = $event['contactPhone'] ?? '';

        $stmtEvent->bind_param("isssssssss", $localId, $name, $date, $location, $organiser, $type, $status, $description, $email, $phone);

        if ($stmtEvent->execute()) {
            $mysqlEventId = $stmtEvent->insert_id;
            if ($mysqlEventId == 0) {
                $idCheck = $conn->query("SELECT id FROM events WHERE local_event_id = $localId");
                if ($row = $idCheck->fetch_assoc()) {
                    $mysqlEventId = $row['id'];
                }
            }

            if (!empty($event['sessions']) && is_array($event['sessions'])) {
                foreach ($event['sessions'] as $session) {
                    $sLocalId     = $session['id'];
                    $sName        = $session['name'];
                    $sTime        = $session['time'];
                    $sSpeaker     = $session['speaker'];
                    $sRoom        = $session['room'];
                    $sType        = $session['type'];
                    $sDescription = $session['description'] ?? '';

                    $stmtSession->bind_param("iissssss", $mysqlEventId, $sLocalId, $sName, $sTime, $sSpeaker, $sRoom, $sType, $sDescription);
                    $stmtSession->execute();
                }
            }

            $successCount++;
        }
    }

    $stmtEvent->close();
    $stmtSession->close();

    echo json_encode(["status" => "success", "message" => "Successfully synced $successCount events and their sessions to MySQL."]);
} else {
    echo json_encode(["status" => "error", "message" => "No valid event data received."]);
}

$conn->close();
?>