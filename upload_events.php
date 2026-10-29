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
    
    $stmt = $conn->prepare("INSERT INTO events 
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

        $stmt->bind_param("isssssssss", $localId, $name, $date, $location, $organiser, $type, $status, $description, $email, $phone);
        
        if ($stmt->execute()) {
            $successCount++;
        }
    }

    $stmt->close();
    echo json_encode(["status" => "success", "message" => "Successfully synced $successCount events to MySQL database."]);
} else {
    echo json_encode(["status" => "error", "message" => "No valid event data received."]);
}

$conn->close();
?>