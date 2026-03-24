import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        component: () => import('../layout/MainLayout.vue'),
        redirect: '/prompts',
        children: [
            {
                path: 'prompts',
                name: 'PromptList',
                component: () => import('../views/PromptListPage.vue'),
                meta: { title: '提示词管理' }
            },
            {
                path: 'filler',
                name: 'Filler',
                component: () => import('../views/FillerPage.vue'),
                meta: { title: '前后缀填充' }
            },
            {
                path: 'polishing',
                name: 'Polishing',
                component: () => import('../views/PolishingPage.vue'),
                meta: { title: '提示词润色' }
            },
            {
                path: 'assistants',
                name: 'Assistants',
                component: () => import('../views/AssistantListPage.vue'),
                meta: { title: '分析助手' }
            },
            {
                path: 'chat',
                name: 'Chat',
                component: () => import('../views/ChatPage.vue'),
                meta: { title: '助手对话' }
            },
            {
                path: 'trash',
                name: 'Trash',
                component: () => import('../views/TrashPage.vue'),
                meta: { title: '回收站' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - 提示词词库` : '提示词词库管理系统'
    next()
})

export default router
