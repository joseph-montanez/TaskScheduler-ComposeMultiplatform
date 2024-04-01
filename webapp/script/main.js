function askNotificationPermission() {
    // Check if the Notification permission has already been granted
    if (Notification.permission === "granted") {
        // The user has already granted permission
        console.log("Permission to receive notifications has been granted");
    } else if (Notification.permission !== "denied") {
        // Request permission from the user
        Notification.requestPermission().then(permission => {
            if (permission === "granted") {
                console.log("Permission to receive notifications has been granted");
                // You can subscribe them to your push notification service here
            }
        });
    }
}