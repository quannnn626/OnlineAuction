// 通用工具函数
const CommonUtils = {
    // 发送AJAX请求
    ajax: function(url, options) {
        options = options || {};
        const method = options.method || 'GET';
        const data = options.data || {};
        const success = options.success || function() {};
        const error = options.error || function() {};

        const xhr = new XMLHttpRequest();
        xhr.open(method, url, true);
        xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
        
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    try {
                        const result = JSON.parse(xhr.responseText);
                        success(result);
                    } catch (e) {
                        error(e);
                    }
                } else {
                    error(new Error('请求失败：' + xhr.status));
                }
            }
        };
        
        if (method === 'POST' || method === 'PUT') {
            xhr.send(JSON.stringify(data));
        } else {
            xhr.send();
        }
    }
};

