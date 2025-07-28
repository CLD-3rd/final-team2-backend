// 모든 페이지에서 공통으로 사용할 웹소켓 관리 로직
class WebSocketManager {
    constructor() {
        this.stompClient = null;
        this.notificationSubscription = null;
        this.chatSubscriptions = [];
        this.sessionId = localStorage.getItem('wsSessionId');
    }

    async connect() {
        if (this.stompClient && this.stompClient.connected) {
            return this.stompClient;
        }

        return new Promise((resolve, reject) => {
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);

            this.stompClient.connect(
                { Authorization: 'Bearer ' + getCookie('accessToken') },
                () => {
                    this.sessionId = 'user-' + Date.now();
                    localStorage.setItem('wsSessionId', this.sessionId);

                    // 필수 알림 구독 (모든 페이지에서 유지)
                    this.subscribeNotifications();
                    resolve(this.stompClient);
                },
                (error) => reject(error)
            );
        });
    }

    subscribeNotifications() {
        if (this.notificationSubscription) return;

        this.notificationSubscription = this.stompClient.subscribe(
            '/user/queue/notifications',
            (message) => {
                const notification = JSON.parse(message.body);
                showNotification(notification); // 각 페이지에 정의된 함수 호출
            }
        );
    }

    subscribeChat(roomId, isGroup, callback) {
        const destination = isGroup
            ? `/sub/chat/room/${roomId}`
            : '/user/queue/messages';

        const sub = this.stompClient.subscribe(destination, callback);
        this.chatSubscriptions.push(sub);
        return sub;
    }

    cleanupChatSubscriptions() {
        this.chatSubscriptions.forEach(sub => sub.unsubscribe());
        this.chatSubscriptions = [];
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// 전역 객체 생성
window.wsManager = new WebSocketManager();