// 모든 페이지에서 공통으로 사용할 웹소켓 관리 로직
class WebSocketManager {
    constructor() {
        this.stompClient = null;
        this.notificationSubscription = null;
        this.chatSubscriptions = [];
        this.sessionId = localStorage.getItem('wsSessionId');
        this.directMessageSubscription = null; // ✅ 1:1 메시지 구독을 위한 변수 추가
        this.groupChatSubscription = null;    // ✅ 그룹 채팅 구독 변수 추가
        this.onDirectMessageReceived = null;  // ✅ 1:1 메시지 도착 시 실행될 콜백 함수

        // 🔥 STOMP 디버깅 활성화 (추가된 부분)
        this.debugStomp = (str) => {
            console.log("[STOMP DEBUG]", str);
        };
    }

    async connect() {
        if (this.stompClient && this.stompClient.connected) {
            console.log("이미 연결되어 있습니다.");
            return this.stompClient;
        }

        try {
            // 1. 서버에 티켓을 요청 (apiClient는 common.js에 정의되어 있음)
            console.log("웹소켓 연결 티켓을 요청합니다...");
            const response = await apiClient.get('/api/ws-ticket');
            const ticket = response.data.ticket;

            if (!ticket) {
                throw new Error("서버로부터 유효한 티켓을 받지 못했습니다.");
            }
            console.log("티켓 발급 성공. STOMP 연결을 시작합니다.");

            // 2. STOMP 연결 시작
            return new Promise((resolve, reject) => {
                const socket = new SockJS('/ws');
                this.stompClient = Stomp.over(socket);
                this.stompClient.debug = null; // 배포 시에는 디버그 끄기

                // 3. Stomp CONNECT 헤더에 발급받은 티켓을 직접 넣어줌
                const connectHeaders = {
                    'Authorization': ticket
                };

                this.stompClient.connect(connectHeaders, () => {
                    console.log("WebSocket 연결 성공!");
                    // 기존 구독 로직은 그대로 실행
                    this.subscribeNotifications();
                    this.subscribeDirectMessages();
                    resolve(this.stompClient);
                }, (error) => {
                    console.error('WebSocket 연결 실패:', error);
                    reject(error);
                });
            });

        } catch (error) {
            console.error('웹소켓 연결 과정에서 에러 발생:', error);
            // apiClient의 전역 에러 핸들러가 로그인 페이지로 리디렉션할 수 있습니다.
            throw error; // 에러를 다시 던져서 호출한 쪽(onload)에서 알 수 있도록 함
        }
    }

    // 알림 구독 (기존과 동일)
    subscribeNotifications() {
        if (this.notificationSubscription) return;
        this.notificationSubscription = this.stompClient.subscribe(
            '/user/queue/notifications',
            (message) => {
                const notification = JSON.parse(message.body);
                // 각 페이지에 window.showNotification이 정의되어 있어야 함
                if (window.showNotification) {
                    window.showNotification(notification);
                }
            }
        );
    }

    // ⭐ [추가] 1:1 메시지를 전역으로 구독하는 함수
    subscribeDirectMessages() {
        if (this.directMessageSubscription) return; // 이미 구독 중이면 실행 안 함
        this.directMessageSubscription = this.stompClient.subscribe(
            '/user/queue/messages',
            (message) => {
                // 메시지가 도착하면, 등록된 콜백 함수를 실행
                if (this.onDirectMessageReceived) {
                    this.onDirectMessageReceived(JSON.parse(message.body));
                }
            }
        );
    }

    // ⭐ [수정] 그룹 채팅 구독 함수 (1:1 로직 제거)
    subscribeGroupChat(roomId, callback) {
        this.cleanupGroupChatSubscription(); // 이전 그룹 채팅 구독은 해제
        this.groupChatSubscription = this.stompClient.subscribe(
            `/sub/chat/room/${roomId}`,
            (message) => callback(JSON.parse(message.body))
        );
        console.log(`[구독] 그룹 채팅방: /sub/chat/room/${roomId}`);
    }

    // ⭐ [수정] 모든 구독을 해제하는 대신, 그룹 채팅 구독만 해제
    cleanupGroupChatSubscription() {
        if (this.groupChatSubscription) {
            this.groupChatSubscription.unsubscribe();
            this.groupChatSubscription = null;
            console.log("[구독 해제] 이전 그룹 채팅방");
        }
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// 전역 객체 생성
window.wsManager = new WebSocketManager();