window.addEventListener('load', (event) => {

    console.log("WebSocket")

    let socket = new WebSocket("wss://" + window.location.host + "/notifications?reportId=" + reportId);

    socket.onmessage = function (event) {

        console.log(`[message] Data received from server: ${event.data}`);

        let report = JSON.parse(event.data);

        let progressElem = document.getElementById('progress');
        let completedElem = document.getElementById('completed');

        progressElem.textContent = report.progress + '%';
        completedElem.textContent = report.completed;
    };

});

