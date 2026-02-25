new Vue({
    el: "#app",
    data() {
        return {
            isCollapse: false,
            activeMenu: null,
            currentPageTitle: "首页",
            currentPageUrl: null,
            menuTree: [],
            menuMap: {}, // 菜单ID到菜单信息的映射
            roleType: 3 // 默认管理员角色，实际应该从登录信息获取
        };
    },
    mounted() {
        this.loadMenus();
    },
    methods: {
        // 加载菜单数据
        loadMenus() {
            const self = this;
            // 请求所有菜单，不区分角色权限
            CommonUtils.ajax('/api/menu/tree?all=true', {
                method: 'GET',
                success: function(result) {
                    if (result.code === 200) {
                        self.menuTree = result.data || [];
                        if (self.menuTree.length === 0) {
                            self.$message.warning('当前角色没有可用菜单，请检查数据库权限配置');
                            return;
                        }
                        self.buildMenuMap(self.menuTree);
                        // 默认选中第一个菜单
                        if (self.menuTree.length > 0) {
                            const firstMenu = self.menuTree[0];
                            if (firstMenu.children && firstMenu.children.length > 0) {
                                self.handleMenuSelect(firstMenu.children[0].id.toString());
                            } else {
                                self.handleMenuSelect(firstMenu.id.toString());
                            }
                        }
                    } else {
                        self.$message.error(result.message || '获取菜单失败');
                    }
                },
                error: function(err) {
                    self.$message.error('获取菜单失败：' + (err.message || '网络错误'));
                    // 如果API失败，显示提示信息
                    self.$message.warning('无法连接到服务器，请检查后端服务是否启动');
                }
            });
        },
        // 构建菜单映射表
        buildMenuMap(menuList) {
            const self = this;
            menuList.forEach(menu => {
                self.menuMap[menu.id] = menu;
                if (menu.children && menu.children.length > 0) {
                    self.buildMenuMap(menu.children);
                }
            });
        },
        // 切换侧边栏
        toggleSidebar() {
            this.isCollapse = !this.isCollapse;
        },
        // 菜单选择处理
        handleMenuSelect(menuId) {
            const menu = this.menuMap[menuId];
            if (menu) {
                this.activeMenu = menuId;
                this.currentPageTitle = menu.menuName;
                // 根据菜单路径跳转到对应页面
                if (menu.menuPath) {
                    // 如果路径是完整URL，直接使用；否则拼接为页面路径
                    if (menu.menuPath.startsWith('http')) {
                        this.currentPageUrl = menu.menuPath;
                    } else {
                        // 将路径转换为页面路径，如 /admin/user -> /page/admin-user
                        // 去掉开头的/，将/替换为-
                        let pagePath = menu.menuPath.replace(/^\//, '').replace(/\//g, '-');
                        this.currentPageUrl = '/page/' + pagePath;
                    }
                } else {
                    this.currentPageUrl = null;
                }
            }
        },
        // 刷新页面
        handleRefresh() {
            if (this.currentPageUrl) {
                const iframe = document.querySelector('.main-content iframe');
                if (iframe) {
                    iframe.src = iframe.src;
                }
            }
            this.$message.success('页面已刷新');
        },
        // 下拉菜单命令处理
        handleCommand(command) {
            switch (command) {
                case "profile":
                    this.$message.info("跳转到个人中心");
                    break;
                case "settings":
                    this.$message.info("跳转到系统设置");
                    break;
                case "logout":
                    this.handleLogout();
                    break;
            }
        },
        // 退出登录
        handleLogout() {
            this.$confirm("确定要退出登录吗?", "提示", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning",
            })
                .then(() => {
                    this.$message.success("退出登录成功");
                    // 这里可以添加退出登录逻辑，如跳转到登录页
                    // window.location.href = '/login';
                })
                .catch(() => {
                    // 取消操作
                });
        }
    }
});

