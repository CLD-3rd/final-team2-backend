// Axios 전역 인스턴스 (모든 요청에 쿠키 포함)
const apiClient = axios.create({
    withCredentials: true
});

// 전역 에러 핸들러: 인증 실패 시 로그인 페이지로 이동
apiClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            alert('세션이 만료되었거나 로그인이 필요합니다.');
            window.location.href = '/'; // 로그인 페이지로 리디렉션
        }
        return Promise.reject(error);
    }
);

/**
 * 전역 로그아웃 함수
 */
async function logout() { // async 함수로 변경
    if (window.wsManager && window.wsManager.stompClient?.connected) {
        window.wsManager.disconnect();
    }
    localStorage.clear(); // 로컬 스토리지 비우기

    try {
        // ❗️ 서버에 이 API를 만들어야 합니다.
        // 서버에 로그아웃을 요청하여 HttpOnly 쿠키를 삭제하도록 합니다.
        await apiClient.post('/api/auth/logout');
        alert('로그아웃되었습니다.');
    } catch (error) {
        console.error('로그아웃 처리 중 오류 발생:', error);
        alert('로그아웃 중 문제가 발생했습니다.');
    } finally {
        // 성공/실패 여부와 관계없이 로그인 페이지로 이동
        window.location.href = '/';
    }
}

/**
 * 전역 알림 표시 함수
 * @param {object} notification - { title, content, timestamp }
 */
function showNotification(notification) {
    // 이미 컨테이너가 없으면 새로 생성
    let container = document.getElementById('notificationContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notificationContainer';
        container.className = 'notification-container';
        document.body.appendChild(container);
    }

    const notiElement = document.createElement('div');
    notiElement.className = 'notification';

    notiElement.innerHTML = `
        <button class="notification-close" onclick="this.parentElement.remove()">×</button>
        <div class="notification-title">
            <strong>${notification.title || '알림'}</strong>
        </div>
        <div class="notification-content">
            ${notification.content}
        </div>
        <div class="notification-timestamp">
            ${new Date(notification.timestamp).toLocaleTimeString()}
        </div>
    `;

    container.appendChild(notiElement);
    setTimeout(() => {
        notiElement.style.opacity = '0';
        setTimeout(() => notiElement.remove(), 500);
    }, 5000);
}

// 다른 페이지에서 showNotification을 바로 호출할 수 있도록 window 객체에 할당
window.showNotification = showNotification;