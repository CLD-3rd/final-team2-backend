class WebSocketManager {
    constructor() {
        this.stompClient = null;
        this.isConnected = false;
        this.chatSubscriptions = new Map();
        this.notificationSubscription = null;
        this.notificationCallback = null; // 알림 콜백 저장용
        this.connectAttempt = 0; // 연결 시도 횟수
    }

    connect(notificationCallback) {
        if (this.isConnected || this.stompClient?.connected) {
            if (notificationCallback) {
                this.notificationCallback = notificationCallback;
            }
            return;
        }

        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = () => {};

        this.stompClient.connect(
            { Authorization: 'Bearer ' + getCookie('accessToken') },
            () => {
                this.isConnected = true;
                this.connectAttempt = 0;
                console.log('WebSocket 연결 성공');

                // 알림 구독 (최초 1회만)
                if (!this.notificationSubscription) {
                    this.notificationSubscription = this.stompClient.subscribe(
                        '/user/queue/notifications',
                        (message) => {
                            const notification = JSON.parse(message.body);
                            if (this.notificationCallback) {
                                this.notificationCallback(notification);
                            }
                            // 기본 알림 처리 (콜백 없을 때)
                            showDefaultNotification(notification);
                        }
                    );
                }

                if (typeof updateConnectionStatus === 'function') {
                    updateConnectionStatus(true);
                }
            },
            (error) => {
                console.error('연결 실패:', error);
                this.isConnected = false;
                this.connectAttempt++;

                const delay = Math.min(5000 * this.connectAttempt, 30000); // 최대 30초
                setTimeout(() => this.connect(notificationCallback), delay);

                if (typeof updateConnectionStatus === 'function') {
                    updateConnectionStatus(false);
                }
            }
        );

        if (notificationCallback) {
            this.notificationCallback = notificationCallback;
        }
    }

    subscribeToChat(roomId, isGroup, callback) {
        if (!this.isConnected) {
            console.warn('연결되지 않음. 구독 시도 중...');
            return false;
        }

        const destination = isGroup
            ? `/sub/chat/room/${roomId}`
            : '/user/queue/messages';

        const subscription = this.stompClient.subscribe(
            destination,
            (message) => callback(JSON.parse(message.body))
        );

        this.chatSubscriptions.set(roomId, subscription);
        return true;
    }

    unsubscribeFromChat(roomId) {
        const sub = this.chatSubscriptions.get(roomId);
        if (sub) {
            sub.unsubscribe();
            this.chatSubscriptions.delete(roomId);
        }
    }

    sendMessage(destination, message) {
        if (this.isConnected) {
            this.stompClient.send(destination, {}, JSON.stringify(message));
            return true;
        }
        return false;
    }
}

// 전역 인스턴스 (중요: window에 할당하여 페이지 이동 시에도 유지)
window.wsManager = new WebSocketManager();

// 기본 알림 처리 함수
function showDefaultNotification(notification) {
    console.log('Received notification:', notification);
    // 간단한 알림 UI 표시 (모든 페이지에서 동작)
    const toast = document.createElement('div');
    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.right = '20px';
    toast.style.padding = '10px';
    toast.style.background = '#333';
    toast.style.color = 'white';
    toast.style.borderRadius = '5px';
    toast.style.zIndex = '1000';
    toast.innerHTML = `
        <strong>${notification.title}</strong>
        <p>${notification.content}</p>
    `;
    document.body.appendChild(toast);

    setTimeout(() => toast.remove(), 5000);
}

// 쿠키 유틸리티
function getCookie(name) {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [key, value] = cookie.trim().split('=');
        if (key === name) return value;
    }
    return null;
}