class WebSocketManager {
    constructor() {
        this.stompClient = null;
        this.notificationHandler = null;
        this.connect();
    }

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        const headers = {
            Authorization: 'Bearer ' + getCookie('accessToken')
        };

        this.stompClient.connect(headers,
            () => this.onConnectSuccess(),
            (error) => this.onConnectError(error)
        );
    }

    onConnectSuccess() {
        console.log('WebSocket 연결 성공');

        // 알림 구독
        this.stompClient.subscribe('/user/queue/notifications', (message) => {
            const notification = JSON.parse(message.body);
            if (this.notificationHandler) {
                this.notificationHandler(notification);
            } else {
                console.warn('Notification handler not set');
            }
        });
    }

    onConnectError(error) {
        console.error('WebSocket 연결 실패:', error);
        // 5초 후 재연결 시도
        setTimeout(() => this.connect(), 5000);
    }

    setNotificationHandler(handler) {
        this.notificationHandler = handler;
    }
}

// 쿠키에서 값 가져오기 (공통 함수)
function getCookie(name) {
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        const cookie = cookies[i].trim();
        if (cookie.startsWith(name + '=')) {
            return cookie.substring(name.length + 1);
        }
    }
    return null;
}

// 전역 WebSocket 인스턴스 생성
let webSocketManager = null;

// WebSocketManager 초기화 함수 (페이지별로 호출해야 함)
function initWebSocketManager(notificationHandler) {
    if (getCookie('accessToken')) {
        webSocketManager = new WebSocketManager();
        if (notificationHandler) {
            webSocketManager.setNotificationHandler(notificationHandler);
        }
    }
}